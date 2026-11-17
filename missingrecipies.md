# Missing machine-block recipes

Audit of all 155 registered Revo blocks, using the native recipe manager and packaged recipe JSON. Block drops and loot tables do not count as recipes. Optional-integration recipes are checked in the source even when their dependency is absent from the test server. The complete inventory is in [machine-recipe-audit.json](docs/machine-recipe-audit.json).

**No missing block recipes were added.** The explicitly requested eleven mob-egg recipes and eleven typed vanilla-spawner recipes, plus the two mob-preserving spawner-tier upgrades, are the exception. Eggs and placed spawners cannot spawn mobs.

## Nexus recipes to define

These blocks are now registered with items, models, loot, mining tags and executable structure definitions. Their survival crafting recipes are deliberately pending. Counts below are physical blocks per blueprint, not additional recipe ingredients.

| Missing output | Used by (quantity) | Status |
|---|---|---|
| `revoindustry:electroplating_line` | Plating Line (1) | No crafting/assembler recipe |
| `revoindustry:nexus_beacon` | Portal (5) | No crafting/assembler recipe |
| `revoindustry:nexus_builder` | Builder (1) | No crafting/assembler recipe |
| `revoindustry:nexus_casing` | Portal (281) | No crafting/assembler recipe |
| `revoindustry:nexus_ceramic_panel` | Portal (44) | No crafting/assembler recipe |
| `revoindustry:nexus_controller` | Portal (1) | No crafting/assembler recipe |
| `revoindustry:nexus_core` | Portal (1) | No crafting/assembler recipe |
| `revoindustry:nexus_deck_grate` | Portal (48) | No crafting/assembler recipe |
| `revoindustry:nexus_foundation` | Portal (178) | No crafting/assembler recipe |
| `revoindustry:nexus_foundation_chiseled` | Portal (11) | No crafting/assembler recipe |
| `revoindustry:nexus_hazard` | Portal (48) | No crafting/assembler recipe |
| `revoindustry:nexus_light_amber` | Portal (2) | No crafting/assembler recipe |
| `revoindustry:nexus_light_blue` | Portal (10) | No crafting/assembler recipe |
| `revoindustry:nexus_light_violet` | Portal (4) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_accelerator_glass` | Collider (200) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_bath_wall` | Plating Line (26) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_casing` | Casting Plant (53), Plating Line (45), Collider (336) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_ceramic_panel` | Collider (20) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_deck_grate` | Casting Plant (32), Plating Line (32) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_gantry_rail` | Plating Line (14) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_hazard` | Casting Plant (4), Plating Line (4) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_hydraulic_cylinder` | Casting Plant (2) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_injector_housing` | Collider (2) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_light_amber` | Casting Plant (2) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_light_blue` | Plating Line (1) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_magnet_coil` | Collider (61) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_mould_bed` | Casting Plant (9) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_pressure_plate` | Casting Plant (21), Plating Line (35) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_pressure_tank` | Casting Plant (6) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_reinforced_column` | Casting Plant (10), Plating Line (12), Collider (156) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_ribbed_panel` | Casting Plant (10), Plating Line (10) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_service_panel` | Casting Plant (3), Plating Line (6) | No crafting/assembler recipe |
| `revoindustry:nexus_machine_vent` | Plating Line (4) | No crafting/assembler recipe |
| `revoindustry:nexus_rail` | Portal (51) | No crafting/assembler recipe |
| `revoindustry:nexus_reinforced_column` | Portal (20) | No crafting/assembler recipe |
| `revoindustry:nexus_ribbed_panel` | Portal (20) | No crafting/assembler recipe |
| `revoindustry:nexus_ring` | Portal (48) | No crafting/assembler recipe |
| `revoindustry:nexus_service_panel` | Portal (4) | No crafting/assembler recipe |
| `revoindustry:nexus_trim` | Portal (27) | No crafting/assembler recipe |
| `revoindustry:nexus_vent` | Portal (6) | No crafting/assembler recipe |
| `revoindustry:particle_collider` | Collider (1) | No crafting/assembler recipe |
| `revoindustry:pressure_casting_plant` | Casting Plant (1) | No crafting/assembler recipe |

## Additional recipe work

| Item / system | Missing work |
|---|---|
| `revoindustry:nexus_construction_program` | Retained builder program needs its recipe and server-verified endgame unlock. No survival recipe exists yet. |
| Nexus processing chain | The three native MI recipe types are registered; the proposed alloy, staged plating, target, essence and lattice recipes/items still need the approved recipe ledger. No production recipes were invented in this registration pass. |
| Nexus bill of materials | Apply the approved roughly 1.5× material target across the block and Core recipes, including embedded intermediates, without double charging. Keep the builder cheap. |

## Existing routes and exceptions

| Block / group | Audit result |
|---|---|
| Existing Revo machine controllers, casings, hatches and parts | Existing recipe routes found; inspect the JSON inventory for every output and recipe ID. |
| Three `revoindustry:*power_converter` tiers | Recipes already exist and require Flux Networks plus MI. Their absence in a stripped test instance is not a missing pack recipe. |
| `revocultivation:cotton_crop` | Uses the `revocultivation:cotton_seeds` item and its existing seed recipe. The block ID is not a separate craftable item. |
| `revoindustry:salt_water` | Fluid-world block has no item by design; geode processing produces the fluid. |
| `revoextraction:dust` | No grid recipe: hammer Sand or Red Sand using the existing `HammerItem.result` interaction. This is a real manual production route, not a missing progression ingredient. |
| Native MI item/fluid/energy hatches used by Nexus | Existing native MI recipes; no duplicate cosmetic hatches were registered. |

## How to refresh

Run `dev.cmd :revoindustry:runNexusTests -PrevoNexusTests`, confirm the native tests pass, then run `.local/venv/Scripts/python.exe scripts/audit_machine_recipes.py`. This refreshes the registry evidence and this report without creating recipes.
