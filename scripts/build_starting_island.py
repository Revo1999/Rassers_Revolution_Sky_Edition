"""Build the Skyblock Builder structure and reproducible starting-world config."""
from pathlib import Path
import json
from nbtlib import File, Compound, List, Int, String, Byte

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / 'pack/overrides/config'
def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')

def main():
    palette, ids, blocks = [], {}, {}
    def state(name, props=None):
        key = (name, tuple(sorted((props or {}).items())))
        if key not in ids:
            entry = Compound({'Name': String(name if ':' in name else 'minecraft:' + name)})
            if props: entry['Properties'] = Compound({k:String(v) for k,v in props.items()})
            ids[key] = len(palette); palette.append(entry)
        return ids[key]
    def put(x,y,z,name,props=None,nbt=None):
        entry = Compound({'pos': List[Int]([x,y,z]), 'state': Int(state(name,props))})
        if nbt is not None: entry['nbt'] = nbt
        blocks[x,y,z] = entry
    # A compact, tapered natural island. Structure origin is y=64; grass y=68.
    for y,radius in enumerate([1.35,2.2,3.3,4.25,4.6]):
        for x in range(15):
            for z in range(15):
                dx,dz=x-7,z-7
                if dx*dx+dz*dz <= radius*radius + ((x*13+z*7)%3-1)*0.4:
                    put(x,y,z,'grass_block' if y==4 else ('dirt' if y>=3 else 'stone'),
                        {'snowy':'false'} if y==4 else None)
    put(6,4,6,'dirt')
    for y in range(5,11): put(6,y,6,'oak_log',{'axis':'y'})
    for y,radius in [(8,2),(9,3),(10,3),(11,2),(12,1)]:
        for x in range(6-radius,7+radius):
            for z in range(6-radius,7+radius):
                if abs(x-6)+abs(z-6)<=radius+1 and (x,y,z) not in blocks:
                    put(x,y,z,'oak_leaves',{'distance':'1','persistent':'false','waterlogged':'false'})
    put(9,5,6,'craftingstation:crafting_station')
    put(10,5,7,'torch')
    put(4,5,9,'short_grass')
    put(5,5,10,'dandelion')
    path=CONFIG/'skyblockbuilder/templates/islands/revo_start.nbt'
    path.parent.mkdir(parents=True,exist_ok=True)
    File(Compound({'DataVersion':Int(3955),'size':List[Int]([15,15,15]),
                   'palette':List[Compound](palette),'blocks':List[Compound](list(blocks.values())),
                   'entities':List[Compound]([])}),gzipped=True).save(path)
    assert (9,4,10) in blocks and (9,5,10) not in blocks and (9,6,10) not in blocks
    spawns={'north':[[9,5,10]],'south':[],'west':[],'east':[]}
    write(CONFIG/'skyblockbuilder/templates.json5',{
        'defaultOffset':0,'mainSpawnIsland':None,
        'templateList':[{'name':'Revo Starting Island','desc':'One oak. A crafting station. From Nothing, Build Everything.',
                         'file':'revo_start.nbt','spawns':spawns,'offset':[0,0,0],'surroundingBlocks':{},'spreads':[]}],
        'spawnPointReferences':{'default':spawns},'surroundingBlockReferences':{},'spreadReferences':{}})
    write(CONFIG/'skyblockbuilder/world.json5',{
        'biomes':{'minecraft:overworld':{'allow_list':True,'elements':['minecraft:plains']},
                  'minecraft:the_nether':{'allow_list':False,'elements':[]}},
        'surface':False,'surfaceSettings':{'minecraft:overworld':[],'minecraft:the_nether':[],'minecraft:the_end':[]},
        'islandDistance':1024,'seaHeight':63,'preventScheduledTicks':True,'leaveToOverworld':False})
    write(CONFIG/'skyblockbuilder/structures.json5',{
        'structuresToGenerate':{'allow_list':True,'elements':[]},
        'featuresToGenerate':{'allow_list':True,'elements':[]}})
    write(CONFIG/'skyblockbuilder/spawn.json5',{'spawnProtectionRadius':0,'spawnDimension':'minecraft:overworld',
          'skipCenterIslandCreation':False,'radiusToFindValidSpawn':8,
          'Height':{'heightCalculationType':'set','range':{'bottom':64,'top':319},'offset':0}})
    default=CONFIG/'defaultworldtype/client-config.toml'
    default.parent.mkdir(parents=True,exist_ok=True)
    default.write_text('[world-preset]\nworld-preset = "skyblockbuilder:skyblock"\ndisable-button = false\nallowed-world-types = []\n',encoding='utf-8')
    write(CONFIG/'skyblockbuilder/inventory.json5',{'clearInitialInventory':True,'dropItems':True,'initialInventoryType':'spawn'})
    write(CONFIG/'skyblockbuilder/starter_inventory.json5',{'items':[{'Slot':'mainhand','Item':{'id':'revoindustry:revolution_book','count':1}}]})
    print(f'Built {len(blocks)} blocks; spawn has a solid floor and two clear headroom blocks.')

if __name__=='__main__': main()
