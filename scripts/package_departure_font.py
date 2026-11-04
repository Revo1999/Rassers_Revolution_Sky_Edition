"""Convert Departure Mono to Minecraft-compatible TrueType (SIL OFL 1.1)."""
from pathlib import Path
import json
import shutil
import zipfile
from PIL import Image, ImageDraw, ImageFont
from fontTools.ttLib import TTFont
from fontTools.fontBuilder import FontBuilder
from fontTools.pens.ttGlyphPen import TTGlyphPen
from fontTools.pens.cu2quPen import Cu2QuPen
from copy import deepcopy

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'art/fonts/departure-mono'
PACK = ROOT / 'pack/overrides/resourcepacks/revo-departure-mono'

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def main():
    original = SOURCE / 'DepartureMono-Regular.otf'
    target = PACK / 'assets/revo_font/font/departure_mono.ttf'
    target.parent.mkdir(parents=True, exist_ok=True)
    face = TTFont(original)
    builder = FontBuilder(face['head'].unitsPerEm, isTTF=True)
    builder.setupGlyphOrder(face.getGlyphOrder())
    builder.setupCharacterMap(face.getBestCmap())
    source_glyphs = face.getGlyphSet()
    glyphs = {}
    for name in face.getGlyphOrder():
        pen = TTGlyphPen(None)
        source_glyphs[name].draw(Cu2QuPen(pen, max_err=0.1, reverse_direction=True))
        glyphs[name] = pen.glyph()
    builder.setupGlyf(glyphs)
    builder.setupHorizontalMetrics(dict(face['hmtx'].metrics))
    builder.setupHorizontalHeader(ascent=face['hhea'].ascent, descent=face['hhea'].descent,
                                  lineGap=face['hhea'].lineGap)
    builder.font['OS/2'] = deepcopy(face['OS/2'])
    builder.setupNameTable({'familyName': 'Revo Departure Mono', 'styleName': 'Regular',
        'uniqueFontIdentifier': 'RevoDepartureMono-1.500', 'fullName': 'Revo Departure Mono Regular',
        'psName': 'RevoDepartureMono-Regular', 'version': 'Version 1.500',
        'copyright': 'Copyright 2022-2024 Helena Zhang (helenazhang.com).',
        'manufacturer': 'Helena Zhang', 'designer': 'Helena Zhang',
        'description': 'TrueType format conversion of Departure Mono for Minecraft.',
        'licenseDescription': 'SIL Open Font License, Version 1.1.',
        'licenseInfoURL': 'https://openfontlicense.org/'})
    builder.setupPost()
    builder.save(target)
    converted = TTFont(target)
    assert converted.getBestCmap() == face.getBestCmap()
    assert converted['hmtx'].metrics == face['hmtx'].metrics
    (target.parent / 'departure_mono.otf').unlink(missing_ok=True)
    # Minecraft 1.21.1 requires TrueType outlines, rejecting the original CFF.
    # Native 11px em: original 8px capitals, 7px advances and 1px strokes.
    # No fractional rescaling, altered spacing or reduced-height glyphs.
    definition = {'providers': [{
        'type': 'ttf', 'file': 'revo_font:departure_mono.ttf', 'size': 11,
        'oversample': 1, 'shift': [0, 0], 'skip': ''}]}
    write_json(PACK / 'assets/revo_font/font/departure.json', definition)
    # Separate provider instances avoid sharing a mutable FreeType face between
    # the named and default fonts during Minecraft's parallel font preparation.
    write_json(PACK / 'assets/minecraft/font/default.json', definition)
    write_json(PACK / 'pack.mcmeta', {'pack': {'pack_format': 34,
        'description': 'Revo Departure Mono\nOriginal font by Helena Zhang | SIL OFL 1.1'}})
    shutil.copyfile(SOURCE / 'LICENSE', PACK / 'LICENSE-DepartureMono.txt')
    (PACK / 'CREDITS.txt').write_text(
        'Departure Mono 1.500 by Helena Zhang\n'
        'Copyright 2022\u20132024 Helena Zhang (helenazhang.com).\n'
        'Original font: https://departuremono.com/\nAuthor: https://helenazhang.com/\n'
        'Licensed under the SIL Open Font License, Version 1.1.\n'
        'See LICENSE-DepartureMono.txt for the full license.\n\n'
        'Revo Departure Mono is a TrueType format conversion for Minecraft.\n'
        'Original character mappings and glyph outlines are retained.\n'
        'Original advance widths and bearings retained; native 8px capitals without rescaling.\n'
        'The converted font remains licensed under SIL OFL 1.1.\n'
        'Original OTF is preserved in the project art/fonts directory.\n', encoding='utf-8')
    for i in range(5):
        (PACK / f'assets/revo_font/textures/font/departure_{i:02}.png').unlink(missing_ok=True)
    cmap = face.getBestCmap()
    assert len({face['hmtx'][cmap[ord(c)]][0] for c in 'WiIl.,0123456789 '}) == 1
    assert all(ord(c) in cmap for c in '\u00c6\u00d8\u00c5\u00e6\u00f8\u00e5')
    font = ImageFont.truetype(str(target), 22)
    preview = Image.new('RGB', (760, 280), '#1c2326')
    draw = ImageDraw.Draw(preview)
    lines = ['RASSERS REVOLUTION: SKY EDITION', 'From Nothing, Build Everything.',
             'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz',
             'WiIl.,:;!? 0123456789  \u00c6\u00d8\u00c5 \u00e6\u00f8\u00e5',
             'Departure Mono by Helena Zhang']
    for i, line in enumerate(lines):
        draw.text((18, 10 + i * 42), line, font=font, fill='white')
    preview.save(ROOT / 'docs/departure-font-preview.png')
    output = ROOT.parent / 'Revo-Departure-Mono-MC-1.21.1.zip'
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(PACK.rglob('*')):
            if path.is_file():
                archive.write(path, path.relative_to(PACK).as_posix())
    with zipfile.ZipFile(output) as archive:
        assert archive.testzip() is None
    print(f'Packaged {len(cmap)} mapped characters at native 8px capitals with original metrics.')

if __name__ == '__main__':
    main()
