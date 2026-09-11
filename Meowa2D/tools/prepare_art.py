from pathlib import Path
from PIL import Image, ImageDraw
import shutil

ROOT = Path(r"D:\CODE\Meowa2D")
SRC = ROOT / "meowa-output" / "deliverables"
ART = ROOT / "HourglassPalace" / "Assets" / "Art"


def copy(src: Path, dst: Path) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)


def make_tile(path: Path, color, noise=True) -> None:
    img = Image.new("RGBA", (16, 16), color)
    px = img.load()
    if noise:
        for y in range(16):
            for x in range(16):
                if (x + y * 3) % 7 == 0:
                    r, g, b, a = px[x, y]
                    px[x, y] = (max(0, r - 18), max(0, g - 14), max(0, b - 10), a)
        for i in range(16):
            px[i, 0] = (max(0, color[0] - 40), max(0, color[1] - 30), max(0, color[2] - 20), 255)
            px[i, 15] = (min(255, color[0] + 18), min(255, color[1] + 12), min(255, color[2] + 8), 255)
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)


def slice_sheet(path: Path, outdir: Path, prefix: str) -> list[Path]:
    im = Image.open(path).convert("RGBA")
    w, h = im.size
    pix = im.load()
    visited = [[False] * h for _ in range(w)]

    def solid(x: int, y: int) -> bool:
        return 0 <= x < w and 0 <= y < h and pix[x, y][3] > 24

    blobs = []
    for y in range(h):
        for x in range(w):
            if visited[x][y] or not solid(x, y):
                continue
            stack = [(x, y)]
            visited[x][y] = True
            cells = []
            minx = maxx = x
            miny = maxy = y
            while stack:
                cx, cy = stack.pop()
                cells.append((cx, cy))
                minx = min(minx, cx)
                maxx = max(maxx, cx)
                miny = min(miny, cy)
                maxy = max(maxy, cy)
                for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
                    if 0 <= nx < w and 0 <= ny < h and not visited[nx][ny] and solid(nx, ny):
                        visited[nx][ny] = True
                        stack.append((nx, ny))
            if maxx - minx < 10 or maxy - miny < 10:
                continue
            crop = Image.new("RGBA", (maxx - minx + 1, maxy - miny + 1), (0, 0, 0, 0))
            cp = crop.load()
            for cx, cy in cells:
                cp[cx - minx, cy - miny] = pix[cx, cy]
            blobs.append((minx, miny, crop))
    blobs.sort(key=lambda t: (t[1] // 48, t[0]))
    outdir.mkdir(parents=True, exist_ok=True)
    written = []
    for i, (_, _, crop) in enumerate(blobs):
        dest = outdir / f"{prefix}_{i:02d}.png"
        crop.save(dest)
        written.append(dest)
    return written


def main() -> None:
    hero_nobg = ROOT / "meowa-output" / "hero-64-nobg"
    hero_src = SRC / "hero" / "miaobaibai_64x80.png"
    if hero_nobg.exists():
        pngs = list(hero_nobg.rglob("*.png"))
        if pngs:
            hero_src = pngs[0]
    copy(hero_src, ART / "Hero" / "miaobaibai.png")
    copy(SRC / "hero" / "miaobaibai_side_canonical.png", ART / "Hero" / "miaobaibai_portrait.png")
    copy(SRC / "map" / "background.png", ART / "Map" / "background.png")
    copy(SRC / "map" / "midground.png", ART / "Map" / "midground.png")
    copy(SRC / "map" / "foreground.png", ART / "Map" / "foreground.png")
    copy(SRC / "enemies" / "enemy_sheet.png", ART / "Enemies" / "enemy_sheet.png")
    copy(SRC / "props" / "trap_sheet.png", ART / "Props" / "trap_sheet.png")
    copy(SRC / "props" / "prop_sheet_v1.png", ART / "Props" / "prop_sheet.png")

    enemies = slice_sheet(SRC / "enemies" / "enemy_sheet.png", ART / "Enemies" / "Sliced", "enemy")
    traps = slice_sheet(SRC / "props" / "trap_sheet.png", ART / "Props" / "Sliced", "trap")
    props = slice_sheet(SRC / "props" / "prop_sheet_v1.png", ART / "Props" / "Loot", "loot")
    print("enemies", [p.name for p in enemies])
    print("traps", [p.name for p in traps])
    print("loot", [p.name for p in props])

    make_tile(ART / "Tiles" / "sandstone.png", (196, 140, 82, 255))
    make_tile(ART / "Tiles" / "sandstone_dark.png", (148, 96, 58, 255))
    make_tile(ART / "Tiles" / "teal.png", (46, 140, 148, 255))
    spike = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(spike)
    d.polygon([(8, 1), (14, 15), (2, 15)], fill=(168, 176, 188, 255), outline=(40, 44, 52, 255))
    spike.save(ART / "Tiles" / "spike.png")
    print("art ready")


if __name__ == "__main__":
    main()
