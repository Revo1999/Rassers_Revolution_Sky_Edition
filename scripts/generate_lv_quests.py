"""Extend the existing book with LV, preserving every earlier chapter/quest/task ID.
Only writes Chapter 5, its recipe provenance, and an idempotent next-chapter hint.
Quest offsets below are permanent: append new offsets, never renumber existing ones.
"""
from pathlib import Path
import json
from quest_presentation import banner

ROOT=Path(__file__).resolve().parents[1]
CHAPTERS=ROOT/'pack/overrides/config/ftbquests/quests/chapters'
BASE=0x5E80000000000000
MI='modern_industrialization:'
OFFSETS={
 'welcome':1,'cables':2,'budget':3,'batteries':4,'hulls':5,'supply':6,
 'nickel':7,'nickel_dust':8,'invar_mix':9,'invar':10,'diamonds':11,'blade':12,'macerator':13,
 'polarizer':14,'magnets':15,'motors':16,'pistons':17,'conveyors':18,'arms':19,'assembler':20,
 'locks':21,'circuits':22,'components':23,'wiremill':24,'compressor':25,'mixer':26,
 'cupronickel_mix':27,'cupronickel':28,'wire':29,'magnetic_wire':30,'insulation':31,
 'coils':32,'casings':33,'energy':34,'controller':35,'ports':36,'commission':37,
 'bauxite':38,'nuggets':39,'aluminum':40,'handoff':41,'cutter':42,'furnace':43,
 'lubricant':44,'upgrades':45,'greenhouse':46,'salt':47,'converter':48,
}
IDS={k:f'{BASE+n:016X}' for k,n in OFFSETS.items()}

