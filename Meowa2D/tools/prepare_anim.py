"""Turn the generated hero webp loops into clean, uniform pixel sprite strips.

The raw loops render the cat on a flat grey card (97,97,97).  Unity needs
transparent, identically sized frames so a sprite strip can be sliced on a
fixed grid, so we flood the grey in from the border, crop every frame with one
shared bounding box, and write a single horizontal strip per animation.
"""
from collections import deque
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ART = ROOT / "HourglassPalace" / "Assets" / "Art" / "Hero"
BG = (97, 97, 97)
TOL = 34


def close_to_bg(px, tol=TOL):
    if px[3] == 0:
        return True
    return all(abs(px[i] - BG[i]) <= tol for i in range(3))


def key_out(frame: Image.Image) -> Image.Image:
    img = frame.convert("RGBA")
    w, h = img.size
    px = img.load()
    seen = [[False] * h for _ in range(w)]
    q = deque()
    for x in range(w):
        for y in (0, h - 1):
            if close_to_bg(px[x, y]) and not seen[x][y]:
                seen[x][y] = True
                q.append((x, y))
    for y in range(h):
        for x in (0, w - 1):
            if close_to_bg(px[x, y]) and not seen[x][y]:
                seen[x][y] = True
                q.append((x, y))
    while q:
        cx, cy = q.popleft()
        px[cx, cy] = (0, 0, 0, 0)
        for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
            if 0 <= nx < w and 0 <= ny < h and not seen[nx][ny] and close_to_bg(px[nx, ny]):
                seen[nx][ny] = True
                q.append((nx, ny))
    # soften leftover grey halo pixels that survived the flood (anti-aliased rim)
    for y in range(h):
        for x in range(w):
            p = px[x, y]
            if p[3] and close_to_bg(p, 12):
                px[x, y] = (0, 0, 0, 0)
    return img


# Every animation shares one cell so the pivot (bottom centre = the feet) is
# identical in all of them; switching state must not make the cat hop.
CELL = (64, 88)


def load_cropped(name: str):
    src = ART / f"{name}.webp"
    if not src.exists():
        print(f"skip {src.name} (missing)")
        return None
    anim = Image.open(src)
    frames = []
    for i in range(getattr(anim, "n_frames", 1)):
        anim.seek(i)
        frames.append(key_out(anim.copy()))

    box = None
    for f in frames:
        b = f.getbbox()
        if b is None:
            continue
        box = b if box is None else (
            min(box[0], b[0]), min(box[1], b[1]), max(box[2], b[2]), max(box[3], b[3]))
    if box is None:
        print(f"skip {name} (fully transparent)")
        return None
    return [f.crop(box) for f in frames]


def write_strip(frames, out_name: str) -> None:
    cw, ch = CELL
    strip = Image.new("RGBA", (cw * len(frames), ch), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        if f.width > cw or f.height > ch:
            f = f.resize((min(f.width, cw), min(f.height, ch)), Image.NEAREST)
        x = i * cw + (cw - f.width) // 2
        y = ch - f.height - 1  # feet one pixel above the cell floor
        strip.paste(f, (x, y))
    dest = ART / out_name
    strip.save(dest)
    print(f"{dest.name}: {len(frames)} frames of {cw}x{ch}")


def main() -> None:
    for name, out in (("miaobaibai_idle", "miaobaibai_idle_strip.png"),
                      ("miaobaibai_walk", "miaobaibai_walk_strip.png")):
        frames = load_cropped(name)
        if frames:
            write_strip(frames, out)


if __name__ == "__main__":
    main()
