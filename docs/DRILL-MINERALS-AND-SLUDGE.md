# Drill minerals and sludge

Revo drills provide the mineral feedstocks. Gems are recovered by processing sludge rather than dropping loose diamonds or lapis directly from drills. Native MI Steam/Electric Quarry controller crafting and assembler recipes are disabled; they are not part of the resource ladder.

## Before electricity

The Iron Drill's regular saved sequence now has **13 outputs**: its previous 12 minerals plus one **Mineral Sludge**. Wooden extraction is unchanged.

The Steel Drill's regular saved sequence now has **16 outputs**:

| Output | Count per regular sequence |
|---|---:|
| Raw iron | 3 |
| Raw copper | 1 |
| Raw tin | 1 |
| Coal | 2 |
| Raw zinc | 1 |
| Raw gold | 1 |
| Redstone | 1 |
| Raw lead | 2 |
| Raw antimony | 1 |
| Mineral Sludge | 1 |
| Gem Sludge | 2 |

As before, every 128th Iron/Steel output is Flux Dust instead of the regular scheduled output. Output blocking preserves the cursor and does not reroll resources. Existing water, RPM, repair and hopper behavior is retained.

**First battery route:** mechanical Steel Drill -> raw lead + raw antimony -> native ore processing -> equal lead/antimony dust mixed into Battery Alloy -> plates/curved plates -> Redstone Batteries -> Basic Machine Hull. Steel drill construction needs neither lead nor antimony. Gem Sludge supplies the first diamonds needed for the native Bronze Macerator.

## Washing gems

Use an existing Create Encased Fan blowing through water onto the sludge. Belts, depots and standard Create item handling can automate this. No new processing block is needed.

| Input | Native Create fan-washing output |
|---|---|
| 1 Mineral Sludge | 1 Quartz, guaranteed |
| 1 Gem Sludge | 1 Diamond + 2 Lapis Lazuli, guaranteed; 25% chance of 1 Emerald |

The Iron Drill sludge route makes quartz available before Steel Drill diamonds. The Steel Drill no longer outputs loose diamonds or lapis. Existing items in player storage remain valid. Ordinary mod processing byproducts and the existing Trading Colony are not removed by this change; these are the pack's primary drill/sludge supply routes.

## Electric extraction

All existing saved program indices **0–6** retain their meaning. New programs append to the menu:

| Program | Power and work | Output / requirements |
|---|---|---|
| Antimony (7) | 16 EU/t for 200 powered ticks; 3,200 EU total | 1 Raw Antimony. No survey core required. |
| Crystalline Sludge (8) | 128 EU/t for 1,200 powered ticks; 153,600 EU total | 1 Crystalline Sludge. Requires a retained Deep Survey Core. |

Mixed Metals now cycles silver, nickel, lead, bauxite and antimony. A selectable antimony route improves scaling after the mechanical bootstrap; it does not replace the earlier mechanical source.

Feed **one Crystalline Sludge** to the existing MI Centrifuge: **32 EU/t for 400 ticks** produces **two Certus Quartz Crystals and two Quartz**. The recipe requires MI and AE2 to be installed. The core/128 EU/t requirement keeps this beyond the first LV drill, and Certus comes from sludge processing rather than direct drill output.

This supplies starter Certus, not the complete AE2 unlock. Inscriber Presses, starter Sky Stone and deliberate storage/autocrafting gates remain separate work. Tungsten and uranium also remain later drill-tier work, outside the MV feedstock changes here.

Electric input/output restrictions, full-output pauses and saved work/cursor behavior remain intact. Switching programs resets partial work as before; removing the required core pauses advanced extraction without spending power.

## Quests and development

Four new quests cover battery minerals, first batteries, gem washing and crystalline sludge. First LV Electricity now depends on the first-battery quest. Existing quest IDs remain stable. The Early Quartz and Steel Drill resource quests explain sludge processing.

Generate assets and quests with:

```powershell
.local/venv/Scripts/python.exe scripts/generate_drill_minerals.py
```

Run this generator after older progression generators when regenerating the full pack. It preserves other quest entries and can be repeated safely.

Native verification:

```powershell
.\dev.cmd :revoextraction:runMineralTests -PrevoMineralTests
.\dev.cmd build collectMods
```

The test fixture covers real mechanical output/automatic export, native Create washing results, electric antimony, advanced core/power/save guards, actual MI Centrifuge Certus processing and disabled quarry controller recipes. Test-only sources are excluded from production JARs.
