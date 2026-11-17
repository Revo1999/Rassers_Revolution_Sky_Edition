# Revo's Creationalization

A small pack-owned Create/Modern Industrialization compatibility module, inspired by the integration idea behind Greate. This pack uses MI rather than GregTech. Start with KubeJS, which is already pinned, instead of adding an eighth compiled mod. New machines, voltage-aware kinetic machinery and custom processing logic would warrant a Java mod later.

The editable module is [revo_creationalization.js](../pack/overrides/kubejs/server_scripts/revo_creationalization.js). It uses native MI recipe JSON through KubeJS `event.custom`, needs no extra scripting addon, and reloads with `/reload`. Recipe IDs use `revo_creationalization:`. It conditionally loads when both Create and MI are present.

## Recipe proposals

Amounts are per operation. **Added** rows are the only newly enabled recipes. **Suggested** rows are design proposals; machine compatibility, item IDs, existing recipe coverage and exact costs must be checked before enabling them. Assembler availability is the earliest machine gate, not a claim that a recipe is MV-only. A cheap 8 EU/t recipe can run in later machinery without bringing its ingredients forward.

| Status | Bridge / machine | Ingredients | Output | Progression protection |
|---|---|---|---|---|
| **Added** | MI Assembler: Brass Hand | 1 Andesite Alloy + 4 Brass Sheets/Plates | 1 Create Brass Hand | Exact native crafting cost; still needs brass. 8 EU/t, 100 ticks. |
| **Added** | MI Assembler: Whisk | 2 Andesite Alloy + 5 Iron Sheets/Plates | 1 Create Whisk | Exact native crafting cost. Crafting remains the bootstrap. 8 EU/t, 100 ticks. |
| Suggested | MI Assembler: Deployers | 1 Electron Tube + 1 Andesite Casing + 1 Brass Hand | 1 Deployer | Exact pinned native cost. Automates assembly without replacing intermediate parts. No early brass or quartz. |
| Suggested | MI Assembler: Mechanical Arms | 3 Brass Plates + 1 Andesite Alloy + 1 Precision Mechanism + 1 Brass Casing | 1 Mechanical Arm | Exact pinned native cost. Consumes a complete mechanism; no direct circuit-to-arm shortcut. |
| Suggested | MI Assembler: Smart Chutes | 1 Brass Plate + 1 Chute + 1 Electron Tube | 1 Smart Chute | Exact pinned native cost; no free filtering electronics. |
| Suggested | MI Assembler: Brass Funnels | 1 Electron Tube + 1 Brass Ingot + 1 Dried Kelp | 2 Brass Funnels | Exact pinned native cost and batch yield. |
| Suggested | MI Assembler: Revo drill repair kits | Exact current tier-specific recipe, retaining leather, plates and copper/other parts | Native repair-kit quantity | Wooden/iron/steel costs stay distinct; no repair-kit recycling. |
| Suggested | MI Assembler: Revo kinetic inputs and output hatches | Their full current recipes, including the previous-tier part where upgrades consume one | Native output quantity | Retains the drill upgrade ladder and casing/core costs. |
| Suggested | MI Cutting Machine: Macaw's/Create Deco construction parts | Same metal/wood inputs and total material cost as each native recipe | Native batch quantity | Construction automation; whitelist decorative outputs only, not machine casings. |
| Suggested | MI Assembler: Create Fluid Pipes | 2 Copper Plates + 1 Copper Ingot | 4 Create Fluid Pipes | Exact pinned native cost and batch yield; keep distinct from MI fluid pipes. |
| Suggested | MI Mixer: Super Glue | Full native ingredient bill, preserving all slime/sap/leather-related costs actually required | Native glue output | Uses established biological supplies; no new slime source. Container accounting must match. |
| Suggested | Create belts + MI Centrifuge: salt production line | Salt Geode Chamber's salt water, using the existing centrifuge recipe | Existing Salt Dust yield | A factory-layout integration, not a new chemistry recipe. Never add early fan-washing of brine into salt. |
| Suggested | Create handling + MI refining: sludge production line | Existing Mineral/Gem/Crystalline Sludge and their current processing recipes | Existing gem yields | Keep early washing and advanced Certus centrifuging separate. No minerals or gems created from stone. |
| Suggested, late | MI Assembler: Nexus subassemblies | Full finalized cast, plated and exotic components, plus native advanced MI intermediates | Finalized Nexus subassembly quantity | Only after the three dedicated material machines exist. Never bypass them with ordinary alloy mixing. |

