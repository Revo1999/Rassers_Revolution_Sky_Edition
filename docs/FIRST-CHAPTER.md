# First chapter: From Leaves to Machines

New Skyblock worlds start with one Revolution Book and a placed Crafting Station.
The book opens FTB Quests. Its art currently uses Minecraft's book as a placeholder
for the owner's replacement texture. Existing saves and player inventories are
preserved; the island changes apply to newly generated islands.

## Renewable opening

1. Make a crook from four sticks. Breaking leaves with it also collects leaves and
   regularly supplies matching saplings. Replant before using the last sapling.
2. Craft the all-wood Composter. Four organic items become compost in eight seconds;
   compost crafts into dirt. It runs on elapsed time without rotational power.
3. Make a Wooden Hammer before obtaining stone. Hammering dirt produces four
   Stone Pebbles; a 2 by 2 recipe makes one cobblestone. Upgrade to a Stone Hammer.
4. Either mine or right-click with a hammer: cobblestone becomes gravel, gravel
   becomes sand, and sand becomes dust. Each conversion costs one durability.
5. Put a Rain Barrel outside. Rain supplies 5 mB per tick, filling its 1,000 mB
   capacity in ten seconds. A roof blocks collection. Completing the first barrel
   quest starts a five-minute shower through an automatic FTB command reward,
   including for players without operator permission.
6. One dust and a full barrel produce one clay block. Craft that into four clay
   balls. The output slot must have room before water or dust is consumed.
7. Craft unfired clay buckets and an unfired crucible, then fire them in a furnace.
   Put a torch below the fired crucible and feed it cobblestone. Four cobblestone
   make one bucket of lava in forty seconds with a torch. Lit campfires double
   that rate; fire triples it and lava quadruples it. An unlit campfire gives no heat.
8. Collect water with a clay bucket and establish a cobblestone generator. Clay
   buckets survive water use; pouring lava consumes the clay bucket.
9. Craft the first wooden drill and a hand crank. Starter recipes for Create shafts
   and the crank avoid requiring iron or andesite alloy before the drill exists.

The drill occupies 3 by 3 by 3 blocks: twelve supports, ten placed casings,
one core, controller, kinetic input and output hatch, with the bottom centre empty.
Craft two cores in total because the controller recipe uses one. EMI contains the
interactive construction guide. The controller reports operation and progress;
items are collected through the hatch.

## Quest and manual layout

The first chapter has 35 quests with non-consuming item detection. A separate Tips
chapter has six unrestricted entries. It supplies replacement Revolution Books,
Modern Industrialization's guidebook, AE2's guide and the FTB Quests book. AE2
addon documentation lives inside the AE2 guide. Create uses its native Ponder
system; the remaining installed building and utility mods use EMI and tooltips.
No unlock dependencies are attached to Tips. The existing five cosmetic quests
remain in the Revo Employee Handbook with their original IDs.

Canonical chapter filenames are their chapter IDs: `5e10000000000000.snbt`,
`5e1f000000000000.snbt` and `5e70000000000000.snbt`. Do not distribute duplicate
copies of the Handbook under different filenames. The one-time development cleanup
archives exact duplicates under `.local/quest-backups`; it preserves player progress.

## Vessels, models and interfaces

Both vessels are single-block passive machines. Hand feeding, two-slot menus,
fluid capabilities and sided item automation access the same saved contents.
The Rain Barrel exports clay through its front. GUI inventories use the shared
176 by 184 layout, player rows at y=102 and hotbar at y=160. The small black Revo
R is at (158,82). Neither vessel has RPM input.

Editable native Blockbench sources are in `art/blockbench/chapter-one`.
The barrel uses five cubes, and each crucible uses nine. The barrel interior floor
is y=2; crucible floor y=5; contents rise to y=14 within the 0..16 block envelope.
Water and lava use animated vanilla textures and live tank height. Wood shares
the visibly dithered Cultivation palette; raw clay and fired ceramic each have
one material with clustered pixel wear. Keep this simple geometry and visible
dithering when revising the assets. `build_models.js` uses native Blockbench APIs
and exports both editable sources and Minecraft runtime models.

## Rebuild and verification

Run `scripts/generate_first_chapter.py` for recipes and quests, and
`scripts/build_starting_island.py` for the NBT template (its Python requirements
are in `scripts/world-requirements.txt`). Regenerating the cultivation data also
preserves the all-wood composter recipe.

`scripts/validate_first_chapter.py` checks IDs, prerequisites, renewable recipe
reachability, the starting island and native model geometry/UVs.
`dev.cmd :revoextraction:runChapterTests -PrevoChapterTests` runs eight real server
tests for hammer/crook drops, rain and roof behaviour, atomic clay mixing, heated
lava, clay bucket exchange, persistence and fluid simulation.
The opt-in `-PrevoChapterUiQa` client creates a separate uniquely named QA save
to test a real fresh start, book rewards and vessel interfaces. QA classes are
excluded from release jars. Use a normal `dev.cmd build collectMods` before
`scripts/package_modpack.py`.

## Quest presentation refresh

All 35 quests now have explicit item icons, milestone shapes and a compact map
with four labelled sections. Existing quest/task IDs are preserved. Chapter 2
continues through a hand-powered Create workshop, the Iron Drill and a water-power
automation branch. See [QUEST-BOOK.md](QUEST-BOOK.md) for editing and free-manual
recycling protection.
