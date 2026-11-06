"""Apply the connected-glass, tank and five-block tree updates after older content generators."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'revocultivation/src/main/resources'
def put(p,obj):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
for tier in ['iron','steel']:
    name=tier+'_water_tank'
    put(RES/f'assets/revocultivation/blockstates/{name}.json',{'variants':{'formed=false':{'model':'revocultivation:block/'+name},'formed=true':{'model':'revocultivation:block/empty'}}})
    put(RES/f'assets/revocultivation/models/item/{name}.json',{'parent':'revocultivation:block/'+name})
    put(RES/f'data/revocultivation/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'revocultivation:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    put(RES/f'data/revocultivation/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['PGP','GBG','PGP'],'key':{'P':{'tag':'c:plates/'+tier},'G':{'item':'minecraft:glass'},'B':{'item':'minecraft:bucket'}},'result':{'id':'revocultivation:'+name,'count':1}})
    p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';j=json.loads(p.read_text(encoding='utf-8-sig'));j['values']=list(dict.fromkeys(j['values']+['revocultivation:'+name]));put(p,j)
langpath=RES/'assets/revocultivation/lang/en_us.json';lang=json.loads(langpath.read_text(encoding='utf-8-sig'))
lang.update({'block.revocultivation.iron_water_tank':'Iron Farm Water Tank','block.revocultivation.steel_water_tank':'Steel Farm Water Tank','part.revocultivation.tank':'Attached water tank','part.revocultivation.soil':'Dirt planting block',
 'guide.revocultivation.steel_tree':'5 x 5 base: dirt centre, 8 soil beds, 4 corners, 9 casings. Tank left, hatch right, controller front. Leave 7 blocks above clear.',
 'guide.revocultivation.iron_base':'5 x 5 base around a full 3 x 3 soil bed. Tank left-centre; converter rear-right; hatch immediately in front of converter, facing right.',
 'guide.revocultivation.iron_glass':'Leave the 3 x 3 middle chamber empty for nine crop models. Join the borderless glass walls inside the steel frame. Keep corner supports.',
 'guide.revocultivation.iron_lights':'Roof: 8 interior glass panels around the central grow light; casing border and corner supports. Four thin white LED strips run under the square top beams and switch on automatically at night or during rain. Supply 8 EU/t and water.',
 'hint.revocultivation.real_tree':'Clear 5 x 5 x 7 above the base. Farm-grown tree blocks are tracked.'})
put(langpath,lang)
industry=ROOT/'revoindustry/src/main/resources/assets/revoindustry'
put(industry/'models/block/empty.json',{'parent':'minecraft:block/block','textures':{'particle':'revoindustry:block/power_converter/side'},'elements':[]})
for name in ['power_converter','advanced_power_converter','elite_power_converter']:
    p=industry/f'blockstates/{name}.json';old=json.loads(p.read_text(encoding='utf-8-sig'))['variants'];variants={}
    for key,value in old.items():
        if 'integrated=' in key:continue
        variants[key+',integrated=false']=value
        variants[key+',integrated=true']={'model':'revoindustry:block/empty'}
    # This generator is idempotent when called against its own previously generated output.
    if not variants:
        for key,value in old.items():
            if key.endswith(',integrated=false'):
                variants[key]=value;variants[key.replace('integrated=false','integrated=true')]={'model':'revoindustry:block/empty'}
    assert len(variants)==12,name;put(p,{'variants':variants})
chapter=ROOT/'pack/overrides/config/ftbquests/quests/chapters/5e40000000000000.snbt';book=json.loads(chapter.read_text(encoding='utf-8-sig'))
for quest in book['quests']:
    if quest['id']=='5E4000000000000B':quest['description']=[
        'Rebuild the Steel Tree Grower as a 5 x 5 x 1 base: 4 supports, 9 casings, 8 steel soil beds, a vanilla dirt block in the centre, steel controller, right-centre hatch and left-centre Steel Water Tank. Follow EMI.',
        'Supply an oak sapling and water. The attached tank stores 16000 mB; a 20-second cycle uses 500 mB, charged when growth begins. Fill by bucket or pipe. Water appears in the model, UI and Jade.',
        'Keep 5 x 5 x 7 space above the base clear and provide natural daylight above the canopy. A real oak tree grows from vanilla logs/leaves halfway through the cycle.',
        'The farm records its own tree blocks across reloads and removes only those. Manually harvesting or replacing them cancels the automatic harvest, preventing duplicate output. Collect the cycle output through the hatch.']
    if quest['id']=='5E4000000000000C':quest['description']=[
        'Build a 5 x 5 x 3 Iron Greenhouse around a full 3 x 3 soil bed: 11 supports, 21 casings, 20 glass panels, 9 soil beds, controller, hatch, Iron Water Tank, Basic Power Converter and grow light. Leave the 3 x 3 middle chamber empty. EMI shows every layer.',
        'Steel framing supports the connected glass borders and four thin white LED strips. Fit the tank left-centre and converter rear-right on the bottom layer. The hatch goes immediately in front of the converter on the right side. Face converter FE rearward so EU touches the hatch; keep FE to EU mode. The fitted enclosure appears when formed.',
        'Supply 8 EU/t to the hatch, or 80 FE/t to the converter with the default ratio. The greenhouse buffer stores 1600 EU; the converter stores no power.',
        'Nine native wheat/carrot/potato/beetroot models fill the bed, with real age stages. Each 10-second harvest uses 250 mB water. The attached 16000 mB tank accepts buckets/pipes and renders its real water level. Grow lights support day/night and indoor operation.']
put(chapter,book)
print('Updated two tank blocks/recipes, converter assembly states, EMI language and existing quest text.')
