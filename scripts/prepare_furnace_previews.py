import json,zipfile,base64,io
from pathlib import Path
from PIL import Image
root=Path(__file__).resolve().parents[1];art=root/'art/blockbench/furnaces';manifest=json.loads((art/'manifest.json').read_text());mi=zipfile.ZipFile(root/'.local/integration-mods/Modern-Industrialization-2.5.11.jar');iron=zipfile.ZipFile(root/'.local/integration-mods/ironfurnaces-neoforge-1.21.1-4.3.2.jar')
def png(data):
 im=Image.open(io.BytesIO(data)).convert('RGBA');return im.crop((0,0,im.width,im.width)).resize((16,16),Image.Resampling.NEAREST)
def enc(im):
 b=io.BytesIO();im.save(b,format='PNG');return 'data:image/png;base64,'+base64.b64encode(b.getvalue()).decode()
def compose(base,overlay):
 im=base.copy();im.alpha_composite(overlay);return im
projects=[]
for tier in manifest['tiers']:
 name=tier['tier'];ns,path=tier['casing'].split(':')
 if ns=='modern_industrialization':
  model=json.loads(mi.read(f'assets/{ns}/models/block/{path}.json'));texture=model['textures']['all'].split(':')[1];casing=png(mi.read(f'assets/{ns}/textures/{texture}.png'))
 else:casing=png((root/f'revoindustry/src/main/resources/assets/revoindustry/textures/block/furnaces/{name}_casing.png').read_bytes())
 f=tier['furnace'].split(':')[1];controller=compose(casing,png((root/'revoindustry/src/main/resources/assets/revoindustry/textures/block/furnaces/controller_front.png').read_bytes()))
 item=png((root/f'revoindustry/src/main/resources/assets/revoindustry/textures/block/furnaces/furnace_item_input_hatch_{tier["hatchGrade"]}.png').read_bytes())
 output=png((root/f'revoindustry/src/main/resources/assets/revoindustry/textures/block/furnaces/furnace_item_output_hatch_{tier["hatchGrade"]}.png').read_bytes())
 vent=png((root/'revoindustry/src/main/resources/assets/revoindustry/textures/block/furnaces/furnace_vent.png').read_bytes())
 fuel=png((root/f'revoindustry/src/main/resources/assets/revoindustry/textures/block/furnaces/solid_fuel_hatch_{tier["hatchGrade"]}.png').read_bytes())
 textures={'casing':enc(casing),'controller':enc(controller),'input':enc(compose(casing,item)),'output':enc(compose(casing,output)),'vent':enc(compose(casing,vent)),'fuel':enc(compose(casing,fuel))}
 cubes=[]
 for x in range(3):
  for y in range(3):
   for z in range(3):
    if (x,y,z)==(1,1,1):continue
    kind='controller' if (x,y,z)==(1,0,0) else 'input' if (x,y,z)==(0,0,0) else 'output' if (x,y,z)==(2,0,0) else 'fuel' if (x,y,z)==(1,0,2) else 'vent' if (x,y,z)==(1,2,1) else 'casing'
    cubes.append({'name':f'{kind}_{x}_{y}_{z}','from':[x*16,y*16,z*16],'to':[(x+1)*16,(y+1)*16,(z+1)*16],'kind':kind})
 projects.append({'name':name+'_multiblock_furnace_assembled','textures':textures,'cubes':cubes})
(art/'preview-input.json').write_text(json.dumps(projects),encoding='utf-8')
print('Prepared native assembly geometry and dependency-based preview textures for',len(projects),'tiers.')
