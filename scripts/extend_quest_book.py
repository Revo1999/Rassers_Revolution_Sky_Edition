"""Stable, additive LV/MV chapters and the optional early running-shoe branch."""
from pathlib import Path
import argparse,json
from quest_presentation import banner

ROOT=Path(__file__).resolve().parents[1]
CHAPTERS=ROOT/'pack/overrides/config/ftbquests/quests/chapters'
MI='modern_industrialization:'
def mid(name):return MI+name
def write(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
def ident(base,n):return f'{base+n:016X}'
LV=0x5EC0000000000000
MV=0x5ED0000000000000
SHOES=0x5EA0000000000000

class Chapter:
    def __init__(self,base,title,icon,order):
        self.base=base;self.data={'id':ident(base,0),'filename':ident(base,0).lower(),'title':title,'icon':{'id':icon,'count':1},'order_index':order,'default_quest_shape':'circle','quests':[],'images':[]}
    def add(self,n,title,icon,x,y,deps,text,items=(),milestone=False,optional=False):
        # Explicit IDs must never be renumbered when adding future quests.
        tasks=[{'id':ident(self.base,0x100000+n*16+i),'type':'item','item':{'id':item,'count':1},'count':count,'consume_items':False} for i,(item,count) in enumerate(items)]
        if not tasks:tasks=[{'id':ident(self.base,0x100000+n*16),'type':'checkmark'}]
        q={'id':ident(self.base,n),'title':title,'icon':{'id':icon,'count':1},'x':float(x),'y':float(y),'shape':'hexagon' if milestone else 'diamond' if optional else 'circle','size':1.25 if milestone else 1.0,'dependencies':[ident(self.base,d) if isinstance(d,int) else d for d in deps],'description':text,'tasks':tasks,'rewards':[]}
        if optional:q['optional']=True
        self.data['quests'].append(q)
    def section(self,n,title,x,y,width):self.data['images'].append(banner(self.base,n,title,x,y,width))
    def save(self):write(CHAPTERS/(self.data['filename']+'.snbt'),self.data)

def low_voltage():
    c=Chapter(LV,'Chapter 6: Electronics and Separation',mid('electric_blast_furnace'),46)
    c.section(1,'LV WORKSHOP',4.5,-2,12);c.section(2,'COILS AND FIRST METALS',18,-2,12)
    c.section(3,'ELECTRONICS',21,10,9);c.section(4,'SEPARATION MACHINES',30,10,9)
    c.add(1,'An Electric Workshop',mid('lv_steam_turbine'),0,0,['5E80000000000029'],[
        'Continue after Chapter 5: The LV Factory has established the EBF and first aluminum. This map reviews the metal routes before extending electronics and separation. Keep steam, water, fuel and battery supplies running.',
        'Check this after powering an actual electric production cycle. Item quests detect possession; checkmark quests are your own commissioning confirmations. Neither consumes materials.'
    ],milestone=True)
    c.add(2,'Wire and Cut',mid('electric_wiremill'),3,0,[1],[
        'Make the electric Wiremill and Cutting Machine using their native recipes. Keep bronze/steel tools available while these are being built.',
        'Use wire, cable, rod and fine-wire recipes shown in EMI. Do not assume a Create sheet is an electrical wire or that every plate can be pressed in Create.'
    ],[(mid('electric_wiremill'),1),(mid('electric_cutting_machine'),1)])
    c.add(3,'Drill the Feedstocks',mid('bauxite_dust'),3,4,[1,'5E5000000000000B'],[
        'Select Nickel, Silver and Bauxite on the Electric Geological Drill. Standard deposits consume 16 EU/t for 200 powered ticks per output.',
        'Minerals come from drills; gems come from sludge processing. The disabled native quarries are not part of this factory.'
    ],[(mid('raw_nickel'),1),(mid('raw_silver'),1),(mid('bauxite_dust'),9)])
    c.add(4,'Copper Meets Nickel',mid('cupronickel_ingot'),6,4,[3],[
        'Process the drilled metals into dust. The MI Mixer combines one Copper Dust and one Nickel Dust into two Cupronickel Dust.',
        'Smelt and shape the alloy using its native recipes. Cupronickel starts the EBF coil branch without needing aluminum first.'
    ],[(mid('cupronickel_ingot'),8)])
    c.add(5,'Magnetic Winding Wire',mid('cupronickel_wire_magnetic'),9,4,[4,2],[
        'Produce Cupronickel Wire and Magnetic Cupronickel Wire with the native recipes. The EBF controller specifically needs the magnetic wire.',
        'Prepare extra ordinary cupronickel cable for the coil blocks. Follow EMI rather than substituting unrelated wire tags.'
    ],[(mid('cupronickel_wire_magnetic'),7)])
    c.add(6,'Cupronickel Coils',mid('cupronickel_coil'),12,4,[5],[
        'Eight Cupronickel Cables craft one coil block. This task detects your first block; manufacture the full set in the MI Guidebook blueprint.',
        'Use a consistent supported coil tier. A better coil does not provide electricity by itself.'
    ],[(mid('cupronickel_coil'),1)])
    c.add(7,'Electric Blast Furnace',mid('electric_blast_furnace'),12,0,[5],[
        'Craft the EBF controller with seven Magnetic Cupronickel Wires, one Basic Machine Hull and one vanilla Blast Furnace.',
        'The crafting-table recipe is the bootstrap. You do not need to build an Assembler or Electrolyzer first.'
    ],[(mid('electric_blast_furnace'),1)],milestone=True)
    c.add(8,'Commission the EBF',mid('electric_blast_furnace'),15,2,[6,7],[
        'Use the MI Guidebook to place the heatproof structure, cupronickel coils and native item/energy hatches. Configure hatch access and keep the required hollow spaces clear.',
        'Check this after the structure forms and its energy input is connected. Supply a sustained 32 EU/t for the starter aluminum and silicon recipes; a charged buffer alone is not continuous generation.'
    ],milestone=True)
    c.add(9,'Pack the Bauxite',mid('bauxite_block'),18,0,[8,3],[
        'Pack nine drilled Bauxite Dust into one Bauxite Block using the native recipe.',
        'This is the inefficient bootstrap feedstock. The Electrolyzer belongs after the first aluminum/electronics, because building it already requires them.'
    ],[(mid('bauxite_block'),1)])
    c.add(10,'Your First Aluminum',mid('aluminum_ingot'),21,0,[9],[
        'EBF: one Bauxite Block becomes one Aluminum Ingot at 32 EU/t for 2,000 ticks. Keep producing until you can support the electronics branch.',
        'Do not send your first bauxite to a machine you cannot build yet. Later native refining improves efficiency.'
    ],[(mid('aluminum_ingot'),1)],milestone=True)
    c.add(11,'Silicon from Sludge Quartz',mid('silicon_dust'),18,4,[8,'5E50000000000003'],[
        'Wash Mineral Sludge for Quartz, then use the available native maceration route for Quartz Dust.',
        'EBF: one Quartz Dust plus one Coke Dust gives one Silicon Dust at 32 EU/t for 400 ticks. Keep the Coke Oven and dust processing supplied.'
    ],[(mid('silicon_dust'),4)])
    c.add(12,'Plain Silicon Plates',mid('silicon_plate'),21,4,[11],[
        'Follow the native Silicon Dust -> ingot -> plate recipes. Ordinary silicon plates work in the starter Diode and Transistor crafting recipes.',
        'Monocrystalline or doped silicon is not required for these first components.'
    ],[(mid('silicon_plate'),4)])
    c.add(13,'Gold Meets Silver',mid('electrum_ingot'),6,8,[3],[
        'Mix one Gold Dust with one Silver Dust for two Electrum Dust, then smelt the ingots.',
        'Gold comes from the mechanical drill line and silver from electric geology. Keep both supplies renewable.'
    ],[(mid('electrum_ingot'),8)])
    c.add(14,'Electrum Wiring',mid('electrum_fine_wire'),9,8,[13,2],[
        'Make both Electrum Cable and Electrum Fine Wire with the native wire-processing recipes.',
        'Fine wire is consumed in electronic components; insulated cable is used in boards, machines and the upcoming MV network.'
    ],[(mid('electrum_cable'),4),(mid('electrum_fine_wire'),4)])
    c.add(15,'First Diodes',mid('diode'),24,4,[12,14],[
        'Use the crafting-table Diode recipe with plain Silicon Plate, glass, Electrum Fine Wire and Steel Plates.',
        'Keep this native manual bootstrap. More advanced silicon recipes are optional efficiency improvements.'
    ],[(mid('diode'),2)])
    c.add(16,'First Transistors',mid('transistor'),24,8,[12,14],[
        'The starter crafting recipe uses three Silicon Plates, one Electrum Fine Wire and three Steel Plates, and makes two Transistors.',
        'Use the native recipe in EMI; your first electronic circuit does not require an electric circuit assembler.'
    ],[(mid('transistor'),2)])
    c.add(17,'Electronic Circuit Boards',mid('electronic_circuit_board'),21,12,[10,14],[
        'Combine Aluminum Plates, Electrum Cable, an Analog Circuit Board and a Redstone Battery using the native crafting-table recipe.',
        'This board still uses the earlier rubber/paper, battery-alloy and analog-electronics supply lines.'
    ],[(mid('electronic_circuit_board'),1)])
    c.add(18,'Electronic Circuits',mid('electronic_circuit'),24,12,[15,16,17],[
        'One Electronic Circuit combines an Electronic Circuit Board, two Diodes, two Transistors and four Analog Circuits.',
        'Craft the first batches by hand. Continue producing aluminum, silicon, electrum and the old analog components instead of dismantling their factories.'
    ],[(mid('electronic_circuit'),2)],milestone=True)
    c.add(19,'Large Motors',mid('large_motor'),27,12,[18],[
        'A Large Motor combines an Electronic Circuit, two Aluminum Rods and six earlier Motors in the native recipe.',
        'These are manufactured components, not simple upgrades that remove the old motor line.'
    ],[(mid('large_motor'),1)])
    c.add(20,'Large Pumps',mid('large_pump'),30,12,[19],[
        'A Large Pump needs two Electronic Circuits, three Aluminum Rotors, one Large Motor and three earlier Pumps.',
        'Keep water/steam and material processing running while making the larger batch needed by separation and chemistry machines.'
    ],[(mid('large_pump'),1)])
    c.add(21,'The Native Centrifuge',mid('centrifuge'),33,12,[20],[
        'Build the single-block MI Centrifuge with its native recipe. This follows aluminum, electronics, Large Motors and a Large Pump.',
        'Its first jobs include salt water separation and native lava processing. Crystalline Sludge still needs the retained Deep Survey Core and its higher-power drill program.'
    ],[(mid('centrifuge'),1)],milestone=True)
    c.add(22,'Electrolysis After Bootstrap',mid('electrolyzer'),30,16,[20],[
        'Now build the Electrolyzer: its Aluminum Rods, Electronic Circuit and Large Pump are available.',
        'Use it for gases and improved refining. It is downstream of the first EBF aluminum, not the source of that first ingot.'
    ],[(mid('electrolyzer'),1)])
    c.add(23,'A Working LV Factory',mid('electronic_circuit'),33,16,[21,22],[
        'Check this after the EBF has produced aluminum and silicon, the component lines are replenishing themselves, and both separation machines can complete a powered cycle.',
        'Keep mechanical extraction and sludge washing operating. Chapter 7 adds MV generation, controlled fluid chemistry and renewable plastic.'
    ],milestone=True)
    c.add(24,'Assembler Bootstrap',mid('assembler'),9,0,[2],[
        'Craft the first Assembler with four Robot Arms, two Analog Circuits, two Conveyors and a Basic Machine Hull. Its own machine recipe is not the bootstrap.',
        'This is available before electronic circuits. It automates native assembly; later fluid recipes need correctly configured fluid inputs.'
    ],[(mid('assembler'),1)],optional=True)
    c.add(25,'Creationalization', 'create:brass_hand',9,-4,[24],[
        "Revo's Creationalization adds same-cost Assembler recipes for Brass Hands and Whisks. MI already supplies several other Create integration recipes.",
        'Check this after automating a useful Create component. Keep brass, quartz and mechanism ingredients intact; these bridges do not grant earlier material tiers.'
    ],optional=True)
    c.save()

def medium_voltage():
    c=Chapter(MV,'Chapter 7: MV - Power and Chemistry',mid('mv_steam_turbine'),47)
    c.section(1,'BUILD AN MV POWER NETWORK',10.5,-2,25);c.section(2,'GASES AND ACIDS',9,3,21);c.section(3,'RENEWABLE PLASTIC AND MANUFACTURING',12,11,27)
    c.add(1,'Beyond the First Circuit',mid('electronic_circuit'),0,0,[ident(LV,23)],[
        'Your first circuits and large components establish the manufacturing base. Now separate the MV power branch from the chemistry branch; both support the finished factory.',
        'These quests guide the installed MI recipes. They do not unlock the art-only Nexus machines or bypass the implemented MV AE2 gates.'
    ],milestone=True)
    c.add(2,'Silicon Batteries',mid('silicon_battery'),3,0,[1],[
        'Craft two Silicon Batteries using Silicon Dust, Battery Alloy plates/curved plates and Electrum Cable.',
        'The battery recipe still needs lead and antimony from your drill supply. Silicon batteries do not replace the need to generate electricity.'
    ],[(mid('silicon_battery'),2)])
    c.add(3,'Advanced Casings',mid('advanced_machine_casing'),6,0,[2],[
        'Use the native Advanced Machine Casing recipe in EMI. Stock its required processed materials before committing your last aluminum to machines.',
        'The subsequent hull consumes the casing, batteries, electronic circuit and electrum cables.'
    ],[(mid('advanced_machine_casing'),1)])
    c.add(4,'Advanced Machine Hull',mid('advanced_machine_hull'),9,0,[3],[
        'Combine one Advanced Machine Casing, two Silicon Batteries, one Electronic Circuit and three Electrum Cables using the native recipe.',
        'Keep a spare supply of the smaller components so building your generator does not stall the factory that makes its replacements.'
    ],[(mid('advanced_machine_hull'),1)],milestone=True)
    c.add(5,'MV Steam Generation',mid('mv_steam_turbine'),12,0,[4],[
        'Build the MV Steam Turbine with the native advanced hull, electronic circuit, aluminum rotors, Large Motors, pipes and electrum cable.',
        'Expand renewable boiler water and fuel delivery. Available steam throughput determines sustained generation; a larger turbine cannot fix starved boilers.'
    ],[(mid('mv_steam_turbine'),1)])
    c.add(6,'Cable the MV Network',mid('electrum_cable'),15,0,[5],[
        'Build a dedicated Electrum Cable network and check the native cable voltage limits. Keep lower-tier consumers on a suitable supply.',
        'Item detection confirms possession only. Connect and inspect the actual network before continuing.'
    ],[(mid('electrum_cable'),16)])
    c.add(7,'Step Down for LV',mid('mv_lv_transformer'),18,0,[6],[
        'Use the MV to LV Transformer for the older network. Check its input/output sides and cable limits; the LV to MV Transformer has the opposite job.',
        'Changing voltage does not create energy. Keep enough generation for the combined simultaneous load.'
    ],[(mid('mv_lv_transformer'),1)])
    c.add(8,'Sustained MV Power',mid('mv_steam_turbine'),21,0,[7],[
        'Check this after the MV network runs a production load while the LV side continues working through its transformer.',
        'Watch boiler fuel, water, steam delivery and machine input buffers over several cycles. A momentary full buffer is not a sustained factory.'
    ],milestone=True)
    c.add(9,'Chemical Reactor',mid('chemical_reactor'),0,5,[1],[
        'Build the native Chemical Reactor using the now-established Electronic Circuits, aluminum parts and Large Pump.',
        'This chapter uses the existing single-block machines. No new coil-tiered processing multiblock is required.'
    ],[(mid('chemical_reactor'),1)])
    c.add(10,'Separate Fluid Lines',mid('fluid_pipe'),3,5,[9],[
        'Give each feed and product a deliberate pipe route, configured machine side and storage destination. Keep tanks and output slots from blocking byproducts.',
        'Check this after transferring an actual input and collecting a machine output. Never mix fluids because their names or colours look similar.'
    ])
    c.add(11,'Renewable Salt Supply',mid('salt_dust'),6,5,[10,'5E50000000000014'],[
        'Keep the 3 x 3 x 3 Salt Geode Chamber supplied with water and 16 EU/t. Pipe its salt water into the native Centrifuge.',
        'Collect Salt Dust with the existing recipe. Alternatively the pulled Salt Crystal Geode program in the powered Iron Greenhouse supplies salt at 8 EU/t with water; it does not require the Centrifuge.'
    ],[(mid('salt_dust'),8)])
    c.add(12,'Water into Gases',mid('electrolyzer'),9,5,[10],[
        'Run the native water Electrolyzer recipe and route hydrogen and oxygen to separate destinations.',
        'Check this after collecting both products. Leave space for every output; a full gas destination can stop the entire separation cycle.'
    ])
    c.add(13,'Chlorine from Salt',mid('salt_dust'),12,5,[11,12],[
        'Use native salt electrolysis to obtain chlorine and its other product. Keep hydrogen from water electrolysis available for the acid step.',
        'Check this after collecting chlorine and routing all byproducts. Salt comes from the chamber/centrifuge line, not a quarry deposit.'
    ])
    c.add(14,'Hydrochloric Acid',mid('hydrochloric_acid_bucket'),15,5,[13],[
        'Chemical Reactor: 1,000 mB Hydrogen plus 1,000 mB Chlorine makes 2,000 mB Hydrochloric Acid at 8 EU/t for 100 ticks.',
        'Check this after collecting acid in its own fluid destination. Native advanced metal refining will use it later.'
    ])
    c.add(15,'Sulfur from Lava',mid('sulfur_dust'),9,9,[10],[
        'Keep the early crucible/cobblestone lava supply working and process lava in the native Centrifuge recipe at 32 EU/t.',
        'Collect its sulfur and handle the other outputs. Sulfur already has this refining route; a new drill deposit is unnecessary.'
    ],[(mid('sulfur_dust'),1)])
    c.add(16,'The Distillery',mid('distillery'),0,13,[9],[
        'Build the native Distillery with Electronic Circuits and Large Pumps. Connect input/output fluid routes and power.',
        'The renewable ethanol route works on a void island and does not require finding an oil deposit.'
    ],[(mid('distillery'),1)])
    c.add(17,'Sugar Solution',mid('sugar_solution_bucket'),3,13,[16,'5E50000000000001'],[
        'Grow sugar cane in your greenhouse and craft sugar. Native Mixer: eight Sugar plus 1,000 mB Water makes 1,000 mB Sugar Solution.',
        'Check this after collecting a batch. Keep the paper supply for rubber separate from the cane you divert into chemistry.'
    ])
    c.add(18,'Distill Renewable Ethanol',mid('ethanol_bucket'),6,13,[17],[
        'The native Distillery turns 1,000 mB Sugar Solution into only 10 mB Ethanol at 8 EU/t for 800 ticks.',
        'Check this after a completed distillation cycle. This starter route is deliberately low-yield: scale crops, water and parallel processing rather than assuming one bucket in gives one bucket out.'
    ])
    c.add(19,'Ethylene Feedstock',mid('ethylene_bucket'),9,13,[18,15],[
        'Chemical Reactor: 1,000 mB Ethanol plus one Sulfur Dust produces 500 mB Ethylene and 400 mB Sulfuric Acid at 10 EU/t for 280 ticks.',
        'Check this after collecting both fluids. Give the acid byproduct an output destination so it cannot stop ethylene production.'
    ])
    c.add(20,'Your First Polyethylene',mid('polyethylene_bucket'),12,13,[19],[
        'Use the native lead route first: 500 mB Ethylene plus four Lead Tiny Dust gives 300 mB Polyethylene at 12 EU/t for 700 ticks.',
        'Check this after collecting plastic. Chromium gives a later alternative yield, but is not required to bootstrap the first plastic. Keep making lead from drills.'
    ],milestone=True)
    c.add(21,'Fluid-Aware Assembly',mid('assembler'),15,13,[20,ident(LV,24)],[
        'Connect Polyethylene to the existing Assembler fluid input and configure its item supply/output. The Assembler itself was craftable before these fluids.',
        'Check this after the machine can receive its plastic feed. Keep enough separate inputs for the native upgrade recipe.'
    ])
    c.add(22,'Basic Machine Upgrades',mid('basic_upgrade'),18,13,[21],[
        'Assembler: two Conveyors, four Motors, two Robot Arms and one Analog Circuit Board make four Basic Upgrades at 8 EU/t for 200 ticks.',
        'Stock these while maintaining the old component lines. Basic Upgrades do not themselves require plastic.'
    ],[(mid('basic_upgrade'),4)])
    c.add(23,'Advanced Machine Upgrades',mid('advanced_upgrade'),21,13,[22],[
        'Assembler: four Basic Upgrades, two Large Motors, one Large Pump, one Electronic Circuit Board and 1,000 mB Polyethylene make four Advanced Upgrades at 8 EU/t for 400 ticks.',
        'These tie the old mechanical components, EBF electronics and renewable chemistry into one production chain.'
    ],[(mid('advanced_upgrade'),4)])
    c.add(24,'Use the Extra Throughput',mid('advanced_upgrade'),24,13,[23,8],[
        'Install supported upgrades in an appropriate native electric machine and complete a production cycle on the intended power network.',
        'Check this after confirming the upgrade is accepted and its input/output logistics can keep up. More throughput may increase demand; follow the machine diagnostics.'
    ])
    c.add(25,'An Integrated MV Factory',mid('mv_steam_turbine'),24,5,[24,14],[
        'Check this after sustaining MV power, retaining a working LV supply, producing Electronic Circuits, handling gases/acids and making Advanced Upgrades from renewable plastic.',
        'This completes the guided first-MV factory, not every MI recipe. Advanced metallurgy and later power need their own chapters; AE2 starter recipes and MV circuit gates are now implemented, while separate autocrafting/addon boundaries still need review. The Nexus models are still art/design and do not count as implemented machines.'
    ],milestone=True)
    c.save()

def shoes():
    c=Chapter(SHOES,'Field Equipment: Running Shoes','revocultivation:white_running_shoes',5)
    c.section(1,'EARLY TEXTILES AND CUSHIONED SOLES',9,-2,21)
    c.add(1,'Start Growing Cotton','revocultivation:cotton_seeds',0,0,['5E50000000000001'],[
        'Combine one Wheat Seeds and one String for two Cotton Seeds. Plant on farmland and grow like wheat, or retain a seed in either greenhouse.',
        'Mature farmland cotton gives one Cotton and two Seeds; the greenhouse produces three Cotton plus one Seed per normal crop cycle.'
    ],[('revocultivation:cotton_seeds',2)],optional=True)
    c.add(2,'Soft Woven Uppers','revocultivation:cotton',3,0,[1],[
        'Harvest four Cotton for a pair. Keep seeds for the next crop; bonemeal can help ordinary farmland plants grow.',
        'Cotton is a new textile crop, not wool or silk with a different name.'
    ],[('revocultivation:cotton',4)],optional=True)
    c.add(3,'Simple Shoe Latex','revocultivation:shoe_latex',0,4,['5E50000000000002'],[
        'Two Leaves plus one Slimeball craft two Shoe Latex. The wooden Mob Harvester Slime program supplies the slime.',
        'These are early shoe materials. They do not substitute for MI Rubber Sheets or bypass its industrial rubber route.'
    ],[('revocultivation:shoe_latex',2)],optional=True)
    c.add(4,'Cure the Rubber','revocultivation:natural_shoe_rubber',3,4,[3],[
        'Smelt Shoe Latex in an ordinary furnace: one latex becomes one Natural Shoe Rubber. Prepare two for a pair.',
        'No steel, electric machine or EBF is needed.'
    ],[('revocultivation:natural_shoe_rubber',2)],optional=True)
    c.add(5,'Glue for the Soles','revocultivation:shoe_glue',6,4,[3,'5E50000000000001'],[
        'One Slimeball plus one Sugar makes four Shoe Glue. Reserve a separate String for the laces.',
        'Shoe Glue is a consumable ingredient, not a whole Create Super Glue tool.'
    ],[('revocultivation:shoe_glue',1),('minecraft:string',1)],optional=True)
    c.add(6,'Lace Up','revocultivation:white_running_shoes',9,0,[2,4,5],[
        'Craft four Cotton, two Natural Shoe Rubber, one Shoe Glue and one String into White Running Shoes. All eight ingredients fit a normal crafting grid.',
        'Wear them in the boots slot for 25% movement speed and no fall damage. Carrying or holding shoes grants no bonus; taking them off removes both effects. They do not prevent void, lava or other non-fall damage.'
    ],[('revocultivation:white_running_shoes',1)],milestone=True,optional=True)
    c.add(7,'Choose Your Colour','revocultivation:blue_running_shoes',12,0,[6],[
        'To craft a coloured pair, use the same eight base ingredients plus one corresponding dye. This makes a fresh pair; it is not a recolouring or repair recipe.',
        'Available: white, black, brown, red, orange, yellow, green, cyan, blue, purple, magenta and pink. All colours have the same bonuses. Check this after trying your preferred pair.'
    ],optional=True)
    c.save()

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--render',action='store_true');args=parser.parse_args()
    low_voltage();medium_voltage();shoes()
    path=CHAPTERS/'5e50000000000000.snbt';chapter=json.loads(path.read_text());salt=next(q for q in chapter['quests'] if q['id']=='5E50000000000014')
    dep=ident(LV,21)
    if dep not in salt['dependencies']:salt['dependencies'].append(dep)
    salt['description']=[
        'First build the native Centrifuge through Chapter 6: EBF aluminum/silicon, Electronic Circuits, Large Motors and a Large Pump. The geode can be constructed earlier and its salt water buffered.',
        'Pipe salt water from the powered geode chamber into the MI Centrifuge and collect Salt Dust with its native recipe. You do not need a new fluid solidifier or a manual salt bootstrap.'
    ]
    write(path,chapter)
    if args.render:
        import render_quest_maps as maps
        maps.OUT=ROOT/'art/quests/lv-mv';maps.OUT.mkdir(parents=True,exist_ok=True)
        rendered=[maps.render(CHAPTERS/(name+'.snbt'),image) for name,image in [('5ec0000000000000','lv-metallurgy.png'),('5ed0000000000000','mv-chemistry.png'),('5ea0000000000000','running-shoes.png')]]
        write(maps.OUT/'previews.json',rendered)
    print('Added 25 LV quests, 25 MV quests and 7 optional running-shoe quests; preserved existing IDs.')
if __name__=='__main__':main()
