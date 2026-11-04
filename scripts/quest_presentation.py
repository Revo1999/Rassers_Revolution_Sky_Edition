"""Early quest presentation, using the fields supported by the pinned FTB Quests build."""

def banner(base, n, title, x, y, width=12):
    return {'id': f'{base+0x7000+n:016X}', 'x': float(x), 'y': float(y),
        'width': float(width), 'height': .9, 'image': '#263843', 'title': title,
        'text_on_image': True, 'text_shadow': True, 'text_inset': 3,
        'position_locked': True, 'order': -10}

def apply(quests, ids, positions, icons, milestones=(), optional=()):
    by_id = {q['id']: q for q in quests}
    for key, ident in ids.items():
        q = by_id[ident]
        q['x'], q['y'] = map(float, positions[key])
        item = icons.get(key)
        if not item:
            item = next((t['item']['id'] for t in q['tasks'] if t['type']=='item'), None)
        assert item, key
        q['icon'] = {'id': item, 'count': 1}
        q['shape'] = 'hexagon' if key in milestones else 'diamond' if key in optional else 'circle'
        q['size'] = 1.25 if key in milestones else 1.0
        if key in optional: q['optional'] = True
    return {key: by_id[ident] for key, ident in ids.items()}

def first_chapter(quests, ids):
    positions = {
        'welcome':(0,0), 'station':(0,2.5), 'logs':(2.5,0), 'sticks':(5,0),
        'crook':(7.5,0), 'leaves':(10,0), 'sapling':(7.5,-2.5),
        'composter':(10,2.5), 'compost':(7.5,2.5), 'dirt':(5,2.5), 'woodhammer':(5,-2.5),
        'pebbles':(13,0), 'cobble':(15.5,0), 'stonehammer':(18,0),
        'gravel':(18,2.5), 'sand':(15.5,2.5), 'dust':(13,2.5), 'furnace':(20.5,5),
        'barrel':(0,7), 'clay':(2.5,7), 'clayballs':(5,7), 'rawcrucible':(7.5,7),
        'crucible':(10,7), 'heat':(10,9.5), 'lava':(10,12), 'bucket':(5,9.5),
        'water':(2.5,9.5), 'generator':(7.5,14.5),
        'shaft':(13,8), 'crank':(13,10.5), 'cores':(21,8), 'controller':(21,10.5),
        'supports':(23.5,13), 'parts':(18.5,13), 'drill':(21,15.5)
    }
    icons = {'welcome':'revoindustry:revolution_book', 'station':'craftingstation:crafting_station',
        'heat':'minecraft:torch', 'generator':'minecraft:cobblestone',
        'parts':'revoextraction:wooden_drill_casing', 'drill':'revoextraction:wooden_drill_controller'}
    q = apply(quests, ids, positions, icons, ['welcome','dirt','cobble','clay','crucible','controller','drill'])
    # Placing the furnace after the stone tool keeps the stone section's route clear.
    q['furnace']['dependencies'] = [ids['stonehammer']]
    q['supports']['dependencies'] = [ids['cores']]
    q['drill']['description'] += ['Next: open Chapter 2 to smelt your raw iron, build a Create workshop and upgrade to the Iron Drill.']
    return [banner(0x5E10000000000000,1,'01 / GROVE & COMPOST',5,-4.5),
        banner(0x5E10000000000000,2,'02 / SOIL INTO STONE',18,-4.5),
        banner(0x5E10000000000000,3,'03 / CLAY, WATER & FIRE',5,5.5),
        banner(0x5E10000000000000,4,'04 / YOUR FIRST DRILL',18,6.5)]

