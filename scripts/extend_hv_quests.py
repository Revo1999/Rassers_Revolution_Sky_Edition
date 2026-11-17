"""Guide existing post-MV refining, heating coils, digital logic and first HV."""
from extend_quest_book import Chapter,mid,ident,MV,ROOT,CHAPTERS
import argparse
HV=0x5EB0000000000000

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--render',action='store_true');args=parser.parse_args()
    c=Chapter(HV,'Chapter 7: HV - Refining, Coils and Logic',mid('hv_steam_turbine'),48)
    for n,title,x,y,w in [(1,'REFINE THE DRILL FEEDSTOCKS',9,-2,21),(2,'FREEZING AND HEATING COILS',9,10,21),(3,'DIGITAL LOGIC',9,18,15),(4,'FIRST HV NETWORK',9,26,21)]:c.section(n,title,x,y,w)
    def q(n,title,item,x,y,deps,text,items=(),milestone=False):
        c.add(n,title,mid(item),x,y,deps,text,[(mid(i),count) for i,count in items],milestone=milestone)
    q(1,'Refine Before You Expand','centrifuge',0,0,[ident(MV,25)],[
        'Continue from the integrated MV factory. Keep drills, sludge washing, aluminum/silicon, electronic circuits, gases and plastic running.',
        'Chromium and manganese have native refining routes from current drill products. Check this when the existing factory can support another processing branch. No quarry or new loose-gem source is required.'
    ],milestone=True)
    q(2,'Redstone Refining','ruby_dust',3,0,[1],[
        'Centrifuge ten Redstone into five Iron Dust, one Ruby Dust, one Quartz Dust and a chance of Neodymium Tiny Dust. Collect all outputs.',
        'Ruby Dust here is a chemical intermediate from drilled redstone, not a loose Ruby gemstone. Gather six dust for the next refining step.'
    ],[('ruby_dust',6)])
    q(3,'Prepare Chromium Solution','chromium_crushed_dust',6,0,[2],[
        'Centrifuge six Ruby Dust into one Chromium Crushed Dust and two Aluminum Dust. Dissolve the crushed chromium in 9,000 mB Hydrochloric Acid in a Chemical Reactor.',
        'Check this after collecting chromium hydrochloric solution. Keep its fluid route separate from other acid solutions.'
    ])
    q(4,'Recover Chromium Dust','chromium_dust',9,0,[3],[
        'Electrolyze each 1,000 mB Chromium Hydrochloric Solution into three Chromium Tiny Dust, plus hydrogen/chlorine outputs. Pack nine tiny dust into one dust.',
        'Keep Chromium Dust for stainless steel and Kanthal. A raw chromium drill deposit is unnecessary for this route.'
    ],[('chromium_dust',2)])
    q(5,'Refine the Raw Iron','manganese_crushed_dust',3,4,[1],[
        'Centrifuge six Raw Iron from the drill line into eight Iron Dust and one Manganese Crushed Dust.',
        'Reserve raw iron for this branch rather than immediately smelting every item. Manganese is already a native refining byproduct.'
    ],[('manganese_crushed_dust',1)])
    q(6,'Sulfuric Leaching','chemical_reactor',6,4,[5],[
        'Produce Sulfuric Acid from native sulfur, oxygen and water processing, or collect it from ethanol-to-ethylene processing.',
        'Dissolve one Manganese Crushed Dust in 9,000 mB Sulfuric Acid. Check this after collecting manganese sulfuric solution.'
    ])
    q(7,'Recover Manganese Dust','manganese_dust',9,4,[6],[
        'Electrolyze each 1,000 mB manganese solution into three Manganese Tiny Dust and 900 mB recovered Sulfuric Acid.',
        'Pack nine tiny dust into one Manganese Dust. Route the acid back to a useful, separate storage destination.'
    ],[('manganese_dust',1)])
    q(8,'Freezer Before Hot Metal','vacuum_freezer',0,8,[1],[
        'Craft the native Vacuum Freezer controller with three Large Pumps, three Electronic Circuits, an Advanced Machine Hull and two Aluminum Large Plates.',
        'It needs neither stainless steel nor Kanthal to bootstrap. Assemble the native blueprint and configure hatches before accumulating hot ingots.'
    ],[('vacuum_freezer',1)],True)
    q(9,'Blend Stainless Steel','stainless_steel_dust',12,4,[4,7],[
        'Mixer: six Iron Dust, one Chromium Dust, one Nickel Dust and one Manganese Dust make nine Stainless Steel Dust.',
        'Keep these feedstocks renewable through drilling and native refining. There is no new high-tier ore reward.'
    ],[('stainless_steel_dust',9)])
    q(10,'Hot Stainless Steel','stainless_steel_hot_ingot',15,4,[9,8],[
        'The native 32 EU/t EBF recipe turns Stainless Steel Dust into hot ingots. The starter coil setup can run this recipe with a suitable power supply.',
        'Hot ingots still need the Vacuum Freezer before they become finished fabrication material.'
    ],[('stainless_steel_hot_ingot',1)])
    q(11,'Finish the Stainless Steel','stainless_steel_ingot',18,4,[10],[
        'Vacuum Freezer: one hot stainless ingot becomes one finished ingot at 32 EU/t for 250 ticks.',
        'Expand this line for the plates, rods, gears and rotors used in the next hull and mechanical components.'
    ],[('stainless_steel_ingot',8)],True)
    q(12,'Prepare Kanthal','kanthal_dust',3,12,[11],[
        'Mixer: one Stainless Steel Dust, one Chromium Dust and one Aluminum Dust make three Kanthal Dust.',
        'Retain stainless blend as dust instead of blasting every batch immediately.'
    ],[('kanthal_dust',3)])
    q(13,'First Kanthal Ingots','kanthal_ingot',6,12,[12],[
        'Run Kanthal Dust through its native 32 EU/t EBF recipe, then cool the hot ingots in the Vacuum Freezer.',
        'This makes the first Kanthal before Kanthal coils exist. Never make the upgraded coil a prerequisite for its own starter alloy.'
    ],[('kanthal_ingot',1)])
    q(14,'Upgrade the Heating Coils','kanthal_coil',9,12,[13],[
        'Process Kanthal into wire and cable. Eight Kanthal Cables craft one coil; make the complete supported set from the MI Guidebook.',
        'Upgrade the EBF coil structure consistently. The coils allow higher-demand recipes but do not supply their energy or fluids.'
    ],[('kanthal_coil',1)],True)
    q(15,'A Higher-Demand EBF Cycle','electric_blast_furnace',12,12,[14],[
        'Prepare a sustained 64 EU/t EBF supply and an oxygen fluid input for annealed copper. Check coil compatibility and the formed structure.',
        'Check this after a compatible cycle can run. This is your commissioning confirmation, not automatic detection of power or coils.'
    ])
    q(16,'Annealed Copper Wire','annealed_copper_wire',15,12,[15],[
        'EBF: one Copper Dust and 250 mB Oxygen make a hot Annealed Copper Ingot at 64 EU/t for 600 ticks. Cool it with the native freezer route.',
        'Make plates and wire from the finished ingot. Advanced Motors require annealed copper wire; ordinary copper is not interchangeable.'
    ],[('annealed_copper_wire',4)])
    q(17,'Resistors and Op-Amps','op_amp',3,20,[11],[
        'Craft Resistors from paper, Copper Fine Wire and Coal Dust, or use native bulk assembly. Craft Op-Amps from Resistors, Transistors and a Diode.',
        'Native manual component recipes bootstrap the first logic gates. Monocrystalline silicon and an iridium starter are not required here.'
    ],[('resistor',4),('op_amp',1)])
    q(18,'AND, OR and NOT','and_gate',6,20,[17],[
        'Use native gate recipes with Op-Amps, Resistors, Diodes and Aluminum Wire. Prepare one AND Gate, one OR Gate and two NOT Gates.',
        'Keep the earlier component lines running: digital logic consumes them rather than replacing them.'
    ],[('and_gate',1),('or_gate',1),('not_gate',2)])
    q(19,'Digital Circuit Board','digital_circuit_board',9,20,[11],[
        'Assembler: six Aluminum Cables, one Silicon Battery, one Electronic Circuit Board, four Stainless Steel Plates and 750 mB Polyethylene make one board.',
        'Keep the fluid-fed Assembler supplied by the renewable plastic line.'
    ],[('digital_circuit_board',1)])
    q(20,'Your First Digital Circuit','digital_circuit',12,20,[18,19],[
        'Combine one OR Gate, two NOT Gates, one AND Gate, four Electronic Circuits and one Digital Circuit Board with the native crafting recipe.',
        'This does not automatically unlock AE2. Its starter presses, Sky Stone and pack-specific technology gates remain separate work.'
    ],[('digital_circuit',1)],True)
    q(21,'Sodium Batteries','sodium_battery',0,28,[1],[
        'Salt electrolysis supplies Sodium Dust alongside chlorine. Craft Sodium Batteries with sodium, battery-alloy plates/curved plates and Aluminum Cable.',
        'Make two for the first Turbo Machine Hull. Keep drilling lead and antimony for the battery-alloy shell.'
    ],[('sodium_battery',2)])
    q(22,'The Turbo Machine Hull','turbo_machine_hull',3,28,[21,20],[
        'Eight Stainless Steel Plates around an Advanced Machine Hull make the Turbo Machine Casing. Add two Sodium Batteries, a Digital Circuit and three Aluminum Cables for the hull.',
        'Turbo is the first HV hull in this MI version. The titanium-based Highly Advanced Machine Hull belongs later and must not block this bootstrap.'
    ],[('turbo_machine_hull',1)],True)
    q(23,'Advanced Motors','advanced_motor',6,28,[16],[
        'Craft Advanced Motors with native Stainless Steel Rods, a Magnetic Stainless Steel Rod, Annealed Copper Wire and Aluminum Cable.',
        'Make two for the HV Steam Turbine, and prepare its stainless rotors and separate Aluminum Cable supply.'
    ],[('advanced_motor',2)])
    q(24,'HV Steam Generation','hv_steam_turbine',9,28,[22,23],[
        'Use the native recipe: Turbo Machine Hull, Digital Circuit, two Advanced Motors, two Stainless Steel Rotors, pipes and Aluminum Cable.',
        'Expand steam delivery, fuel and boiler water. A higher-tier turbine cannot reach sustained output from an undersized steam supply.'
    ],[('hv_steam_turbine',1)],True)
    q(25,'Protect Earlier Networks','hv_mv_transformer',12,28,[24],[
        'Use suitable Aluminum Cable on HV and an HV to MV Transformer for the old factory. Retain the separate MV-to-LV supply.',
        'HV storage is an optional sodium-battery/Turbo-Hull buffer. Transformers and buffers do not create energy.'
    ],[('hv_mv_transformer',1)])
    q(26,'A Working HV Factory','hv_steam_turbine',15,28,[25],[
        'Check this after sustained HV generation runs a real load while the earlier MV/LV lines remain operational. Confirm stainless, annealed wire and digital circuits can replenish themselves.',
        'This is the first-HV milestone. Later mineral deposits, AE2 starter gates, nuclear materials and Nexus runtime implementation are documented separately in the progression-gap table.'
    ],milestone=True)
    c.save()
    if args.render:
        import render_quest_maps as maps
        maps.OUT=ROOT/'art/quests/hv';maps.OUT.mkdir(parents=True,exist_ok=True)
        maps.render(CHAPTERS/(ident(HV,0).lower()+'.snbt'),'hv-progression.png')
    print('Added 26 first-HV quests; no new resource or gameplay recipes.')
if __name__=='__main__':main()