Recommended next batch: Deployer, Mechanical Arm, sorting parts and drill repair kits. These connect actual production lines while preserving the parts players already need to manufacture.

## Existing bridges to retain

The pinned MI 2.5.11 JAR already contains `data/modern_industrialization/recipe/compat/create/` recipes for cogwheels, large cogwheels, Electron Tubes, Precision Mechanisms, Create casings, rubber/kelp belts, brass sheets, shafts, and several mixer/chemical processes. Do not add duplicate recipes solely to put them under the new brand. Inspect pack overrides before relying on a particular native recipe.

Revo already adds early copper/tin/brick milling, bronze mixing and bronze pressing, plus steel pressing. Those bootstrap routes remain in their existing modules. The native Precision Mechanism assembler recipe already consumes five small gears, five large gears, a gold plate and five MI iron bolts, with an 80% output probability; do not replace it with a cheap guaranteed circuit recipe.

## Rules for every new bridge

- Minerals originate in the appropriate Revo drill tier. Gems originate in sludge. Native quarries stay excluded.
- Keep MI steel, aluminum/silicon, rubber, electronics, salt/chemistry and MV manufacturing steps. Create rotation, heat, RPM and a recipe's name are not substitutes for real tier requirements.
- Preserve manual/bootstrap recipes when adding later automated assembly. A machine must never be required to manufacture its own first controller or prerequisites.
- Begin with identical costs and yields. Audit all dismantling, nugget/ingot/block, fluid-container and alternate-mod routes before granting bulk discounts.
- Use shared material tags only for true equivalents, such as plates of the same metal. Never equate circuits, mechanism tiers or chemically different fluids.
- Recipe book/quest visibility is guidance, not enforcement. Use consumed gated ingredients, the correct recipe machine, its real EU/t requirement and server-side machine rules.
- Keep Wither Egg/hatchery/Nether Star processing intact. No ordinary mixer or assembler recipe manufactures Nether Stars, skulls, netherite templates or late AE2 unlocks.

## Adding an assembler recipe

Inside the existing recipe event, add a call using MI's native ingredient format:

```js
assemble('brass_hand', [
  { item: 'create:andesite_alloy', amount: 1 },
  { tag: 'c:plates/brass', amount: 4 }
], 'create:brass_hand', 100, 8)
```

Arguments are ID suffix, inputs, one output item, duration in ticks and EU/t. Other machine recipe types can use `event.custom` directly. Keep a single owner for each recipe ID. Check EMI and KubeJS reload logs, then run the recipe in the real machine before describing a proposal as game-tested.

## Verification and sources

The two added input bills were compared with the installed Create 6.0.10 crafting recipes. Their MI JSON structure follows the installed MI 2.5.11 Electron Tube assembler bridge. Script syntax and emitted recipe payloads are checked locally; an in-game reload and actual assembler cycle remain pending.

- [Greate source and integration scope](https://github.com/GreateBeyondTheHorizon/Greate).
- [KubeJS recipe events and native custom JSON](https://kubejs.com/wiki/tutorials/recipes/).
- Local progression: [Create and steam](CREATE-IRON-STEAM.md), [drill minerals/sludge](DRILL-MINERALS-AND-SLUDGE.md), [through-MV audit](PROGRESSION-GAPS-THROUGH-MV.md) and [Nexus design](NEXUS-ENDGAME.md).