def create_chapter(chapter):
    ids, quests = chapter.ids, chapter.quests
    positions = {
        'welcome':(0,0), 'iron':(2.5,0), 'andesite':(5,0), 'alloy':(7.5,0),
        'casing':(7.5,2.5), 'shafts':(5,2.5), 'cogs':(2.5,2.5), 'largecogs':(0,2.5),
        'gearbox':(0,5), 'manual':(2.5,5), 'press':(5,5), 'depot':(7.5,5), 'sheets':(10,5),
        'cores':(13,5), 'supports':(15.5,2.5), 'controller':(15.5,5), 'parts':(18,5),
        'speed':(20.5,5), 'assembly':(23,5), 'metals':(23,8), 'storage':(20.5,8),
        'torque':(0,7.5), 'wheels':(0,12), 'continuous':(5,12),
        'water_iron':(13,12), 'auto_eject':(18,12),
        'gold':(23,16), 'goldsheet':(20.5,16), 'wrench':(18,16)
    }
    icons = {'welcome':'create:andesite_casing', 'manual':'create:hand_crank',
        'torque':'create:large_cogwheel', 'continuous':'create:water_wheel',
        'parts':'revoextraction:iron_drill_casing', 'speed':'revoextraction:iron_drill_input',
        'assembly':'revoextraction:iron_drill_controller', 'metals':'modern_industrialization:raw_tin',
        'storage':'minecraft:chest', 'water_iron':'create:water_wheel', 'auto_eject':'minecraft:hopper'}
    q = apply(quests, ids, positions, icons, ['welcome','sheets','controller','assembly','metals','auto_eject'],
        ['torque','gold','goldsheet','wrench'])
    def change(key, title=None, deps=None, text=None):
        if title: q[key]['title'] = title
        if deps is not None: q[key]['dependencies'] = [ids[d] for d in deps]
        if text is not None: q[key]['description'] = text
    change('welcome', text=[
        'Your wooden drill is the foundation. Smelt its raw iron, learn Create gearing and pressing, then craft the Iron Drill.',
        'Start by hand if you want: the crank can press your plates and commission one iron drill. The lower branch builds toward unattended water power and automatic output.',
        'Every icon represents the item or machine used in that step. Item tasks detect your inventory without consuming it; checkmarks confirm practical work.'])
    change('gearbox', deps=['shafts'], text=[
        'A Gearbox routes shafts around corners without changing RPM. Craft it for your workshop power line.',
        'Next, learn what the hand crank can support. You can build the first press before making water wheels.'])
    change('press', deps=['manual'], text=[
        'Craft a Mechanical Press with an iron block, Andesite Casing and a Shaft.',
        'Give it a dedicated hand crank or a workshop shaft line. Disconnect the drill while cranking the press if the network is overstressed.'])
    change('sheets', 'Plates for the Upgrade', text=[
        'Press a batch of 42 Iron Sheets. These are accepted as iron plates by every iron-drill upgrade recipe.',
        'Upgrade-only bill: 12 support plates + 18 casing plates + 8 core plates + 4 port plates = 42.',
        'Also reserve 8 loose iron ingots for the two cores, plus the ingredients for their wooden counterparts. The 18 casings include 8 consumed by the controller and 10 placed in the structure.'])
    q['sheets']['tasks'][0]['count'] = 42
    change('cores', text=[
        'Craft TWO Iron Drill Cores. Each uses one Wooden Drill Core, four iron ingots and four iron plates.',
        'One iron core is consumed by the controller; the second is placed in the centre of the middle layer. The dark graphite cutting head is already part of this tier.'])
    change('supports', 'Build the Steel Gantry', text=[
        'Upgrade twelve Wooden Drill Supports with one iron plate each. These form the twelve corner positions in the build guide.',
        'When formed, the iron machine becomes a steel gantry with angled legs, I-beams and skid rails. The blueprint still occupies 3 x 3 x 3 blocks.',
        'Empty the wooden hatch before dismantling and upgrading its parts, or keep the wooden drill and build this one beside it.'])
    change('parts', text=[
        'After crafting the controller, keep ten Iron Drill Casings, one core, one kinetic input and one output hatch for placement.',
        'Each support/casing upgrades its wooden counterpart with one iron plate. Each input/hatch upgrades its wooden counterpart with two iron plates.',
        'Use the Iron Drill construction category in EMI for the exact layers and port directions.'])
    change('speed', 'Choose Your Drive', deps=['parts'], text=[
        'The iron drill needs at least 32 RPM and applies 8 SU per RPM: 256 SU at 32 RPM.',
        'Manual commissioning: connect a hand crank directly, or through a 1:1 shaft/gearbox line. Its default 32 RPM and 256 SU support one iron drill with no other machine loads.',
        'Water route: gear a water-wheel line up to at least 32 RPM. Use enough wheels for the drill and any other connected machines.',
        'Gearing the crank DOWN to 16 RPM reduces load, but the iron drill will say Too slow. Gearing it UP to 64 RPM needs 512 SU, beyond one crank. Confirm once your chosen line is ready.'])
    change('assembly', 'Commission the Iron Drill', text=[
        'Build all three layers using the interactive Iron Drill guide in EMI. Keep the bottom centre empty.',
        'Controller at front, boxed shaft input on the right at the centre of block height 2, output hatch low on the left. Face every port outward.',
        'Run it at 32+ RPM with your crank or water line. Confirm after the graphite drill head rotates and the hatch receives an item.'])
    change('metals', text=[
        'Collect raw copper, raw tin and coal from the iron drill. Every twelve-item sequence contains 3 raw iron, 2 raw copper, 2 raw tin, 2 coal and one each of raw zinc, raw gold and redstone.',
        'These resources open MI Steam Age. You can reach them with the crank; finish the water-power and chest branch below for an unattended supply.'])
    change('wheels', deps=['manual'], text=[
        'Build two Water Wheels and supply flowing water as shown in Ponder. Connected generators must turn in compatible directions.',
        'A regular water wheel starts at 8 RPM. Two large-to-small 2:1 gear stages raise that to 32 RPM for the iron drill.',
        'Add wheels as the workshop grows. Gear ratios change speed and stress demand; they do not create extra capacity.'])
    change('continuous', 'Build the Water Power Line', deps=['wheels'], text=[
        'Run a shaft line from your water wheels through the workshop. Use gearboxes to route it and cog pairs to set each branch speed.',
        'Try it on the wooden drill or press first, then check the network keeps moving without holding the crank.',
        'Confirm after the water line runs a machine unattended.'])
    change('storage', 'Give the Hatch a Destination', deps=['assembly'], text=[
        'Prepare two chests and a hopper for your production area. One chest directly against the output hatch face is enough for the drill to auto-eject.',
        'Use the hopper for collection or a second storage stage. Items are accessed through the hatch, not the controller.',
        'The final automation check below asks you to watch the setup fill a chest without cranking.'])
    change('water_iron', deps=['assembly','continuous'])
    change('auto_eject', deps=['storage','water_iron'])
    return [banner(chapter.base,1,'01 / THE CREATE WORKSHOP',5,-2.5),
        banner(chapter.base,2,'02 / CRAFT THE IRON DRILL',18,-2.5),
        banner(chapter.base,3,'03 / WATER-POWERED WORKSHOP',5,10.3),
        banner(chapter.base,4,'04 / COLLECT & AUTOMATE',18,10.3),
        banner(chapter.base,5,'OPTIONAL / WORKSHOP TOOLS',20.5,14.3,9)]
