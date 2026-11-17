# Progression gaps after MV

Updated after pulling repository commit `039d7b9` on 17 November 2026. The merged
book has **287 quests in fourteen chapters**, including the published 48-quest LV
chapter, ten crystal/resource quests and the local electronics/MV/HV continuations.
This audit describes the merged source, not a completed survival playthrough.

## Remaining progression work

| Priority | Gap | Where it matters | Current state / next action |
|---|---|---|---|
| P1 | Complete fresh-island LV-to-HV validation | First HV factory | Source and quest checks pass. Verify the complete resource/steam/energy budget in survival. |
| P1 | Platinum/iridium feedstock provenance | Later electronics | Native platinum-solution refining can yield iridium; current Revo deposit programs still lack a verified platinum feed. Establish a drill-rooted source and retain native refining. |
| P1 | Titanium mineral feedstock | Later casings and Tungsten Survey Core | No current drill titanium feed was found. Add the appropriate mineral/refining route. First HV still uses stainless Turbo Hulls, not titanium Highly Advanced Hulls. |
| P1 | Monazite/cadmium feedstock | Cadmium batteries and later hulls | Native monazite refining exists; a reachable Revo mineral feed remains to establish. |
| P1 | Reach the implemented tungsten program | Later metallurgy | Tungsten drilling is implemented at 128 EU/t for 400 ticks with a retained Tungsten Survey Core. Its titanium requirement still depends on the preceding feedstock gap. No second tungsten program is needed. |
| P1 | Full nuclear fuel-processing branch | Nuclear technology and final MI requirements | Uranium drilling is implemented at 512 EU/t for 1,200 ticks with a Nuclear Survey Core. Verify the tungsten/core prerequisites and subsequent native fuel cycle; uranium extraction itself is no longer missing. |
| P1/design | Separate AE2 autocrafting, wireless and addon boundaries | Beyond basic MV storage | Presses, Sky Stone and MV Electronic Circuit gates for machines/processors now exist. Decide and audit any additional later autocrafting/addon gate; do not undo the pulled MV unlock. |
| P2 | Automatic production milestones | Quest commissioning | Item tasks detect possession; fluid/power/network commissioning uses explicit self-reported checkmarks. Add server-side milestones if automatic proof is wanted. |
| P2 | Reproduce the historical Applied Create integration warning | Optional storage addon | Earlier audit reported a missing JEI integration class. Confirm it on the merged pinned pack before treating it as an active blocker. |
| P2 | Wider optional machine quest coverage | Furnace/vacuum/storage/farm tiers | The main LV/MV/HV route is guided, but not every implemented convenience tier has its own quest. Add optional branches without forcing every tier onto the critical path. |
| P1/endgame | Nexus gameplay implementation | Final pack completion | Portal/caster/plating/collider art and blueprints exist. Runtime controllers, recipes, Core, builder material accounting and final activation remain to implement. |
| P1/endgame | Concrete contribution from all major systems | Final mastery objective | Finalize intermediate recipes and production milestones after high-tier feedstocks and runtime Nexus machines exist. |

Minerals must remain rooted in appropriately gated drills; gems come from sludge.
Native chemical dust/byproduct refining remains useful. Quarries remain excluded.

## Gaps closed by the pulled repository work

| Previous gap | Implemented route now retained |
|---|---|
| First AE2 presses | Steel/diamond/material press blanks followed by the Create Mechanical Press. |
| First Sky Stone | Heated cobblestone + Ender Pearl + Certus dust mixing. |
| Basic AE2 technology gate | MI Electronic Circuits gate the Inscriber, Energy Acceptor, Controller and finished processors, including assembler alternatives. |
| Direct early electric salt production | Salt Crystal Geode retained in the powered Iron Greenhouse: water and 8 EU/t. The separate chamber/Centrifuge route remains available. |
| Tungsten / uranium extraction | Retained survey cores select the implemented 128/512 EU/t drill programs. Upstream titanium and full later fuel processing still need validation. |
| Controlled Wither Skeleton skulls | Netherite-gated Steel Harvester program; every 64th completed harvest yields a skull. Powered Hatchery still processes the resulting Wither Eggs. |
| First Netherite template | Craft the implemented blank and mechanically press it; native copying remains. |
| Nether Wart, Ghast Tears, chorus and shulker resources | Added seed/synthesis and gated greenhouse/harvester programs. |
| Reliable red dye | Poppy starter and greenhouse production. |

