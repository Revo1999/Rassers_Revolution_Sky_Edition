# Missing progression through MV

Audited 15 November 2026 against commit `29208e4`, current Revo source, quest files and pinned MI 2.5.11 recipes/classes. Scope: the intended void-island route through a working MV factory, including its chemistry and advanced manufacturing branches. This is a source/recipe audit, not a completed survival playthrough. Optional AE2 and equipment branches are separated from the industrial critical path.

**Resource rule confirmed by the user:** mineral/ore feedstocks must come from Revo drills at the appropriate tier; gems must come from sludge processing. Native MI quarries are excluded at every tier. Existing MI refining and chemistry remain useful after the correct feedstock is available.

**Implementation update:** the identified battery bootstrap is now repaired: mechanical Steel Drills supply lead and antimony, electric geology includes antimony, and drill sludge processes into gems/Certus. First batteries are quested before LV power. Native quarry controller recipes are disabled. See [implemented behavior](DRILL-MINERALS-AND-SLUDGE.md). The diagnostic dependency explanations below describe the original gaps; remaining MV work is primarily guidance and full-chain survival validation.

## 1. Original critical-path gaps — now resolved

| Priority | Missing access | Where progression stops | What needs adding |
|---|---|---|---|
| P0 | **Lead before the first LV hull** | The Redstone Battery needs battery-alloy plates and curved plates. Battery alloy needs lead + antimony. Before this update, Revo lead production was only in the Electric Geological Drill, whose recipe already requires a Basic Machine Hull. That hull itself requires two Redstone Batteries. | A controlled **pre-electric lead source**, such as a steel-tier mechanical drill output. Keep the electric lead program as the faster/selectable upgrade. |
| P0 | **Antimony before the first LV hull, with a renewable supply afterward** | Before this update, no Revo drill program supplied antimony. It is needed for the same battery alloy, then later Silicon Batteries and MV hull/storage production. | A controlled **pre-electric antimony source**. A rare steel-tier mechanical output plus a later selectable electric deposit would fit the current ladder. Adding it only after the first LV hull leaves the bootstrap blocked. |

These were two missing feedstocks in one battery dependency problem. Mechanical Steel Drill lead/antimony now enter before the batteries, breaking this original loop:

```text
Lead + Antimony -> Battery Alloy -> Redstone Batteries -> Basic Machine Hull
                                                        |
                                                        v
                                              Electric Geological Drill
                                                        |
                                                        v
                                                       Lead
```

First LV Steam Turbine, basic electric machines, Electric Geological Drill and initial EBF infrastructure share the hull dependency. The new mechanical feedstocks resolve its material bootstrap; a complete survival verification remains outstanding.

### Required drill and sludge work

| Resource group | Required route | Current gap |
|---|---|---|
| Lead | Steel-tier mechanical drill before the first LV hull; retain selectable electric lead afterward. | Implemented in the Steel Drill: two Raw Lead per regular 16-output sequence. |
| Antimony | Steel-tier mechanical drill before the first LV hull; later selectable electric deposit. | Implemented: one Raw Antimony per regular Steel Drill sequence; electric Antimony program and mixed output also available. |
| Other industrial mineral/ore feedstocks | Extend the appropriate Revo drill tier whenever a later recipe requires an otherwise unavailable deposit. Output raw feedstock for processing. | Review beyond MV separately, including tungsten and uranium. Native quarry recipes never count as completed Revo supply routes. |
| Diamonds, lapis, emeralds and other progression gems | Sludge processing, with yields and unlocks matched to first use. | Implemented Mineral/Gem Sludge washing and Crystalline Sludge centrifuging. Direct Steel Drill diamonds/lapis are replaced with Gem Sludge; the existing Trading Colony remains an optional trade route. |
| Certus Quartz and other deliberately gem-classified crystals | Sludge processing at their technology unlock. | Implemented: Mineral Sludge washes into quartz; advanced Crystalline Sludge centrifuges into Certus and quartz. No direct Certus drill output. |

The mineral/gem split also needs a check of native processing byproducts, such as quartz from washing and Ruby Dust from redstone centrifuging. These recipes currently exist; decide which count as acceptable refining byproducts and which bypass the sludge unlock. They have not been removed by this audit.

