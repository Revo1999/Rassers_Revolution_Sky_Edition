# Early progression audit — 6 November 2026

Historical findings before the resource-bridge update. Coke milling, Cow leather, Steel Blaze rods and rare drill Flux Dust are now implemented. See EARLY-PROGRESSION-AUDIT-2.md for remaining work.

Scope: current source at `5d13884`, quest chapters 1–4 and the pinned installed recipe data (MI 2.5.11, Create 6.0.10, Iron Furnaces 4.3.2, Flux Networks 8.0.0). This is a static recipe/source audit, not a fresh survival playthrough. Conditional compatibility recipes for absent mods were not treated as usable sources. Natural spawning/trading is distinguished from an intentional machine-based resource path, as required by the supplied design.

## 1. Blocking: the first steel / first diamond loop

The Iron Drill supplies coal items, not coal ore or coal dust. Its pool and the Steel Drill's diamond unlock are implemented in `DrillTier.java`.

The installed MI chain is:

- `materials/uncooked_steel_dust.json`: seven iron dust + two coke dust -> uncooked steel dust.
- `materials/blast_furnace/steel.json`: uncooked steel dust -> steel ingot in the Steam Blast Furnace.
- `materials/coke/macerator/gem.json`: coke -> coke dust requires a macerator.
- Alternative `materials/coke_oven/coke_dust.json`: coal dust -> coke dust. However, coal-item -> coal-dust also requires the macerator (`materials/coal/macerator/gem.json`).
- Native Forge Hammer coal recipes accept coal ore or crushed coal dust, not the coal items produced by the Revo drill.
- `steam_age/bronze/macerator_asbl.json` requires two diamonds.
- The first renewable diamonds are in the Steel Drill, whose construction requires steel.

No early custom coal/coke milling bridge was found. Native MI quarry recipes do not rescue this opening: its Steam Quarry itself requires steel and an invar rotary blade.

**Smallest fix:** provide an early Create Millstone route from coke to coke dust, or coal to coal dust. Keep MI's existing uncooked-steel and Steam Blast Furnace process. Add explicit quests for this preparation and the blast furnace; Chapter 4 currently jumps from coke to detecting steel with a reference to EMI.

## 2. Blocking: the greenhouse's compulsory power converter

The Iron Greenhouse structurally requires a Basic Power Converter even when supplied with native MI EU. Its recipe uses `fluxnetworks:flux_core`.

Installed Flux Core crafting requires obsidian, Flux Dust and an Eye of Ender. Flux Networks' shipped tooltip says Flux Dust is obtained by compressing redstone with bedrock and obsidian. The custom starting island has no bedrock, and no alternative early Flux Dust recipe was found. The current Revo biological machines supply zombie, skeleton and spider drops; they do not supply blaze rods for the eye.

**Fix:** establish an intentional early converter component recipe, or supply early Flux Dust / Eye of Ender paths at the intended technology level. Also establish and quest a genuinely obtainable FE/EU generator; converters do not generate power. Create: New Age's magnetite has no supplied crafting recipe or Revo extraction entry, so its worldgen-dependent start also needs attention if that is the chosen generator branch.

## 3. Missing controlled early resource: leather

The Drill Repair Kit requires two leather, and Chapter 4's commissioning quest depends on the repair-kit quest. Current harvesters cover only zombies, skeletons and spiders. A cow colony/egg path is not implemented.

Leather is not absolutely impossible through vanilla animals: passive spawning may provide it. However, the explicit machine-based early route is missing. Native MI's rotten-flesh-to-leather recipe requires an electric Chemical Reactor and oxygen; Create's leather recipes recycle saddles/horse armour rather than establish a renewable bootstrap.

**Fix:** an early leather process from available biological materials, or a reachable cow/passive-animal habitat. Do not make the early maintenance milestone depend on late chemistry.

## 4. Missing explicit resource bridges: Create brass and quartz

The Iron Drill gives zinc and gold, but the broader Create workshop is incomplete:

- Brass mixing requires a heated Blaze Burner; there is no current Revo blaze production route.
- Electron tubes require polished rose quartz, which starts with quartz.
- Create washing can yield quartz from crushed raw gold, but the supplied raw-gold processing route uses Crushing Wheels. Those need Mechanical Crafters, which already require electron tubes and brass casing.
- The precision-mechanism byproduct route also requires Deployers, which need electron tubes and brass hands.

Therefore “quartz exists in a washing recipe” does not by itself establish an early bootstrap. A deliberate first-quartz route and an Overworld blaze/heat route need to be assigned to the appropriate age.

## 5. Cultivation and biological progression needs a quested bootstrap

Working routes already exist: oak renewal, compost/dirt, silk/string, mob-harvester bones, carrots and potatoes. The island starts with grass/short grass, and skeleton bones enable renewable bonemeal/grass seed acquisition. Wheat should not be labelled unobtainable.

Missing or unassigned controlled starter routes include sugar cane/paper, cactus/green dye and beetroot seeds. Create's dough + lime-dye slime recipe is installed, but it still needs an obtainable lime-dye chain. Future slime/blaze/passive-animal colonies are design concepts, not current machine programs.

The main early quest chapters scarcely introduce the wooden farms, silkworm/string chain, food production or these starter resources. The Handbook's machine-detection cosmetics are not a substitute for teaching their production loops.

## 6. Planned early infrastructure is still a stub

`RevoStorage.java` is only an empty mod entry point. The planned spring-powered Mechanical Workstation, its winding and its storage/crafting upgrades are not implemented. The starting Crafting Station works, but it does not supply those planned mechanics.

## 7. Later tier resources are not covered by the current Revo generation ladder

The three drill pools do not include silver or the later industrial metal ladder. Harvesters do not provide Enderman/Blaze/Nether species. Consequently the complete furnace set being registered and functional does not mean all its tiers have an intentional, accessible resource route: silver, emerald, Eye of Ender, magma cream, soul materials, netherite and packed ice need to be assigned and checked. Some vanilla spawning/trading or later MI recipes may provide alternatives; these are not all claimed to be globally impossible.

Likewise, late-AE2 and no-dimension-travel goals still require intentional Certus Quartz/Sky Stone/press and Nether/End resource paths plus recipe gates at their intended ages.

## What is already covered

`python scripts/validate_first_chapter.py` passes: 35-quest DAG, non-consuming tasks, ungated guide replacement, wood-to-stone/drill bootstrap, renewable rain/clay/lava and the custom island. The current Create bridges cover andesite, copper/tin milling, brick dust, first bronze and bronze/steel plate pressing. Existing first-chapter validation stops before the steel loop, which explains why that loop was not caught by its reachability check.

## Suggested implementation order

1. Close the coal/coke-dust bootstrap and explicitly quest first steel.
2. Establish early leather / passive-animal production and maintenance access.
3. Resolve the converter/Flux Dust/Eye of Ender and first generator gates.
4. Add planned quartz/blaze and plant-starter paths with clear age gates.
5. Finish early resource quests and the Mechanical Workstation, then audit every higher-tier material before extending the main quest line.

No gameplay recipes, configurations or machine logic were changed during this audit.
