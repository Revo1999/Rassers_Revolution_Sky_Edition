from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'revoindustry/src/main/resources'
NAMES=['power_converter','advanced_power_converter','elite_power_converter']
def put(path,data):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
for i,name in enumerate(NAMES):
 variants={}
 for face,rot in [('north',{}),('east',{'y':90}),('south',{'y':180}),('west',{'y':270}),('up',{'x':90}),('down',{'x':270})]:variants['facing='+face]={'model':'revoindustry:block/'+name,**rot}
 put('assets/revoindustry/blockstates/'+name+'.json',{'variants':variants})
 put('assets/revoindustry/models/item/'+name+'.json',{'parent':'revoindustry:block/'+name})
 put('data/revoindustry/loot_table/blocks/'+name+'.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'revoindustry:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 metal=['minecraft:iron_ingot','minecraft:gold_ingot','minecraft:diamond'][i]
 # The basic tier uses the checked-in native MI Assembler recipe.
 if i>0:
  put('data/revoindustry/recipe/'+name+'.json',{'neoforge:conditions':[{'type':'neoforge:mod_loaded','modid':'fluxnetworks'},{'type':'neoforge:mod_loaded','modid':'modern_industrialization'}],'type':'minecraft:crafting_shaped','pattern':['IRI','CMC','IRI'],'key':{'I':{'item':metal},'R':{'item':'minecraft:redstone'},'C':{'item':'minecraft:copper_ingot'},'M':{'item':'revoindustry:'+NAMES[i-1]}},'result':{'id':'revoindustry:'+name,'count':1}})
p=R/'data/minecraft/tags/block/mineable/pickaxe.json';d=json.loads(p.read_text(encoding='utf-8'));d['values']=list(dict.fromkeys(d['values']+['revoindustry:'+n for n in NAMES]));put('data/minecraft/tags/block/mineable/pickaxe.json',d)
p=R/'assets/revoindustry/lang/en_us.json';d=json.loads(p.read_text(encoding='utf-8'));d.update({'block.revoindustry.power_converter':'Basic Power Converter','block.revoindustry.advanced_power_converter':'Advanced Power Converter','block.revoindustry.elite_power_converter':'Elite Power Converter','config.jade.plugin_revoindustry.power_converter':'Power Converter','tooltip.revoindustry.converter':'Teal face: FE. Copper face opposite: MI EU. Open to switch direction.','tooltip.revoindustry.converter_rate':'Direct FE / MI EU conversion only. No power storage.'});put('assets/revoindustry/lang/en_us.json',d)
print('Generated three converter tiers, six orientations, recipes, names and loot.')
