"""Classic one-pixel dithering for the three RevoIndustry vacuum tiers."""
from pathlib import Path
from PIL import Image
COLORS=[(155,54,43),(43,110,159),(123,71,172)]
MATERIALS=['body','metal','dark','grille','light','fan','output']
CHECK=((0,2),(3,1))
def painted_palette(base):
 return [tuple(max(0,min(255,round(v*f+n))) for v in base) for f,n in [(.48,0),(.72,0),(.96,0),(1.12,12)]]
def pixel(palette,x,y,level):
 level=max(0,min(3,level));low=int(level);fraction=level-low
 return palette[min(3,low+(fraction>(CHECK[y%2][x%2]+.5)/4))]
def paint_texture(material,base,color):
 steel=[(66,80,91),(103,119,130),(144,161,170),(188,204,209)]
 dark=[(20,28,34),(33,44,53),(49,63,72),(71,87,96)]
 paint=painted_palette(color)
 palette=paint if material=='body' else steel if material=='metal' else [(76,140,150),(112,184,194),(165,221,225),(220,250,245)] if material=='light' else dark
 im=Image.new('RGB',(16,16));p=im.load()
 for y in range(16):
  for x in range(16):
   level=2.6-x*.075-y*.06
   if material=='dark':level+=.35 if y%4==0 else -.2
   if material=='grille':level+=.65 if x%3==0 else -.9
   if material=='fan':level-=.65
   p[x,y]=pixel(palette,x,y,level)
 # Every panel is shaded with single-pixel interleaving; avoid large checker patches.
 if material=='body':
  for y in [0,15]:
   for x in range(16):p[x,y]=pixel(paint,x,y,2.8 if y==0 else .65)
  for y in range(4,7):
   for x in range(3,13):p[x,y]=pixel(steel,x,y,2.25 if y==4 else .75)
  for x,y in [(1,1),(14,1),(1,14),(14,14)]:p[x,y]=steel[0];p[x+1 if x==1 else x-1,y]=steel[2]
 if material=='metal':
  p[2,1]=steel[0];p[3,1]=steel[3]
 if material=='output':
  for y in range(8):
   for x in range(8):
    pal=steel if x in [0,7] or y in [0,7] else paint if x in [1,6] or y in [1,6] else dark
    p[x,y]=pixel(pal,x,y,2.5-(x+y)*.13)
  for x,y in [(2,3),(3,3),(4,3),(5,3),(4,2),(4,4)]:p[x,y]=steel[3]
 if material=='fan':
  for y in range(8):
   for x in range(8):
    if abs(x-y)<=1 or abs(x+y-7)<=1:p[x,y]=pixel(steel,x,y,1.9-(x+y)*.06)
  for y in [3,4]:
   for x in [3,4]:p[x,y]=steel[2]
 return im
if __name__=='__main__':
 root=Path(__file__).resolve().parents[1]
 for tier,color in enumerate(COLORS,1):
  for material in MATERIALS:
   path=root/f'revoindustry/src/main/resources/assets/revoindustry/textures/block/vacuum_{tier}_{material}.png'
   paint_texture(material,None,color).save(path)
 print('Repainted 21 vacuum textures with four-tone palettes and one-pixel checker dithering.')
