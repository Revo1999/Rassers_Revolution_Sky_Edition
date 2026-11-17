"""Native pixel textures, block/item models and early textile recipes for running shoes."""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revocultivation/src/main/resources'
ART=ROOT/'art/blockbench/running-shoes'
COLORS={'white':'e9ecec','black':'292b30','brown':'835432','red':'b93732','orange':'ed8629','yellow':'edc83d','green':'527b32','cyan':'238a97','blue':'3654a4','purple':'8241b4','magenta':'bd4eb3','pink':'e98fa7'}
def put(path,data):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
def save(im,path):path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
def shade(color,k):return tuple(max(0,min(255,int(int(color[i:i+2],16)*k))) for i in [0,2,4])
def recipe(name,inputs,out,count=1):
    put(RES/f'data/revocultivation/recipe/running_shoes/{name}.json',{'type':'minecraft:crafting_shapeless','category':'equipment','ingredients':[{'tag':i[1:]} if i.startswith('#') else {'item':i} for i in inputs],'result':{'id':out,'count':count}})
def main():
    ART.mkdir(parents=True,exist_ok=True)
    langpath=RES/'assets/revocultivation/lang/en_us.json';lang=json.loads(langpath.read_text(encoding='utf-8'))
    sheet=Image.new('RGB',(768,504),'#202832');sd=ImageDraw.Draw(sheet)
    for n,(name,color) in enumerate(COLORS.items()):
        im=Image.new('RGBA',(64,64),(0,0,0,0));d=ImageDraw.Draw(im)
        d.rectangle((0,0,30,10),fill='#2d353f')
        for x in range(1,28,4):d.rectangle((x,3,x+1,8),fill='#46505c')
        d.rectangle((0,12,31,22),fill='#dddcd1');d.line((0,13,30,13),fill='#fff8e5');d.line((0,20,30,20),fill='#a2a8a6')
        d.rectangle((0,24,23,35),fill=shade(color,1))
        # Low-contrast, native-pixel checker clusters suggest woven fabric.
        for y in range(24,36):
            for x in range(24):
                if (x+y)%2==0:d.point((x,y),fill=shade(color,.88))
        # Two simple cuboids per shoe; all identifying detail stays in the pixels.
        accent='#f3f2e8' if name not in ['white','yellow'] else '#536778'
        for x in [1,11]:d.line((x,31,x+2,30,x+3,30),fill=accent)
        # The upper's top UV occupies [6,10) x [24,30). Its middle two
        # columns are 7 and 8; three columns would shift the laces sideways.
        d.rectangle((7,24,8,27),fill=shade(color,.7))
        d.rectangle((7,28,8,29),fill='#252d34')
        for y in [25,27]:d.line((7,y,8,y),fill='#f3f0e2')
        texture=f'assets/revocultivation/textures/models/armor/running_shoes/{name}_layer_1.png'
        save(im,RES/texture);save(im,ART/f'textures/{name}.png')
        # Models are edited and exported exclusively through Blockbench MCP.
        ingredients=['revocultivation:cotton']*4+['revocultivation:natural_shoe_rubber']*2+['revocultivation:shoe_glue','minecraft:string']
        if name!='white':ingredients+=['minecraft:'+name+'_dye']
        recipe(name,ingredients,f'revocultivation:{name}_running_shoes')
        lang[f'item.revocultivation.{name}_running_shoes']=name.title()+' Running Shoes'
        # Pixel-art colourway card; the actual inventory and worn models are 3D.
        card=Image.new('RGBA',(64,36),(0,0,0,0));c=ImageDraw.Draw(card)
        for ox,oy in [(5,2),(27,9)]:
            c.rectangle((ox+1,oy+6,ox+25,oy+17),fill=shade(color,1))
            for yy in range(oy+8,oy+17):
                for xx in range(ox+1,ox+26):
                    if (xx+yy)%2==0:c.point((xx,yy),fill=shade(color,.88))
            c.rectangle((ox,oy+18,ox+26,oy+21),fill='#e9e7da')
            c.line((ox+7,oy+13,ox+12,oy+10,ox+15,oy+10),fill=accent)
            c.rectangle((ox+3,oy+6,ox+8,oy+7),fill='#252d34')
            for xx in [13,17]:c.line((ox+xx,oy+6,ox+xx+1,oy+7),fill='#f4f1e7')
        px=n%4*192;py=n//4*168;sheet.paste(card.resize((192,108),Image.Resampling.NEAREST),(px,py),card.resize((192,108),Image.Resampling.NEAREST));sd.text((px+14,py+127),name.upper(),fill='#ebede7')
    save(sheet,ART/'colourways.png')
    recipe('cotton_seeds',['minecraft:wheat_seeds','minecraft:string'],'revocultivation:cotton_seeds',2)
    recipe('shoe_latex',['#minecraft:leaves','#minecraft:leaves','minecraft:slime_ball'],'revocultivation:shoe_latex',2)
    recipe('shoe_glue',['minecraft:slime_ball','minecraft:sugar'],'revocultivation:shoe_glue',4)
    put(RES/'data/revocultivation/recipe/running_shoes/cure_rubber.json',{'type':'minecraft:smelting','category':'misc','ingredient':{'item':'revocultivation:shoe_latex'},'result':{'id':'revocultivation:natural_shoe_rubber'},'experience':.1,'cookingtime':200})
    for name,label in {'cotton':'Cotton','cotton_seeds':'Cotton Seeds','shoe_latex':'Shoe Latex','natural_shoe_rubber':'Natural Shoe Rubber','shoe_glue':'Shoe Glue'}.items():
        lang['item.revocultivation.'+name]=label
        im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
        if name=='cotton':
            for x,y in [(4,7),(8,4),(10,8),(6,10)]:d.rectangle((x-2,y-2,x+2,y+2),fill='#e8e3d5');d.point((x,y-1),fill='#ffffff')
            d.line((7,10,8,14),fill='#63733c')
        elif name=='cotton_seeds':
            for x,y in [(4,7),(9,4),(11,10),(6,12)]:d.rectangle((x,y,x+2,y+2),fill='#73583b');d.point((x,y),fill='#b6aa6a')
        elif name=='natural_shoe_rubber':
            d.polygon([(3,4),(10,3),(13,7),(12,12),(4,13),(2,9)],fill='#383b39');d.line((4,5,9,4,11,7),fill='#686b5b')
        else:
            d.polygon([(3,8),(6,3),(10,4),(13,9),(11,13),(4,12)],fill='#f4db9b' if name=='shoe_glue' else '#e9e3c5');d.line((5,8,7,5,9,6),fill='#fff6dd')
        save(im,RES/f'assets/revocultivation/textures/item/{name}.png');put(RES/f'assets/revocultivation/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'revocultivation:item/{name}'}})
    lang['block.revocultivation.cotton_crop']='Cotton Plant';lang['program.revocultivation.cotton']='Cotton'
    lang['tooltip.revocultivation.running_shoes']='Lightweight woven uppers and cushioned rubber soles.'
    lang['tooltip.revocultivation.running_shoes_fall']='No fall damage while worn. Does not protect against the void.'
    put(langpath,lang)
    variants={}
    for age in range(8):
        variants[f'age={age}']={'model':f'revocultivation:block/cotton_stage_{age}'}
        im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);top=max(2,13-age*2)
        d.line((8,15,8,top),fill='#6e813b')
        for x,y in [(4,11),(11,9),(5,6)]:
            if y>=top:d.line((8,y+2,x,y),fill='#899848',width=2)
        if age>=5:
            for x,y in [(4,9),(11,7),(7,3)]:
                d.rectangle((x-1,y-1,x+2,y+2),fill='#d4ceb9' if age<7 else '#eee8d7');d.point((x,y),fill='#fffaf0')
        save(im,RES/f'assets/revocultivation/textures/block/cotton_stage_{age}.png')
        put(RES/f'assets/revocultivation/models/block/cotton_stage_{age}.json',{'parent':'minecraft:block/crop','textures':{'crop':f'revocultivation:block/cotton_stage_{age}'}})
    put(RES/'assets/revocultivation/blockstates/cotton_crop.json',{'variants':variants})
    mature={'condition':'minecraft:block_state_property','block':'revocultivation:cotton_crop','properties':{'age':'7'}}
    put(RES/'data/revocultivation/loot_table/blocks/cotton_crop.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'revocultivation:cotton_seeds'}]},{'rolls':1,'conditions':[mature],'entries':[{'type':'minecraft:item','name':'revocultivation:cotton_seeds'}]},{'rolls':1,'conditions':[mature],'entries':[{'type':'minecraft:item','name':'revocultivation:cotton'}]}]})
    put(RES/'data/minecraft/tags/block/crops.json',{'replace':False,'values':['revocultivation:cotton_crop']})
    put(RES/'data/c/tags/item/crops/cotton.json',{'replace':False,'values':['revocultivation:cotton']})
    put(RES/'data/c/tags/item/seeds/cotton.json',{'replace':False,'values':['revocultivation:cotton_seeds']})
    put(RES/'data/revocultivation/advancement/recipes/running_shoes.json',{'criteria':{'cotton':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['revocultivation:cotton']}]}}},'rewards':{'recipes':['revocultivation:running_shoes/'+n for n in COLORS]}})
    print('Generated 12 shoe colourways, cotton crop, early materials and 16 recipes.')
if __name__=='__main__':main()
