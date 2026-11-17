# Feeding Helmets

RevoIndustry adds three wearable military-style feeding helmets for Minecraft
1.21.1 / NeoForge 21.1.265. Each has a visible tube leading from its ration module
to the mouth. All three have native Blockbench sources and 3D item/armor models.

| Item | Food inventory | Armor | Durability |
| --- | --- | --- | --- |
| Feeding Helmet | 1×1, one stack | Iron helmet | 165 |
| Advanced Feeding Helmet | 2×2, four stacks | Iron helmet | 275 |
| Elite Feeding Helmet | 5×5, twenty-five stacks | Diamond helmet | 363 |

Right-click while holding a helmet to open its food inventory. Sneak-right-click
to equip it, or put it in the ordinary helmet slot. Food storage stays with the
individual item when it is unequipped, dropped or saved. Food-only slots support
normal and shift-click transfers. The helmet itself is locked while its menu is
open, including number-key and off-hand swaps. Feeding pauses while that helmet's
inventory is being edited. It resumes when the menu closes.

While worn by a survival/adventure player, the helmet checks once per second.
It chooses the largest stored food portion whose nutrition fits the missing hunger;
equal-nutrition foods prefer higher saturation. A carrot restores three hunger
points (1.5 drumsticks), so it waits at 18/20 and eats at 17/20. It never consumes
an oversized portion just because that food can normally be eaten while full.
Non-positive-nutrition items are not accepted. Held helmets do not feed, and
creative/spectator players do not consume rations. Manual item use takes priority.

Food uses the item's ordinary consumption implementation: nutrition, saturation,
food effects, consumption statistics and container returns are retained. Bowls or
other returned items go into the player's inventory, with normal overflow handling.
The food properties are read from the actual stack, supporting component-based and
modded food rather than a hard-coded list.

The three recipes use vanilla iron/diamond helmets and ordinary crafting materials.
They do not consume lower-tier feeding helmets, so upgrading cannot silently
destroy stored food. Find the recipes in EMI or the item IDs below:

- `revoindustry:feeding_helmet`
- `revoindustry:advanced_feeding_helmet`
- `revoindustry:elite_feeding_helmet`

Sources: [native models](../art/blockbench/revoindustry/feeding-helmets),
[three-tier preview](../art/blockbench/revoindustry/feeding-helmets/three-tiers.png),
[in-game screenshots](../screenshots/feeding-helmets).
The art builder `scripts/build_feeding_helmet_models.py` runs against the open
Blockbench MCP desktop server and generates the runtime item models and worn
geometry from the same native-authored boxes. The Steve head in each source is a
fit reference, marked not to export. Pixel density is one texture pixel per model
unit (16 pixels per block); tiers add modules rather than denser textures.

## Verification

Run native tests with:

```
.\dev.cmd :revoindustry:runFeedingTests -PrevoFeedingTests --offline --no-watch-fs
```

Eight native GameTests cover the exact 1.5-drumstick threshold, food choice,
full-hunger refusal, component-sensitive food, all three capacities and save/load,
worn-only tick feeding, saturation/effects/bowls, menu persistence and swap locks,
editing pauses and loaded recipes. The visual fixture uses an isolated copied save
named `Revo Feeding Helmet QA`, captures worn armor and all three synchronized
inventories, and verifies a real survival player is fed by normal server ticks.
QA fixtures and tests are excluded from production jars.

Implementation follows the pinned [NeoForge container/component API](https://docs.neoforged.net/docs/1.21.1/inventories/container/).
