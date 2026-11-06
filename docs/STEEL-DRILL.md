# Water-cooled Steel Drill

The third drill tier is a 3 x 3 x 3 Create-powered multiblock. It adds water cooling,
repairable dry-running damage, and a new steel portal gantry. Wooden and Iron Drills
keep their existing recipes, models and behaviour.

## Building and running

Use the interactive Steel Drill construction recipe in EMI. Place 12 supports,
9 casings, one core, controller, shaft input, output hatch and water tank. Keep the
bottom centre empty. The tank replaces the left-middle casing above the output
hatch. Controller and shaft stay centred in the middle layer; the hatch reaches
the ground. Face the shaft and item hatch outward, as shown in EMI.

Supply at least **64 RPM** and **16 SU/RPM**: **1024 SU at 64 RPM**. Water wheels
need gearing and enough total generating capacity. One crank cannot power this
tier. Production uses 600 rotational work per item, capped at 256 work/tick.

The tank holds **8000 mB of water**, accepting buckets and fluid pipes on any
face, including before formation. Only water is accepted. Output is accessed
through the nine-slot output hatch and auto-ejects into an adjacent inventory at
cycle completion. Controller interfaces have no inventory and retain the small
black R in the lower right; output hatches have no R/footer. Jade shows compact
status, RPM, progress and water, with orientation corrections only in the controller.

The fixed saved 12-item sequence yields 3 raw iron, 1 raw copper, 1 raw tin,
2 coal, 1 raw zinc, 1 raw gold, 1 redstone, 1 lapis and 1 diamond. Optional-mod ores
fall back to raw iron if their item is unavailable. Blocked output cannot reroll
the sequence. The water-cooled drill makes the first renewable diamonds for MI
maceration without changing the Iron Drill's earlier output pool.

## Cooling and repair

Cooling consumes **100 mB per 600 rotational work**, distributed across working
ticks with a persistent fractional remainder. Water also pays for partial work;
an item boundary can leave cooling already spent on the next cycle. Empty or
insufficient coolant stops production immediately while the driven spindle
attempts to run. After **100 dry-running ticks (five seconds)** it seizes.

Stopping the shaft pauses damage. Successful cooling clears accumulated dry
damage before seizure. A missing/unloaded structure, insufficient RPM or full
output does not consume water or add damage. A seized drill preserves all output
and stays seized through reloads and structure repairs. Breaking/replacing its
controller also preserves seizure/dry damage in the dropped item's custom data.

To repair: rebuild the structure, **stop the shaft**, fill at least 100 mB water,
and right-click any machine part with a **Drill Repair Kit**. One kit is consumed
only after a successful repair (creative players retain the kit). The water is a
minimum starting reserve, not an extra repair charge. Repair restarts cycle work
without deleting output or rerolling the saved production sequence. The kit runs
before normal block interaction, so clicking a controller or hatch repairs it
instead of opening its screen. Kits are made with two steel plates, two leather
and a copper ingot.

## Tank and particles

The reservoir is a real windowed tank. Static cutout glass uses the original
Minecraft glass texture; fluid is rendered separately inside the tank using
**minecraft:block/water_still** from Minecraft's live block atlas. It inherits
vanilla animation, biome water tint and resource-pack replacements. Its height
follows the synchronized stored amount. The standalone unformed tank also shows
its real fluid level. Water is never an opaque painted panel or a duplicated
inventory. Breaking the tank loses its stored water, as with the other primitive
Revo vessels.

The server emits vanilla **DRIPPING_WATER** particles around the cutting head
only during cooled production. No cooling particles run while stopped, seized,
dry or output-blocked. Seizure produces a short smoke burst and a metallic sound.

## Recipes and quests

Supports/casings upgrade their iron counterparts with one steel plate; input and
hatch use two plates. A core needs four steel ingots, four plates and an iron core.
The controller consumes eight additional steel casings around a steel core. Craft
two cores. A tank needs four steel plates, three glass panes, a bucket and copper.
The complete drill therefore uses **45 steel plates + eight loose steel ingots**,
plus underlying iron parts and tank ingredients. A kit adds two plates. Create
pressing of steel ingots into MI steel plates supplements native MI recipes.

Chapter 4: Steel and Coolant adds ten quests after the existing Coke milestone.
It covers native MI steel, frame/core/controller/ports, water automation, repair,
64 RPM commissioning and the first diamond. Earlier quest and task IDs are kept.
The native MI guidebook and EMI remain authoritative for producing steel itself.

## Modeling contract

Sources: `art/blockbench/revoextraction/steel/*.bbmodel`. Native builder:
`art/blockbench/revoextraction/build_steel_drill.js`. These were built, painted,
animated, saved and exported through the desktop Blockbench MCP server. The
assembled source uses 84 cubes (including one editor-only water preview), with
three runtime model groups: body, drill_bit and drive_gear. Runtime fluid is custom
geometry, independent of the editor's first-frame water preview.

Physical size is 48 x 48 x 48 pixels. Full-machine sources divide coordinates by
three; runtime rendering restores that scale. Standalone components use 16 pixels
per block without division. Density stays one texel per physical pixel, using a
512-square atlas with padded, proportionate face patches. Petrol-green
paint, gray fabricated steel, dark cast metal and muted copper cooling lines use
clustered dithering and painted fasteners. Glass/water originals and Mojang
attribution are recorded under `steel/vanilla/manifest.json`.

The shaft endpoint is physical (48,24,24), flush with the block boundary. The
drill pivot is (24,18,24); the drive pivot is (46,24,24). The rigid cutting axle
reaches the final disc. The visible fluid interior spans (2.2,19.6,19.2) to
(10.8,32.8,28.8). Four-second native working animation previews bit Y rotation
and opposite drive X rotation; runtime follows actual Create RPM.

The original visual checks covered native model renders, cutting clearance and UVs. The resource-bridge update additionally passed six native server GameTests, including a formed Iron Drill producing Flux Dust, full-buffer pause and save/reload persistence. See EARLY-RESOURCE-BRIDGES.md.
