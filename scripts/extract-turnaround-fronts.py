#!/usr/bin/env python3
"""Build square transparent companion sprites from the front panels of turnarounds."""

from __future__ import annotations

import shutil
import struct
import subprocess
import zlib
from collections import deque
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TURNAROUNDS = ROOT / "art" / "turnarounds"
OUTPUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
SPRITE_SIZE = 512
FRONT_WIDTH_RATIO = 0.40
BACKGROUND_DISTANCE_SQUARED = 1_500
CHARACTERS = [
    ("gpt_daughter", "gpt_daughter.png"),
    ("claude_daughter", "claude_daughter.png"),
    ("whale_daughter", "whale_daughter.png"),
    ("gugugaga", "gugugaga.png"),
    ("fengxiong", "fengxiong.png"),
    ("tuge", "tuge.png"),
    ("miaobubu", "miaobubu.png"),
    ("miaobaibai", "miaobaibai.png"),
    ("yamei", "yamei.png"),
    ("yumei", "yumei.png"),
]


def run(command: list[str], *, capture: bool = False) -> bytes:
    result = subprocess.run(
        command,
        check=True,
        stdout=subprocess.PIPE if capture else subprocess.DEVNULL,
        stderr=subprocess.PIPE,
    )
    return result.stdout if capture else b""


def png_chunk(kind: bytes, data: bytes) -> bytes:
    return (
        struct.pack(">I", len(data))
        + kind
        + data
        + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)
    )


def write_rgba_png(path: Path, width: int, height: int, rgba: bytes) -> None:
    rows = bytearray()
    stride = width * 4
    for y in range(height):
        rows.append(0)
        rows.extend(rgba[y * stride : (y + 1) * stride])
    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    payload = b"\x89PNG\r\n\x1a\n"
    payload += png_chunk(b"IHDR", header)
    payload += png_chunk(b"IDAT", zlib.compress(bytes(rows), 9))
    payload += png_chunk(b"IEND", b"")
    path.write_bytes(payload)


def border_background(rgb: bytes, width: int, height: int) -> tuple[int, int, int]:
    samples = [[], [], []]
    for y in range(2, min(10, height - 2)):
        for x in range(2, min(10, width - 2)):
            p = (y * width + x) * 3
            for channel in range(3):
                samples[channel].append(rgb[p + channel])
    for y in range(2, min(10, height - 2)):
        for x in range(max(0, width - 10), width - 2):
            p = (y * width + x) * 3
            for channel in range(3):
                samples[channel].append(rgb[p + channel])
    return tuple(sorted(channel)[len(channel) // 2] for channel in samples)


def make_front(source: Path, destination: Path) -> None:
    dimensions = run(
        [
            "ffprobe",
            "-v",
            "error",
            "-select_streams",
            "v:0",
            "-show_entries",
            "stream=width,height",
            "-of",
            "csv=s=x:p=0",
            str(source),
        ],
        capture=True,
    ).decode().strip()
    source_width, height = [int(value) for value in dimensions.split("x")]
    width = max(2, int(source_width * FRONT_WIDTH_RATIO) & ~1)
    rgb = run(
        [
            "ffmpeg",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(source),
            "-vf",
            f"crop={width}:{height}:0:0",
            "-frames:v",
            "1",
            "-f",
            "rawvideo",
            "-pix_fmt",
            "rgb24",
            "pipe:1",
        ],
        capture=True,
    )
    background = border_background(rgb, width, height)

    pixel_count = width * height
    queued = bytearray(pixel_count)
    transparent = bytearray(pixel_count)
    queue: deque[int] = deque()

    def enqueue(index: int) -> None:
        if not queued[index]:
            queued[index] = 1
            queue.append(index)

    for x in range(width):
        enqueue(x)
        enqueue((height - 1) * width + x)
    for y in range(height):
        enqueue(y * width)
        enqueue(y * width + width - 1)

    while queue:
        index = queue.popleft()
        offset = index * 3
        distance = sum(
            (rgb[offset + channel] - background[channel]) ** 2
            for channel in range(3)
        )
        if distance > BACKGROUND_DISTANCE_SQUARED:
            continue
        transparent[index] = 1
        x = index % width
        y = index // width
        if x > 0:
            enqueue(index - 1)
        if x + 1 < width:
            enqueue(index + 1)
        if y > 0:
            enqueue(index - width)
        if y + 1 < height:
            enqueue(index + width)

    rgba = bytearray(pixel_count * 4)
    for index in range(pixel_count):
        source_offset = index * 3
        target_offset = index * 4
        rgba[target_offset : target_offset + 3] = rgb[source_offset : source_offset + 3]
        rgba[target_offset + 3] = 0 if transparent[index] else 255

    intermediate = destination.with_suffix(".cropped.png")
    write_rgba_png(intermediate, width, height, bytes(rgba))
    run(
        [
            "ffmpeg",
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(intermediate),
            "-vf",
            f"scale={SPRITE_SIZE}:{SPRITE_SIZE}:force_original_aspect_ratio=decrease,"
            f"pad={SPRITE_SIZE}:{SPRITE_SIZE}:(ow-iw)/2:(oh-ih)/2:color=black@0,format=rgba",
            "-frames:v",
            "1",
            str(destination),
        ]
    )
    intermediate.unlink()


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    expected = {
        f"{character_id}_{mood}.png"
        for character_id, _ in CHARACTERS
        for mood in ("idle", "happy", "sleep")
    }
    for asset in OUTPUT.glob("*.png"):
        if asset.name not in expected:
            asset.unlink()
    for character_id, filename in CHARACTERS:
        source = TURNAROUNDS / filename
        if not source.is_file():
            raise FileNotFoundError(source)
        idle = OUTPUT / f"{character_id}_idle.png"
        make_front(source, idle)
        for mood in ("happy", "sleep"):
            shutil.copyfile(idle, OUTPUT / f"{character_id}_{mood}.png")
        print(f"Built {character_id} idle/happy/sleep artwork")


if __name__ == "__main__":
    main()
