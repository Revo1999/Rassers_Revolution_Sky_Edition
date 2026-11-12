# Progression TODO

Updated: 12 November 2026. Based on the [follow-up progression audit](docs/EARLY-PROGRESSION-AUDIT-2.md).

The original twelve tasks are implemented. See [the implementation guide](docs/PROGRESSION-EXPANSION.md), [QA report](docs/PROGRESSION-EXPANSION-QA.md), and the new [recipe audit](docs/RECIPE-AUDIT-3.md) for the resulting behavior and remaining follow-up work.

## Completed resource bridges

- [x] Create Millstone: Coke -> Coke Dust before diamonds.
- [x] Wooden and Steel Mob Harvesters: Cow mode for leather and beef.
- [x] Steel Mob Harvester: Blaze mode for blaze rods.
- [x] Iron and Steel Drills: saved Flux Dust output every 128 completed cycles.

## Implemented original checklist

- [x] **1. Ender Pearls for Eyes of Ender and Flux Cores** — Add Enderman to the Steel Mob Harvester: 1 pearl per cycle, using the existing safe contained entity system. Unlock through the steel filter, without requiring a pearl/egg to start it. Existing blaze rods supply powder; keep vanilla Eye crafting.

- [x] **2. Create Blaze Burner bootstrap** — Add an empty-burner recipe using 4 iron plates around 1 brick block, retaining the native netherrack recipe. Right-click the formed Steel Harvester in Blaze mode with an Empty Blaze Burner to exchange it for a filled burner while retaining the colony. Require one complete production cycle so placement alone cannot instantly fill burners.

- [x] **3. Quartz before Create crushing wheels** — Add Millstone processing: 1 raw gold -> 1 Create crushed raw gold, 400 processing time, no bonus experience. Keep native washing (9 gold nuggets + 50% quartz). This bypasses the crushing-wheel / mechanical-crafter / electron-tube dependency and uses existing Iron Drill gold.

- [x] **4. Sugar cane, paper and the first rubber sheets** — Add Sugar Cane to the wooden greenhouse with a retained starter and 3 cane per cycle. Bootstrap 1 cane from 1 compost + 1 wheat seed + 1 oak leaf block. Quest the native MI coke-dust + water -> raw synthetic oil -> blast-furnace synthetic oil -> paper + oil -> rubber-sheet chain. Do not invent a parallel rubber chemistry chain.

- [x] **5. Early Create belts without ocean kelp** — Add Kelp to the wooden greenhouse, producing 3 per cycle from a retained starter. Bootstrap 1 kelp from 1 compost + 1 wheat seed + a water bucket (return bucket). Smelt/dry it and retain native belt crafting. MI already supplies a 6-rubber-sheet belt alternative; document that later route too.

- [x] **6. Reliable slime for Create Super Glue** — Add Slime to the Wooden Mob Harvester, 1 slimeball per cycle, no captured slime needed. The installed dough + lime-dye recipe is another route, but green dye currently lacks a controlled starter. This keeps a biological resource in the mob machine.

- [x] **7. Cactus / green dye and beetroot starter coverage** — Add Cactus to the wooden greenhouse (3/cycle); craft the first cactus from sand + compost + wheat seed. Add first beetroot seed crafting from compost + wheat seed + red dye. Retain current beetroot growing program. Reuse these starter recipes in EMI and quests.

- [x] **8. An explicit first-electricity path** — Add a quest chain through native MI rubber sheets, tin cables, analog circuit, motors and the hand-crafted LV Steam Turbine. Demonstrate steam -> EU -> machine input; a converter is not a generator. If Create: New Age must also be an early branch, add a steel-gated magnetite recipe, e.g. 4 iron ingots + 4 iron dust + 1 redstone -> 1 magnetite block, after checking its generator yield.

- [x] **9. Silver, nickel, lead and bauxite extraction** — Implement the next Electric Drill tier after LV power. Use selectable geological programs for these metals, with a basic mixed program as the default. Do not require one of its own exclusive metals to build its first controller. This supplies the silver furnace and later industrial alloys without relying on MI worldgen/quarries.

- [x] **10. Emerald and later furnace-tier inputs** — Add a trading colony program exchanging 16 wheat or paper for 1 emerald. Add Steel Harvester Magma Cube mode for magma cream. Reserve synthetic netherrack/soul sand, packed ice and Ancient Debris for explicit later processing/extraction unlocks before exposing those furnace tiers as progression requirements. Do not hand out Netherite at the first steel drill.

- [x] **11. Planned mechanical storage workstation** — Implement RevoStorage's currently empty entrypoint as a spring-powered workstation: 9-slot crafting grid + 27-slot storage, hand winding first, Create shaft charging later. Persist inventory and charge; support hopper access. Keep network storage/autocrafting for the electrical age.

- [x] **12. Quest coverage and an end-to-end progression check** — Add early branches for greenhouse starters, mob filter selection, Cow leather and drill maintenance; then quartz, filled burners, rubber and first power. Preserve existing quest IDs. Add a fresh-start reachability check through LV power, plus native tests for pearl output, burner capture and each new recipe chain.

## Original suggested order

Start with **1–4 and 6**, then **5, 7 and 8**. Items **9–11** are larger feature work. Apply **12** alongside each batch, finishing with a fresh-island playthrough through the first powered greenhouse.
