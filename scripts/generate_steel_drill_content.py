"""Recipes, block assets, translations and a stable next-tier quest branch."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revoextraction/src/main/resources'
ASSETS=RES/'assets/revoextraction'
def put(p,obj):
    p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
def ingredient(value):return {'tag':value[1:]} if value.startswith('#') else {'item':value}
def recipe(name,pattern,key):
    put(RES/f'data/revoextraction/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:ingredient(v) for k,v in key.items()},'result':{'id':'revoextraction:'+name,'count':1}})
def upgrade(part,n):
    put(RES/f'data/revoextraction/recipe/steel_drill_{part}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':'revoextraction:iron_drill_'+part}]+[{'tag':'c:plates/steel'}]*n,'result':{'id':'revoextraction:steel_drill_'+part,'count':1}})
parts={'support':'Support','casing':'Casing','core':'Core','controller':'Controller','input':'Kinetic Input','output_hatch':'Item Output Hatch','water_tank':'Water Tank'}
lang_path=ASSETS/'lang/en_us.json';lang=json.loads(lang_path.read_text(encoding='utf-8-sig'))
for part,title in parts.items():
    name='steel_drill_'+part;lang['block.revoextraction.'+name]='Steel Drill '+title
    if part!='water_tank':
        old=ASSETS/f'blockstates/iron_drill_{part}.json';put(ASSETS/f'blockstates/{name}.json',json.loads(old.read_text().replace('iron_drill_', 'steel_drill_')))
    else:put(ASSETS/f'blockstates/{name}.json',{'variants':{'formed=false':{'model':'revoextraction:block/'+name},'formed=true':{'model':'revoextraction:block/empty'}}})
    put(ASSETS/f'models/item/{name}.json',{'parent':'revoextraction:block/'+name})
    put(RES/f'data/revoextraction/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'revoextraction:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for part,n in [('support',1),('casing',1),('input',2),('output_hatch',2)]:upgrade(part,n)
recipe('steel_drill_core',['IPI','PCP','IPI'],{'I':'#c:ingots/steel','P':'#c:plates/steel','C':'revoextraction:iron_drill_core'})
recipe('steel_drill_controller',['CCC','CSC','CCC'],{'C':'revoextraction:steel_drill_casing','S':'revoextraction:steel_drill_core'})
recipe('steel_drill_water_tank',['PGP','GTG','PCP'],{'P':'#c:plates/steel','G':'minecraft:glass_pane','T':'minecraft:bucket','C':'#c:ingots/copper'})
recipe('drill_repair_kit',[' P ','LCL',' P '],{'P':'#c:plates/steel','L':'minecraft:leather','C':'#c:ingots/copper'})
put(ASSETS/'models/item/drill_repair_kit.json',{'parent':'minecraft:item/generated','textures':{'layer0':'revoextraction:item/drill_repair_kit'}})
# Reuse the existing Create workshop for steel plates. Native MI plate recipes remain available.
put(RES/'data/revoextraction/recipe/pressing/steel_plate.json',{'type':'create:pressing','ingredients':[{'tag':'c:ingots/steel'}],'results':[{'id':'modern_industrialization:steel_plate','count':1}],'neoforge:conditions':[{'type':'neoforge:mod_loaded','modid':'modern_industrialization'}]})
lang.update({
 'gui.revoextraction.steel_drill':'Steel Drill',
 'emi.category.revoextraction.steel_drill_assembly':'Steel Drill Construction',
 'item.revoextraction.drill_repair_kit':'Drill Repair Kit',
 'gui.revoextraction.water':'Water %s/%s mB',
 'gui.revoextraction.overheat':'Dry-running damage: %s%%',
 'gui.revoextraction.state.dry':'No coolant - overheating',
 'gui.revoextraction.hint.dry':'Refill water. The drill seizes after 5s dry.',
 'gui.revoextraction.state.broken':'Drill seized - repair required',
 'gui.revoextraction.hint.broken':'Stop drive, add water, use a Drill Repair Kit.',
 'gui.revoextraction.hint.too_slow':'Increase shaft speed. Check the RPM in EMI.',
 'gui.revoextraction.hint.minimum_rpm':'Supply at least %s RPM at the shaft input.',
 'gui.revoextraction.repair.not_broken':'This drill does not need a repair kit.',
 'gui.revoextraction.repair.incomplete':'Rebuild the complete multiblock before repairing.',
 'gui.revoextraction.repair.stop_drive':'Stop the shaft before using the repair kit.',
 'gui.revoextraction.repair.need_water':'Add at least 100 mB water before repairing.',
 'gui.revoextraction.repair.success':'Drill repaired. Restore the coolant supply before restarting.'})
put(lang_path,lang)
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';tag=json.loads(p.read_text(encoding='utf-8-sig'));tag['values']=list(dict.fromkeys(tag['values']+['revoextraction:steel_drill_'+k for k in parts]));put(p,tag)
# Separate, stable chapter IDs leave every existing quest/task/completion ID untouched.
base=0x5E40000000000000
hid=lambda n:f'{base+n:016X}'
quests=[];ids={}
def quest(key,title,item=None,count=1,deps=(),x=0,y=0,text=(),items=None):
    n=len(quests)+1;ids[key]=hid(n);inputs=items or ([(item,count)] if item else [])
    tasks=[{'id':hid(0x1000+n*16+i),'type':'item','item':{'id':name,'count':1},'count':amount,'consume_items':False} for i,(name,amount) in enumerate(inputs)] or [{'id':hid(0x1000+n*16),'type':'checkmark'}]
    quests.append({'id':hid(n),'title':title,'icon':{'id':item or 'revoextraction:steel_drill_controller','count':1},'x':float(x),'y':float(y),'shape':'hexagon' if key in ['steel','controller','commission','diamonds'] else 'circle','dependencies':[ids.get(d,d) for d in deps],'description':list(text),'tasks':tasks,'rewards':[]})
quest('steel','Steel from Steam','modern_industrialization:steel_ingot',8,['5E3000000000001C'],0,0,[
 'Mill Coke in a Create Millstone for Coke Dust. Forge-hammer iron into dust, combine seven Iron Dust with two Coke Dust for Uncooked Steel Dust, then process it in the native MI Steam Blast Furnace.',
 'The next Revo drill stays Create-powered, but adds real water cooling and maintenance. Keep the Iron Drill working while building the upgrade.'])
quest('plates','A Reinforced Frame','modern_industrialization:steel_plate',45,['steel'],3,0,[
 'Press steel ingots into steel plates with the Create Mechanical Press, or use native MI plate recipes.',
 'The drill itself needs 45 steel plates: 12 supports, 17 casings (8 inside the controller), 8 for two cores, 4 for the ports and 4 for the tank. Also reserve 8 loose steel ingots for the cores.',
 'A repair kit needs another two steel plates, copper and leather. Select Cow on the Wooden Mob Harvester filter; each cycle gives one leather and two raw beef. The underlying iron components must be crafted separately.'])
quest('frame','Build the Portal Gantry',deps=['plates'],x=6,y=-2,items=[('revoextraction:steel_drill_support',12),('revoextraction:steel_drill_casing',9)],text=[
 'Upgrade Iron Drill Supports and Casings with steel plates. The finished structure uses 12 supports and 9 placed casings.',
 'A water tank replaces the casing directly above the output hatch. Use the Steel Drill construction view in EMI.'])
quest('cores','A Heavier Cutting Assembly','revoextraction:steel_drill_core',2,['plates'],6,1,[
 'Craft TWO Steel Drill Cores. One is consumed when crafting the controller; place the other in the centre of the middle layer.'])
quest('controller','Control the Steel Drill','revoextraction:steel_drill_controller',1,['cores'],9,1,[
 'Surround a steel core with eight extra Steel Drill Casings. The controller belongs at the front centre of the middle layer.',
 'The controller shows RPM, cycle progress, water and damage. Item storage remains exclusively in the output hatch.'])
quest('ports','Shaft, Hatch and Coolant Tank',deps=['plates'],x=6,y=4,items=[('revoextraction:steel_drill_input',1),('revoextraction:steel_drill_output_hatch',1),('revoextraction:steel_drill_water_tank',1)],text=[
 'Install the right-middle shaft input, lower-left item output hatch and left-middle water tank above it. Face the shaft and item hatch outward.',
 'The visible shaft is centred in block 2 of the machine height. The hatch automatically ejects into an adjacent inventory at cycle completion.'])
quest('water','Cool the Cutting Head',deps=['ports'],x=9,y=4,text=[
 'Fill the side tank BEFORE starting. It stores 8000 mB of water and accepts buckets or fluid pipes on any face.',
 'The drill consumes 100 mB per 600 rotational work, spread over its cycle. Minecraft water renders inside the glass tank and falls as it empties. Working jets use the vanilla dripping-water particles.',
 'Connect an MI water pump or Create pump network for a steady supply. Missing coolant prevents production and causes seizure after five seconds of dry running.'])
quest('kit','Prepare for Maintenance','revoextraction:drill_repair_kit',1,['plates'],9,-2,[
 'Keep a repair kit nearby. If the Steel Drill seizes, its output is preserved.',
 'Stop the Create shaft, rebuild any missing frame parts, add at least 100 mB water, then right-click any part with the kit. One kit is consumed only after a successful repair.',
 'Breaking and replacing the controller preserves dry-running damage; it does not repair the machine.'])
quest('commission','Commission the Water-Cooled Drill',deps=['frame','controller','water','kit'],x=12,y=1,text=[
 'Build the 3 x 3 x 3 structure from the EMI layer guide, leave the bottom centre empty and supply water.',
 'Deliver at least 64 RPM and 1024 SU at that speed. One crank is insufficient. Gear up your water-wheel network and add enough generating capacity; gears do not create SU.',
 'Confirm after seeing Working, a moving cutting head, falling water level and cooling drips. A full output stops water use and dry-running damage.'])
quest('diamonds','A New Depth of Resources','minecraft:diamond',1,['commission'],15,1,[
 'Collect a diamond from the Steel Drill. Its fixed 12-item sequence yields 3 iron, 1 copper, 1 tin, 2 coal, 1 zinc, 1 gold, 1 redstone, 1 lapis and 1 diamond.',
 'Use the diamond supply for the native MI Bronze Macerator and further processing. Keep the water line and output chest supplied.'])
put(ROOT/'pack/overrides/config/ftbquests/quests/chapters/5e40000000000000.snbt',{'id':hid(0),'filename':hid(0).lower(),'title':'Chapter 4: Steel and Coolant','icon':{'id':'revoextraction:steel_drill_controller','count':1},'order_index':30,'default_quest_shape':'circle','quests':quests})
print(f'Generated seven steel blocks, eight crafting recipes, one pressing recipe and {len(quests)} quests.')
