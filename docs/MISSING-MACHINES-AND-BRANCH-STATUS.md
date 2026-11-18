# Missing machines, sludge and branch status

Checked 17 November 2026 against main `780f7b2`, the pushed machine branch
`cdd1968`, the saved local LV work and the `Revo Latest Machines` showroom.
This document accompanies their integration into main.

## Why some things were missing

The first showroom launched main, not the separate machine feature branch.
Several apparently missing features had already been pushed to GitHub, but
only to `feat/mechanical-sluice`. Other systems were implemented in main but
were not supplied or assembled into processing lines in the showroom.

| Feature | What existed before this merge | What was missing from the first showroom |
| --- | --- | --- |
| Mechanical Sluice and Gravel Feeder | Implemented and pushed on the machine branch | Main did not contain their registration, behavior or assets. The showroom also had no sluice fixture. |
| Mineral Sludge and Gem Sludge | Implemented on main as drill outputs, with Create washing recipes | The showroom had drill structures, but no powered drill-to-washing demonstration or stocked sludge samples. These are items, not standalone machine blocks. |
| Crystalline Sludge | Implemented on main as an advanced Electric Geological Drill output, with an MI Centrifuge recipe | No configured drill, retained survey core, power supply and centrifuge line demonstrating it. |
| Salt Crystal Geode in an Iron Greenhouse | Implemented and pushed on the machine branch | Main instead had the separate Salt Geode Chamber route. The greenhouse's retained geode program was not present in that client. Both routes are retained by this merge. |
| Nether Wart, Chorus, Poppy and advanced harvester programs | Implemented and pushed on the machine branch | Main lacked those added programs; the showroom's ordinary farm displays did not demonstrate their starters and catalysts. |
| Tungsten and Uranium drill programs and survey cores | Implemented and pushed on the machine branch | Main's Electric Drill stopped at Antimony and Crystalline Sludge. |
| First AE2 presses, Sky Stone and Netherite template routes | Implemented and pushed on the machine branch | Their recipes were absent from the main-only client. These are progression recipes rather than additional machine structures. |
| Feeding helmets, quiet machine audio and directional furnace ports | Implemented and pushed on the machine branch | Absent from the main-only client. Helmets are equipment rather than a machine display. |
| Wither Hatchery and submerged Salt Geode Chamber | Implemented and pushed on main | Both were included in the 36-display showroom; most of the gallery was a viewing fixture rather than a continuously supplied factory. |
| LV Factory chapter and copper/zinc battery bootstrap | Saved local work, not pushed before this session | The stash contained the 48-quest LV chapter, bridge lesson, recipe, generator, validator and test edits. This merge includes that work. |
| Nexus portal, Pressure Casting Plant, Electroplating Line and Particle Collider | Models, blueprints, animation studies and specification pushed on main | The original showroom lacked a runtime Nexus installation. See the current status below. |

## Sludge routes that already exist

| Material | Source | Processing | Result |
| --- | --- | --- | --- |
| Mineral Sludge | Saved regular output sequence of Iron and Steel Drills | Create Encased Fan blowing through water; belts/depots can handle items | One Quartz per sludge |
| Gem Sludge | Steel Drill regular sequence | Native Create fan washing | One Diamond and two Lapis, plus 25% chance of one Emerald |
| Crystalline Sludge | Electric Geological Drill, retained Deep Survey Core, 128 EU/t for 1,200 powered ticks | Existing MI Centrifuge, 32 EU/t for 400 ticks | Two Certus Quartz Crystals and two Quartz |

The mechanical sluice is a separate gravel-processing machine. Its current
implementation consumes gravel and pumped water; its artwork mentioning
sludge does not establish a sludge input recipe. Do not describe it as a
working sludge washer without implementing and checking that behavior.

