"""Drill mineral/sludge assets, native processing recipes and stable quest updates."""
from pathlib import Path
import json, zipfile
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'revoextraction/src/main/resources'

def put(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')

def main():
    for name, colors in {
        'mineral_sludge': ['#302d29', '#51483a', '#71614a', '#97816a', '#c4b59a'],
        'gem_sludge': ['#282e32', '#434854', '#626c78', '#78999c', '#c0d9cf'],
        'crystalline_sludge': ['#242b39', '#424d69', '#626e95', '#8cadd1', '#d6eaf2'],
    }.items():
        img = Image.new('RGBA', (16, 16)); draw = ImageDraw.Draw(img)
        draw.polygon([(2,11),(3,7),(5,6),(5,4),(8,3),(11,5),(12,8),(14,10),(13,13),(10,14),(4,14),(1,12)], fill=colors[0])
        for box, color in [((3,9,11,12),1),((5,6,10,10),2),((6,4,8,6),3),((3,10,5,11),3),((9,8,11,9),3),((6,7,7,8),4),((10,11,11,11),4)]:
            draw.rectangle(box, fill=colors[color])
        texture = RES / f'assets/revoextraction/textures/item/{name}.png'
        texture.parent.mkdir(parents=True, exist_ok=True); img.save(texture)
        put(RES / f'assets/revoextraction/models/item/{name}.json', {'parent':'minecraft:item/generated','textures':{'layer0':f'revoextraction:item/{name}'}})
    langpath = RES / 'assets/revoextraction/lang/en_us.json'
    lang = json.loads(langpath.read_text(encoding='utf-8-sig'))
    for name in ['mineral_sludge','gem_sludge','crystalline_sludge']:
        lang[f'item.revoextraction.{name}'] = name.replace('_',' ').title()
    put(langpath, lang)
    for name, results in {
        'mineral_sludge': [{'id':'minecraft:quartz','count':1}],
        'gem_sludge': [{'id':'minecraft:diamond','count':1}, {'id':'minecraft:lapis_lazuli','count':2}, {'id':'minecraft:emerald','count':1,'chance':0.25}],
    }.items():
        put(RES / f'data/revoextraction/recipe/splashing/{name}.json', {'type':'create:splashing','ingredients':[{'item':f'revoextraction:{name}'}],'results':results})
    put(RES / 'data/revoextraction/recipe/crystalline_sludge_centrifuging.json', {
        'type':'modern_industrialization:centrifuge','eu':32,'duration':400,
        'neoforge:conditions':[{'type':'neoforge:mod_loaded','modid':'modern_industrialization'},{'type':'neoforge:mod_loaded','modid':'ae2'}],
        'item_inputs':[{'item':'revoextraction:crystalline_sludge','amount':1}],
        'item_outputs':[{'item':'ae2:certus_quartz_crystal','amount':2},{'item':'minecraft:quartz','amount':2}],
    })
    # Replace the native controller recipes at their exact resource paths.
    # Controllers remain registered for existing saves/admin use, but cannot be crafted.
    with zipfile.ZipFile(ROOT / '.local/integration-mods/Modern-Industrialization-2.5.11.jar') as jar:
        for path in jar.namelist():
            if '/recipe/' not in path or not path.endswith('.json'): continue
            data = json.loads(jar.read(path)); result = data.get('result', {})
            produced = [o.get('item') for o in data.get('item_outputs', [])] if isinstance(data.get('item_outputs', []), list) else []
            if (isinstance(result, dict) and result.get('id') in ['modern_industrialization:steam_quarry','modern_industrialization:electric_quarry']) or any(i in ['modern_industrialization:steam_quarry','modern_industrialization:electric_quarry'] for i in produced):
                data['neoforge:conditions'] = [{'type':'neoforge:false'}]
                put(RES / path, data)
    chapterpath = ROOT / 'pack/overrides/config/ftbquests/quests/chapters/5e50000000000000.snbt'
    chapter = json.loads(chapterpath.read_text()); quests = {q['id']:q for q in chapter['quests']}
    quests['5E50000000000003']['description'] = ['The Iron Drill produces Mineral Sludge alongside ores. Wash it with an Encased Fan blowing through water to recover one Quartz per sludge. No diamonds or advanced machinery are required.', 'Minerals come from drills; recover gems and crystals by processing their sludge.']
    new = [
        (0x15,'Battery Minerals Before Electricity','modern_industrialization:raw_antimony', ['5E40000000000009'], -6,3,
         ['The mechanical Steel Drill now supplies raw lead and raw antimony before electricity. Process them with native steam maceration and metallurgy. The drill itself needs neither mineral to build.', 'Mix equal Lead Dust and Antimony Dust for Battery Alloy. Do not wait for the electric drill: its hull already needs batteries.'],
         [('modern_industrialization:raw_lead',2),('modern_industrialization:raw_antimony',1)]),
        (0x16,'The First Battery Hull','modern_industrialization:redstone_battery',['5E50000000000015','5E50000000000008'],-3,3,
         ['Use native battery-alloy plates and curved plates, tin cable and redstone to craft two Redstone Batteries. A Basic Machine Hull consumes two batteries.', 'Keep the Steel Drill producing antimony. The Electric Geological Drill later offers a selectable Antimony program.'],
         [('modern_industrialization:redstone_battery',2)]),
        (0x17,'Wash the Gem Sludge','revoextraction:gem_sludge',['5E40000000000009'],-3,0,
         ['The Steel Drill produces Gem Sludge instead of loose diamonds and lapis. Wash each sludge with a water-backed Create Encased Fan: one Diamond and two Lapis are guaranteed; an Emerald has a 25% chance.', 'Automate extraction and washing with the existing item hatch, belts and funnels. Full drill outputs retain the saved next result.'],
         [('revoextraction:gem_sludge',1),('minecraft:diamond',1)]),
        (0x18,'Crystals from Deep Sludge','ae2:certus_quartz_crystal',['5E5000000000000F','5E50000000000014'],12,12,
         ['Select Crystalline Sludge on the Electric Geological Drill. Retain a Deep Survey Core in its upper slot; supply 128 EU/t for 1200 powered ticks per sludge.', 'A native MI Centrifuge processes one Crystalline Sludge at 32 EU/t for 400 ticks into two Certus Quartz Crystals and two Quartz. Certus is recovered from sludge, never dropped directly by the drill.', 'This establishes starter Certus only. Inscriber Presses, starter Sky Stone and the full AE2 unlock still need their own progression.'],
         [('revoextraction:crystalline_sludge',1),('ae2:certus_quartz_crystal',2)]),
    ]
    for n,title,icon,deps,x,y,description,items in new:
        ident = f'5E500000000000{n:02X}'
        q = {'id':ident,'title':title,'icon':{'id':icon,'count':1},'x':float(x),'y':float(y),'shape':'circle','dependencies':deps,'description':description,
             'tasks':[{'id':f'{0x5E50000000100000+n*16+i:016X}','type':'item','item':{'id':item,'count':1},'count':count,'consume_items':False} for i,(item,count) in enumerate(items)],'rewards':[]}
        if ident in quests: chapter['quests'][chapter['quests'].index(quests[ident])] = q
        else: chapter['quests'].append(q)
    deps = quests['5E5000000000000A']['dependencies']
    if '5E50000000000016' not in deps: deps.append('5E50000000000016')
    quests['5E5000000000000B']['description'][0] = 'The LV Electric Drill uses 16 EU/t for 200 powered ticks per resource. Mixed cycles silver, nickel, lead, bauxite and antimony; select individual deposits in its menu. Mechanical steel extraction supplies the first battery minerals.'
    put(chapterpath, chapter)
    steelpath = chapterpath.with_name('5e40000000000000.snbt')
    steel = json.loads(steelpath.read_text())
    for q in steel['quests']:
        if q['id'] == '5E4000000000000A':
            q['description'] = ['Steel extraction now adds raw lead and antimony for the first LV batteries, Mineral Sludge for quartz, and Gem Sludge for diamonds/lapis/emeralds.', 'Process sludge with water-backed Create Encased Fans. The drill no longer drops loose diamonds or lapis. Keep existing steam ore processing running.']
    put(steelpath, steel)

if __name__ == '__main__': main()
