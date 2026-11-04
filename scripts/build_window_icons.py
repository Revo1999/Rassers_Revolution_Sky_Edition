"""Prepare the supplied artwork at standard window/launcher icon sizes."""
from pathlib import Path
import json
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'art/branding/revo_icon_source.png'
TARGET=ROOT/'pack/overrides/config/fancymenu/assets/icons'
SIZES=[16,20,24,32,40,48,64,96,128,256,512,1024]

def main():
    image=Image.open(SOURCE).convert('RGBA')
    # Remove transparent margins only, preserving all visible artwork and aspect ratio.
    bounds=image.getchannel('A').getbbox()
    if bounds is None:raise ValueError('Source icon is empty')
    artwork=image.crop(bounds)
    side=max(artwork.size)
    padding=max(1,round(side*0.035))
    square=Image.new('RGBA',(side+2*padding,side+2*padding))
    square.alpha_composite(artwork,((square.width-artwork.width)//2,(square.height-artwork.height)//2))
    TARGET.mkdir(parents=True,exist_ok=True)
    for size in SIZES:
        square.resize((size,size),Image.Resampling.LANCZOS).save(TARGET/f'revo_{size}.png')
    master=square.resize((1024,1024),Image.Resampling.LANCZOS)
    ico_sizes=[(s,s) for s in SIZES if s<=256]
    master.save(TARGET/'revo.ico',format='ICO',sizes=ico_sizes)
    master.save(TARGET/'revo.icns',format='ICNS')
    (TARGET/'sizes.json').write_text(json.dumps({'source':'art/branding/revo_icon_source.png',
        'pngSizes':SIZES,'icoSizes':[s for s in SIZES if s<=256],
        'fancyMenuWindowsLinux':[16,32],'fancyMenuMacOS':'revo.icns',
        'preparation':'Transparent margins trimmed; aspect ratio retained; transparent square padding.'},indent=2)+'\n')
    for size in SIZES:
        with Image.open(TARGET/f'revo_{size}.png') as f:
            assert f.size==(size,size) and f.mode=='RGBA'
    with Image.open(TARGET/'revo.ico') as f:
        assert f.ico.sizes()==set(ico_sizes)
    with Image.open(TARGET/'revo.icns') as f:
        assert f.size==(1024,1024)
    print('Generated 12 PNG sizes (16–1024), multi-size ICO and macOS ICNS.')

if __name__=='__main__':main()
