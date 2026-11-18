"""Generate the checked Nexus recipe graph and its material registration catalog.

No model geometry is authored here. Item models are collected separately from
Blockbench MCP's native generated-item template.
"""
from pathlib import Path
from collections import Counter
import json
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revoindustry/src/main/resources'
MI='modern_industrialization:'; RE='revoindustry:'
ITEMS=set(); RECIPES={}
def item(name):
 if ':' not in name:ITEMS.add(name);return RE+name
 return name
def stack(name,n=1,**extra):return {'item':item(name),'amount':n,**extra}
def fluid(name,n):return {'fluid':name if ':' in name else RE+name,'amount':n}
def machine(name,kind,ins,outs,fi=(),fo=(),eu=2048,ticks=200):
 assert name not in RECIPES,name
 d={'type':kind if ':' in kind else MI+kind,'eu':eu,'duration':ticks}
 if ins:d['item_inputs']=[stack(*v) if isinstance(v,tuple) else v for v in ins]
 if outs:d['item_outputs']=[stack(*v) if isinstance(v,tuple) else v for v in outs]
 if fi:d['fluid_inputs']=[fluid(*v) for v in fi]
 if fo:d['fluid_outputs']=[fluid(*v) for v in fo]
 for v in d.get('item_inputs',[])+d.get('item_outputs',[]):assert 0<v['amount']<=64,(name,v)
 assert len(d.get('item_inputs',[]))<=8,name
 RECIPES[name]=d
# Native machines perform all preparatory processing.
machine('alloy_blend','mixer',[(MI+'iridium_dust',16),(MI+'tungsten_dust',32),(MI+'superconductor_dust',16),('minecraft:netherite_ingot',2)],[('nexus_alloy_blend',8)],eu=8192,ticks=800)
machine('alloy_heating','blast_furnace',[('nexus_alloy_blend',8)],[('hot_nexus_alloy_ingot',8)],fi=[(MI+'uu_matter',1000)],eu=128,ticks=1600)
machine('alloy_cooling','vacuum_freezer',[('hot_nexus_alloy_ingot',8)],[('nexus_alloy_ingot',8)],eu=256,ticks=800)
cast_cost={'panel':4,'light_panel':2,'heavy_panel':8,'casing_blank':7,'carrier':1,'frame':4,'ring_blank':8,'core_housing':24}
mould_selectors=dict(zip(cast_cost,['minecraft:iron_ingot','minecraft:iron_nugget','minecraft:iron_block','minecraft:obsidian','minecraft:quartz','minecraft:iron_bars','minecraft:ender_pearl','minecraft:netherite_ingot']))
for shape,cost in cast_cost.items():
 mould='nexus_mould_'+shape;rough='rough_nexus_'+shape;finished='nexus_'+shape
 machine('mould_'+shape,'assembler',[(MI+'titanium_plate',8),(MI+'steel_plate',8),(MI+'titanium_gear',2),(mould_selectors[shape],1)],[ (mould,1)],eu=2048,ticks=400)
 machine('cast_'+shape,RE+'pressure_casting_plant',[('nexus_alloy_ingot',cost),(mould,1)],[(rough,1),(mould,1)],fi=[(MI+'nitrogen',1000),(MI+'cryofluid',1000)],fo=[('warm_nexus_coolant',1000)],eu=32768,ticks=400)
 machine('finish_'+shape,'compressor',[(rough,1)],[(finished,1)],eu=8192,ticks=200)