Evidence: [drill minerals and sludge](DRILL-MINERALS-AND-SLUDGE.md),
[mechanical sluice](MECHANICAL-SLUICE.md),
[crystal and late resources](CRYSTAL-AND-LATE-RESOURCES.md), and the registered
recipes under `revoextraction/src/main/resources/data/revoextraction/recipe/`.

## Current status

Rechecked against the merged source and current native recipe manager. The table above documents pre-merge history. The production Nexus blocks, hatches, builder and machines already existed by the time of this follow-up. The survival recipes, the endgame chapter and the new builder interface now complete that route.

| Entry in the old checklist | Current status |
| --- | --- |
| Mechanical Sluice, Gravel Feeder and drill outputs | Registered and craftable. Sluice processes gravel and water. Create washing handles Mineral and Gem Sludge. |
| Crystalline Sludge and first Certus | The 3×3 Electric Drill produces core-gated Crystalline Sludge. An MI Centrifuge makes Certus and Quartz. The compact Digital Drill is also available. |
| Salt, wart, chorus, flowers, cotton and advanced mobs | Existing cultivation and retained spawner routes are in the pack. The submerged salt chamber has its own sediment-to-seawater route. |
| Tungsten, uranium, AE2 presses, Sky Stone and first Netherite template | Existing progression recipes and quest routes are present. Platinum, Deep Debris and Monazite are now additional core-gated drill programmes. |
| Nexus runtime and survival progression | The three native MI machines, builder, portal and Core now have survival recipes. The new chapter tracks preparation, production, construction and server-verified activation. |
| Showroom and pacing | The gameplay fixtures verify individual production routes. A continuously supplied combined showroom and a full survival pacing run remain separate QA work. |

See [the Nexus build and material guide](NEXUS-MATERIAL-LEDGER.md) and [the current recipe audit](../missingrecipies.md).

## Merge compatibility decisions

- Main's Electric Drill indices 0–8 remain unchanged: Antimony is 7 and
  Crystalline Sludge is 8. Tungsten and Uranium occupy 9 and 10. The feature
  branch's direct-Certus program is superseded by main's sludge/centrifuge route.
  On older feature-branch saves, reselect old Certus/Antimony programs after
  updating; those two branch histories assigned different meanings to 7 and 8.
- Cotton uses program 32 alongside the feature branch's existing wart (17),
  chorus (18), salt (19) and poppy (31) programs. Farm selection derives from
  the retained starter, so cotton remains distinguishable from wart.
- Main's Wither chapter keeps its `5E600...` IDs. The conflicting crystal
  chapter moves to `5E900...`. Existing main progress IDs are preserved;
  crystal chapter progress from old feature saves needs separate migration.
- The new starter-battery bridge lesson uses `5E50000000000019`, preserving
  main's existing salt and sludge quest IDs. Existing LV chapter IDs are kept.

The original local stashes are retained as recovery copies. Runtime worlds,
downloaded mod jars, local launch helpers and caches are not source commits.

## Integration verification

The merged source passed 106 distinct native tests: 38 progression, optimization
and sluice tests; seven drill-mineral/sludge tests; 20 cultivation/lighting tests;
three running-shoes tests; 30 industry tests; and eight feeding-helmet tests.
The combined progression/sluice run used a local Gradle init script to exclude
duplicate copies of shared test templates. The other suites ran separately.

The LV validator passed with 204 quests in ten chapters, 473 unique progress
IDs, 44 verified recipe references, no dependency cycles and no LV map
wire/node collisions. Existing main quest/task/reward IDs were preserved.
The progression asset check, sluice asset/motion check and feeding-helmet
asset check also passed. The progression UV check allows the existing
Blockbench plane-separation adjustments of up to 0.008 model units across
both edges; no model geometry was changed for this integration.
The final production build passed all 13 unit tests and produced seven mod
jars, checked to contain no test, QA or benchmark helper classes.

These are source, asset and native server checks. They do not establish that
the already running showroom contains the newly merged displays, or that a
fresh survival playthrough of the entire combined pack has been completed.