### Quarries are excluded

MI's installed JAR contains quarry recipes, but they are **not accepted progression routes at any tier**. They do not resolve a missing Revo mineral deposit and are not proposed as alternatives. The repository design already excludes native quarry progression; the user has reaffirmed this. Both native quarry controllers now have their crafting and assembler recipes disabled.

The selected-recipe evidence retains the native quarry data only to explain the earlier audit; it is not an implementation recommendation.

## 2. Missing progression work through MV

These are missing pack guidance, milestones or validation, rather than absent native ingredient recipes.

| Priority | Missing work | Concrete content needed |
|---|---|---|
| Done | **Battery bootstrap quests** | Mechanical battery minerals and first batteries are now quested; First LV Electricity depends on the first-battery quest. |
| P1 | **First aluminum and silicon milestone** | Teach Electric Blast Furnace construction, power/hatches/coils, and the starter routes below. Do not send players to an Electrolyzer for their first aluminum: its crafting already needs aluminum and electronic components. |
| P1 | **Electronic-component production chain** | Guide silver -> electrum, aluminum parts, silicon plates, undoped Diodes/Transistors, Electronic Circuit Boards/Circuits, Large Motors and Large Pumps. These are prerequisites for the Centrifuge, Electrolyzer and chemical machines; the current salt quest summarizes them without a full manufacturing branch. |
| P1 | **A complete MV power milestone** | Add quests for Silicon Batteries, Advanced Machine Casing/Hull, MV Steam Turbine or MV Diesel Generator, electrum cabling, LV/MV transformers, MV energy hatches and storage. Include generation capacity and maintaining the earlier LV network. Native crafting exists. |
| P1 | **Chemistry and plastic branch** | Guide renewable water/gases, salt -> chlorine, hydrochloric acid, lava -> sulfur, sulfuric acid, ethylene and polyethylene. Include the renewable sugar-solution/ethanol route so plastic does not depend on finding oil terrain. |
| P1 | **MV manufacturing and speed upgrades** | Guide the Assembler, Basic/Advanced Upgrades, polyethylene use and machine throughput. Define which production goals count as finishing MV. An installed machine or a generic First Electricity quest is not a complete progression chapter. |
| P2 | **Advanced metallurgy branch** | Explain chromium and manganese extraction, stainless-steel alloying, hot ingots and the Vacuum Freezer, followed by Digital Circuit Boards if they belong at the chosen late-MV boundary. These have recipes; they should not be replaced with free ore deposits merely because the chain is obscure. |
| P1 | **Fresh-island verification through MV** | After fixing the feedstocks, verify a new survival start through first batteries, LV power, electric geology, aluminum/silicon, electronic circuits, salt processing, MV generation/storage and polyethylene. Existing targeted machine tests do not establish that complete chain. |

## 3. Ingredients that already have routes

Except for the pre-LV bootstrap above, these routes exist in the current implementation/pinned MI recipes. They remain downstream of fixing the battery problem.

