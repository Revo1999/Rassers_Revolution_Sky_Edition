# Progression gaps after MV

Audited 17 November 2026 against the current Revo source, quest files and installed
**MI 2.5.11** recipes. Scope: finish the first **HV** factory, then identify the
next blockers. This is a source/recipe audit, not a completed survival playthrough.

**Result:** no new mineral deposit or missing recipe was found on the checked
native first-HV route. It uses existing drill feedstocks and native refining.
Chapter 7 now teaches that route in **26 quests**, bringing the book to **228 quests
in twelve chapters**. Later technologies still have the gaps below.

## What is still missing

| Priority | Missing work / material | Where it matters | Current situation | Required next action |
|---|---|---|---|---|
| **P1** | Fresh-island validation through LV, MV and HV | First HV completion | Recipes and source routes were checked; quest dependencies and items validate. No continuous survival run proves the complete resource/energy budget. | Play through the bootstrap, verify coils/hatches and measure sustained steam, gas, acid, plastic and component throughput. Fix actual stops found by that run. |
| **P1** | Automated production detection in quests | Commissioning and chemistry milestones | Machine power, fluid production and sustained-network quests are explicit player-confirmed checkmarks. Item tasks detect possession. | Add server-side production milestones if automatic proof is desired; preserve the existing quest IDs. |
| **P1, later** | Reachable platinum feedstock for iridium | Monocrystalline silicon and later electronics | Native platinum-solution purification can yield Iridium Tiny Dust. The Revo drill has no platinum program; a reachable platinum refining feed must be established. The native ore-to-raw recipe does not supply an ore starter. | Add an appropriately gated drill mineral feedstock or verify a valid refining byproduct rooted in an existing drill output, then retain native platinum chemistry. Do not grant iridium directly. |
| **P1, later** | Titanium mineral feedstock | Titanium components and Highly Advanced casings | Native downstream processing exists, but the current Revo deposit programs do not supply a titanium feedstock. It is not a requirement for the first HV Turbo Hull. | Define the next drill-tier titanium deposit and its native refining path before writing an EV-style quest gate. |
| **P1, later** | Monazite/cadmium source | Cadmium batteries and later hulls | Native monazite centrifuging yields cadmium. No monazite deposit is present in the current Revo drill programs. | Provide the appropriately gated mineral feedstock and keep the native separation chain. Do not treat a processing recipe as a renewable deposit. |
| **P1, later** | Tungsten feedstock | Later metallurgy and Nexus targets/materials | Current drill programs have no tungsten deposit. Disabled MI quarries cannot fill the gap. | Add a later-tier drill program and verify all native precursor/refining requirements. |
| **P1, later** | Uranium/nuclear feedstocks | Nuclear power and final MI/Nexus requirements | Current drill programs do not supply uranium. Installed native recipes do not make the deposit renewable. | Add staged drill feedstocks and a complete fuel-processing quest branch before requiring nuclear components. |
| **P1, optional storage** | First AE2 Inscriber Presses | First processors | Starter Certus already comes from Crystalline Sludge, but the first four presses still lack a deliberately implemented pack bootstrap. Duplication is not a first-press source. | Add a technology-gated first-press fabrication route; define separate later autocrafting requirements. |
| **P1, optional storage** | First Sky Stone | AE2 infrastructure | No verified renewable Revo sky-island starter route is established by this audit. Meteorite-dependent assumptions do not close it. | Add a suitable geological/crafted route at the selected AE2 tier, then verify the whole starter network. |
| **P1, design** | Enforced AE2/addon technology gates | Storage, wireless and autocrafting | The design intends late wired storage, then later wireless/autocrafting. A quest dependency alone does not prevent early crafting. | Decide the boundaries, audit native/addon recipes and enforce gated ingredients in actual recipes. |
| **P2, needs reproduction** | Applied Create integration issue | Optional AE2/Create manufacturing | An earlier audit recorded a missing AE2 JEI integration class. It was not reproduced by this source audit. | Reproduce with the current pinned pack before changing mods or claiming the issue remains active. |
| **P1, later** | Controlled Wither Skeleton skull supply | Renewable Wither Eggs and Nether Stars | The Wither ritual and powered Hatchery exist, but controlled renewable skull production remains unfinished. This does not block native first HV. | Add the appropriate gated mob program/drop route; retain the real powered Wither hatch/kill process. |
| **P2, equipment** | First Netherite Upgrade template | Equipment progression | Ancient Debris is renewable through Deep Survey; a first template is still a separate starter gap. Native duplication needs one already. | Define a gated first-template route without replacing the debris and smithing chain. |
| **P2, later biology** | Nether Wart, Ghast Tears and End materials | Expanded biology, equipment and later crafts | Controlled sources remain incomplete; they are not prerequisites of the checked first-HV turbine route. | Add targeted biological/drill/processing routes at their actual use tier, with explicit seed or starter access. |
| **P2, guidance** | Wider Revo machine coverage | Optional furnaces, vacuums, storage and farm upgrades | The main LV/MV/HV line now has guidance, but that is not a quest for every implemented Revo machine or tier. | Audit optional machine coverage and add side branches without forcing every decorative or convenience tier onto the critical path. |
| **P1, endgame** | Runtime Nexus machines, Core and Builder | Final pack completion | Portal, Pressure Casting Plant, Electroplating Line and Collider models/blueprints are delivered. Their gameplay controllers, recipes, synchronized processing and builder placement remain pending. | Implement and validate the three machines, Core, construction program/material accounting and final portal activation. Art files do not count as completed production. |
| **P1, endgame** | Every major system contributing to Nexus | Final recipe and mastery checks | The design calls for broad MI/Revo mastery; not every optional machine/system has a concrete required contribution yet. | Finalize material intermediates and production milestones after the missing high-tier sources and runtime machines are implemented. |

