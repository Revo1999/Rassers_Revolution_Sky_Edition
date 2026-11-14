"""Package the six original game captures without retouching or overlays."""
from pathlib import Path
import json,zipfile,html
from PIL import Image,ImageDraw

R=Path(__file__).resolve().parents[1]
D=R/'screenshots/promo'
shots=[('01-first-island','The First Island','Morning'),('02-glass-and-growth','Glass and Growth','Day'),('03-copper-current','Copper Current','Golden hour'),('04-greenhouse-after-dark','Greenhouse After Dark','Night'),('05-night-shift','The Night Shift','Night'),('06-industry-in-the-clouds','Industry Above the Clouds','Blue hour')]
assert (D/'capture-result.txt').read_text().startswith('PASS:')
sheet=Image.new('RGB',(1600,1470),(24,30,30));draw=ImageDraw.Draw(sheet)
cards=[]
for i,(name,title,time) in enumerate(shots):
    p=D/(name+'.png');im=Image.open(p)
    assert im.size==(2560,1440),(name,im.size)
    thumb=im.convert('RGB').resize((800,450),Image.Resampling.LANCZOS)
    x=(i%2)*800;y=(i//2)*490;sheet.paste(thumb,(x,y));draw.text((x+18,y+463),f'{i+1}. {title} / {time}',fill=(233,224,204))
    cards.append(f'<a href="{name}.png"><img src="{name}.png" alt="{html.escape(title)}"><h2>{html.escape(title)}</h2><p>{time} · 2560×1440</p></a>')
sheet.save(D/'contact-sheet.jpg',quality=92)
(D/'index.html').write_text('<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Rassers Revolution — Skybase</title><style>body{margin:32px;background:#181e1e;color:#e9e0cc;font:16px system-ui}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(420px,1fr));gap:24px}a{color:inherit;text-decoration:none;background:#252e2c;padding-bottom:8px}img{width:100%;display:block}h2,p{margin:12px 18px}header{margin-bottom:28px}header p{margin:8px 0;color:#aeb9ad}</style><header><h1>Rassers Revolution: Sky Edition</h1><p>Six in-game scenes · A connected, functioning skyblock base · Daylight and night lighting</p></header><main>'+''.join(cards)+'</main></html>',encoding='utf-8')
(D/'README.md').write_text('# Skyblock promotional screenshots\n\nSix original 2560×1440 Minecraft captures using the installed Complementary Reimagined shaders. No HUD, compositing or retouching on the original PNGs.\n\n'+''.join(f'- [{title} — {time}]({name}.png)\n' for name,title,time in shots)+'\n[Browse the gallery](index.html) · [Contact sheet](contact-sheet.jpg)\n\nThe separate `Revo Promo Skybase` save contains the connected homestead, greenhouse, copper workshop, foundry and storage hall. The two greenhouse compositions show the same farm by day and with its grow lights on at night. Automation uses normal machine ticks and real inventory/fluid connections. The creative-built showcase uses native creative motors and FE supplies, with stocked input chests. See `automation-proof.txt` for actual production counters.\n',encoding='utf-8')
manifest={'screenshots':[{'file':name+'.png','title':title,'lighting':time,'pixels':[2560,1440]} for name,title,time in shots],'originals':'Native Minecraft screenshots, no image alteration','world':'Revo Promo Skybase','shader':'Complementary Reimagined r5.9.3','automation_proof':(D/'automation-proof.txt').read_text()}
(D/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
dest=Path(r'C:\Users\Victor\Desktop\RasersRevoModpack\skyblock-promo-screenshots.zip')
with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED) as z:
    for name,_,_ in shots:z.write(D/(name+'.png'),name+'.png')
    for name in ['README.md','manifest.json','automation-proof.txt','contact-sheet.jpg','index.html']:z.write(D/name,name)
print(dest)
