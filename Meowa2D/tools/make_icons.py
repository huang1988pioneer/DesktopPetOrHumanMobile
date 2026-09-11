"""Generate the handful of pixel icons and tiles the slice needs but the
generated art packs do not contain: HUD paw hearts, the interaction hand,
the moon-gate stone, and the djinn's sand projectile.

Everything is drawn on a 16px grid with the same thick 1px dark outline the
rest of the palace art uses, so the HUD and the world stay one style.
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ART = ROOT / "HourglassPalace" / "Assets" / "Art"
UI = ART / "UI"
TILES = ART / "Tiles"

OUTLINE = (26, 20, 18, 255)
PAW_FULL = (236, 232, 226, 255)
PAW_PAD = (232, 150, 168, 255)
PAW_HURT = (150, 132, 128, 255)
PAW_EMPTY = (68, 58, 60, 255)
BRONZE = (176, 122, 58, 255)
BRONZE_DARK = (108, 70, 34, 255)
MOON = (196, 214, 246, 255)
MOON_DEEP = (44, 56, 104, 255)
SAND = (226, 190, 118, 255)


def new(size=16):
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def paw(body, pad, outline=True):
    """A 16x16 cat paw: main pad plus four toes."""
    img = new()
    d = ImageDraw.Draw(img)
    d.ellipse((3, 7, 12, 14), fill=body, outline=OUTLINE if outline else None)
    for cx in (3, 6, 9, 12):
        top = 3 if cx in (6, 9) else 5
        d.ellipse((cx - 1, top, cx + 1, top + 3), fill=body,
                  outline=OUTLINE if outline else None)
    d.ellipse((6, 9, 9, 12), fill=pad)
    return img


def hand():
    """Interaction prompt: an open hand reaching for a mechanism."""
    img = new()
    d = ImageDraw.Draw(img)
    d.rectangle((5, 7, 11, 13), fill=PAW_FULL, outline=OUTLINE)
    for i, x in enumerate((5, 7, 9)):
        d.rectangle((x, 3 + i % 2, x + 1, 7), fill=PAW_FULL, outline=OUTLINE)
    d.rectangle((11, 6, 12, 9), fill=PAW_FULL, outline=OUTLINE)
    return img


def flat(color, top_light=True, size=16):
    """A plain block tile with a lit top edge, matching the sandstone tiles."""
    img = Image.new("RGBA", (size, size), color)
    px = img.load()
    for y in range(size):
        for x in range(size):
            if (x * 5 + y * 3) % 11 == 0:
                r, g, b, a = px[x, y]
                px[x, y] = (max(0, r - 16), max(0, g - 14), max(0, b - 12), a)
    d = ImageDraw.Draw(img)
    if top_light:
        d.line((0, 0, size - 1, 0), fill=tuple(min(255, c + 34) for c in color[:3]) + (255,))
    d.line((0, size - 1, size - 1, size - 1),
           fill=tuple(max(0, c - 40) for c in color[:3]) + (255,))
    return img


def moon_gate_tile():
    img = flat(MOON_DEEP)
    d = ImageDraw.Draw(img)
    d.ellipse((3, 3, 12, 12), outline=MOON)
    d.ellipse((6, 2, 14, 10), fill=MOON_DEEP)
    return img


def sand_blade():
    """Djinn projectile: a small crescent of compressed sand."""
    img = new(12)
    d = ImageDraw.Draw(img)
    d.ellipse((0, 1, 9, 10), fill=SAND, outline=OUTLINE)
    d.ellipse((3, 1, 12, 10), fill=(0, 0, 0, 0))
    return img


def door_tile():
    img = flat(BRONZE_DARK)
    d = ImageDraw.Draw(img)
    d.line((8, 0, 8, 15), fill=BRONZE)
    d.line((0, 8, 15, 8), fill=BRONZE)
    return img


def main():
    UI.mkdir(parents=True, exist_ok=True)
    TILES.mkdir(parents=True, exist_ok=True)
    paw(PAW_FULL, PAW_PAD).save(UI / "paw_full.png")
    paw(PAW_HURT, (150, 110, 118, 255)).save(UI / "paw_hurt.png")
    paw(PAW_EMPTY, PAW_EMPTY).save(UI / "paw_empty.png")
    hand().save(UI / "icon_hand.png")
    flat((22, 18, 30, 255), top_light=False).save(UI / "panel.png")
    moon_gate_tile().save(TILES / "moonstone.png")
    door_tile().save(TILES / "bronze_door.png")
    flat((64, 52, 44, 255)).save(TILES / "pillar.png")
    sand_blade().save(TILES / "sand_blade.png")
    print("icons written to", UI, "and", TILES)


if __name__ == "__main__":
    main()