# High-tier native shells bootstrap every new machine without its own products.
from nexus_builder_layout import integrate_builder
manifest=integrate_builder(json.loads((RES/'nexus/structures.json').read_text()))
(RES/'nexus/structures.json').write_text(json.dumps(manifest,indent=2)+'\n')
base_inputs={
 'casing':[(MI+'steel_machine_casing',1),(MI+'titanium_plate',4),(MI+'iridium_plate',1)],
 'reinforced_column':[(MI+'titanium_rod',4),(MI+'nuclear_alloy_plate',2)],
 'pressure_plate':[(MI+'titanium_plate',2),(MI+'stainless_steel_plate',2)],
 'accelerator_glass':[('minecraft:glass',4),(MI+'iridium_plate',1),(MI+'superconductor_wire',1)],
 'magnet_coil':[(MI+'superconductor_coil',1),(MI+'nuclear_alloy_plate',2),(MI+'copper_wire',2)],
 'ceramic_panel':[(MI+'titanium_plate',2),('minecraft:quartz',4)],
 'deck_grate':[(MI+'steel_rod',4),(MI+'titanium_plate',1)],
 'bath_wall':[(MI+'stainless_steel_plate',4),('minecraft:glass',2)],
 'hydraulic_cylinder':[(MI+'piston',2),(MI+'titanium_plate',4),(MI+'pump',1)],
 'pressure_tank':[(MI+'large_tank',1),(MI+'titanium_plate',4)],
 'injector_housing':[(MI+'robot_arm',1),(MI+'iridium_plate',4),(MI+'superconductor_wire',4)],
 'gantry_rail':[(MI+'titanium_rod',4),(MI+'steel_gear',2)],
 'mould_bed':[(MI+'titanium_plate',4),(MI+'steel_plate',4),(MI+'steel_ring',1)],
 'ribbed_panel':[(MI+'stainless_steel_plate',4),(MI+'steel_rod',2)],
 'service_panel':[(MI+'titanium_plate',2),(MI+'electronic_circuit',1)],
 'hazard':[(MI+'steel_plate',4),('minecraft:yellow_dye',1),('minecraft:black_dye',1)],
 'light_amber':[(MI+'titanium_plate',2),('minecraft:glowstone_dust',4),('minecraft:yellow_dye',1)],
 'light_blue':[(MI+'titanium_plate',2),('minecraft:glowstone_dust',4),('minecraft:blue_dye',1)],
 'vent':[(MI+'steel_curved_plate',4),(MI+'motor',1)]}
for name in manifest['blocks']:
 short=name.split(':')[1]
 if short.startswith('nexus_machine_'):
  machine('block_'+short,'assembler',base_inputs[short.removeprefix('nexus_machine_')],[(name,1)],eu=8192,ticks=200)
