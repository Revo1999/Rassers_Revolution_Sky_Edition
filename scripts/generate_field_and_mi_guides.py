"""Build the optional field gear and MI progression quest chapters."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHAPTERS = ROOT / 'pack/overrides/config/ftbquests/quests/chapters'
MI = 'modern_industrialization:'


def chapter(prefix, title, icon, order, rows):
    def ident(number):
        return f'{prefix}00000000{number:04X}'

    quests = []
    for index, (name, item, x, y, dependencies, description, targets) in enumerate(rows, 1):
        tasks = []
        for task_index, target in enumerate(targets or [None], 1):
            task = {'id': f'{prefix}1{index:05X}{task_index:06X}'}
            if target:
                task.update(type='item', item={'id': target, 'count': 1}, count=1, consume_items=False)
            else:
                task.update(type='checkmark')
            tasks.append(task)
        quests.append(dict(id=ident(index), title=name, icon={'id': item, 'count': 1},
                           x=x, y=y, description=description,
                           shape='diamond' if prefix == '5E2F' else 'hexagon' if index in (1, 2, 3, 13, 23, 27) else 'circle',
                           dependencies=[ident(dep) if isinstance(dep, int) else dep for dep in dependencies],
                           tasks=tasks, rewards=[]))
        if prefix == '5E2F':
            quests[-1]['optional'] = True
    data = dict(id=ident(0), filename=ident(0).lower(), title=title, order_index=order,
                icon={'id': icon, 'count': 1}, default_quest_shape='circle', quests=quests)
    assert len(data['id']) == 16
    (CHAPTERS / (data['filename'] + '.snbt')).write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')


def gear(title, item, x, y, deps, *description, task=True):
    return title, item, x, y, deps, list(description), [item] if task else []


def mi(title, item, x, y, deps, *description, extra=(), checkmark=False):
    targets = [] if checkmark else [MI + item, *(MI + key for key in extra)]
    return title, MI + item, x, y, deps, list(description), targets


def main():
    shoes = 'revocultivation:white_running_shoes'
    chapter('5E2F', 'Field Guide: Wands, Jetpacks and Shoes', 'wands:stone_wand', 6, [
        gear('Pick Your Field Gear', 'wands:stone_wand', 0, 0, [],
             'Use wands to extend your island, shoes for everyday movement, and jetpacks for vertical work. This guide is optional.', task=False),
        gear('A Stone Wand', 'wands:stone_wand', 4, -6, [1],
             'Craft two sticks and one cobblestone diagonally. Carry the building blocks you plan to place. Aim at an existing block and check the placement preview before using the wand.'),
        gear('Build in Lines and Planes', 'wands:iron_wand', 8, -6, [2],
             'Open Controls and search for Building Wands. Find Mode, Orientation and Wand Menu. Try a small line or plane on a safe platform and use the preview to catch the wrong direction.',
             'Copper, iron and diamond wands use the same two-stick pattern with a different tip.', task=False),
        gear('Diamond Wand', 'wands:diamond_wand', 12, -6, [3],
             'Use a diamond and two sticks. Steel Drill Gem Sludge can supply the diamond through Create fan washing.'),
        gear('Netherite Wand', 'wands:netherite_wand', 16, -6, [4],
             'Smith the Diamond Wand with a Netherite Upgrade template and a Netherite Ingot. The Crystals and Distant Resources chapter explains the pack’s first template route.'),
        gear('Running Shoes Workshop', shoes, 4, -3, [1],
             'Follow Field Equipment: Running Shoes for cotton, latex, cured rubber, glue, laces and dyes. Make a white pair first and dye it in any of the 12 available colours.', task=False),
        gear('Wear the Shoes', shoes, 8, -3, [6],
             'Equip a pair in the boots slot for 25% more movement speed and immunity to fall damage. They help with hard landings. Build railings or carry a flight option around the void.', task=False),
        gear('Flight Controls and Fuel', 'ironjetpacks:strap', 4, 0, [1],
             'Set separate Engine and Hover keys for the jetpack you use. Building Wands and jetpacks can share default keys. Test take-off, hover and descent over a wide platform. Watch the fuel or FE readout before flying over the edge.', task=False),
        gear('Iron Jetpacks: Electric Flight', 'ironjetpacks:jetpack', 8, 0, [8],
             'Search Iron Jetpacks in EMI and choose a tier-specific recipe. Wood and stone are starter tiers. Make the strap, coils, matching energy cells, thrusters and capacitor shown for your tier.',
             'Charge the jetpack with FE. One option is a powered Flux Networks Controller with wireless armor charging enabled. Equip the pack, enable its engine and use jump to climb.'),
        gear('Upgrade Your Electric Jetpack', 'ironjetpacks:basic_coil', 12, 0, [9],
             'Check the tier shown on each component and the finished pack. Higher tiers use stronger materials and often draw more FE. Check the FE drain and throttle on the tooltip before committing to a long trip.', task=False),
        gear('MI Diesel Jetpack', MI + 'diesel_jetpack', 8, 3, [8],
             'Craft with two pumps, two motors, two aluminum rotors, an aluminum tank and an Analog Circuit. Use the item tooltip and MI controls to check the fuel and flight settings. Make diesel in the MI Mixer from heavy and light fuel.'),
        gear('Create Jetpack: Pressurized Air', 'create_jetpack:jetpack', 8, 6, [8],
             'Use Create Mechanical Crafters for the 5 by 4 recipe. It needs an Elytra, a Copper Backtank, Precision Mechanisms, chutes, brass plates and a shaft.',
             'Plan around the Elytra requirement. The gear chapter leaves this as an optional late route. Pressurize the pack through the Create backtank system and look up its own engine and hover keys.', task=False),
    ])

    chapter('5EF0', 'MI Guide: Circuits, Power and Advanced Machines', MI + 'analog_circuit', 49, [
        mi('LV: Analog Circuits', 'analog_circuit', 0, 0, [],
           'Start with the rubber-and-copper Analog Circuit Board, resistors, capacitors, copper wire and an inductor. A Basic Machine Hull uses this circuit, redstone batteries, steel casing and tin cable. The LV Factory chapter shows the bootstrap.'),
        mi('MV: Electronic Circuits', 'electronic_circuit', 4, 0, [1],
           'Budget four Analog Circuits, two diodes, two transistors and one Electronic Circuit Board per Electronic Circuit. The Advanced Hull uses this circuit, silicon batteries, an advanced casing and electrum cable. Chapters 6 and 7 cover boards and chemistry.'),
        mi('HV: Digital Circuits', 'digital_circuit', 8, 0, [2],
           'Make AND, OR and NOT gates and the Digital Circuit Board. Each Digital Circuit uses four Electronic Circuits. Use Digital Circuits, sodium batteries and aluminum cable for the Turbo Hull. Chapter 8 covers Kanthal coils and HV power.'),
        mi('Refine Titanium', 'titanium_plate', 12, -6, [3],
           'Recover titanium dust from bauxite in the Electrolyzer. Smelt titanium hot ingots in a Kanthal Electric Blast Furnace, then finish them in the Vacuum Freezer. Keep titanium for advanced casings and nuclear hardware.'),
        mi('Platinum Survey Feedstock', 'raw_platinum', 16, -6, [29],
           'The Electric or Digital Drill’s Platinum program produces raw platinum. Leach it with sulfuric acid and centrifuge the solution, then electrolyze the purified solution.'),
        mi('Recover Iridium', 'iridium_dust', 16, -3, [5],
           'Each 1,000 mB of platinum solution has a 5% chance of yielding one tiny Iridium Dust in the Centrifuge. Pack it into full dust for alloy and plates, and reserve some tiny dust for monocrystalline silicon.'),
        mi('Industrial Gases', 'liquid_air_bucket', 12, 3, [3],
           'Run a retained Air Intake in the Vacuum Freezer to produce liquid air. Centrifuge the liquid air for nitrogen, oxygen and argon. Reserve argon for wafers and coolant, and keep nitrogen for the Pressure Casting Plant.',
           extra=('air_intake',)),
        mi('Cadmium Cells', 'cadmium_battery', 16, 3, [29],
           'The Monazite program on the Electric or Digital Drill supplies the feedstock. Centrifuge nine Monazite Dust into three Cadmium Dust, three Neodymium Dust and three Yttrium Dust. Save neodymium for Collider targets and turn cadmium into batteries.'),
        mi('Vinyl Chloride to PVC', 'polyvinyl_chloride_bucket', 16, 6, [3],
           'React acetylene with hydrochloric acid. Polymerize the Vinyl Chloride in the Chemical Reactor. Use EMI to compare the lead and chromium catalyst recipes. Reserve 1,500 mB PVC per Processing Unit Board.'),
        mi('Silicon Wafers', 'silicon_wafer', 20, -3, [6, 7],
           'Blast-furnace silicon dust with argon and a tiny Iridium Dust input to grow Monocrystalline Silicon. Cut it with lubricant for wafers. This step has a separate recipe from ordinary silicon plates.'),
        mi('Memory and Logic Components', 'random_access_memory', 24, -3, [10],
           'Chemical-react wafers with antimony, aluminum, argon and styrene-butadiene rubber for RAM. MMUs use wafers, an emerald plate and platinum fine wire. ALUs use a diamond plate and logic gates.',
           extra=('memory_management_unit', 'arithmetic_logic_unit')),
        mi('Processing Unit Board', 'processing_unit_board', 20, 3, [8, 9, 5],
           'Pipe PVC into the native Assembler and supply a Digital Circuit Board, eight annealed copper cables, two Cadmium Batteries and four platinum plates.'),
        mi('EV: The Processing Unit', 'processing_unit', 28, 0, [11, 12],
           'Assemble four Digital Circuits, two RAM, one MMU, one ALU and one Processing Unit Board. This is the tier between Digital and Quantum. Keep Digital Circuits in production for both upgrades and existing machines.'),
        mi('Highly Advanced Hull', 'highly_advanced_machine_hull', 32, 0, [13, 4],
           'Use the Processing Unit, two Cadmium Batteries, the titanium-based Highly Advanced Casing and three annealed copper cables. Use EV-rated hatches and match the cable tier to the network.'),
        mi('Nuclear Reactor and Fuel', 'nuclear_reactor', 36, -6, [14, 29],
           'Use the Electric or Digital Drill Uranium program for feedstock. Follow the MI Guidebook for the reactor grid, fuel and cooling setup. Centrifuge a depleted Uranium Fuel Rod to recover tiny plutonium dust and uranium byproducts.',
           extra=('uranium_fuel_rod',)),
        mi('Plutonium Batteries', 'plutonium_battery', 40, -6, [15, 17],
           'Reserve recovered Plutonium for batteries and the implosion chain. Assemble batteries with battery-alloy parts and superconductor cable. Quantum boards and the final hull each consume batteries.'),
        mi('Superconducting Line', 'superconductor_cable', 36, 3, [14, 6],
           'Mix iridium, annealed copper, yttrium and neodymium dust. Blast-furnace the alloy, cool the hot ingots and make wire and cable. Keep the high-power network separate from earlier tiers.'),
        mi('Cryofluid and Cooling Cells', 'cooling_cell', 36, 6, [7, 14, 28],
           'Freeze argon and helium into Cryofluid. Feed Cryofluid, fluid pipes, small heat exchangers and tin plates into the Assembler for cooling cells. Keep a supply for the Collider too.'),
        mi('Hydrogen Isotopes', 'tritium_bucket', 36, 0, [14],
           'Electrolyze water for hydrogen. Centrifuge each 1,000 mB hydrogen for 20 mB deuterium and 1 mB tritium. Bank the deuterium for fusion and feed the tritium to the qubit line. Check off this step once both outputs are flowing.',
           checkmark=True),
        mi('Helium-3 Separation', 'helium_3_bucket', 40, 6, [28],
           'Centrifuge 1,000 mB helium for 5 mB Helium-3. Reserve 50 mB per Quantum Circuit Board. Check off this step once you have a board-sized batch in a tank.',
           checkmark=True),
        mi('Qubits', 'qubit', 40, 3, [19, 18, 17],
           'Supply ALUs, carbon plates, glass panes and superconductor wire. The native Assembler also needs Cryofluid and tritium.'),
        mi('Quantum Circuit Board', 'quantum_circuit_board', 44, -6, [16, 20, 12],
           'Assemble the Processing Unit Board with twelve superconductor cables, two Plutonium Batteries, six iridium plates and 50 mB Helium-3.'),
        mi('Quantum Circuit', 'quantum_circuit', 48, 0, [22, 21, 18],
           'Combine four Processing Units, two qubits, two Cooling Cells and one Quantum Circuit Board. The Nexus controllers and plating line use Quantum Circuits, so plan a repeatable supply.'),
        mi('Quantum Machine Hull', 'quantum_machine_hull', 52, 0, [23, 16],
           'Build a Quantum Casing from a Highly Advanced Hull with iridium plates. Finish the hull with a Quantum Circuit, two Plutonium Batteries and three superconductor cables.'),
        mi('Fusion and Helium Plasma', 'fusion_reactor', 56, -3, [24, 19],
           'Build the native Fusion Reactor with Fusion Chambers, a Quantum Hull and Quantum Circuits. A deuterium-only run can produce Helium Plasma and more tritium. Pipe plasma to the Nexus Particle Collider.',
           extra=('helium_plasma_bucket',)),
        mi('Implosion and UU Matter', 'singularity', 56, 3, [15, 14, 6],
           'Use the MI Implosion Compressor with an Ultradense Metal Ball and 64 native MI Nukes per Singularity. A retained Singularity is also the Electrolyzer catalyst for UU Matter. Keep six available for the Nexus Ignition Cells.',
           extra=('implosion_compressor', 'uu_matter_bucket')),
        mi('Ready for the Nexus Line', 'superconductor_energy_input_hatch', 60, 0, [24, 25, 26],
           'Use Chapter 9 for the Pressure Casting Plant, Electroplating Line, Particle Collider, Nexus Core and Builder. Bring high-tier MI hatches and enough EU for the machinery. The Core and powered portal are the final project.'),
        mi('Shale Oil and Helium', 'oil_drilling_rig', 24, 6, [3],
           'Run the MI Oil Drilling Rig with a Stainless Steel Drill to extract shale oil. A Distillery can take off helium alone, or a Distillation Tower can also recover sulfuric crude oil and sulfuric naphtha. Save helium for Cryofluid and Helium-3.',
           extra=('helium_bucket',)),
        gear('Nuclear Survey Core', 'revoextraction:nuclear_survey_core', 12, -3, [4],
             'The Electric and Digital Drills use this retained core for Uranium, Platinum, Deep Debris and Monazite. Follow Crystals and Distant Resources for core parts and use the Wither Hatchery route for the Nether Star. Keep the Tungsten Survey Core for the Tungsten program.'),
    ])


if __name__ == '__main__':
    main()
