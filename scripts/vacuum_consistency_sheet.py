from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
root=Path(__file__).resolve().parents[1];art=root/'art/blockbench/vacuums/ingame'
canvas=Image.new('RGB',(960,450),(23,28,32));d=ImageDraw.Draw(canvas)
bold=ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf',23);font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',16)
d.text((20,16),'REVOINDUSTRY VACUUMS',font=bold,fill=(232,238,239));d.text((20,47),'10 / 12 / 14 px pipes | Dithered textures | Rear outputs',font=font,fill=(161,180,185))
for i in range(3):
 im=Image.open(art/f'tier-{i+1}.png').convert('RGB').crop((490,285,810,605));canvas.paste(im,(i*320,76))
 d.text((i*320+14,405),f'Mk {i+1} | {[3,5,9][i]}x{[3,5,9][i]}x{[3,5,9][i]} area',font=font,fill=(234,240,242))
 d.text((i*320+14,427),f'{[1,3,9][i]} {"slot" if i==0 else "slots"} | {[2.5,5,20][i]} items/s',font=font,fill=(163,188,196))
canvas.save(art/'all-tiers.png')