See [Crystal and late resources](CRYSTAL-AND-LATE-RESOURCES.md) and
[MV AE2 integration](MACHINE-POLISH-AND-MV-AE2.md) for the pulled implementation.

## First-HV ingredients that already have routes

These need production and guidance, rather than replacement recipes or free deposits.

| Ingredient / milestone | Native route checked | State after this update |
|---|---|---|
| Chromium | Drilled redstone -> Centrifuge Ruby Dust -> Chromium Crushed Dust -> hydrochloric solution -> electrolysis -> tiny dust packed into dust | Covered in Chapter 8. Ruby Dust is a refining intermediate, not a new loose gemstone source. |
| Manganese | Drilled Raw Iron -> Centrifuge Manganese Crushed Dust -> sulfuric solution -> electrolysis -> packed dust | Covered in Chapter 8. No new manganese ore is required. |
| Stainless steel | 6 Iron Dust + 1 Chromium Dust + 1 Nickel Dust + 1 Manganese Dust -> 9 alloy dust -> 32 EU/t EBF hot ingots -> Vacuum Freezer | Covered in Chapter 8. |
| Vacuum Freezer bootstrap | Native controller uses the MV-era Advanced Hull, Electronic Circuits, Large Pumps and Aluminum Large Plates | Covered before the first hot stainless ingot. No stainless/Kanthal self-dependency. |
| Kanthal and coils | Stainless/chromium/aluminum blend -> 32 EU/t EBF hot Kanthal -> freezer -> cable -> coils | Covered. The first alloy does not require its own upgraded coils. |
| Annealed copper | Copper Dust + oxygen -> 64 EU/t EBF hot ingot -> freezer -> plate/wire | Covered after the Kanthal coil/power milestone. |
| First digital logic | Native Resistor/Op-Amp/AND/OR/NOT crafting paths using existing electronic components and Aluminum Wire | Covered. Does not require monocrystalline silicon or an iridium starter. |
| Digital Circuit Board | Native Assembler: Aluminum Cable, Silicon Battery, Electronic Board, Stainless Plates and Polyethylene | Covered. Keep the MV plastic supply. |
| Digital Circuit | Gates + four Electronic Circuits + Digital Circuit Board | Covered. This alone does not implement AE2 unlocks. |
| Sodium Battery | Native salt electrolysis sodium + Battery Alloy + Aluminum Cable | Covered. Salt supply already exists. |
| **First HV hull** | Stainless-based **Turbo Machine Casing/Hull**, Sodium Batteries and Digital Circuit | Covered. **Highly Advanced Machine Hull is a later titanium/cadmium tier.** |
| Advanced Motor | Stainless rods/magnetic rod + Annealed Copper Wire + Aluminum Cable | Covered. |
| HV power and step-down | HV Steam Turbine + Aluminum Cable + HV-to-MV Transformer, retaining the MV-to-LV supply | Covered as the first-HV route. Diesel and storage can be optional later branches. |

## Evidence and scope

The [native recipe snapshot](../art/progression-audit-hv/evidence.json) records MI
2.5.11 facts used by the HV chapter. Current Revo sources and the pulled resource
documents take precedence over the earlier missing-feature conclusions. The
[HV map](../art/quests/hv/hv-progression.png) and
[quest validation](../art/quests/validation.json) cover the reconciled chapter set.
No full survival run or optional-addon error reproduction was performed here.