machine('controller_casting','assembler',[(MI+'quantum_machine_hull',2),(MI+'quantum_circuit',12),(MI+'large_motor',6),(MI+'pump',6),(MI+'robot_arm',6),(MI+'titanium_gear',12),(MI+'superconductor_wire',32)],[(RE+'pressure_casting_plant',1)],eu=8192,ticks=1200)
machine('controller_plating','assembler',[(MI+'quantum_machine_hull',2),(MI+'quantum_circuit',18),(MI+'large_motor',6),(MI+'pump',12),(MI+'robot_arm',6),('nexus_frame',24)],[(RE+'electroplating_line',1)],eu=8192,ticks=1200)
machine('controller_collider','assembler',[(MI+'quantum_machine_hull',4),(MI+'quantum_circuit',24),(MI+'robot_arm',12),(MI+'superconductor_wire',32),('nexus_frame',16),('nexus_contact',16),(MI+'fusion_chamber',8),(MI+'large_motor',8)],[(RE+'particle_collider',1)],eu=32768,ticks=2400)
# Nine mob streams, compacted independently to stay within native machine limits.
drops=['rotten_flesh','bone','string','leather','slime_ball','blaze_rod','ender_pearl','ghast_tear','magma_cream']
for drop in drops:machine('compact_'+drop,'compressor',[('minecraft:'+drop,8)],[('compacted_'+drop,1)],eu=2048,ticks=100)
for group in range(3):machine('mob_group_'+str(group),'assembler',[('compacted_'+d,1) for d in drops[group*3:group*3+3]],[('nexus_mob_group_'+str(group),1)],eu=2048)
machine('mob_concentrate','assembler',[('nexus_mob_group_'+str(g),1) for g in range(3)],[('nexus_mob_concentrate',1)],eu=2048)
machine('plant_nutrients','mixer',[('minecraft:wheat',8),('minecraft:carrot',8),('minecraft:potato',8)],[('nexus_plant_nutrients',8)],fi=[('minecraft:water',1000)],eu=2048)
machine('carbon_mesh','assembler',[('minecraft:charcoal',8),('revocultivation:cotton',8),(MI+'rubber_sheet',4)],[('nexus_carbon_mesh',8)],eu=2048)
machine('biological_catalyst','assembler',[('revocultivation:compost',8),('revocultivation:silk_cocoon',8),('minecraft:paper',8),(MI+'rubber_sheet',8),('nexus_plant_nutrients',1),('nexus_carbon_mesh',1)],[('nexus_biological_catalyst',1)],eu=8192,ticks=400)
machine('star_dust','macerator',[('minecraft:nether_star',1)],[('nexus_star_dust',1)],eu=8192)
machine('astral_essence','chemical_reactor',[('nexus_star_dust',16),('nexus_mob_concentrate',8),('nexus_biological_catalyst',8)],[('astral_essence',8)],eu=8192,ticks=800)
# Three explicit saved plating operations, automated by routing intermediates back.
machine('cleaning_solution','chemical_reactor',[],[],fi=[(MI+'sodium_hydroxide',1000),(MI+'chlorine',250),('minecraft:water',2750)],fo=[('nexus_cleaning_solution',4000)],eu=8192)
machine('plating_electrolyte','chemical_reactor',[(MI+'copper_dust',8)],[],fi=[(MI+'sulfuric_acid',1000),('minecraft:water',1000)],fo=[('nexus_plating_electrolyte',2000)],eu=8192)
machine('unplated_substrate','assembler',[('nexus_carrier',8),('create:precision_mechanism',8),('minecraft:diamond',16),('ae2:certus_quartz_crystal',16)],[('nexus_unplated_substrate',8)],eu=8192,ticks=400)
machine('plating_clean',RE+'electroplating_line',[('nexus_unplated_substrate',8)],[('nexus_clean_substrate',8)],fi=[('nexus_cleaning_solution',1000)],fo=[('nexus_wastewater',1000)],eu=65536,ticks=200)
machine('plating_deposit',RE+'electroplating_line',[('nexus_clean_substrate',8),(MI+'quantum_circuit',8),('ae2:engineering_processor',8),(MI+'superconductor_wire',8)],[('nexus_plated_substrate',8)],fi=[('nexus_plating_electrolyte',1000)],fo=[('nexus_spent_electrolyte',1000)],eu=65536,ticks=400)
machine('plating_rinse',RE+'electroplating_line',[('nexus_plated_substrate',8)],[('nexus_rinsed_substrate',8)],fi=[('minecraft:water',1000)],fo=[('nexus_wastewater',1000)],eu=65536,ticks=200)
machine('electrolyte_recovery','centrifuge',[],[(MI+'copper_dust',1)],fi=[('nexus_spent_electrolyte',1000)],fo=[('nexus_wastewater',1000)],eu=8192,ticks=400)
machine('wastewater_treatment','centrifuge',[],[],fi=[('nexus_wastewater',1000)],fo=[('minecraft:water',750)],eu=2048,ticks=400)
machine('coolant_recovery','vacuum_freezer',[],[],fi=[('warm_nexus_coolant',1000)],fo=[(MI+'cryofluid',750)],eu=256,ticks=400)
machine('contacts','assembler',[('nexus_rinsed_substrate',1),(MI+'copper_wire',1)],[('nexus_contact',1)],eu=8192)
machine('dense_tungsten_target','compressor',[(MI+'tungsten_plate',4)],[('nexus_tungsten_target',1)],eu=8192,ticks=400)
machine('charged_iridium_target','polarizer',[(MI+'iridium_plate',2),(MI+'neodymium_dust',1)],[('nexus_iridium_target',1)],eu=8192,ticks=400)
machine('containment_cell','assembler',[(MI+'tungsten_plate',8),('minecraft:glass',8)],[('nexus_containment_cell',8)],eu=8192)
machine('exotic_matter',RE+'particle_collider',[('astral_essence',8),('nexus_tungsten_target',8),('nexus_iridium_target',8),('nexus_containment_cell',8)],[('stabilized_exotic_matter',8)],fi=[(MI+'helium_plasma',1000),(MI+'cryofluid',4000)],fo=[('warm_nexus_coolant',4000)],eu=262144,ticks=1200)
machine('exotic_matter_decapsulation','centrifuge',[('stabilized_exotic_matter',8)],[('nexus_exotic_matter',8),('nexus_containment_cell',8)],eu=8192,ticks=400)
machine('phase_lattice','assembler',[('nexus_rinsed_substrate',8),('nexus_exotic_matter',8)],[('phase_lattice',8)],eu=32768,ticks=400)
# Portal finishing costs include intermediate content, never a second gross charge.
shape_for={'casing':'casing_blank','foundation':'panel','foundation_chiseled':'panel','ceramic_panel':'panel','deck_grate':'panel','reinforced_column':'heavy_panel','ribbed_panel':'panel','service_panel':'panel','hazard':'panel','trim':'panel','rail':'light_panel','light_blue':'panel','light_amber':'panel','light_violet':'panel','vent':'panel'}
extras={'foundation':[('minecraft:obsidian',4)],'foundation_chiseled':[('minecraft:obsidian',4),('minecraft:diamond',1)],'ceramic_panel':[('minecraft:quartz',4)],'service_panel':[(MI+'quantum_circuit',1)],'deck_grate':[(MI+'titanium_rod',2)],'ribbed_panel':[(MI+'steel_rod',2)],'trim':[(MI+'titanium_gear',1)],'hazard':[('minecraft:yellow_dye',1),('minecraft:black_dye',1)],'rail':[(MI+'titanium_rod',2)],'light_blue':[('minecraft:glowstone_dust',4),('minecraft:blue_dye',1)],'light_amber':[('minecraft:glowstone_dust',4),('minecraft:orange_dye',1)],'light_violet':[('minecraft:glowstone_dust',4),('minecraft:purple_dye',1)],'vent':[(MI+'motor',1)]}
for part,shape in shape_for.items():machine('block_nexus_'+part,'assembler',[(RE+'nexus_foundation',1) if part=='foundation_chiseled' else ('nexus_'+shape,1)]+([('minecraft:diamond',1)] if part=='foundation_chiseled' else extras.get(part,[(MI+'titanium_plate',2)])),[(RE+'nexus_'+part,1)],eu=8192)
machine('essence_capacitor','assembler',[('astral_essence',64),(MI+'superconductor_wire',8)],[('nexus_essence_capacitor',1)],eu=32768)
machine('phased_control_array','assembler',[('phase_lattice',32),(MI+'quantum_circuit',8)],[('nexus_phased_control_array',1)],eu=32768)
machine('ring','assembler',[('nexus_ring_blank',1),('phase_lattice',24),('astral_essence',16),(MI+'superconductor_wire',2)],[(RE+'nexus_ring',1)],eu=32768,ticks=400)
machine('beacon','assembler',[('nexus_heavy_panel',1),('phase_lattice',48),('nexus_essence_capacitor',2),('minecraft:glass',16)],[(RE+'nexus_beacon',1)],eu=32768,ticks=800)
machine('controller_portal','assembler',[('nexus_phased_control_array',3),('astral_essence',32),('nexus_heavy_panel',1),(MI+'quantum_machine_hull',2)],[(RE+'nexus_controller',1)],eu=32768,ticks=1200)
machine('ignition_cell','assembler',[('nexus_alloy_ingot',16),('phase_lattice',8),('astral_essence',16),(MI+'quantum_circuit',4),(MI+'singularity',1)],[('nexus_ignition_cell',1)],eu=32768,ticks=1200)
machine('core','assembler',[('nexus_core_housing',1),('nexus_ignition_cell',6),('nexus_contact',16),('ae2:engineering_processor',16),(MI+'quantum_circuit',8)],[(RE+'nexus_core',1)],eu=32768,ticks=2400)
machine('builder','assembler',[(MI+'steel_plate',8),(MI+'motor',4),(MI+'electronic_circuit',2),('minecraft:chest',1)],[(RE+'nexus_builder',1)],eu=16)
machine('blank_program','assembler',[(MI+'electronic_circuit',1),('minecraft:paper',4)],[('nexus_blank_program',1)],eu=16)
machine('construction_program','assembler',[('nexus_blank_program',1),stack(RE+'nexus_core',1,probability=0)],[(RE+'nexus_construction_program',1)],eu=32768,ticks=200)
# Blocks and existing program are already registered; catalog only new materials.
ITEMS -= set(n.split(':')[1] for n in manifest['blocks']);ITEMS.discard('nexus_construction_program')
FLUIDS={'nexus_cleaning_solution':'80bbcb','nexus_plating_electrolyte':'4fb6ce','nexus_spent_electrolyte':'727d68','nexus_wastewater':'716a57','warm_nexus_coolant':'d0a078'}
for name,d in RECIPES.items():
 p=RES/'data/revoindustry/recipe/nexus'/f'{name}.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2)+'\n')
catalog={'items':sorted(ITEMS),'fluids':FLUIDS};(RES/'nexus/materials.json').write_text(json.dumps(catalog,indent=2)+'\n')
print(len(ITEMS),'new items;',len(FLUIDS),'process fluids;',len(RECIPES),'production recipes')
if __name__=='__main__':pass
