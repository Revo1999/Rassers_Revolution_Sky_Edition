# RevoIndustry Multiblock Furnaces

Ten hollow 3 x 3 x 3 furnace tiers extend the installed Iron Furnaces progression with native Modern Industrialization structure matching, hatch ownership, locking, auto-transfer, machine menus, casing adaptation and holographic construction support. The ordinary Iron Furnaces remain available; their Factory Augment is disabled.

## Structure

Use 21 matching casings, one tier controller, a roof-centre exhaust vent, at least one item input, one item output and one fuel input. The single middle-centre block must be air. Additional hatches may replace the shell casings. Controllers face outward from the bottom front centre. The vent occupies the exact top centre. Structures support all four horizontal orientations; native MI hatch links prevent two controllers from sharing a hatch.

The controller uses the same casing texture as its structure, with Iron Furnaces opening/fire pixels as transparent overlays. Revo hatch fronts use MI's actual recess and coloured extraction-frame sprites at their original pixel positions. Hatches and the vent acquire the structure casing when formed. The same adaptation is supplied to the EMI guide. Rainbow casing is animated.

## Tiers

| Tier | Casing | Parallel lanes | Maximum batch per lane | Default ticks per operation | Minimum item hatch grade |
|---|---|---:|---:|---:|---:|
| Copper | `modern_industrialization:bronze_machine_casing` | 2 | 1 | 180 | 1 |
| Iron | `modern_industrialization:steel_machine_casing` | 2 | 1 | 160 | 1 |
| Silver | `revoindustry:silver_furnace_casing` | 4 | 1 | 140 | 2 |
| Gold | `revoindustry:gold_furnace_casing` | 4 | 1 | 120 | 2 |
| Diamond | `revoindustry:diamond_furnace_casing` | 6 | 1 | 80 | 3 |
| Emerald | `revoindustry:emerald_furnace_casing` | 6 | 1 | 40 | 3 |
| Obsidian | `revoindustry:obsidian_furnace_casing` | 6 | 1 | 20 | 3 |
| Crystal | `revoindustry:crystal_furnace_casing` | 6 | 1 | 40 | 3 |
| Netherite | `modern_industrialization:blastproof_casing` | 6 | 1 | 5 | 3 |
| Rainbow | `revoindustry:rainbow_furnace_casing` | 6 | 64 | 20 | 3 |

Speeds read the live Iron Furnaces configuration and scale with each vanilla/modded SMELTING recipe's cooking duration. The ten normal tiers are included; optional Allthemodium/Vibranium/Unobtainium integration requires another mod that is not installed in this pack. Rainbow retains its stack-sized multismelting. This does not multiply recipe yields. Normal furnace recipes are supported; these machines do not add smoking/blasting modes.

## Item hatches

Revo item input/output hatches have three grades: 1, 2 and 4 slots. They extend MI's native ItemHatch implementation, including configurable slot locks and automatic adjacent-inventory transfer. Bronze, Steel and Advanced MI item hatches are also accepted at their corresponding grades. Copper/iron controllers accept grade 1 or better; silver/gold require grade 2; the remaining tiers require grade 3.

## Fuel inputs

| Grade | Maximum heat transferred per hatch per tick | Solid slots | Liquid/gas capacity |
|---|---:|---:|---:|
| 1 | 64 | 1 | 4,000 mB |
| 2 | 512 | 4 | 16,000 mB |
| 3 | 4,096 | 9 | 64,000 mB |

Solid, Liquid and Gas inputs are separate blocks in each grade. Multiple fuel hatches may be installed and contribute together. These are fuel inputs, not FE/EU sockets. Low-power hatches slow a demanding furnace safely; round-robin lane scheduling prevents starvation.

- Solid: ordinary furnace fuel values, including modded burn-time fuels. Fluid-containing items such as lava buckets belong in liquid handling and are rejected by solid inputs.
- Liquid: lava provides 20 heat/mB. Other liquid fuels use MI's fluid_fuels data map at 2 EU per heat.
- Gas: MI data-map fuels carrying the common `c:gaseous` tag, including hydrogen.
- Water, steam, nonfuel fluids and the wrong fluid phase are rejected by pipe capabilities and server-side menu filters. Filters are restored after reload.

One recipe operation costs its vanilla cooking time in heat, multiplied by the batch item count. Stored unspent fuel heat remains in its hatch across saves. Fuel is consumed only for a valid, output-ready job; repeated transfers cannot exceed the per-tick grade rate.

## Saved jobs and output safety

Each lane escrows its actual ingredient stack and saves the recipe, result, progress and paid heat. Blocked outputs or incomplete structures pause without spending more fuel. Recipe changes cannot produce stale outputs. Breaking a controller returns unfinished ingredients exactly once; spent fuel is not refunded. Completed results respect native MI output locks and component identity. Smelting experience is retained and claimed by opening or breaking the controller.

## Crafting and EMI

Craft a controller from its original Iron Furnaces block surrounded by eight appropriate casings. Existing MI casing recipes are reused. Other casing tiers craft four riveted casings from eight tier materials and a steel machine casing. Hatches and vents have crafting/upgrade recipes. EMI includes all ten layered assembly guides and detailed operation information. Factory Augments are removed from crafting and hidden in EMI; interactions and an Iron Furnaces mixin prevent activation, including augments left in existing saves. Other Iron Furnaces features remain.

## Validation and previews

- `gradlew :revoindustry:runFurnaceTests -PrevoFurnaceTests` tests all tiers/all rotations, mandatory structure parts, hatch grades, fuel filters/rates, mixed recipes, persistence, blocked outputs, break returns, fairness and disabled augments.
- `gradlew :revocultivation:runFarmLightingTests -PrevoFarmLightingTests` checks that the greenhouse night/rain lighting still works with the new required integration.
- `gradlew :revolib:runClient -PrevoFurnaceUiQa` is opt-in visual QA, restricted by an explicit named-save guard to the copied `Revo Furnace QA` world. It builds and photographs all ten tiers, checks real native menus and captures the assembly guide.
- Runtime test helpers and structures are excluded from production jars. No player world is modified by QA.

Editable 26-cube assembled sources and front/rear editor previews are in `art/blockbench/furnaces`. Real built-machine screenshots are in its `ingame` directory. Editor preview composites are visual references; runtime rendering uses native MI machine models and the animated resource textures.

## Sources and attribution

- [Iron Furnaces 4.3.2 source](https://github.com/Qelifern/IronFurnacesNeoForge/tree/4.3.2) — configuration, Factory behavior and derived door pixels; Apache-2.0.
- [Modern Industrialization 2.5.11 source](https://github.com/AztechMC/Modern-Industrialization/tree/2.5.11) — native machine APIs and hatch overlay pixels; MIT.
- The supplied user images guided the riveted casing and matching machine/hatch appearance. See bundled third-party notices for the redistributed derivative sprites.