**Resource rule:** minerals originate in suitably gated drills; gems originate in
sludge processing. Existing chemical dust/byproduct refining remains in use. No
quarry, free manual, recycling loop, unavailable ore or starter-dependent duplicate
recipe counts as a new resource source.

## First-HV ingredients that already have routes

These need production and guidance, rather than replacement recipes or free deposits.

| Ingredient / milestone | Native route checked | State after this update |
|---|---|---|
| Chromium | Drilled redstone -> Centrifuge Ruby Dust -> Chromium Crushed Dust -> hydrochloric solution -> electrolysis -> tiny dust packed into dust | Covered in Chapter 7. Ruby Dust is a refining intermediate, not a new loose gemstone source. |
| Manganese | Drilled Raw Iron -> Centrifuge Manganese Crushed Dust -> sulfuric solution -> electrolysis -> packed dust | Covered in Chapter 7. No new manganese ore is required. |
| Stainless steel | 6 Iron Dust + 1 Chromium Dust + 1 Nickel Dust + 1 Manganese Dust -> 9 alloy dust -> 32 EU/t EBF hot ingots -> Vacuum Freezer | Covered in Chapter 7. |
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

## Suggested next order

1. Validate a new-island factory through the newly guided HV milestone.
2. Establish the platinum/iridium, titanium and monazite/cadmium feedstock routes
   for the actual later component requirements; do not move them into first HV.
3. Decide and implement AE2 starter/gating recipes and their quest branch.
4. Extend higher drill tiers, power and nuclear progression, then implement the
   Nexus gameplay chain using its approved models and material plan.

## Evidence and limits

See [pinned recipe evidence](../art/progression-audit-hv/evidence.json), the
[current drill programs](../revoextraction/src/main/java/dk/revo/revoextraction/ElectricDrillEntity.java),
the [HV quest map](../art/quests/hv/hv-progression.png) and
[quest validation](../art/quests/validation.json).

The recipe evidence records the installed JAR hash and relevant native recipes.
Item/circuit/hull routes were traced for first HV; later feedstock rows identify
missing Revo coverage and required follow-up, not a complete EV/nuclear dependency
proof. Chemistry/commissioning checkmarks are self-reported. No live survival
playthrough, optional-addon error reproduction or game-balance measurement was run.
