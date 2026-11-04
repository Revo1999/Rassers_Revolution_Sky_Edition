# Create, iron extraction and the first steam workshop

Chapter 2 has 29 quests. Chapter 3 has 28. The route is:

Wooden Drill -> smelted iron -> andesite alloy -> water wheels and gearing ->
Mechanical Press -> Iron Drill -> copper/tin dust -> Create bronze mixing ->
MI Bronze Boiler -> steam pipes -> Bronze Compressor -> Bronze Mixer / Water Pump.

The first chapter's IDs and completion state are preserved. The early quest maps
now have explicit icons, section banners and a hand-crank route into the iron drill;
water power and hatch automation form a dedicated branch. See QUEST-BOOK.md. The new chapters use
`5E20000000000000` and `5E30000000000000`, with canonical lowercase filenames.
Chapter 2 depends on the existing first-drill milestone. Chapter 3 depends on
collecting copper, tin and coal from the iron drill. Item tasks never consume the
items. Checkmarks confirm practical construction/operation. Tips remain freely
accessible, after the progression chapters. The Handbook's existing rewards remain.

## Renewable recipe bridges

| Process | Input | Output |
| --- | --- | --- |
| Crafting, alternating 2 x 2 | 2 cobblestone + 2 gravel | 2 andesite |
| Create Millstone | 1 raw copper | 1 MI copper dust |
| Create Millstone | 1 MI raw tin | 1 MI tin dust |
| Create Millstone | 1 vanilla brick | 1 MI brick dust |
| Create Mixer, unheated | 3 copper dust + 1 tin dust | 4 MI bronze dust |
| Create Press | 1 bronze ingot | 1 MI bronze plate |

Milling takes 100 processing units; Create applies its normal speed behavior.
The mixer needs at least 32 RPM. The three dust recipes, bronze mixing and bronze
pressing load only when MI is installed. Native smelting turns bronze dust into
ingots. These recipes appear in EMI through Create's normal integration.

No quartz, brass, blaze burner or diamonds are required to reach the first boiler.
The native MI Bronze Macerator requires diamonds, so it is reserved for a later
chapter. The steam-powered Coke Oven multiblock closes this chapter with coke as a future
steel ingredient. Its material task includes the controller, 21 bricks blocks and
bronze item input, item output and fluid input hatches. Its fluid input takes steam;
an optional fluid output hatch collects creosote. The MI manual supplies its native
3D shape. The pipe quest also detects the MI Wrench needed to configure connections.
The current chapter does not claim to implement steel or electrical progression.

## Iron Drill

The iron machine has six registered, craftable parts, its own blockstates/loot,
editable Blockbench models, atlas and runtime model exports. It uses the exact
3 x 3 x 3 wooden blueprint, requiring exclusively iron-tier parts:

- 12 supports, 10 placed casings, 1 placed core, controller, kinetic input and hatch.
- The controller recipe consumes 8 additional casings and 1 additional core.
- Bottom centre stays air. Core is at the centre of the middle layer.
- The controller occupies the front middle block, on the middle layer.
- From the controller front, the shaft port is on the right, middle layer;
  output hatch is on the left, bottom layer. All ports face outward.

Each support/casing upgrades its wooden counterpart with 1 iron plate. Each
input/hatch uses its wooden counterpart plus 2 iron plates. Each core uses a
wooden core, 4 iron ingots and 4 iron plates. Shared plate tags accept Create sheets.
Empty the old hatch before dismantling the wooden machine to reuse its parts.

| Tier | Minimum speed | Stress impact | Work per item |
| --- | --- | --- | --- |
| Wooden | Any nonzero RPM | 4 SU/RPM | 1200 |
| Iron | 32 RPM | 8 SU/RPM | 600 |

At 32 RPM the iron input uses 256 SU and averages 18.75 ticks per item; individual
cycles round to server ticks with carried work. Work gain is capped at 256 RPM.
Slower input pauses work and reports **Too slow**. Production is kinetic only.

Its saved, repeating sequence is raw iron, raw copper, raw tin, coal, raw iron,
raw copper, coal, raw tin, raw iron, raw zinc, raw gold, redstone. Thus every twelve
items contain 3 iron, 2 copper, 2 tin, 2 coal and one each of zinc, gold, redstone.
Tin is MI's raw tin; zinc is Create's raw zinc. If MI is absent, tin entries fall
back to raw iron; no MI recipe bridge loads in that standalone configuration.

