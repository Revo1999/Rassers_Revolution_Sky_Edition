"""Registration data only. Geometry and the atlas are authored/exported by Blockbench MCP."""
from pathlib import Path
import json

ROOT=Path(__file__).resolve().parents[1]
def write(path,value):
    path=ROOT/path;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,indent=2)+'\n',encoding='utf-8')
def asset(mod,kind,name,data):write(f'{mod}/src/main/resources/assets/{mod}/{kind}/{name}.json',data)
def data(mod,kind,name,value):write(f'{mod}/src/main/resources/data/{mod}/{kind}/{name}.json',value)
parts=['timber_support','wooden_casing','soil_bed','collection_grate','glass_panel','barred_panel','mob_filter','item_hatch']
controllers=[('revocultivation','tree_grower'),('revocultivation','greenhouse'),('revogenetics','mob_harvester')]
asset('revocultivation','models/block','empty',{'elements':[]})
for mod,name in [('revocultivation',p) for p in parts]+controllers:
    directional=name=='item_hatch' or name=='mob_filter' or (mod,name) in controllers
    native=name+'_controller' if (mod,name) in controllers else name
    variants={}
    for formed in [False,True]:
        for i,face in enumerate(['north','east','south','west'] if directional else ['']):
            key=('facing='+face+',' if face else '')+'formed='+str(formed).lower()
            variants[key]={'model':'revocultivation:block/'+('empty' if formed else native)}
            if face:variants[key]['y']=i*90
    asset(mod,'blockstates',name,{'variants':variants})
    asset(mod,'models/item',name,{'parent':'revocultivation:block/'+native})
    data(mod,'loot_table','blocks/'+name,{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':mod+':'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})

def shaped(mod,name,pattern,keys,count=1):
    data(mod,'recipe',name,{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:{'tag' if v.startswith('#') else 'item':v.removeprefix('#')} for k,v in keys.items()},'result':{'id':mod+':'+name,'count':count}})
shaped('revocultivation','timber_support',['LCL','LIL'],{'L':'#minecraft:logs','C':'minecraft:cobblestone','I':'minecraft:iron_nugget'},4)
shaped('revocultivation','wooden_casing',['PPP','PCP','PPP'],{'P':'#minecraft:planks','C':'minecraft:cobblestone'},8)
shaped('revocultivation','soil_bed',['DDD','DWD'],{'D':'minecraft:dirt','W':'revocultivation:wooden_casing'},4)
shaped('revocultivation','collection_grate',['III','CWC'],{'I':'minecraft:iron_bars','C':'minecraft:cobblestone','W':'revocultivation:wooden_casing'},2)
shaped('revocultivation','glass_panel',['PGP','PGP'],{'P':'#minecraft:planks','G':'minecraft:glass'},4)
shaped('revocultivation','barred_panel',['PIP','PIP'],{'P':'#minecraft:planks','I':'minecraft:iron_bars'},4)
shaped('revocultivation','mob_filter',['BIB','BLB'],{'B':'revocultivation:barred_panel','I':'minecraft:iron_nugget','L':'minecraft:lever'})
shaped('revocultivation','item_hatch',['CCC','IHI','CCC'],{'C':'minecraft:cobblestone','I':'minecraft:iron_nugget','H':'minecraft:hopper'})
for mod,name in controllers:
    special='minecraft:oak_sapling' if name=='tree_grower' else 'minecraft:wheat_seeds' if name=='greenhouse' else 'revocultivation:mob_filter'
    shaped(mod,name,['CCC','LSL','CBC'],{'C':'revocultivation:wooden_casing','L':'minecraft:lever','S':special,'B':'minecraft:stone_button'})

lang={
 'machine.revocultivation.tree_grower':'Tree Grower','machine.revocultivation.greenhouse':'Greenhouse','machine.revocultivation.mob_harvester':'Mob Harvester',
 'gui.revocultivation.hatch':'Timber I/O Hatch','gui.revocultivation.tier':'Wooden tier - passive production',
 'gui.revocultivation.guide':'EMI build guide','gui.revocultivation.program':'Program: %s',
 'gui.revocultivation.cycle':'Cycle: %s%% (%s s)','gui.revocultivation.water':'Water: %s / 8000 mB',
 'gui.revocultivation.passive':'Passive - no power required','gui.revocultivation.daylight':'Natural daylight required',
 'gui.revocultivation.inputs':'Inputs','gui.revocultivation.outputs':'Output','gui.revocultivation.buffer':'Buffered output: %s items',
 'message.revocultivation.assemble':'Assemble the machine first. See the controller or EMI guide.',
 'message.revocultivation.filter':'Mob filter: %s',
 'hint.revocultivation.facing':'%s (%s, %s, %s): face %s.',
 'guide.revocultivation.tree':'3 x 3 base. Keep 3 blocks above clear. Controller front, hatch outward.',
 'guide.revocultivation.base':'Build bottom with bed/grate centre. Controller front, hatch on its side.',
 'guide.revocultivation.glass':'Add four pillars and glass walls. Keep the centre empty for crops.',
 'guide.revocultivation.bars':'Add barred walls and the front mob filter. Right-click filter to select species.',
 'guide.revocultivation.top':'Cap the corners and beams. Greenhouse: glass centre. Harvester: casing centre.',
 'recipe.revocultivation.time':'%s seconds per cycle','recipe.revocultivation.retained':'Starter retained; daylight required.','recipe.revocultivation.filter':'Select species on the mob filter.',
 'config.jade.plugin_revocultivation.first_tier_status':'First-tier Revo machine status',
}
states={0:('Incomplete multiblock','Follow the interactive EMI build guide.'),1:('Working','Collect output through the side hatch.'),2:('Starter missing','Put the starter in the hatch input slot.'),3:('Water needed','Add a water bucket or pipe water into the hatch.'),4:('Daylight needed','Expose the roof to natural daytime sky.'),5:('Output blocked','Clear space in the hatch or adjacent inventory.'),6:('Tree space obstructed','Clear 3 x 3 x 3 space above the base.'),7:('Wrong block orientation','Rotate the indicated part.'),8:('Structure not loaded','Load the entire structure to resume.')}
for i,(name,hint) in states.items():lang['state.revocultivation.'+str(i)]=name;lang['hint.revocultivation.'+str(i)]=hint
for name in ['oak','wheat','carrot','potato','beetroot','zombie','skeleton','spider','cow','blaze','none']:lang['program.revocultivation.'+name]=name.capitalize() if name!='none' else 'No starter'
for part in ['support','casing','controller','hatch','bed','grate','glass','bars','filter','air']:lang['part.revocultivation.'+part]=part.capitalize()
for face in ['north','east','south','west','up','down']:lang['direction.revocultivation.'+face]=face
for name in parts:lang['block.revocultivation.'+name]={'timber_support':'Reinforced Timber Support','wooden_casing':'Wooden Casing','soil_bed':'Cultivation Soil Bed','collection_grate':'Collection Grate','glass_panel':'Greenhouse Glass Panel','barred_panel':'Barred Cage Panel','mob_filter':'Mob Species Filter','item_hatch':'Timber I/O Hatch'}[name]
for mod,name in controllers:
    if mod=='revocultivation':lang['block.'+mod+'.'+name]='Wooden '+lang['machine.revocultivation.'+name]+' Controller'
    for suffix,label in [('assembly','Assembly'),('production','Production')]:lang['emi.category.revocultivation.'+name+'_'+suffix]=lang['machine.revocultivation.'+name]+' '+label
asset('revocultivation','lang','en_us',lang)
asset('revogenetics','lang','en_us',{'block.revogenetics.mob_harvester':'Wooden Mob Harvester Controller'})
print('Generated blockstates, item references, self-drop loot, crafting recipes and translations for all three first tiers.')