| Ingredient | Existing route / important distinction |
|---|---|
| Iron, copper, tin, gold, zinc, coal and redstone | Current mechanical drill tiers supply these; the exact available tier differs by resource. |
| Lapis and diamonds | Recovered by washing Steel Drill Gem Sludge, with guaranteed diamond/lapis outputs. |
| Silver, nickel, lead and bauxite | Electric Geological Drill selectable programs. Mechanical lead/antimony now cover the first battery bootstrap; electric extraction improves scaling. |
| First aluminum | Native EBF: **9 bauxite dust -> 1 bauxite block -> 1 aluminum ingot**, at 32 EU/t for the block-processing recipe. A dust-to-nugget route also exists. Use this before the more efficient Electrolyzer route. |
| First silicon | Native EBF: quartz dust + coke dust -> silicon dust, at 32 EU/t. Plain silicon plates work in starter Diode and Transistor crafting; doped silicon is an efficiency improvement. |
| Rubber | Coke dust + water -> raw synthetic oil -> blast-furnace synthetic oil; paper + oil -> Rubber Sheets. Greenhouse sugar cane supplies paper. |
| Salt and chlorine | Salt Geode Chamber -> salt water -> native MI Centrifuge -> Salt Dust; native salt electrolysis supplies chlorine. The chamber is implemented, but its processing machines still sit downstream of the first-battery gap. |
| Sulfur | Native lava centrifuging at 32 EU/t. No new sulfur deposit is required for this route. |
| Chromium | Redstone centrifuging -> Ruby Dust -> Chromium Crushed Dust -> hydrochloric solution -> electrolysis. Lava centrifuging also has a probabilistic Chromium Nugget output. |
| Manganese | Raw iron centrifuging -> Manganese Crushed Dust -> sulfuric solution -> electrolysis. |
| Polyethylene | Native sugar solution -> distilled ethanol -> ethylene with sulfur -> polymerization using lead tiny dust or chromium tiny dust. The lead catalyst route avoids requiring chromium for the first plastic. |
| MV electrical hardware | Native Silicon Batteries, Advanced Machine Hull, MV generators, transformers, storage and energy-hatch recipes exist. |
| Stainless steel | Native iron + chromium + nickel + manganese alloy; EBF produces hot ingots; Vacuum Freezer cools them. This is an extended manufacturing branch, not a requirement for the first MV turbine. |

## 4. Optional branches and scope decisions

| Missing item/system | Relevance through MV |
|---|---|
| **First four Inscriber Presses and first Sky Stone** | First Certus is implemented through sludge. Presses still need fabrication, and Sky Stone needs its own geological/synthesis route. They block AE2 if it is intended by MV, but **do not block native MI MV**. The original design places AE2 later; choose that boundary before adding early starter recipes. |
| **AE2 storage/autocrafting gates and addon integration** | Still needs design and implementation. The earlier audit records an Applied Create JEI integration issue; it has not been newly reproduced in this audit. |
| **Wither Skeleton skull production** | Hatchery/Nether Star processing exists, but controlled skull feedstock does not. Required if boss resources are made part of the MV milestone; not required by the native first-MV hardware recipes inspected here. |
| **First Netherite Upgrade Template** | Equipment progression gap; Ancient Debris exists. Not a prerequisite for native MV infrastructure. |
| **Nether Wart and Ghast Tears** | Controlled potion/biological coverage remains unfinished. Treat as side-branch work unless new MV recipes explicitly require it. |
| **Reliable red-dye/flower starter** | Controlled-start convenience; vanilla flower growing can provide a fallback. Not a core MV metal/circuit blocker. |
| **Tungsten, uranium, End Stone, chorus and shulker shells** | Keep in the wider backlog. They are not prerequisites for the first MV power/storage route inspected here; place them at their actual later branch rather than delaying MV for them. Native quarry alternatives do not satisfy the intended Revo extraction ladder. |

## Remaining recommended order

1. Validate a fresh island through the implemented mechanical battery-mineral and sludge routes into LV power.
2. Add the EBF starter-aluminum/silicon and electronics quest branch.
3. Add MV power, storage, transformers, chemistry/plastic and manufacturing milestones.
4. Verify and balance the complete LV-to-MV factory route.
5. Place AE2 presses/Sky Stone and optional boss/equipment branches at explicit technology boundaries.

## Evidence

- [Selected pinned recipes and machine-behavior findings](../art/progression-audit-mv/evidence.json). Exact MI resource paths are included for inspection in the pinned JAR.
- Revo output definitions: `DrillTier.java` and `ElectricDrillEntity.java` in `revoextraction/src/main/java/dk/revo/revoextraction/`.
- Current first-power quests: `pack/overrides/config/ftbquests/quests/chapters/5e50000000000000.snbt`.
- [Original design](design/Rassers_Revolution_Sky_Edition_v2.md), especially extraction and the late AE2 unlock.
- [Salt chamber implementation](SALT-GEODE.md) and [broader missing-feature backlog](MISSING-FEATURES.md).

Absent-mod compatibility recipes, recycling existing metal parts, ore-processing recipes without an ore source, and duplication recipes without a starter are not accepted as bootstrap sources.
