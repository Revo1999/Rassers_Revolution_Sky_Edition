# Feeding Helmet verification

Eight native GameTests pass. The full-modpack client captured all three worn
armor models and synchronized food inventories in an isolated Normal-difficulty
QA save. A survival player wearing a loaded helmet consumed exactly one carrot
at 17/20 hunger through ordinary inventory ticks and reached 20/20 hunger.

- [All three tiers worn](three-tiers-worn.png)
- [Tier I: 1×1 food inventory](tier-1-inventory.png)
- [Tier II: 2×2 food inventory](tier-2-inventory.png)
- [Tier III: 5×5 food inventory](tier-3-inventory.png)
- [Native Blockbench preview](../../art/blockbench/revoindustry/feeding-helmets/three-tiers.png)
- [Gameplay and crafting](../../docs/FEEDING-HELMETS.md)

The 41 earlier machine screenshots are preserved in `../machines` and are
included in the same repository history. The production jar contains the helmets
and assets and excludes test/visual-fixture classes.
