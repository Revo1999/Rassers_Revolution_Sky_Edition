from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json
ROOT=Path(__file__).resolve().parents[1]
art=ROOT/'art/blockbench/furnaces';tiers=json.loads((art/'manifest.json').read_text())['tiers'];w,h=400,350
sheet=Image.new('RGB',(w*5,h*2+90),(27,32,34))
for i,tier in enumerate(tiers):
    frame=Image.open(art/f'ingame/{tier["tier"]}-world.png').convert('RGB')
    crop=frame.crop((490,320,810,620)).resize((w-16,h-54),Image.Resampling.LANCZOS)
    sheet.paste(crop,(i%5*w+8,i//5*h+90))
draw=ImageDraw.Draw(sheet);font=ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf',23);small=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',16)
draw.text((24,15),'REVOINDUSTRY / ALL TEN FURNACES BUILT IN-GAME',font=font,fill=(231,236,229))
draw.text((24,48),'Matching controller, casing, MI-style ports and roof-centre vent',font=small,fill=(164,185,176))
for i,tier in enumerate(tiers):
    x=i%5*w;y=i//5*h+90
    draw.text((x+14,y+h-48),tier['tier'].upper(),font=font,fill=(232,237,230))
    draw.text((x+14,y+h-21),f'{tier["lanes"]} lanes | batch {tier["batch"]}',font=small,fill=(165,190,178))
sheet.save(art/'ingame/all-tiers-consistency.png')
print('Saved clean comparison of ten real in-game captures.')
