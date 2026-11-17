"""Install existing native Blockbench exports and executable individual-block blueprints.

This collector does not author model geometry or create crafting recipes.
"""
from pathlib import Path
import json, shutil

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revoindustry/src/main/resources'
ASSETS=RES/'assets/revoindustry'
PORTS={'input':'highly_advanced_item_input_hatch','output':'highly_advanced_item_output_hatch',
       'fluid_in':'highly_advanced_fluid_input_hatch','fluid_out':'highly_advanced_fluid_output_hatch',
       'energy':'superconductor_energy_input_hatch'}
MACHINES={'pressure_casting_plant':'control_casting','electroplating_line':'control_plating','particle_collider':'control_collider','nexus_portal':'controller'}

def write(p,d):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2)+'\n')

def main():
 blocks={};structures={}
 for kind,control in MACHINES.items():
  portal=kind=='nexus_portal';folder=ROOT/'art/blockbench'/('nexus-portal' if portal else 'nexus-machines')
  bp=json.loads((folder/('portal-blueprint.json' if portal else f'blueprints/{kind}.json')).read_text())
  prefix='nexus_' if portal else 'nexus_machine_'
  controller='nexus_controller' if portal else kind
  cells=[]
  for cell in bp['cells']:
   part=cell['part'];block='modern_industrialization:'+PORTS[part] if part in PORTS else 'revoindustry:'+controller if part==control else 'revoindustry:'+prefix+part
   cells.append({**cell,'block':block})
   if part not in PORTS:blocks[block]={'part':part,'portal':portal,'controller':part==control,'model':('nexus_' if portal else 'machine_')+part}
  origin=next(c['position'] for c in cells if c['part']==control)
  air=bp.get('requiredAir',[])
  if portal:
   occupied={tuple(c['position']) for c in cells}
   air=[[x,y,4] for x in range(-5,6) for y in range(5,16) if x*x+(y-10)**2<=25 and (x,y,4) not in occupied]
  structures[kind]={'controller':controller,'origin':origin,'cells':cells,'requiredAir':air,'dimensions':bp['dimensions']}
  for p in (folder/'java-models').glob('*.json'):
   target=ASSETS/'models/block'/p.name;target.parent.mkdir(parents=True,exist_ok=True)
   data=json.loads(p.read_text())
   data.setdefault('parent','minecraft:block/block')
   data['textures']['particle']='#0'
   if p.stem=='machine_accelerator_glass':data['render_type']='minecraft:translucent'
   write(target,data)
  for p in (folder/'textures').glob('*.png*'):
   if 'preview' in p.name:continue
   target=ASSETS/'textures/block'/p.name;target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,target)
 blocks['revoindustry:nexus_builder']={'part':'builder','portal':True,'controller':True,'model':'nexus_controller'}
 structures['nexus_builder']={'controller':'nexus_builder','origin':[0,0,0],'dimensions':{'width':3,'height':1,'depth':2},'requiredAir':[],
  'cells':[{'position':p,'part':part,'facing':face,'block':block} for p,part,face,block in [
   ([0,0,0],'builder','north','revoindustry:nexus_builder'),([-1,0,0],'input','west','modern_industrialization:advanced_item_input_hatch'),
   ([1,0,0],'output','east','modern_industrialization:advanced_item_output_hatch'),([0,0,1],'energy','south','modern_industrialization:mv_energy_input_hatch')]]}
 langpath=ASSETS/'lang/en_us.json';lang=json.loads(langpath.read_text())
 for block,entry in blocks.items():
  name=block.split(':')[1]
  if entry['controller']:
   # Native MI controller rendering also supplies facing, overlays and shape previews.
   write(ASSETS/f'models/block/{name}.json',{'loader':'modern_industrialization:machine','casing':'revoindustry:nexus_machine',
    'default_overlays':{'front':'revoindustry:block/nexus_controller_front','front_active':'revoindustry:block/nexus_controller_front'}})
   state={'variants':{'':{'model':f'revoindustry:block/{name}'}}}
  elif name=='nexus_rail':state=json.loads((ROOT/'art/blockbench/nexus-portal/blockstates/nexus_rail.json').read_text())
  else:
   state={'variants':{f'facing={face}':{'model':f'revoindustry:block/{entry["model"]}','y':angle} for face,angle in [('north',0),('east',90),('south',180),('west',270)]}}
  write(ASSETS/f'blockstates/{name}.json',state)
  write(ASSETS/f'models/item/{name}.json',{'parent':f'revoindustry:block/{name if entry["controller"] else entry["model"]}'})
  write(RES/f'data/revoindustry/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':block}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
  lang['block.revoindustry.'+name]=name.replace('_',' ').title()
  if entry['controller']:lang['block.revoindustry.'+name]={'nexus_controller':'Nexus Portal Controller'}.get(name,name.replace('_',' ').title())
 # Fence connectivity uses vanilla neighbour updates and both ordinary and tagged fence neighbours.
 write(RES/'data/minecraft/tags/block/fences.json',{'replace':False,'values':['revoindustry:nexus_rail']})
 write(RES/'data/revoindustry/tags/block/nexus_parts.json',{'replace':False,'values':list(blocks)})
 path=RES/'data/minecraft/tags/block/mineable/pickaxe.json';tag=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
 tag['values']=sorted(set(tag['values'])|set(blocks));write(path,tag)
 lang['item.revoindustry.nexus_construction_program']='Nexus Construction Program'
 lang['emi.category.revoindustry.nexus_assembly']='Nexus Multiblock Assembly'
 write(ASSETS/'models/item/nexus_construction_program.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/paper'}})
 write(ASSETS/'models/machine_casing/nexus_machine.json',{'loader':'modern_industrialization:use_block_model','block':'revoindustry:nexus_machine_casing'})
 for kind in structures:lang['block.revoindustry.'+kind]=kind.replace('_',' ').title()
 write(ASSETS/'lang/en_us.json',lang)
 write(RES/'nexus/structures.json',{'blocks':blocks,'structures':structures})
 surface=json.loads((ROOT/'art/blockbench/nexus-portal/nexus_portal_surface.json').read_text());surface['textures']={'0':'revoindustry:block/nexus_vortex'}
 write(ASSETS/'models/block/nexus_portal_surface.json',surface)
 # Match the approved gentle 16-second preview: 64 frames, five ticks each.
 write(ASSETS/'textures/block/nexus_vortex.png.mcmeta',{'animation':{'frametime':5,'interpolate':True,'width':256,'height':256}})
 # Crop an existing authored atlas tile for the native MI front overlay; no geometry is generated.
 from PIL import Image
 atlas=Image.open(ROOT/'art/blockbench/nexus-machines/textures/nexus_machine_atlas.png')
 model=json.loads((ROOT/'art/blockbench/nexus-machines/java-models/machine_control_casting.json').read_text())
 face=model['elements'][0]['faces']['north']['uv'];scale=atlas.width/16
 atlas.crop(tuple(round(v*scale) for v in face)).resize((16,16),Image.Resampling.NEAREST).save(ASSETS/'textures/block/nexus_controller_front.png')
 print(f'Installed {len(blocks)} registered block resources and {len(structures)} executable blueprints; no block recipes added.')

if __name__=='__main__':main()
