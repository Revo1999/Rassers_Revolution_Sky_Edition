"""Salt-geode textures, recipes and quests; editable geometry is authored via Blockbench MCP."""
from pathlib import Path
import json,zipfile,io
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'revoindustry/src/main/resources';ART=ROOT/'art/blockbench/salt-geode'
def write(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2)+'\n')
def main():
 ART.mkdir(parents=True,exist_ok=True);atlas=Image.new('RGBA',(128,128),(0,0,0,0))
 with zipfile.ZipFile(ROOT/'.local/integration-mods/Modern-Industrialization-2.5.11.jar') as z:
  def sprite(n):return Image.open(io.BytesIO(z.read('assets/modern_industrialization/textures/block/'+n+'.png'))).convert('RGBA').crop((0,0,16,16))
  steel=sprite('steel_machine_casing')
  for tile in range(5):atlas.paste(steel,(tile*16,0))
  for tile,overlay in [(1,'output_fluid'),(2,'output_fluid'),(3,'output_energy')]:
   face=steel.copy();face.alpha_composite(sprite('overlays/'+overlay))
   if tile==2:face.alpha_composite(sprite('overlays/fluid_auto'))
   if tile==3:face.alpha_composite(sprite('overlays/item_auto'))
   atlas.paste(face,(tile*16,0))
 d=ImageDraw.Draw(atlas);d.rectangle((67,3,76,11),fill='#102831');d.line((68,5,74,5),fill='#8ad6da');d.line((69,8,74,8),fill='#77a8b5')
 d.rectangle((80,0,95,15),fill='#4c4b58')
 for x,y in [(1,2),(6,1),(11,3),(4,7),(9,8),(2,12),(12,12)]:
  d.rectangle((80+x,y,81+x,y+1),fill='#66606a');d.line((80+x,y+2,82+x,y+2),fill='#383c49')
 d.rectangle((96,0,111,15),fill='#e3dce0')
 for x,y in [(1,1),(6,2),(12,1),(3,6),(9,7),(1,12),(12,12)]:
  d.line((96+x,y,97+x,y),fill='#fff5ee');d.point((96+x,y+1),fill='#c2b7cc')
 d.rectangle((112,0,127,15),fill='#4d6874');d.rectangle((116,2,123,13),fill='#a9babd');d.line((118,3,118,12),fill='#e6e7db');d.line((122,3,122,12),fill='#677f90')
 tex=RES/'assets/revoindustry/textures/block/saltern_atlas.png';tex.parent.mkdir(parents=True,exist_ok=True);atlas.save(tex);atlas.save(ART/'saltern_atlas.png')
 names={'salt_geode_chamber':'Salt Geode Chamber','geode_water_input':'Geode Water Input Hatch','geode_brine_output':'Geode Salt Water Output Hatch','geode_energy_hatch':'Geode Energy Hatch','saline_geode':'Saline Geode Core'}
 langp=RES/'assets/revoindustry/lang/en_us.json';lang=json.loads(langp.read_text())
 for n,label in names.items():
  lang['block.revoindustry.'+n]=label
  write(RES/f'assets/revoindustry/blockstates/{n}.json',{'variants':{f'facing={face}':{'model':f'revoindustry:block/{n}','y':angle} for face,angle in [('north',0),('east',90),('south',180),('west',270)]}})
  write(RES/f'assets/revoindustry/models/item/{n}.json',{'parent':f'revoindustry:block/{n}'})
  write(RES/f'data/revoindustry/loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'revoindustry:{n}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 lang.update({'item.revoindustry.salt_water_bucket':'Salt Water Bucket','fluid_type.revoindustry.salt_water':'Salt Water','block.revoindustry.salt_water':'Salt Water','emi.category.revoindustry.salt_geode_assembly':'Salt Geode Construction','emi.category.revoindustry.salt_geode_growth':'Submerged Salt Crystal Growth','config.jade.plugin_revoindustry.salt_geode':'Salt geode chamber'})
 write(langp,lang);write(RES/'assets/revoindustry/models/item/salt_water_bucket.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/water_bucket'}})
 def recipe(n,pattern,key,count=1):write(RES/f'data/revoindustry/recipe/{n}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':'revoindustry:'+n,'count':count}})
 recipe('saline_geode',['CQC','QRQ','CQC'],{'C':'minecraft:cobblestone','Q':'minecraft:quartz','R':'minecraft:redstone'})
 recipe('salt_geode_chamber',['SAS','MCM','SPS'],{'S':'modern_industrialization:steel_plate','A':'modern_industrialization:analog_circuit','M':'modern_industrialization:motor','C':'revoindustry:saline_geode','P':'minecraft:piston'})
 recipe('geode_water_input',['SPS','BCB','SPS'],{'S':'modern_industrialization:steel_plate','P':'modern_industrialization:fluid_pipe','B':'minecraft:bucket','C':'revoindustry:wither_chamber_casing'})
 recipe('geode_brine_output',['SBS','PCP','SBS'],{'S':'modern_industrialization:steel_plate','P':'modern_industrialization:fluid_pipe','B':'minecraft:bucket','C':'revoindustry:wither_chamber_casing'})
 recipe('geode_energy_hatch',['SWS','WCW','SWS'],{'S':'modern_industrialization:steel_plate','W':'modern_industrialization:tin_cable','C':'revoindustry:wither_chamber_casing'})
 write(RES/'data/revoindustry/recipe/salt_water_centrifuging.json',{'type':'modern_industrialization:centrifuge','eu':8,'duration':200,'fluid_inputs':[{'fluid':'revoindustry:salt_water','amount':1000}],'item_outputs':[{'item':'modern_industrialization:salt_dust','amount':2}]})
 tagp=RES/'data/minecraft/tags/block/mineable/pickaxe.json';tag=json.loads(tagp.read_text());tag['values']=sorted(set(tag['values'])|{'revoindustry:'+n for n in names});write(tagp,tag)
 chapterp=ROOT/'pack/overrides/config/ftbquests/quests/chapters/5e50000000000000.snbt';chapter=json.loads(chapterp.read_text())
 qs=[
  (19,'A Geode Under Water','revoindustry:salt_geode_chamber',['Build the 3 x 3 x 3 Salt Geode Chamber using the EMI guide. Four smoked-glass panels reveal a saline geode, growing submerged crystal buds and electrical harvesting.','Supply water on the left and 16 EU/t at the rear. Every 20 powered seconds one bucket of water becomes one bucket of salt water. The right hatch outputs it; full output or missing power pauses growth.'],['5E5000000000000A']),
  (20,'Salt from the Centrifuge','modern_industrialization:salt_dust',['Pipe 1000 mB salt water into an existing MI Centrifuge. Its native 8 EU/t recipe takes 200 ticks and produces two MI Salt Dust.','Build the Centrifuge through its native electronic-circuit, large-motor and pump recipes. Electric geology supplies silver and bauxite for the required industrial components.','Salt feeds the native electric Electrolyzer for sodium and chlorine. Neither the first turbine nor this chamber needs salt to be built, so no manual salt grind is required.'],['5E50000000000013','5E5000000000000B'])]
 for index,title,item,desc,deps in qs:
  q={'id':f'5E500000000000{index:02X}','title':title,'icon':{'id':item,'count':1},'x':12.0+(index-19)*3,'y':9.0,'shape':'circle','dependencies':deps,'description':desc,'tasks':[{'id':f'5E500000001000{index:02X}','type':'item','item':{'id':item,'count':1},'count':1,'consume_items':False}],'rewards':[]}
  chapter['quests']=[v for v in chapter['quests'] if v['id']!=q['id']]+[q]
 write(chapterp,chapter)
 (ART/'CREDITS.txt').write_text('Native steel and fluid/energy port sprites: Modern Industrialization 2.5.11, AztechMC / contributors.\nGeode, salt crystal clusters and electrical detail artwork: Revo project.\nLive water uses the vanilla animated water atlas. Editable geometry authored through Blockbench MCP.\n')
 print('Painted salt-geode atlas; wrote five parts, brine centrifuging and two progression quests.')
if __name__=='__main__':main()