def main():
    quests=[];provenance=[]
    def q(key,title,item=None,count=1,deps=(),at=(0,0),text=(),recipes=(),optional=False,milestone=False,items=None,icon=None):
        ident=IDS[key];n=OFFSETS[key]
        inputs=items or ([(item,count)] if item else [])
        tasks=[{'id':f'{BASE+0x1000+n*16+i:016X}','type':'item','item':{'id':name,'count':1},'count':amount,'consume_items':False} for i,(name,amount) in enumerate(inputs)]
        if not tasks:tasks=[{'id':f'{BASE+0x1000+n*16:016X}','type':'checkmark'}]
        value={'id':ident,'title':title,'icon':{'id':icon or item or 'modern_industrialization:guidebook','count':1},
            'x':float(at[0]),'y':float(at[1]),'shape':'hexagon' if milestone else 'diamond' if optional else 'circle',
            'size':1.25 if milestone else 1.0,'dependencies':[IDS.get(d,d) for d in deps],
            'description':list(text),'tasks':tasks,'rewards':[]}
        if optional:value['optional']=True
        quests.append(value);provenance.append({'key':key,'quest_id':ident,'recipe_ids':[r if ':' in r else MI+r for r in recipes]})

    q('welcome','Chapter 5: The LV Factory',deps=['5E5000000000000A'],at=(0,0),icon=MI+'lv_steam_turbine',milestone=True,text=[
        'You have made the first LV Steam Turbine in Progression Routes: Biology to Electricity. Now turn that bootstrap into a repeatable factory.',
        'The main route builds component automation, an Electric Blast Furnace and your first aluminum. Diamonds and convenience machines are optional branches.',
        'Item tasks detect your inventory without consuming anything. Checkmarks are practical jobs: confirm them after building and testing the described setup. Keep the steam workshop running while you upgrade.'])
    q('cables','A Dedicated LV Line',MI+'tin_cable',8,['welcome'],(3,0),[
        'Run tin cable from the turbine output to a small LV workshop. A turbine connects through its marked electrical output face, not every side.',
        'Tin, copper and silver are LV cables in this MI version. Electrum and cupronickel are MV cables; do not substitute them on the LV single-block machine line.'],['materials/tin/craft/cable'])
    q('budget','Watts Before More Machines',deps=['cables'],at=(6,0),icon=MI+'lv_steam_turbine',text=[
        'One LV Steam Turbine converts up to 32 mB steam per tick into 32 EU/t. Two fully supplied turbines can provide 64 EU/t; empty boilers cannot.',
        'The pinned MI guide gives LV cable networks a 256 EU/t sustained input limit. Single-block electric machines connect to LV cable even when you later unlock more advanced chemistry.',
        'Confirm that a powered machine can run continuously without the steam tank draining. Add boiler and water capacity before adding more loads. The power converter changes energy format; it creates no electricity.'])
    q('batteries','Reserve Power in the Hull',MI+'redstone_battery',2,['budget'],(9,0),[
        'Make two Redstone Batteries for one Basic Machine Hull. Before the electric drill, use the pack\'s starter recipe: three copper plates, two zinc ingots, two redstone and two tin cables per battery.',
        'The native battery-alloy recipe remains available once you have lead and antimony. Those metals come from electric drilling, so the copper/zinc option prevents a hull -> battery -> electric drill -> hull loop.'],['revoextraction:bootstrap_redstone_battery','electric_age/battery/lv_battery_asbl'])
    q('hulls','Repeatable Machine Hulls',MI+'basic_machine_hull',2,['batteries'],(12,0),[
        'A Basic Machine Hull uses a steel machine casing, an Analog Circuit, two Redstone Batteries and three Tin Cables.',
        'The crafting-table route works before your first Assembler. Make one small batch now; later give hulls their own assembly recipe and buffer. The existing turbine already used one hull.'],['electric_age/hull/lv_machine_hull_asbl'],milestone=True)
    q('supply','Expand the Steam Supply',deps=['hulls'],at=(15,0),icon=MI+'lv_steam_turbine',optional=True,text=[
        'Keep a steam reserve between generation and consumption, and give water/fuel a renewable input. Use your established steam boiler setup; a Large Steam Boiler is a later optional expansion, not an LV entry requirement.',
        'Add another LV turbine when demand exceeds 32 EU/t. Check steam production, pipe routing and machine buffers before assuming cables are the bottleneck.'])

    q('nickel','Nickel from the Void',MI+'raw_nickel',8,['welcome','5E5000000000000B'],(0,6),[
        'Use the Electric Geological Drill from the route chapter. Select Nickel instead of Mixed Metals when you need a reliable batch.',
        'This program consumes 16 EU/t for 200 powered ticks per raw nickel. It pauses without power or output space. You do not need an MV survey core for nickel.'])
    q('nickel_dust','Keep the Steam Macerator',MI+'nickel_dust',8,['nickel'],(3,6),[
        'Macerate Raw Nickel in your existing steam Macerator. The native recipe guarantees one nickel dust per raw nickel and has a 50% chance for another.',
        'Do not wait for an Electric Macerator: its cutting blade needs invar and diamond dust. Your steam machine deliberately breaks that bootstrap loop.'],['materials/nickel/macerator/raw_metal'])
    q('invar_mix','Two Iron, One Nickel',MI+'invar_dust',3,['nickel_dust'],(6,6),[
        'Mix two iron dust with one nickel dust into three invar dust. Your existing steam Mixer handles this low-energy alloy recipe.',
        'Invar becomes heatproof casings and cutting hardware. Stockpile it alongside steel; the Electric Blast Furnace will use a substantial casing batch.'],['alloy/mixer/invar/dust'])
    q('invar','An Alloy That Takes Heat',MI+'invar_ingot',8,['invar_mix'],(9,6),[
        'Smelt invar dust into ingots in an ordinary furnace or blast furnace. No Electric Blast Furnace is needed for invar.',
        'Prepare plates and gears with your established machines or the native hand-tool recipes. The eight ingots here are a starter batch, not the full EBF material budget.'],['materials/invar/smelting/dust_to_ingot_smelting'])
    q('diamonds','Optional: Diamond Cutting Dust',MI+'diamond_dust',4,['invar'],(12,6),[
        'Macerate four diamonds from Steel Drill Gem Sludge in your existing steam Macerator.',
        'This branch improves processing convenience. It does not block the main Assembler-to-EBF route. Do not try to make the Electric Macerator before preparing this dust.'],['materials/diamond/macerator/gem'],optional=True)
    q('blade','Optional: Invar Rotary Blade',MI+'invar_rotary_blade',1,['diamonds'],(15,6),[
        'Craft the blade from four diamond dust around one invar gear. This is a crafting-table recipe; it does not require an Assembler.',
        'Keep another blade in mind if you also want the Electric Cutting Machine. The blade is a machine ingredient, not a consumable drill maintenance part.'],['electric_age/component/craft/invar_rotary_blade_asbl'],optional=True)
    q('macerator','Optional: Electric Maceration',MI+'electric_macerator',1,['blade','hulls'],(18,6),[
        'Craft the Electric Macerator using the invar rotary blade, two pistons, two motors, two Analog Circuits, a Basic Hull and tin cable.',
        'Connect LV cable, lock input/output slots and feed the same recipes you learned in steam. This upgrades throughput; it does not multiply every raw-metal recipe into a guaranteed two dust.'],['electric_age/machine/macerator_asbl'],optional=True)

    q('polarizer','Magnets Without Redstone Waste',MI+'polarizer',1,['hulls'],(0,14),[
        'Build a Polarizer with tin wire, tin cable, two inductors and a Basic Hull. It is an early LV machine; it does not require aluminum or MV circuits.',
        'The Polarizer makes magnetic steel rods from ordinary steel rods. This replaces the manual recipe that spends redstone around each rod.'],['electric_age/machine/polarizer_asbl'])
    q('magnets','A Batch of Magnetic Rods',MI+'steel_rod_magnetic',4,['polarizer'],(3,12),[
        'Run steel rods through the Polarizer at 8 EU/t. Keep a small output buffer for motor production.',
        'Magnetic steel rods are used in ordinary Motors. Reserve cupronickel wire for the separate magnetic-wire recipe later in this chapter.'],['materials/steel/polarizer/rod_magnetic'])
    q('motors','Motors by the Batch',MI+'motor',4,['magnets'],(6,12),[
        'Make four Motors using magnetic steel rods, steel rods, copper wire and tin cable. The hand-crafted route remains available while you build the first Assembler.',
        'Conveyors, Robot Arms, Pumps and several electric machines all consume motors. A single reusable motor supply is better than rebuilding the ingredients for each machine.'],['electric_age/component/craft/motor_asbl'])
    q('pistons','Pistons for the Production Line',MI+'piston',4,['motors'],(9,12),[
        'MI Pistons use a motor, steel plates, steel rods, a steel gear and tin cable. These are MI components, not vanilla wooden pistons.',
        'Keep steel processing supplied from the steam side. The Electric Compressor needs four pistons by itself.'],['electric_age/component/craft/piston_asbl'])
    q('conveyors','Move Parts, Not Yourself',MI+'conveyor',2,['pistons'],(12,12),[
        'Craft two Conveyors: each uses two Motors, a Tin Cable and six Rubber Sheets.',
        'Keep paper-to-rubber production from the route chapter running. Conveyors are crafting components; physical item transport can still use your chosen pipes, hoppers or Create equipment.'],['electric_age/component/craft/conveyor_asbl'])
    q('arms','A Pair of Robot Arms',MI+'robot_arm',2,['conveyors'],(15,12),[
        'Each Robot Arm needs two Motors, an MI Piston, an Analog Circuit, two steel rods and three Tin Cables.',
        'Make two arms for the first Assembler. Continue using the crafting-table recipe until the Assembler can reproduce its own components.'],['electric_age/component/craft/robot_arm_asbl'])
    q('assembler','The First Assembler',MI+'assembler',1,['arms'],(18,12),[
        'Craft the Assembler from two Robot Arms, two Conveyors, two Analog Circuits and a Basic Machine Hull.',
        'Connect LV power. This machine assembles repeatable component recipes without manual crafting, but it does not supply its own ingredients or electricity.'],['electric_age/machine/assembler_asbl'],milestone=True)
    q('locks','Teach the Assembler Its Job',deps=['assembler'],at=(21,12),icon=MI+'assembler',text=[
        'Choose a simple component recipe in EMI. Lock the required input slots to their intended items and amounts, then reserve enough output space.',
        'Use the native MI auto-transfer controls and selected transfer face, or external item pipes. Test that a full output pauses the line rather than jamming unrelated ingredients.',
        'Confirm this checkmark after the Assembler completes a supplied recipe and transfers its result to storage. Holding a machine item alone does not prove automation.'])
    q('circuits','Automate Analog Circuits',deps=['locks'],at=(24,12),icon=MI+'analog_circuit',text=[
        'Give Analog Circuit Boards and Analog Circuits a repeatable recipe supply. Their MI assembler alternatives use the same LV ingredients you already unlocked.',
        'Keep boards, resistors, capacitors and inductors stocked. One assembler may support several recipes with careful locking; separate assemblers are easier to troubleshoot.',
        'Confirm after another set of circuits arrives in its buffer without hand-crafting. These are LV Analog Circuits; the Electronic Circuit is the later MV gate.'],recipes=['assembler_generated/electric_age/circuit/craft/lv_circuit','assembler_generated/electric_age/circuit/craft/lv_circuit_board'])
    q('components','A Factory That Builds Factories',deps=['circuits'],at=(27,12),icon=MI+'robot_arm',milestone=True,text=[
        'Extend the line to Basic Hulls, Motors, Pistons, Conveyors and Robot Arms. Automate one recipe at a time and keep small buffers between stages.',
        'Confirm once you can order another basic electric machine using buffered parts. There is no requirement to create an enormous factory or fill a chest of every component.'],recipes=['assembler_generated/electric_age/hull/lv_machine_hull'])

    q('wiremill','Electric Wire Production',MI+'electric_wiremill',1,['assembler'],(0,18),[
        'Build the Electric Wiremill with four Motors, two Analog Circuits, tin cable and a Basic Hull.',
        'Use it for copper/tin wire and the upcoming cupronickel batch. Follow each EMI recipe\'s actual yield rather than assuming every hand-tool and machine route has the same material cost.'],['electric_age/machine/wiremill_asbl'])
    q('compressor','Plates for the Next Tier',MI+'electric_compressor',1,['wiremill'],(3,18),[
        'Build the Electric Compressor using four MI Pistons, two Analog Circuits, tin cable and a Basic Hull.',
        'Prepare invar plates/gears for heatproof casings, and later aluminum plates. Keep the steam compressor available until this one is powered and supplied.'],['electric_age/machine/compressor_asbl'])
    q('mixer','An Electric Alloy Station',MI+'electric_mixer',1,['compressor'],(6,18),[
        'The Electric Mixer uses an ordinary Motor, two tin rotors, two Analog Circuits, glass, a fluid pipe and a Basic Hull.',
        'It can continue the low-energy invar and cupronickel alloy recipes. Keep fluid recipes separate from dry alloys if that makes routing easier.'],['electric_age/machine/mixer_asbl'])
    q('cupronickel_mix','Copper Meets Nickel',MI+'cupronickel_dust',8,['mixer','invar_mix'],(9,18),[
        'Mix one copper dust and one nickel dust into two cupronickel dust. This is a 2 EU/t recipe and works before advanced chemistry.',
        'This batch starts the coil supply. Sixteen Cupronickel Coils eventually need 128 insulated cupronickel cables, so scale your nickel, wire and rubber production.'],['alloy/mixer/cupronickel/dust'])
    q('cupronickel','Smelt the Coil Alloy',MI+'cupronickel_ingot',8,['cupronickel_mix'],(12,18),[
        'Smelt cupronickel dust into ingots with an ordinary furnace or blast furnace. The EBF does not need to exist yet.',
        'These ingots split into two uses: insulated cables for coils and magnetic wire for the EBF controller. They are not interchangeable ingredients.'],['materials/cupronickel/smelting/dust_to_ingot_smelting'])
    q('wire','Cupronickel Wire',MI+'cupronickel_wire',16,['cupronickel'],(15,18),[
        'Process cupronickel into wire with your wiremill. Keep the uninsulated wire available for polarizing.',
        'The coils need insulated Cupronickel Cables. The controller needs Magnetic Cupronickel Wire. Prepare both branches before assembling the furnace.'],['materials/cupronickel/wiremill/wire'])
    q('magnetic_wire','Magnetic Heating Wire',MI+'cupronickel_wire_magnetic',8,['wire'],(18,19.5),[
        'Polarize ordinary cupronickel wire at 8 EU/t. The Electric Blast Furnace controller consumes eight of this magnetic wire around a Basic Hull and a vanilla Blast Furnace.',
        'This is an LV Polarizer recipe. No Electrolyzer, Chemical Reactor or Electronic Circuit is needed to bootstrap the EBF.'],['materials/cupronickel/polarizer/wire_magnetic'])
    q('insulation','Insulate the Coil Supply',MI+'cupronickel_cable',8,['wire'],(21,18),[
        'Wrap cupronickel wire in rubber sheets to make insulated Cupronickel Cables. The native hand recipe makes three cables from three wire and six rubber sheets.',
        'Use these cables as coil ingredients. They are MV electrical cable, so do not replace the tin cable feeding your LV machines with them.'],['materials/cupronickel/craft/cable'])

    q('coils','Sixteen Cupronickel Coils',MI+'cupronickel_coil',16,['insulation'],(0,24),[
        'Each coil is a ring of eight insulated cupronickel cables. Prepare sixteen coils for the first EBF\'s two hollow middle layers.',
        'This means 128 cables in total. Feed the wire/rubber lines while you prepare heatproof casings and the controller; you do not need to hand-craft every intermediate.'],['materials/cupronickel/craft/coil'])
    q('casings','Heatproof, Not Ordinary Steel',MI+'heatproof_machine_casing',14,['invar'],(3,24),[
        'Each Heatproof Machine Casing uses eight invar plates and an invar gear. Normal steel casing is not a valid substitute for the EBF caps.',
        'Fourteen casings fit the starter layout with one controller, one item input, one item output and one LV energy input across the two cap layers. More hatch positions replace casing positions; check MI\'s live construction preview.'],['electric_age/casing/heatproof_machine_casing_asbl'])
    q('energy','An Electrical Hatch',MI+'lv_energy_input_hatch',1,['compressor'],(6,24),[
        'Craft an LV Energy Input Hatch from one Tin Cable and one Basic Machine Hull. It is how EU enters an electric multiblock.',
        'Use LV cable on this hatch. A Revo furnace fuel hatch or FE/EU converter is not a substitute for the EBF\'s native MI energy hatch.'],['hatches/basic/energy_input_hatch'])
    q('controller','Electric Blast Furnace Controller',MI+'electric_blast_furnace',1,['magnetic_wire'],(9,24),[
        'Craft the controller from eight Magnetic Cupronickel Wire, a Basic Machine Hull and a vanilla Blast Furnace.',
        'This is a different machine from both the Steam Blast Furnace and your Iron Furnaces multiblocks. It consumes EU through energy hatches rather than solid fuel in its controller.'],['electric_age/machine/electric_blast_furnace_asbl'],milestone=True)
    q('ports','Finish the EBF Parts Kit',deps=['controller','coils','casings','energy'],at=(12,27),icon=MI+'bronze_item_input_hatch',items=[(MI+'bronze_item_input_hatch',1),(MI+'bronze_item_output_hatch',1)],text=[
        'Add one native MI item input hatch and one native MI item output hatch. Bronze versions are enough for this starter recipe.',
        'Use the MI construction guide for valid cap-layer hatch positions. The custom Revo furnace hatches belong to the Iron Furnaces multiblock system, not this EBF.',
        'Your parts kit is 16 coils, 14 heatproof casings, 1 controller and 3 hatches. Leave the two middle centre cells empty.'],recipes=['hatches/bronze/item_input_hatch','hatches/bronze/item_output_hatch'])
    q('commission','Commission the Electric Furnace',deps=['ports'],at=(15,24),icon=MI+'electric_blast_furnace',milestone=True,text=[
        'Build the four-layer EBF: bottom heatproof cap, two hollow 3 x 3 coil rings, top heatproof cap. Put the controller in the bottom cap; hatches belong in the top or bottom cap.',
        'Connect item supply/output and LV electricity. The first bauxite recipe needs a continuous 32 EU/t; a fully supplied LV turbine can provide that baseline.',
        'MI can overclock electric multiblocks beyond this baseline. Add enough real generation before raising consumption. Confirm only after the structure forms and is connected, not just after crafting its controller.'])
    q('bauxite','Bauxite on Demand',MI+'bauxite_dust',9,['commission','5E5000000000000B'],(18,24),[
        'Select Bauxite on the Electric Geological Drill. The ordinary program needs 16 EU/t and 200 powered ticks per dust; no survey core is required.',
        'Keep this resource line running while the EBF works. A separate turbine branch or enough shared generation prevents the drill and furnace from starving each other.'])
    q('nuggets','The First Aluminum Nuggets',MI+'aluminum_nugget',9,['bauxite'],(21,24),[
        'The bootstrap EBF recipe turns ONE bauxite dust into ONE aluminum nugget at 32 EU/t for 200 ticks. Run nine dust for nine nuggets.',
        'Do not wait for an Electrolyzer: it already needs aluminum parts and Electronic Circuits. Bauxite directly in the EBF is the intended way around that dependency.'],['materials/blast_furnace/bauxite_to_aluminum_nugget'])
    q('aluminum','An Ingot Beyond Steel',MI+'aluminum_ingot',1,['nuggets'],(24,24),[
        'Craft nine aluminum nuggets into one aluminum ingot. You can also feed a nine-dust bauxite block to the native EBF block recipe for an ingot.',
        'The later Electrolyzer makes bauxite processing much more efficient, but it is not needed for this first ingot. Continue this bootstrap route until you can afford the next components.'],['materials/aluminum/craft/ingot_from_nugget','materials/blast_furnace/bauxite_to_aluminum_ingot'],milestone=True)
    q('handoff','LV Established: Next Stop MV',deps=['aluminum','components'],at=(27,24),icon=MI+'electronic_circuit',milestone=True,text=[
        'You have a functioning LV component factory and an aluminum route. Confirm once both are working; this is the handoff into the next technology stage.',
        'Next come aluminum parts, electrum cable, Electronic Circuit Boards and Electronic Circuits, then improved components and MV generation. Electronic Circuits are the pack\'s MV circuit gate; Digital Circuits are later.',
        'At MV, prepare the Advanced Machine Hull for the Deep Survey Core. Process Crystalline Sludge for your first Certus.',
        'Electronic Circuits unlock AE2 storage. Digital Circuits in HV unlock AE2 autocrafting and ExtendedAE Infinity Cells.',
        'MI\'s more advanced single-block machines still connect to LV cable. MV cables and transformers matter for MV generators and compatible hatches; do not rewire every basic machine directly to MV.'])

    q('cutter','Optional: Electric Cutting',MI+'electric_cutting_machine',1,['blade','assembler'],(3,36),[
        'Use an invar rotary blade, two Motors, two Conveyors, two Analog Circuits, glass and a Basic Hull for the Electric Cutting Machine.',
        'This is a convenience branch for automated cutting. Keep the existing steam cutting/hand-tool routes until it is ready.'],['electric_age/machine/cutting_machine_asbl'],optional=True)
    q('furnace','Optional: Electric Smelting',MI+'electric_furnace',1,['magnetic_wire'],(24,36),[
        'An Electric Furnace uses Magnetic Cupronickel Wire, Analog Circuits, Tin Cable and a Basic Hull.',
        'This handles ordinary furnace recipes on EU. It does not replace the special Electric Blast Furnace recipes for aluminum, and you may keep your existing fuel-fired furnaces.'],['electric_age/machine/furnace_asbl'],optional=True)
    q('lubricant','Optional: Lubricate the Line',MI+'lubricant_bucket',1,['mixer'],(9,36),[
        'Mix 500 mB creosote with one redstone to obtain 500 mB Lubricant. Make enough to fill a bucket if you want to complete this item task.',
        'The pinned MI guide allows a lubricant container to be used on electric machines for manual efficiency progress. More speed means more power demand; fix supply and buffers before lubricating everything.'],['materials/mixer/lubricant'],optional=True)
    q('upgrades','Optional: Room to Overclock',MI+'basic_upgrade',4,['components'],(12,36),[
        'The Assembler makes four Basic Upgrades from two Conveyors, four Motors, two Robot Arms and an Analog Circuit Board.',
        'Upgrades raise a machine\'s allowed overclock; they do not generate EU or make a missing recipe available. Read the live machine UI for the upgrade amount and power ceiling before installing a batch.'],['electric_age/upgrades/basic'],optional=True)
    q('greenhouse','Optional: Farm Through the Night','revocultivation:iron_greenhouse',1,['5E50000000000012'],(15,36),[
        'Your Iron Greenhouse consumes 8 EU/t and 250 mB water per 200-tick harvest. Its integrated converter accepts configured FE input; its hatch can also accept native EU.',
        'Use the Revo EMI construction preview for the five-block structure, fitted converter and water tank. Supply water and a retained crop starter, then let the actual grow lights enable night production.'],['revocultivation:iron_greenhouse'],optional=True)
    q('salt','Optional: Seawater Feedstock','revoextraction:mineral_sludge',1,['greenhouse'],(18,36),[
        'Craft a Salt Crystal Geode from quartz, sand, compost and a water bucket. The bucket returns. Put the geode in the Iron Greenhouse with power and water.',
        'Each harvest retains the geode and produces one Mineral Sludge for 1600 EU and 250 mB water. Mix the sludge with a bucket of water in the MI Mixer for seawater. Concentrate and centrifuge it for Salt Dust.'],['revoextraction:salt_crystal_starter','revoindustry:mineral_sludge_sea_water_mixing'],optional=True)
    q('converter','Optional: Bridge FE and EU','revoindustry:power_converter',1,['5E5000000000000A','5E50000000000005','assembler'],(21,36),[
        'Run the MI Assembler on LV steam power. Assemble the Basic Power Converter from a spare LV Steam Turbine, a Flux Core, four iron ingots, two redstone and two copper ingots at 8 EU/t.',
        'Keep your working turbine on the steam line. The converter moves existing energy between FE and EU. Point each port at the correct network and check the selected direction.'],['revoindustry:power_converter'],optional=True)

    chapter={'id':f'{BASE:016X}','filename':f'{BASE:016x}','title':'Chapter 5: The LV Factory',
        'icon':{'id':MI+'assembler','count':1},'order_index':45,'default_quest_shape':'circle',
        'images':[banner(BASE,1,'01 / SUPPLY A REAL LV WORKSHOP',10,-2.5,26),
                  banner(BASE,2,'02 / NICKEL, INVAR & OPTIONAL CUTTING',10,3.5,26),
                  banner(BASE,3,'03 / AUTOMATE THE COMPONENTS',13.5,9.5,33),
                  banner(BASE,4,'04 / WIRE, PLATES & HEATING ALLOYS',10.5,15.5,27),
                  banner(BASE,5,'05 / ELECTRIC BLAST FURNACE -> ALUMINUM',13.5,21.5,33),
                  banner(BASE,6,'OPTIONAL / GROW INTO YOUR FACTORY',12,33.5,27)],'quests':quests}
    path=CHAPTERS/f'{BASE:016x}.snbt';path.write_text(json.dumps(chapter,indent=2)+'\n',encoding='utf-8')
    (ROOT/'docs/LV-QUEST-RECIPE-SOURCES.json').write_text(json.dumps({'chapter_id':f'{BASE:016X}','pinned_mi':'2.5.11',
        'guide_sources':['assets/modern_industrialization/mi_guidebook/electric_age/'+n+'.md' for n in ['electricity','basic_machines','advanced_machines','ebf']],
        'runtime_source':'revoextraction ElectricDrillEntity: nickel/bauxite 16 EU/t, 200 ticks; FarmMachine/MachineKind: powered salt program 19',
        'quests':provenance},indent=2)+'\n')
    recipe={'type':'minecraft:crafting_shaped','neoforge:conditions':[{'type':'neoforge:mod_loaded','modid':'modern_industrialization'}],
        'pattern':['cPc','ZRZ','PRP'],'key':{'c':{'item':MI+'tin_cable'},'P':{'tag':'c:plates/copper'},'Z':{'tag':'c:ingots/zinc'},'R':{'item':'minecraft:redstone'}},
        'result':{'id':MI+'redstone_battery','count':1}}
    target=ROOT/'revoextraction/src/main/resources/data/revoextraction/recipe/bootstrap_redstone_battery.json'
    target.write_text(json.dumps(recipe,indent=2)+'\n')
    bridge=CHAPTERS/'5e50000000000000.snbt';data=json.loads(bridge.read_text())
    hint='Next: open Chapter 5: The LV Factory for power distribution, component automation, the Electric Blast Furnace and your first aluminum.'
    quest=next(q for q in data['quests'] if q['id']=='5E5000000000000A')
    if hint not in quest['description']:quest['description'].append(hint)
    bootstrap_hint='Before the first hull, make Redstone Batteries with the copper/zinc starter recipe shown in EMI; native battery alloy is also available from mechanical Steel Drill lead and antimony.'
    if bootstrap_hint not in quest['description']:quest['description'].insert(0,bootstrap_hint)
    if not any(q['id']=='5E50000000000019' for q in data['quests']):
        data['quests'].append({'id':'5E50000000000019','title':'Starter Batteries for LV','icon':{'id':MI+'redstone_battery','count':1},
            'x':12.0,'y':3.0,'shape':'circle','dependencies':['5E50000000000008'],
            'description':['Before electric drilling supplies lead and antimony, craft Redstone Batteries from three copper plates, two zinc ingots, two redstone and two tin cables each.',
                'Two batteries go into one Basic Machine Hull. Zinc comes from your Iron/Steel Drill and copper plates from the existing steam/Create workshop.',
                'The native battery-alloy route stays available later. No previous quest or task ID has changed; this added lesson does not relock an already completed turbine quest.'],
            'tasks':[{'id':'5E50000000100019','type':'item','item':{'id':MI+'redstone_battery','count':1},'count':2,'consume_items':False}],'rewards':[]})
    bridge.write_text(json.dumps(data,indent=2)+'\n')
    print(f'Wrote {len(quests)} stable LV quests, recipe provenance and the existing turbine quest handoff.')

if __name__=='__main__':main()
