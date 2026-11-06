"""Registration assets, upgrade recipes, language and stable farm side quests."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revocultivation/src/main/resources'
GEN=ROOT/'revogenetics/src/main/resources'
def put(p,value):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n',encoding='utf-8')
def ing(value):return {'tag':value[1:]} if value.startswith('#') else {'item':value}
def shapeless(name,items,ns='revocultivation'):
    put((GEN if ns=='revogenetics' else RES)/f'data/{ns}/recipe/{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[ing(i) for i in items],'result':{'id':ns+':'+name,'count':1}})
def shaped(name,pattern,key,ns='revocultivation'):
    put((GEN if ns=='revogenetics' else RES)/f'data/{ns}/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:ing(v) for k,v in key.items()},'result':{'id':ns+':'+name,'count':1}})
langpath=RES/'assets/revocultivation/lang/en_us.json';lang=json.loads(langpath.read_text(encoding='utf-8-sig'))
genlangpath=GEN/'assets/revogenetics/lang/en_us.json';genlang=json.loads(genlangpath.read_text(encoding='utf-8-sig'))
parts={}
base={'support':'timber_support','casing':'wooden_casing','soil_bed':'soil_bed','item_hatch':'item_hatch','collection_grate':'collection_grate','barred_panel':'barred_panel','mob_filter':'mob_filter','glass_panel':'glass_panel'}
for tier,suffixes in [('steel',['support','casing','soil_bed','item_hatch','collection_grate','barred_panel','mob_filter']),('iron',['support','casing','soil_bed','item_hatch','glass_panel'])]:
    for suffix in suffixes:
        name=tier+'_'+suffix;parts[name]={'facing':suffix in ['item_hatch','mob_filter'],'filter':suffix=='mob_filter','model':name,'title':tier.title()+' '+suffix.replace('_',' ').title()}
        n=2 if suffix in ['item_hatch','barred_panel','mob_filter'] else 1
        shapeless(name,['revocultivation:'+base[suffix]]+['#c:plates/'+tier]*n)
parts['iron_grow_light']={'facing':False,'model':'iron_grow_light','title':'Iron Greenhouse Grow Light','light':True}
shaped('iron_grow_light',['PGP','RTR','PGP'],{'P':'#c:plates/iron','G':'minecraft:glass_pane','R':'minecraft:redstone','T':'minecraft:torch'})
machines=[('steel_tree_grower','Steel Tree Grower','tree_grower','steel','revocultivation'),('iron_greenhouse','Iron Electric Greenhouse','greenhouse','iron','revocultivation'),('steel_mob_harvester','Steel Mob Harvester','mob_harvester','steel','revogenetics')]
for name,title,old,tier,ns in machines:
    parts[name]={'facing':True,'model':name+'_controller','title':title+' Controller','ns':ns}
    shaped(name,['PPP','PCP','PPP'],{'P':'#c:plates/'+tier,'C':ns+':'+old},ns)
    lang['machine.revocultivation.'+name]=title
    for category in ['assembly','production']:lang[f'emi.category.revocultivation.{name}_{category}']=title+' '+category.title()
for name,part in parts.items():
    ns=part.get('ns','revocultivation');res=GEN if ns=='revogenetics' else RES;variants={}
    for formed in [False,True]:
        for facing,rotation in ([('north',0),('east',90),('south',180),('west',270)] if part['facing'] else [(None,None)]):
            for species in [None]: # Filter species changes behavior, not its block model.
                for lit in [False,True] if part.get('light') else [None]:
                    key=(','.join(([f'facing={facing}'] if facing else [])+[f'formed={str(formed).lower()}']+([f'species={species}'] if species is not None else [])+([f'lit={str(lit).lower()}'] if lit is not None else [])))
                    value={'model':'revocultivation:block/'+('empty' if formed else part['model'])}
                    if rotation is not None:value['y']=rotation
                    variants[key]=value
    put(res/f'assets/{ns}/blockstates/{name}.json',{'variants':variants})
    put(res/f'assets/{ns}/models/item/{name}.json',{'parent':'revocultivation:block/'+part['model']})
    put(res/f'data/{ns}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':ns+':'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    (genlang if ns=='revogenetics' else lang)['block.'+ns+'.'+name]=part['title']
lang.update({
 'gui.revocultivation.hatch':'Machine I/O Hatch',
 'gui.revocultivation.tier.wooden':'Wooden tier - passive production',
 'gui.revocultivation.tier.steel':'Steel tier - accelerated production',
 'gui.revocultivation.tier.electric':'Iron tier - electric grow lights',
 'gui.revocultivation.water_capacity':'Water: %s / %s mB',
 'gui.revocultivation.energy':'EU: %s/%s | %s EU/t',
 'state.revocultivation.9':'Electric power needed',
 'hint.revocultivation.9':'Supply FE to the rear converter or EU to the hatch.',
 'state.revocultivation.10':'Converter mode incorrect',
 'hint.revocultivation.10':'Set the converter to FE to EU mode.',
 'part.revocultivation.power':'Power Converter',
 'part.revocultivation.light':'Grow light panel',
 'guide.revocultivation.iron_base':'Rear-right corner: Basic Power Converter. Blue FE face points rearward; EU face touches the hatch.',
 'guide.revocultivation.iron_glass':'Build iron pillars and four iron glass panels. Keep the crop chamber empty.',
 'guide.revocultivation.iron_lights':'Finish the roof with the grow light panel in the centre. Supply water and 8 EU/t; works day and night.',
 'config.jade.plugin_revocultivation.first_tier_status':'Revo farm status'})
put(langpath,lang);put(genlangpath,genlang)
for ns,res in [('revocultivation',RES),('revogenetics',GEN)]:
    tag=res/'data/minecraft/tags/block/mineable/pickaxe.json';value=json.loads(tag.read_text(encoding='utf-8-sig')) if tag.exists() else {'replace':False,'values':[]}
    value['values']=list(dict.fromkeys(value['values']+[ns+':'+name for name,part in parts.items() if part.get('ns','revocultivation')==ns]));put(tag,value)
# Add farm branches to the existing chapter without shifting any existing quest/task ID.
chapter=ROOT/'pack/overrides/config/ftbquests/quests/chapters/5e40000000000000.snbt';book=json.loads(chapter.read_text(encoding='utf-8-sig'))
baseid=0x5E40000000000000
for i,(name,title,old,tier,ns) in enumerate(machines,11):
    ident=f'{baseid+i:016X}';book['quests']=[q for q in book['quests'] if q['id']!=ident]
    if name=='iron_greenhouse':text=[
        'Upgrade the wooden greenhouse with iron supports, casings, soil bed, glass panels and an iron I/O hatch. Add the Grow Light Panel and a Basic Power Converter to the structure; the EMI guide shows every layer.',
        'The converter replaces the REAR-RIGHT BOTTOM support. Wrench it so its blue FE face points outward to the rear; the EU face touches the hatch. Keep FE to EU mode. The converter itself stores no power.',
        'Supply 8 EU/t, or 80 FE/t with the default 10 FE/EU setting. FE enters the converter; native MI EU can enter the hatch. The greenhouse holds a 1600 EU buffer.',
        'A retained crop starter and 250 mB water produce a harvest every 10 seconds. Its 16000 mB tank accepts bucket or piped water. Powered grow lights let it work through the night and indoors. Output remains in the hatch.']
    elif name=='steel_tree_grower':text=[
        'Upgrade the wooden Tree Grower using the steel controller, four steel supports, two steel casings, a steel soil bed and steel I/O hatch. Follow the one-layer EMI construction guide.',
        'A retained oak sapling produces six logs, two leaves, two sticks and a sapling every 20 seconds. Keep natural daylight and the 3 x 3 x 3 growing space above the base clear.',
        'The hatch auto-ejects into an adjacent inventory and pauses safely when outputs are full.']
    else:text=[
        'Build the 3 x 3 x 3 steel cage from twelve supports, seven casings, three barred panels, a collection grate, species filter, item hatch and steel controller. See EMI for layers and directions.',
        'The selected Zombie, Skeleton or Spider remains an actual persistent machine-owned mob. It cannot despawn or escape. Right-click the front filter to change species.',
        'The steel harvester produces the existing species drops every 7.5 seconds. This is passive production. All drops must fit before a cycle completes; the hatch auto-ejects.']
    book['quests'].append({'id':ident,'title':title,'icon':{'id':ns+':'+name,'count':1},'x':float(3+(i-11)*5),'y':8.0,'shape':'diamond','dependencies':['5E20000000000014' if tier=='iron' else '5E40000000000002'],'description':text,'tasks':[{'id':f'{baseid+0x1000+i*16:016X}','type':'item','item':{'id':ns+':'+name,'count':1},'count':1,'consume_items':False}],'rewards':[]})
put(chapter,book)
print(f'Generated {len(parts)} metal farm block assets/recipes and three stable quest branches.')
