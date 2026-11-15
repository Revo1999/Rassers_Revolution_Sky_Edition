# Missing features and progression gaps

Based on checkout `a80e627`, the pinned mod artifacts, current quest files, the latest recipe audit and the original design. This is a development backlog, not a claim that every possible vanilla or exploration fallback is unavailable.

## Ender Chests and tesseracts

| Feature | Current status |
|---|---|
| Vanilla Ender Chest | Present. Its recipe is eight obsidian around one Eye of Ender. Steel Harvester Enderman and Blaze programs supply the pearl and powder route. |
| Modded, channel-based Ender Chests / Ender Tanks | No dedicated Ender Storage mod is installed. |
| Standalone tesseract-style item/fluid/energy transport | No dedicated Tesseract transport mod is installed. |
| Tesseract API | Installed, but its own metadata identifies it as a developer library. It does not establish the presence of Tesseract transport blocks. |
| Wireless energy | Flux Networks is installed, with Revo EU/FE converters. |
| AE2 quantum network links | Included in the installed AE2 artifact; the intended AE2 unlock route remains unfinished. This is an ME-network system, not a standalone Tesseract. |

## 1. Controlled resource routes still needed

These are the remaining gaps identified in [Recipe Audit 3](RECIPE-AUDIT-3.md). Suggested recipes in that audit are proposals, not implemented routes.

1. **Starter Certus Quartz** for AE2.
2. **The first four inscriber presses**; duplication needs an existing press.
3. **Starter Sky Stone**, without depending on meteorites or an existing input.
4. **Resolved:** [Salt Geode Chamber](SALT-GEODE.md) plus native MI centrifuging now provides renewable salt feedstock.
5. **Antimony, tungsten and uranium deposits**, unlocked at suitable industrial stages.
6. **Nether Wart and Ghast Tears** through controlled cultivation/mob production.
7. **Starter End Stone, chorus and shulker shells** for End-related recipes.
8. **Controlled Wither Skeleton skull production**. The new [Wither Hatchery](WITHER-HATCHERY.md) now turns the standard ritual materials into eggs, then uses a powered chamber and real contained Wither death to produce Nether Stars. The skull feedstock route remains unfinished.
9. **The first Netherite Upgrade smithing template**. Ancient Debris already exists; equipment upgrading still needs a starter template.
10. **A controlled red-dye/flower starter**. This is a convenience improvement: vanilla flowers can provide a fallback.

## 2. Progression systems not finished

- **AE2 technology gates:** establish reachable starter materials, then separate basic storage, wireless access, autocrafting and advanced addon unlocks.
- **Later MI progression:** connect advanced metallurgy, chemistry, circuits, power and rare resources into a complete quested route. Installed MI machines do not by themselves provide a finished skyblock progression.
- **Later quest chapters:** current quests cover four numbered chapters and the biology-to-electricity branch; the planned digital manufacturing, industrial mastery and Genesis chapters are absent.
- **Full extraction-control ladder:** the current mechanical drills use saved fixed mixtures, and electric geology supplies selectable programs. The broader design's weighted categories, saved multi-target priorities and inventory-aware production remain future features.
- **Advanced extraction tiers:** the original high-alloy/exotic drill ladder is not fully implemented. Current Wooden/Iron/Steel drills and the electric geological drill cover only part of that vision.
- **Full genetics loop:** current harvesters offer selectable contained mobs. The original manufactured eggs -> hatchery -> fed, growing populations -> managed habitats loop is not implemented as designed.
- **Broader cultivation coverage:** exotic species and plants still need technology placement and renewable starters.
- **Mechanical-to-digital storage transition:** define and verify the intended AE2 migration and long-term balance against physical storage and addon alternatives.
- **Genesis Core:** the final craft, its proposed ultimate components, recipes, final quest and completion rewards remain conceptual.
- **Expanded milestone cosmetics:** the five current Handbook quests cover early milestones; the broader age-by-age reward plan is not complete.

## 3. Logistics feature decisions

These are optional additions, rather than requirements already committed in the original design:

- Channel-based shared Ender Chests and Ender Tanks for remote item/fluid logistics.
- A standalone tesseract-style transport system, if desired alongside Flux and AE2.
- Recipes, technology gates and quest guidance for either addition, so remote logistics fit the factory progression.

## 4. Integration and validation work

- **Applied Create compatibility:** the latest QA/audit records a missing AE2 JEI integration class, `tamaized/ae2jeiintegration/api/integrations/jei/IngredientConverter`. EMI otherwise continued loading. This is a documented issue, not a new reproduction on this computer.
- **Fresh-island survival validation:** targeted tests pass, but a complete survival playthrough through the intended endgame has not been established.
- **Pacing and balance:** validate production rates, resource costs, power demand, maintenance, storage convenience and cross-mod bypasses across the complete progression.
- **Multiplayer and scale:** sustained multi-player/dedicated-server and large-factory testing remain beyond the existing targeted verification.

## Suggested development order

1. Close the AE2 starter-material gaps and salt route.
2. Define AE2 storage/autocrafting gates and resolve its addon integration issue.
3. Complete later metals, biological resources and advanced MI quest coverage.
4. Decide whether remote item/fluid transport should use Ender Storage, tesseracts or AE2-based logistics.
5. Expand genetics and programmable extraction where the original design is still desired.
6. Implement the Genesis dependency tree, completion quest and rewards.
7. Play through and balance the resulting complete route.

## Sources

- [Current mod lock](../pack/mods.lock.json) and the locally installed pinned JARs, including vanilla's Ender Chest recipe, Tesseract API metadata and AE2 quantum-link recipes.
- [Latest recipe audit](RECIPE-AUDIT-3.md).
- [Current progression expansion](PROGRESSION-EXPANSION.md) and [its QA scope](PROGRESSION-EXPANSION-QA.md).
- [Current progression overview](PROGRESSION-OVERVIEW.md).
- [Original design](design/Rassers_Revolution_Sky_Edition_v2.md), especially extraction, genetics, AE2, rewards and Genesis.
