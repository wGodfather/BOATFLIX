"""Convert the supplied BOATFLIX artwork into desktop and shared UI assets.

Run from any directory with Python and Pillow. The source artwork is preserved.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
SOURCE = Path(__file__).with_name("boatflix.png")
DRAWABLE = ROOT / "composeApp/src/commonMain/composeResources/drawable"
DESKTOP = ROOT / "composeApp/src/desktopMain/resources/icons"

image = Image.open(SOURCE).convert("RGBA")
assert image.width == image.height, "The supplied icon must be square"
icon = image.resize((1024, 1024), Image.Resampling.LANCZOS)

# Preserve resource names so existing theme and icon preferences remain compatible.
for path in DRAWABLE.glob("app_icon_*.png"):
    icon.save(path)
for path in DESKTOP.glob("*.png"):
    icon.save(path)
for path in DESKTOP.glob("*.ico"):
    icon.save(path, sizes=[(n, n) for n in (16, 24, 32, 48, 64, 128, 256)])
for path in DESKTOP.glob("*.icns"):
    icon.save(path)

# Keep the wide wordmark proportions used by the sidebar and startup screens.
font = ImageFont.truetype(str(ROOT / "composeApp/src/commonMain/composeResources/font/jetbrains_sans_bold.ttf"), 100)
label = "BOATFLIX"
bounds = font.getbbox(label)
mark = Image.new("RGBA", (160 + bounds[2] + 24, 144))
mark.alpha_composite(icon.resize((144, 144), Image.Resampling.LANCZOS))
ImageDraw.Draw(mark).text((168, (144 - (bounds[3] - bounds[1])) / 2 - bounds[1]), label, font=font, fill="#EFC26D")
for path in DRAWABLE.glob("app_logo_wordmark*.png"):
    mark.save(path)

ANDROID = ROOT / "composeApp/src/androidMain/res"
for path in ANDROID.glob("mipmap-*/*.webp"):
    size = Image.open(path).size
    if "foreground" in path.name:
        foreground = Image.new("RGBA", size)
        inset = round(size[0] * 0.64)
        foreground.alpha_composite(image.resize((inset, inset), Image.Resampling.LANCZOS),
                                   ((size[0] - inset) // 2, (size[1] - inset) // 2))
        foreground.save(path, lossless=True)
    elif "monochrome" in path.name:
        scaled = image.resize(size, Image.Resampling.LANCZOS)
        mask = scaled.convert("L").point(lambda value: 255 if value > 95 else 0)
        monochrome = Image.new("RGBA", size, "white")
        monochrome.putalpha(mask)
        monochrome.save(path, lossless=True)
    else:
        image.resize(size, Image.Resampling.LANCZOS).save(path, lossless=True)
for path in ANDROID.glob("drawable-nodpi/ic_splash_logo*.webp"):
    image.resize(Image.open(path).size, Image.Resampling.LANCZOS).save(path, lossless=True)
banner = Image.new("RGB", (320, 180), "#080808")
banner.paste(image.resize((136, 136), Image.Resampling.LANCZOS), (12, 22))
tv_font = ImageFont.truetype(str(ROOT / "composeApp/src/commonMain/composeResources/font/jetbrains_sans_bold.ttf"), 27)
ImageDraw.Draw(banner).text((155, 71), "BOATFLIX", font=tv_font, fill="#EFC26D")
banner_path = ANDROID / "drawable-xhdpi/boatflix_tv_banner.png"
banner_path.parent.mkdir(parents=True, exist_ok=True)
banner.save(banner_path)
print("BOATFLIX desktop, Android, TV and shared UI assets generated.")