The shared controller retains RPM, cycle progress, orientation diagnostics and
the small black R. The output-only hatch has normal player inventory spacing
and no R footer. It auto-ejects through its front at cycle end. Full storage
pauses production without discarding or rerolling the next item. Jade remains
compact; facing correction details stay in the controller. EMI provides the
interactive layer-by-layer construction view and a tier/output information page.

## Art source and block heights

`art/blockbench/revoextraction/build_iron_tier.js` delegates to
`build_iron_plated.js` in native Blockbench via MCP, using scoped Undo and native
project/Java codecs. The pre-repaint iron sources are archived under
`variants/iron-before-plating` and retain the owner's shaft, foot and hatch geometry.
The current atlas is freshly painted sheet metal: light riveted iron panels,
grey-blue steel beams, steel skid plates, copper bearing accents and a cyan
controller display. There is no wood grain or masonry texture in the iron tier.
Plate seams, bolt heads, small wear marks and visible ordered dithering are painted
into shared regions at Minecraft pixel scale.

`iron_structure.js` performs the structural pass inside the same native Undo scope.
The iron assembly is now a steel drilling gantry, with 22.5-degree splayed lower
legs, I-section upper columns, 45-degree knee braces, connected skid rails, a stepped
motor/transmission housing, a heavy spindle cradle and two guide rails. An exposed
riser and elbow carry the visual discharge path to the hatch. The enclosed shaft
input attaches through a transmission web and support seat. The controller mount
extends into the transmission case. The rotating cutting geometry is preserved. Its flights and central axle now use
a darker graphite steel palette, with lighter worn cutting edges. The frame and
bright spindle guide rails retain their separate grey-blue/silver materials.

Individual supports have actual I-sections with foot/head plates and web ribs.
Casings have raised edge rails and a vent cassette; the core uses heavy spindle
cheeks and a bearing bridge. The unassembled reference uses these same new part
shapes. Original wooden and earlier iron variants remain archived.

The eight `iron_drill_*.bbmodel` files include all six blocks plus assembled and
unassembled sources. The assembly is 84 cubes: 74 body, 8 rotating drill and 2
rotating input components (enclosed gear and exposed axle). The unassembled
reference has 206 cubes; individual support/casing/core sources use 9/6/12 cubes.
One 512 x 512 atlas serves the iron tier. Full-machine sources are one third of
world dimensions: one source unit covers three Minecraft pixels, with the runtime
renderer scaling the model by three. Individual parts use normal 0..16 block units.
Do not apply that scale twice to the individual part models.

The shaft centre is y=8 in the assembled source, corresponding to world y=24
pixels: the centre of block height 2. The drill axle reaches its lowest disc.
The kinetic input is now an enclosed gearbox inspired by Create's encased shafts.
The assembled box has a 14 x 14 pixel face with a thick, highlighted steel rim,
a recessed charcoal face and a solid 4 x 4 central axle. Its axle end remains
flush with the outer x=48 pixel face, centred at y=z=24. The axle and enclosed
gear share the runtime X-axis rotation; the gear's swept corners fit inside the
housing. The standalone input and unassembled reference model share the same
framed, recessed design. The input remains compatible with the existing shaft
connection and block orientation.

The controller spans source y=16/3..32/3, corresponding to the full middle block.
The drill rotates around source (8,6,8); the side gear around (46/3,8,8).
Runtime animation is driven by working RPM and stops with the machine.

## Build and checks

Generate data with `python scripts/generate_create_steam_progression.py`.
Build with `dev.cmd assemble collectMods :revolib:syncPackOverrides`, then package
with `python scripts/package_modpack.py`. The desktop dev launcher uses these
sources; a fresh launch copies the canonical quest overrides into the run folder.
Existing quest completion data is kept. In a running world, reload FTB Quests or
restart the development client to load the new definitions.

Only compilation, native model inspection and static artifact/quest checks were
performed for this release, at the owner's request. No client or GameTests were
launched. Play-test recipe pacing, Create stress capacity, steam pipe configuration
and the quest layout in game. Older verification reports describe earlier builds.
