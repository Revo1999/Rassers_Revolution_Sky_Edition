# Recipe audit 3 - 12 November 2026

**Later implementation update:** the [Electric Wither Hatchery](WITHER-HATCHERY.md)
now provides a controlled egg-to-Nether-Star route using the normal Wither ritual
materials. Row 8's Wither Skeleton skull source remains unfinished. The other
unimplemented proposals below retain their original audit status.

**Salt-route update:** row 4 is now implemented by the [submerged Salt Geode
Chamber](SALT-GEODE.md), which outputs salt water for native MI centrifuging.

Run after the progression/storage implementation and passing native/client verification. The local index contains 13,450 recipe definitions from pinned installed jars and current Revo source. This includes compatibility definitions; recipes gated by absent mods are not accepted as working routes. Source inspection, loaded native recipes and implemented machine programs are the evidence. This is not a claim that every late-game branch has been completed in survival.

The original twelve tasks are implemented. New controlled routes now cover pearls, burners, quartz, greenhouse crops, slime, first-power guidance, silver/nickel/lead/bauxite, emeralds, magma, terrain synthesis, ice, Ancient Debris and chest-drive crafting storage.

## New numbered list for review

All rows below are proposals only, not implemented in this batch. Quantities are initial balancing suggestions.

| # | Remaining gap | Concrete next implementation |
|---|---|---|
| 1 | First Certus Quartz for AE2 | Add a crystal deposit program to the Electric Geological Drill after LV power: 1 Certus Quartz crystal per 400 ticks at 16 EU/t. Keep native charging, dust and Fluix processing. Existing charged/dust multiplication recipes already require starter Certus. |
| 2 | AE2's first four inscriber presses | Add four steel/diamond-gated mechanical pressing recipes for the Logic, Calculation, Engineering and Silicon presses, or unlock a single complete press set through an explicit progression quest. Installed native press recipes copy an existing press, so they do not establish the first set on a meteorite-free island. |
| 3 | First Sky Stone | Add a heated mixer recipe: 4 cobblestone + 1 Ender Pearl + 1 Certus dust -> 4 Sky Stone. Keep native Sky Dust smelting and AE2 processing afterward. Sky Stone -> Sky Dust -> Sky Stone does not create a starter. |
| 4 | Controlled salt feedstock | Add an electric evaporator/brine program or a small salt deposit program after LV power. Suggested start: 1000 mB water + 16 EU/t for 400 ticks -> 1 MI salt dust. Retain native salt chemistry. Current milling/packing recipes require existing salt-bearing inputs. |
| 5 | Antimony, tungsten and uranium before their industrial branches | Add separate deposits with distinct progression: antimony at LV, tungsten at a later power tier, uranium only after a radiation/nuclear milestone. Do not require a deposit's exclusive metal in its first controller. Current Electric Drill programs stop at silver/nickel/lead/bauxite; native metal recycling recipes are not an ore source. |
| 6 | Nether wart and potion resources | Add an Iron Greenhouse soul-sand bed program for Nether Wart, with a steel-gated starter made from soul sand + compost + Blaze powder. Add a later contained Ghast program for tears. Existing Create wart-block crushing needs the first wart block; absent-mod ore compatibility recipes do not supply tears. |
| 7 | First End Stone, chorus and shulker shells | Add an End-themed processing milestone after pearls and higher electricity: synthetic End Stone starter, a retained chorus greenhouse program, then a high-tier Shulker colony. Installed MI End Stone mixing doubles an existing End Stone input; it does not make the first one. Keep these behind a clear progression boundary. |
| 8 | Wither Skeleton skulls and the ordinary Nether Star path | Add a high-tier Wither Skeleton program with a saved rare skull interval, then a deliberate Wither encounter or dedicated boss-processing machine. Soul sand exists now. MI's extreme singularity/nuke star recipe is a late alternative, not the normal first-star route. |
| 9 | First Netherite Upgrade smithing template | Ancient Debris is now available, but native template duplication consumes an existing template. Add a one-time high-tier unlock or a costly diamond/netherite pressing recipe, then retain vanilla duplication. This concerns equipment upgrading; it does not block recipes that only consume Netherite ingots. |
| 10 | Controlled red-dye starter / flower coverage | Add a wooden greenhouse Poppy program or a compost + bone meal flower starter. Beetroot starter crafting currently needs red dye, while beetroot later supplies it. Bone-mealing suitable grass can provide flowers, so classify this as a controlled-start improvement rather than an absolute deadlock. |

Suggested next batch: **1-4**, then **5-6**. Rows **7-9** need a deliberate late-game design. Row **10** is a small quality-of-life change.

## Routes specifically not reported as missing

- Sulfur has a native MI lava-centrifuging route at 32 EU/t (1000 mB lava -> sulfur and metal byproducts). It needs documentation and power, rather than a new free sulfur recipe.
- Bauxite electrolysis exists at 32 EU/t, giving aluminum dust and titanium tiny dust. The new drill provides its bauxite feedstock.
- MI capacitors, resistors, inductors, analog circuits, motors and the LV Steam Turbine have crafting-table recipes. An assembler-only dependency loop must not be inferred from their generated assembler variants.
- Native Create washing supplies quartz after the new raw-gold milling bridge. Native rubber chemistry and belt alternatives remain available.
- Natural spawning, flowers, wandering traders, structures or enabled dimensions can offer fallbacks. The gaps above focus on the intended controlled machine routes for the supplied island design.

## Verification and evidence

See [QA report](PROGRESSION-EXPANSION-QA.md) and [selected pinned recipe evidence](../art/progression-expansion/recipe-audit-evidence.json). The client also logs an existing Applied Create JEI integration failure for missing `tamaized/ae2jeiintegration/api/integrations/jei/IngredientConverter`; its general EMI loading continues. That is a separate compatibility issue, not a new Revo recipe route.
