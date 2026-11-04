"""Reproducible single-block machine recipes and registration resources."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revocultivation/src/main/resources'
def put(name,value):
 p=RES/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n',encoding='utf-8')
def ingredient(s):return {'tag':s[1:]} if s.startswith('#') else {'item':s}
def shaped(name,pattern,key):
 put('data/revocultivation/recipe/'+name+'.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:ingredient(v) for k,v in key.items()},'result':{'id':'revocultivation:'+name}})
def shapeless(name,ins,out,count=1):
 put('data/revocultivation/recipe/'+name+'.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[ingredient(s) for s in ins],'result':{'id':out,'count':count}})
for name in ['composter','silkworm_cultivator']:
 put('assets/revocultivation/blockstates/'+name+'.json',{'variants':{'facing='+face:{'model':'revocultivation:block/'+name,'y':rot} for face,rot in [('north',0),('east',90),('south',180),('west',270)]}})
 put('assets/revocultivation/models/item/'+name+'.json',{'parent':'revocultivation:block/'+name})
 put('data/revocultivation/loot_table/blocks/'+name+'.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'revocultivation:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for name in ['compost','silkworm','silk_cocoon','silk']:
 put('assets/revocultivation/models/item/'+name+'.json',{'parent':'minecraft:item/generated','textures':{'layer0':'revocultivation:item/'+name}})
shaped('composter',['L L','P P','PPP'],{'L':'#minecraft:logs','P':'#minecraft:planks'})
shaped('silkworm_cultivator',['S S','PLP','CCC'],{'S':'minecraft:stick','P':'#minecraft:planks','L':'#minecraft:leaves','C':'minecraft:cobblestone'})
shapeless('silkworm',['#minecraft:leaves']*4,'revocultivation:silkworm')
shapeless('dirt_from_compost',['revocultivation:compost'],'minecraft:dirt')
shapeless('string_from_cocoon',['revocultivation:silk_cocoon'],'minecraft:string',2)
shapeless('silk',['revocultivation:silk_cocoon']*2,'revocultivation:silk')
shapeless('string_from_silk',['revocultivation:silk'],'minecraft:string',4)
for tag,values in [('minecraft:blocks/mineable/axe',['revocultivation:composter','revocultivation:silkworm_cultivator']),('c:items/strings',['revocultivation:silk']),('c:items/silks',['revocultivation:silk'])]:
 ns,path=tag.split(':');kind,path=path.split('/',1);put(f'data/{ns}/tags/{kind[:-1]}/{path}.json',{'replace':False,'values':values})
names={'gui.revocultivation.duration':'%ss cycle','recipe.revocultivation.compost_time':'8s per full batch','recipe.revocultivation.passive_time':'Automatic, no power','small.revocultivation.status.5':'Ready - collect output','small.revocultivation.ready':'Ready - collect output','emi.category.revocultivation.composting':'Composting','emi.category.revocultivation.silkworm_cultivation':'Silkworm Cultivation','block.revocultivation.composter':'Composter','block.revocultivation.silkworm_cultivator':'Silk Worm Cultivator','item.revocultivation.compost':'Compost','item.revocultivation.silkworm':'Silkworm','item.revocultivation.silk_cocoon':'Silk Cocoon','item.revocultivation.silk':'Silk','small.revocultivation.status.0':'Working','small.revocultivation.status.1':'Needs plant material','small.revocultivation.status.2':'Needs a starter silkworm','small.revocultivation.status.3':'Output full','recipe.revocultivation.worm':'20s · starter worm retained','recipe.revocultivation.tamping':'8 tamps · 8s at 32 RPM','recipe.revocultivation.small_output':'Front output auto-ejects'}
for obsolete in ['gui.revocultivation.tamp','small.revocultivation.status.4','recipe.revocultivation.tamping','recipe.revocultivation.small_drive']:names.pop(obsolete,None)
for lang in ['en_us','da_dk']:
 path='assets/revocultivation/lang/'+lang+'.json';p=RES/path;data=json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {};[data.pop(k,None) for k in ['gui.revocultivation.tamp','small.revocultivation.status.4','recipe.revocultivation.tamping','recipe.revocultivation.small_drive']];data.update(names);put(path,data)
print('Single-block resources generated')

