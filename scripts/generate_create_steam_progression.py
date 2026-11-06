"""Stable Create -> iron drill -> MI steam quests, plus the skyblock recipe bridges."""
from pathlib import Path
import json
from quest_presentation import create_chapter

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'revoextraction/src/main/resources'
CHAPTERS = ROOT / 'pack/overrides/config/ftbquests/quests/chapters'

def put(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')

def ingredient(value):
    return {'tag': value[1:]} if value.startswith('#') else {'item': value}

def recipe(name, data, mi=False):
    if mi:
        data['neoforge:conditions'] = [{'type': 'neoforge:mod_loaded', 'modid': 'modern_industrialization'}]
    put(RES / f'data/revoextraction/recipe/{name}.json', data)

def shaped(name, pattern, key, output=None, count=1):
    recipe(name, {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': pattern,
        'key': {k: ingredient(v) for k, v in key.items()},
        'result': {'id': output or f'revoextraction:{name}', 'count': count}})

def shapeless(name, inputs, output):
    recipe(name, {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
        'ingredients': [ingredient(i) for i in inputs], 'result': {'id': output, 'count': 1}})

shaped('skyblock_andesite', ['CG', 'GC'], {'C': 'minecraft:cobblestone', 'G': 'minecraft:gravel'}, 'minecraft:andesite', 2)
for part in ['support', 'casing']:
    shapeless('iron_drill_' + part, [f'revoextraction:wooden_drill_{part}', '#c:plates/iron'], f'revoextraction:iron_drill_{part}')
shaped('iron_drill_core', ['IPI', 'PCP', 'IPI'], {'I': 'minecraft:iron_ingot', 'P': '#c:plates/iron', 'C': 'revoextraction:wooden_drill_core'})
shaped('iron_drill_controller', ['CCC', 'CSC', 'CCC'], {'C': 'revoextraction:iron_drill_casing', 'S': 'revoextraction:iron_drill_core'})
for part in ['input', 'output_hatch']:
    shapeless('iron_drill_' + part, [f'revoextraction:wooden_drill_{part}', '#c:plates/iron', '#c:plates/iron'], f'revoextraction:iron_drill_{part}')

for source, output, name in [('minecraft:raw_copper', 'modern_industrialization:copper_dust', 'copper'),
        ('modern_industrialization:raw_tin', 'modern_industrialization:tin_dust', 'tin'),
        ('minecraft:brick', 'modern_industrialization:brick_dust', 'brick')]:
    recipe('milling/' + name + '_dust', {'type': 'create:milling', 'ingredients': [{'item': source}],
        'processing_time': 100, 'results': [{'id': output, 'count': 1}]}, mi=True)
recipe('mixing/first_bronze', {'type': 'create:mixing',
    'ingredients': [{'tag': 'c:dusts/copper'}] * 3 + [{'tag': 'c:dusts/tin'}],
    'results': [{'id': 'modern_industrialization:bronze_dust', 'count': 4}]}, mi=True)
recipe('pressing/bronze_plate', {'type': 'create:pressing', 'ingredients': [{'tag': 'c:ingots/bronze'}],
    'results': [{'id': 'modern_industrialization:bronze_plate', 'count': 1}]}, mi=True)

lang_path = RES / 'assets/revoextraction/lang/en_us.json'
lang = json.loads(lang_path.read_text(encoding='utf-8-sig'))
for suffix, title in [('support', 'Support'), ('casing', 'Casing'), ('core', 'Core'),
        ('controller', 'Controller'), ('input', 'Kinetic Input'), ('output_hatch', 'Item Output Hatch')]:
    name = 'iron_drill_' + suffix
    lang['block.revoextraction.' + name] = 'Iron Drill ' + title
    for kind in ['blockstates', 'models/item']:
        wood = RES / f'assets/revoextraction/{kind}/wooden_drill_{suffix}.json'
        put(RES / f'assets/revoextraction/{kind}/{name}.json', json.loads(wood.read_text(encoding='utf-8').replace('wooden_drill_', 'iron_drill_')))
    put(RES / f'data/revoextraction/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'revoextraction:' + name}],
         'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
lang.update({'gui.revoextraction.iron_drill': 'Iron Drill',
    'gui.revoextraction.kinetic': 'Create-powered extraction',
    'emi.category.revoextraction.iron_drill_assembly': 'Iron Drill Construction',
    'gui.revoextraction.state.too_slow': 'Too slow',
    'gui.revoextraction.hint.too_slow': 'Iron drill needs at least 32 RPM'})
put(lang_path, lang)
tag = RES / 'data/minecraft/tags/block/mineable/pickaxe.json'
data = json.loads(tag.read_text(encoding='utf-8-sig')) if tag.exists() else {'replace': False, 'values': []}
data['values'] = list(dict.fromkeys(data['values'] + ['revoextraction:iron_drill_' + p for p in ['support', 'casing', 'core', 'controller', 'input', 'output_hatch']]))
put(tag, data)

class Chapter:
    def __init__(self, base, title, icon, order):
        self.base, self.title, self.icon, self.order = base, title, icon, order
        self.quests, self.ids = [], {}
    def hid(self, offset): return f'{self.base + offset:016X}'
    def q(self, key, title, item=None, count=1, deps=(), x=0, y=0, text=(), items=None):
        i = len(self.quests) + 1
        self.ids[key] = self.hid(i)
        inputs = items or ([(item, count)] if item else [])
        tasks = [{'id': self.hid(0x1000 + i * 16 + j), 'type': 'item',
            'item': {'id': ident, 'count': 1}, 'count': n, 'consume_items': False} for j, (ident, n) in enumerate(inputs)]
        if not tasks: tasks = [{'id': self.hid(0x1000 + i * 16), 'type': 'checkmark'}]
        quest = {'id': self.ids[key], 'title': title, 'x': float(x), 'y': float(y),
            'description': list(text), 'tasks': tasks, 'rewards': []}
        if item: quest['icon'] = {'id': item, 'count': 1}
        if deps: quest['dependencies'] = [self.ids.get(d, d) for d in deps]
        self.quests.append(quest)
        return self.ids[key]
    def save(self):
        put(CHAPTERS / (self.hid(0).lower() + '.snbt'), {'id': self.hid(0), 'filename': self.hid(0).lower(),
            'title': self.title, 'icon': {'id': self.icon, 'count': 1}, 'order_index': self.order,
            'default_quest_shape': 'circle', 'images': getattr(self, 'images', []), 'quests': self.quests})

c = Chapter(0x5E20000000000000, 'Chapter 2: The Create Revolution', 'create:water_wheel', 10)
c.q('welcome', 'A Revolution in Motion', deps=['5E10000000000023'], text=[
    'Your wooden drill turns composted leaves into an industrial supply chain. Now replace hand power with Create, press iron plates and build the Iron Drill.',
    'Item tasks detect your inventory without consuming it. Checkmarks are practical milestones to confirm after building.',
    'Use EMI for recipes and Revo multiblock layers. Hold the Ponder key over Create blocks for animated instructions.'])
c.q('iron', 'Iron From the Void', 'minecraft:iron_ingot', 16, ['welcome'], 2, 0, [
    'The wooden drill gives one Raw Iron in every five-item sequence. Smelt it in your furnace.',
    'Save the cobble, gravel and sand too. Leave the drill turning while you build the workshop.'])
c.q('andesite', 'Stone for the Workshop', 'minecraft:andesite', 4, ['iron'], 4, 0, [
    'Craft two cobblestone and two gravel in an alternating 2 x 2 square into two Andesite.',
    'Both ingredients come from your wooden drill or hammer. No quartz or trip to the Nether is needed.'])
c.q('alloy', 'Andesite Alloy', 'create:andesite_alloy', 8, ['andesite'], 6, 0, [
    'Craft Andesite Alloy with andesite and iron nuggets. Later the Mechanical Mixer makes this recipe more efficient.',
    'Alloy is the foundation for Create casings and mechanisms.'])
c.q('casing', 'Wrap It in Alloy', 'create:andesite_casing', 4, ['alloy'], 8, 0, [
    'Strip a log, then apply Andesite Alloy to it. Check the casing recipe and Ponder if needed.'])
c.q('shafts', 'Move the Motion', 'create:shaft', 8, ['casing'], 10, 0, [
    'Extend shaft lines from the wheels to machines. The pack starter recipe still works.',
    'Shafts carry rotational speed and stress capacity. They do not transmit FE or MI electricity.'])
c.q('cogs', 'Around the Corner', 'create:cogwheel', 4, ['shafts'], 12, 0, [
    'Small cogs transfer rotation between adjacent cogwheels. Ponder shows which faces mesh.'])
c.q('largecogs', 'A Change of Gear', 'create:large_cogwheel', 2, ['cogs'], 14, 0, [
    'A powered large cog driving a small cog doubles RPM. Driving a large cog with a small cog halves it.',
    'Use multiple stages when needed. Higher RPM increases stress demand; it does not create more capacity.'])
c.q('wheels', 'Let Water Do the Work', 'create:water_wheel', 2, ['largecogs'], 16, 0, [
    'Build two Water Wheels and supply flowing water as shown in Ponder. Align connected wheel shafts so their rotation agrees.',
    'Connect more wheels if your workshop becomes overstressed. Leave room to extend the power line.'])
c.q('gearbox', 'A Tidy Power Line', 'create:gearbox', 1, ['wheels'], 18, 0, [
    'A Gearbox changes the direction of a shaft line. It does not increase RPM.',
    'Route a branch to the wooden drill input, keeping the hatch face clear for a chest.'])
c.q('continuous', 'Retire the Hand Crank', deps=['gearbox'], x=20, y=0, text=[
    'Power the wooden drill from your water wheels. Open the controller and check it reports Working with a non-zero RPM.',
    'The output hatch auto-ejects at cycle end into an inventory against its outward face. Confirm once it runs unattended.'])
c.q('press', 'The First Press', 'create:mechanical_press', 1, ['continuous'], 22, 0, [
    'Craft a Mechanical Press using an iron block, Andesite Casing and a Shaft. Give it rotational power.'])
c.q('depot', 'A Place to Press', 'create:depot', 1, ['press'], 24, 0, [
    'Put a Depot beneath the press with the working space shown by Ponder. Place ingots on the depot.'])
c.q('sheets', 'Hammered Into Shape', 'create:iron_sheet', 32, ['depot'], 26, 0, [
    'Press iron ingots into Iron Sheets. Revo iron-tier recipes accept the shared iron plate tag, including these sheets.',
    'This is a first batch; the full drill consumes more plates. Keep pressing while you prepare its parts.'])
c.q('cores', 'A Stronger Cutting Core', 'revoextraction:iron_drill_core', 2, ['sheets'], 26, 3, [
    'Surround each Wooden Drill Core with four iron ingots and four iron plates.',
    'Craft TWO Iron Drill Cores: one is consumed by the controller recipe and the other goes in the centre of the structure.'])
c.q('supports', 'An Iron Skeleton', 'revoextraction:iron_drill_support', 12, ['cores'], 28, 3, [
    'Upgrade Wooden Drill Supports using iron plates. The structure needs twelve iron supports: all four corners on each of three layers.',
    'Empty and dismantle the wooden drill before reusing its parts, or build the iron drill beside it. Wooden and iron parts cannot be mixed.'])
c.q('controller', 'The Next Machine', 'revoextraction:iron_drill_controller', 1, ['cores'], 26, 6, [
    'Craft the Iron Drill Controller with eight Iron Drill Casings around an Iron Drill Core.',
    'Its panel reports RPM, cycle progress and structure problems. Items are managed in the hatch interface.'])
c.q('parts', 'Finish the Iron Frame', deps=['controller'], x=28, y=6, items=[
    ('revoextraction:iron_drill_casing', 10), ('revoextraction:iron_drill_input', 1),
    ('revoextraction:iron_drill_output_hatch', 1), ('revoextraction:iron_drill_core', 1)], text=[
    'Prepare ten placed casings, the kinetic input, the output hatch and the remaining core. The controller already consumed eight additional casings.',
    'Upgrade each wooden input/hatch with two iron plates. Open the controller assembly category in EMI for all three layers.'])
c.q('speed', 'Thirty-Two and Turning', deps=['parts','largecogs','wheels'], x=30, y=6, text=[
    'Gear the iron drill input to AT LEAST 32 RPM. Its controller shows Too slow below that speed.',
    'Stress impact is 8 SU per RPM: 256 SU at 32 RPM, 512 SU at 64 RPM. Leave capacity for other machines or give the drill its own wheels.',
    'Confirm after your shaft line reaches the required speed without becoming overstressed.'])
c.q('assembly', 'Iron Into Industry', deps=['speed','supports'], x=32, y=6, text=[
    'Build the 3 x 3 x 3 Iron Drill using its interactive EMI construction guide. Keep the bottom centre empty.',
    'Controller at front, shaft input on its right at the middle block height, output hatch low on its left. Ports must face outward.',
    'It uses 600 rotational work per item, pauses when full and resumes without losing the next output. Confirm once the rotating drill is producing.'])
c.q('metals', 'The Bronze Ingredients', deps=['assembly'], x=34, y=6, items=[
    ('minecraft:raw_copper', 3), ('modern_industrialization:raw_tin', 1), ('minecraft:coal', 1)], text=[
    'The iron drill has a repeating twelve-item sequence: three raw iron, two raw copper, two raw tin, two coal, one raw zinc, one raw gold and one redstone.',
    'Copper and tin lead into MI bronze. Coal fuels the first boiler. The next chapter begins here.'])
c.q('storage', 'Somewhere for Everything', deps=['continuous'], x=20, y=-3, items=[('minecraft:chest', 2), ('minecraft:hopper', 1)], text=[
    'Put a chest directly against the hatch output face for automatic ejection after each cycle.',
    'A hopper or item pipe can also extract from the hatch. The controller has no inventory slots.',
    'Storage is a useful side branch; it does not gate the steam chapter.'])
c.q('gold', 'A Little Gold on the Side', 'minecraft:gold_ingot', 3, ['metals'], 34, 9, [
    'Smelt the iron drill raw gold. Save zinc and redstone for later chapters; brass is not needed to start MI steam.'])
c.q('goldsheet', 'Sheets of Gold', 'create:golden_sheet', 3, ['gold'], 32, 9, [
    'Press gold ingots into Golden Sheets for the normal Create wrench recipe.'])
c.q('wrench', 'The Workshop Wrench', 'create:wrench', 1, ['goldsheet'], 30, 9, [
    'Craft the Create Wrench. It helps rearrange kinetic machines and rotate supported Revo machines.',
    'The MI steam machines also have their own wrench tools; check EMI and the MI manual for their controls.'])
# Appended nodes preserve every existing quest and task id.
c.q('manual', 'A Hand-Powered Workshop', deps=['gearbox','largecogs'], text=[
    'A hand crank turns at 32 RPM and has 256 SU of capacity at the default Create settings in this pack.',
    'Use it for one press or one iron drill at a time. Disconnect other machines while commissioning; a gearbox routes rotation without reducing load.',
    'Confirm after planning a dedicated crank line or a water-powered workshop branch.'])
c.q('torque', 'Slow Down a Workshop Branch', deps=['manual'], text=[
    'Optional gearing lesson: crank (32 RPM) -> small cog -> large cog -> press or wooden drill (16 RPM).',
    'Reduction lowers the downstream stress demand. A press or wooden drill can run slowly, which leaves capacity for the rest of a small manual network.',
    'The iron drill requires 32 RPM. Return to a direct 1:1 crank line for it; do not use the reduced 16 RPM branch. Confirm after trying a reduction gear.'])
c.q('water_iron', 'Retire the Crank', deps=['assembly','continuous'], text=[
    'Move the iron drill onto the water-powered line. Check its controller reports Working at 32 RPM or faster.',
    'Use two large-to-small gear stages to turn a regular 8 RPM wheel line into 32 RPM, and supply enough stress capacity.',
    'Confirm after watching several production cycles without touching the hand crank.'])
c.q('auto_eject', 'An Unattended Ore Supply', deps=['storage','water_iron'], text=[
    'Place a chest directly against the hatch outward face, or extract through the hatch with your hopper/storage route.',
    'Watch the water-powered drill complete a cycle and push its output into storage. Full inventories should pause production safely.',
    'Confirm once items arrive automatically. Keep this line running while you begin MI bronze and steam.'])
c.images=create_chapter(c)
c.save()

s = Chapter(0x5E30000000000000, 'Chapter 3: MI - The Steam Age', 'modern_industrialization:bronze_boiler', 20)
s.q('welcome', 'From Rotation to Steam', deps=[c.ids['metals']], text=[
    'Keep Create running: it will make the first bronze needed to build Modern Industrialization steam machines.',
    'This chapter ends with a working bronze workshop. Electrical machines, steel and the diamond-gated macerator come later.',
    'The MI manual is freely available in Tips & Guides. EMI contains the actual component and processing recipes.'])
s.q('metals', 'Copper and Tin', deps=['welcome'], x=2, y=0, items=[('minecraft:copper_ingot', 8), ('modern_industrialization:tin_ingot', 4)], text=[
    'Smelt a few raw copper and raw tin into ingots. Copper will become gears, rods and rotors.',
    'Keep some RAW copper and tin aside for the first bronze dust.'])
s.q('mill', 'A Mill for the Metals', 'create:millstone', 1, ['metals'], 4, 0, [
    'Craft a Millstone and give it rotation. This pack adds milling recipes: one Raw Copper -> one Copper Dust; one Raw Tin -> one Tin Dust.',
    'The same mill turns a fired vanilla Brick into one MI Brick Dust for refractory clay. No crushing wheels are needed.'])
s.q('basin', 'A Bowl for Alloys', 'create:basin', 1, ['mill'], 6, 0, [
    'Place a Basin below the future Mechanical Mixer. Ponder shows the working distance.'])
s.q('mixer', 'Mix Before Steam', 'create:mechanical_mixer', 1, ['basin'], 8, 0, [
    'Craft the Mechanical Mixer and power it at 32 RPM or higher. Make its whisk using pressed iron sheets.',
    'Give this branch enough stress capacity alongside the drill. The bronze recipe is unheated, so no Blaze Burner is needed.'])
s.q('dusts', 'Three Copper, One Tin', deps=['mixer'], x=10, y=0, items=[
    ('modern_industrialization:copper_dust', 12), ('modern_industrialization:tin_dust', 4)], text=[
    'Mill twelve raw copper and four raw tin. Mix at a ratio of THREE Copper Dust to ONE Tin Dust.',
    'One unheated batch gives four Bronze Dust. These quests detect items without consuming them; you can then put the same dust in the basin.'])
s.q('bronzedust', 'The First Bronze Alloy', 'modern_industrialization:bronze_dust', 16, ['dusts'], 12, 0, [
    'Run the Create mixing recipe: 3 copper dust + 1 tin dust -> 4 bronze dust.',
    'This is the bridge into MI; its own bronze mixer can take over after steam is available.'])
s.q('bronze', 'Welcome to the Bronze Age', 'modern_industrialization:bronze_ingot', 16, ['bronzedust'], 14, 0, [
    'Smelt Bronze Dust into Bronze Ingots. Continue producing bronze; the complete workshop needs several batches.'])
s.q('forge', 'The Forge Hammer', 'modern_industrialization:forge_hammer', 1, ['bronze'], 16, 0, [
    'Craft the MI Forge Hammer block from iron ingots and heavy weighted pressure plates.',
    'Its interface offers manual material shaping before powered machines. Recipes without a hand tool are less efficient but need no extra hammer item.',
    'Use its recipe selector to make copper/bronze rods, rings, bolts and curved plates. This block is different from your stone hammer.'])
s.q('plates', 'Press the Bronze', 'modern_industrialization:bronze_plate', 16, ['forge'], 18, 0, [
    'The Create Mechanical Press turns one Bronze Ingot into one Bronze Plate. This keeps your original workshop useful.',
    'You can also use the native Forge Hammer recipes; check their ingredient counts in EMI.'])
s.q('curved', 'Bend the Metal', 'modern_industrialization:bronze_curved_plate', 6, ['plates'], 20, 0, [
    'Use the MI Forge Hammer to make Bronze Curved Plates. Its tool-free recipe uses two bronze ingots per curved plate.',
    'The six plates are the walls of the first fluid-pipe batch. A hand hammer can improve manual shaping later.'])
s.q('copperparts', 'Small Parts, Big Machines', deps=['forge'], x=18, y=3, items=[
    ('modern_industrialization:copper_rod', 4), ('modern_industrialization:copper_ring', 3), ('modern_industrialization:copper_bolt', 8)], text=[
    'Make copper rods, rings and bolts in the Forge Hammer. Press copper ingots into Create Copper Sheets for recipes accepting copper plates.',
    'Copper gears use plates, bolts and a ring. Rotor blades use curved copper plates and a copper rod. Use EMI to work backwards from each part.'])
s.q('gears', 'Copper Gears', 'modern_industrialization:copper_gear', 2, ['copperparts'], 20, 3, [
    'Craft copper gears for the compressor. Make more for the mixer and water pump later.'])
s.q('rotors', 'Blades and Rotors', 'modern_industrialization:copper_rotor', 2, ['copperparts'], 22, 3, [
    'Shape curved copper plates, craft blades, then assemble rotors with a ring and bolts.',
    'Two copper rotors go into each batch of sixteen basic MI fluid pipes.'])
s.q('pipes', 'A Path for the Steam', 'modern_industrialization:fluid_pipe', 16, ['curved','rotors'], 24, 3, [
    'Craft sixteen MI Fluid Pipes from six Bronze Curved Plates, two Copper Rotors and one Glass Pane.',
    'Craft the MI Wrench from six bronze plates. Right-click the centre of a pipe with it to force a connection to an empty machine; right-click connections to change them. Different pipe colours stay separate.',
    'These carry FLUIDS. Steam machines do not accept Create shafts or Forge Energy.'], items=[('modern_industrialization:fluid_pipe',16), ('modern_industrialization:wrench',1)])
s.q('brickdust', 'Refractory Ingredients', 'modern_industrialization:brick_dust', 8, ['mill','bronze'], 14, -3, [
    'Smelt clay balls into vanilla Bricks, then mill the bricks into Brick Dust.',
    'Keep unfired clay balls too. Your rain barrel and dust loop continue supplying all this clay.'])
s.q('fireclay', 'Clay That Takes the Heat', 'modern_industrialization:fire_clay_dust', 12, ['brickdust'], 16, -3, [
    'Craft two Brick Dust and two Clay Balls into three Fire Clay Dust. Four batches make twelve dust.'])
s.q('refractory', 'Fire the Boiler Lining', 'modern_industrialization:fire_clay_bricks', 3, ['fireclay'], 18, -3, [
    'Smelt the twelve Fire Clay Dust into twelve Fire Clay Bricks, then craft four bricks into each Fire Clay Bricks block.',
    'The boiler needs three of these blocks.'])
s.q('tank', 'A Bronze Tank', 'modern_industrialization:bronze_tank', 1, ['plates'], 20, -3, [
    'Craft the Bronze Tank with eight Bronze Plates around a glass block. It is a component in the boiler recipe.',
    'Craft another if you want a separate fluid buffer.'])
s.q('boiler', 'The First Boiler', 'modern_industrialization:bronze_boiler', 1, ['tank','refractory'], 22, -3, [
    'Assemble the Bronze Boiler from its tank, furnace, bronze plates and refractory base.',
    'Supply WATER and solid FUEL such as coal or charcoal. The boiler converts water into steam over time.',
    'Use the native MI interface and manual to identify input and output tanks. The Create crank is not a boiler input.'])
s.q('steam', 'Raise Steam', deps=['boiler','pipes'], x=26, y=0, text=[
    'Fill the boiler with water, add fuel and let it heat. Connect its steam output to a separate steam pipe network.',
    'Keep the water supply and steam line separate; set the fluid filters/connections using MI pipe controls if sharing pipe space.',
    'Confirm after steam is visibly leaving the boiler. Keep it supplied with water and fuel.'])
s.q('casing', 'A Casing for the Workshop', 'modern_industrialization:bronze_machine_casing', 1, ['plates','forge'], 24, y=6, text=[
    'Make a Bronze Gear using bronze plates, ring and bolts, then surround the gear with eight Bronze Plates.',
    'Each bronze processing machine consumes a casing. Keep your alloy line working.'])
s.q('compressor', 'Your First Steam Machine', 'modern_industrialization:bronze_compressor', 1, ['casing','gears','steam'], 28, 0, [
    'Craft the Bronze Compressor with a Forge Hammer, copper rods/gears, a Bronze Machine Casing and three MI Fluid Pipes.',
    'Connect steam to its fluid input and choose a valid processing recipe from EMI.'])
s.q('compressed', 'Let Steam Shape It', deps=['compressor'], x=30, y=0, text=[
    'Run a native compressor recipe using steam. Check EMI for the exact ingot, double-ingot or plate recipe supported by this version.',
    'Confirm after collecting the first result. You now have a working MI steam workshop.'])
s.q('mimixer', 'The Alloy Line Moves to MI', 'modern_industrialization:bronze_mixer', 1, ['compressed','rotors'], 30, 3, [
    'Build a Bronze Mixer, connect steam and move copper/tin dust mixing to MI when convenient.',
    'The Create mixer remains a valid backup and still makes andesite alloy and other Create recipes.'])
s.q('pump', 'Water on Tap', 'modern_industrialization:bronze_water_pump', 1, ['compressed','rotors'], 28, -3, [
    'Build and supply steam to an MI Bronze Water Pump. Surround it horizontally with water source blocks; more adjacent sources improve its output.',
    'Pipe that water into your boiler on a separate water network. Keep a starting water reserve so an empty system can be restarted.'])
s.q('cokeoven', 'A Foundation for Steel', 'modern_industrialization:coke_oven', 1, ['compressed'], 32, 0, [
    'Craft the Coke Oven controller from bricks around a furnace. This is a MULTIBLOCK: also prepare 21 Bricks blocks, one bronze item input hatch, one item output hatch and one fluid input hatch.',
    'Follow the 3D Coke Oven shape in the MI guidebook. Hold the MI Wrench to see missing blocks and structure errors. Hatches replace valid casing positions.',
    'The oven needs STEAM through the fluid input hatch and COAL through the item input. Take Coke from the item output. An optional fluid output hatch replaces another brick to collect creosote.'], items=[('modern_industrialization:coke_oven',1), ('minecraft:bricks',21), ('modern_industrialization:bronze_item_input_hatch',1), ('modern_industrialization:bronze_item_output_hatch',1), ('modern_industrialization:bronze_fluid_input_hatch',1)])
s.q('coke', 'Ready for the Next Revolution', 'modern_industrialization:coke', 1, ['cokeoven'], 34, 0, [
    'Finish the Coke Oven multiblock, supply steam and coal through its hatches, then collect Coke. Maintain your drill, bronze supply and working steam line.',
    'Mill Coke into Coke Dust in a Create Millstone. This closes the steel route before the Steel Drill supplies your first diamonds. Continue to Chapter 4 for the water-cooled Steel Drill and maintenance.'])
s.save()

tips = CHAPTERS / '5e1f000000000000.snbt'
data = json.loads(tips.read_text(encoding='utf-8-sig')); data['order_index'] = 90; put(tips, data)
put(ROOT / 'art/quests/create-steam-map.json', {'chapters': [
    {'id': chapter.hid(0), 'title': chapter.title, 'quests': chapter.ids, 'questCount': len(chapter.quests)} for chapter in [c, s]],
    'ironDrill': {'size': [3, 3, 3], 'minimumRpm': 32, 'stressPerRpm': 8, 'workPerItem': 600,
        'outputsPer12': {'minecraft:raw_iron': 3, 'minecraft:raw_copper': 2, 'modern_industrialization:raw_tin': 2,
            'minecraft:coal': 2, 'create:raw_zinc': 1, 'minecraft:raw_gold': 1, 'minecraft:redstone': 1}}})
print(f'Generated {len(c.quests)} Create quests + {len(s.quests)} MI steam quests; six iron parts and six bridge recipes.')

# The early coke bridge must remain available without a diamond-gated macerator.
put(ROOT / "revoextraction/src/main/resources/data/revoextraction/recipe/milling/coke_dust.json", {"type":"create:milling","ingredients":[{"item":"modern_industrialization:coke"}],"processing_time":100,"results":[{"id":"modern_industrialization:coke_dust","count":1}],"neoforge:conditions":[{"type":"neoforge:mod_loaded","modid":"modern_industrialization"}]})
