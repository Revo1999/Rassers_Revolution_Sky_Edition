# Revo Departure Mono

Minecraft Java 1.21.1 resource pack based on **Departure Mono 1.500 by Helena Zhang**.
Original: https://departuremono.com/ — Author: https://helenazhang.com/
Copyright 2022–2024 Helena Zhang. Font: SIL Open Font License 1.1.
The complete original license and attribution are included in the resource pack.

## Use

The editable pack is in `pack/overrides/resourcepacks/revo-departure-mono`.
Development launches copy it into `revolib/run/resourcepacks` automatically.
Enable **Revo Departure Mono** in Options → Resource Packs, above other font packs.
It is already selected in this machine's development-client options.
For another Minecraft 1.21.1 installation, copy the distributable ZIP into its
resourcepacks directory and select it in-game. Do not extract the ZIP there.

The pack replaces the default UI/chat font. Characters not covered by Departure Mono
continue using lower-priority vanilla providers. Minecraft's enchanting font and
mods that explicitly choose another font are unaffected.

## Assets and rebuilding

- A TrueType conversion and native FreeType-backed Minecraft font provider.
- Separate named font: `revo_font:departure` for custom text components.
- Source OTF, upstream README and original license: `art/fonts/departure-mono`.
- Font packaging script: `scripts/package_departure_font.py`.

In a Python environment with `scripts/font-requirements.txt` installed, run:

    python scripts/package_departure_font.py

This regenerates the pack, the preview in `docs/departure-font-preview.png`, and
`Revo-Departure-Mono-MC-1.21.1.zip` beside the repository directory.
The earlier 9px bitmap conversion distorted letters and changed monospaced spacing;
it has been removed. Minecraft 1.21.1 rejects CFF fonts, so the packaging script
converts the supplied OTF outlines to TrueType and preserves every character mapping.
The derivative is named Revo Departure Mono and remains under SIL OFL 1.1.

The owner selected the **faithful native pixel grid** over a seven-pixel adaptation.
Capital height is **8 GUI pixels**, one pixel taller than vanilla's seven, with the
original **7 GUI pixels of advance per character**. Provider size is 11, oversample
is 1, and shift is zero. There is no fractional resizing or changed spacing. Source
glyphs are not squeezed into seven rows; a larger atlas cannot preserve eight
distinct source rows inside seven target rows. Departure remains monospaced.
Original OTF is preserved byte-for-byte in art/fonts. The preview uses an exact 2x
native raster for inspection; the in-game provider renders at native size.
No game-code dependency or font installation in Windows is required.

Validation: all 1,079 Unicode mappings, original advance widths and side bearings
are retained. At native size, every mapped character's visible pixel silhouette,
bitmap bearing and advance match the original OTF's unhinted native design grid
through FreeType at Minecraft's alpha cutoff. This avoids CFF hinting shifts while
retaining the actual authored pixel grid. Capitals are eight pixels high and
advances are exactly seven pixels.
