# Progression balance audit through MV

Reviewed 19 November 2026 against base commit `17d9e1b` and the pinned recipes
for `0.1.47-dev`. The scope covers the starter island, Create, steam, LV, and
MV, including equipment and AE2 routes reachable during those stages. Updated
for `0.1.48-dev` after retiring the Trading Colony.

## Emerald crafting

Four Steel Drill Gem Sludge in a 2×2 crafting grid yield one guaranteed
emerald. The existing wash gives the same expected number of emeralds across
four sludge, plus its diamond and lapis outputs, but the emerald drops are
random. The new recipe trades those other outputs for certainty. The quest
book points players to the craft after the Steel Drill. Existing colonies
keep their stored items for recovery and have stopped trading.

## Changes in 0.1.49-dev

| Issue | Applied change |
| --- | --- |
| The starter rain reward ran for 15 seconds and claimed automatically. | Catch a Cloud now gives a manually claimed five-minute shower. Place the barrel under open sky first, then collect enough water to make three clay blocks. |
| The Mechanical Sluice gave early lapis, emeralds and diamonds. | The same saved output slots now give coal, raw iron and raw copper. The sluice still gives flint and quartz. |
| The Steel Drill produced two Gem Sludge every 16 outputs. | It now produces one every 32 outputs. The theoretical supply at 64 RPM falls from 960 to 240 diamonds per hour with continuous processing. Lead and antimony keep the same cadence. |
| Buffered LV power could unlock Certus and Ancient Debris immediately after first electricity. | The shared Deep Survey Core recipe now needs one MV Advanced Machine Hull. Both survey programs still accept buffered power once the core is installed. |
| The Iron Greenhouse skipped the seawater and centrifuge chain. | The retained geode yields Mineral Sludge. A new 8 EU/t MI Mixer recipe uses one sludge and 1,000 mB water to make 1,000 mB seawater. The chamber and centrifuge finish the salt. |
| FE generation could supersede the first steam turbine. | The Basic Power Converter is assembled at 8 EU/t using a spare LV Steam Turbine. Its existing upgrades still require the previous converter. |
| MV AE2 storage arrived alongside pattern autocrafting and Infinity Cells. | MV Electronic Circuits still unlock storage. The Crafting Unit, Pattern Provider, Molecular Assembler and both Infinity Cells now require an HV Digital Circuit. |
| Lava centrifuging supplied 11 copper nuggets per bucket. | The recipe gives two copper nuggets and still gives 17 sulfur dust. The other byproducts and processing cost stay the same. |

## Other checks

- The relevant quarry controller crafting and assembler routes have disabled
  overrides. The quest reward sweep found the starter rain command, free guide
  books and cosmetics. The recipe guard covers using the free manuals as
  input for production and recycling.
- The spawn egg mixin disables player spawning, dispenser spawning and
  offspring interactions. Placed spawner ticking is disabled separately.
- The literal-item recipe sweep read 13,464 definitions from pinned jars and
  Revo recipe overrides and checked 5,989 single-material crafting, stonecutting,
  smelting and blasting conversions. It found zero profitable two-step reversal
  pairs in that subset. Tag variants, longer chains, machine processing and
  runtime interactions require separate tests.
- The normal Create Crafts & Additions alternator has a 0.75 generation
  efficiency. The Revo converter uses the same FE/EU ratio in both directions
  and shares its per-tick budget. The examined routes establish early
  renewable generation. Cross-addon motor/generator loops still need a
  runtime throughput test before ruling out net-positive behavior.

## Source locations

- Rain quest and generator under `pack/overrides/config/ftbquests/quests/chapters/5e10000000000000.snbt` and `scripts/generate_first_chapter.py`. Duration parsing was checked in the installed Minecraft 1.21.1 `WeatherCommand` and `TimeArgument` sources.
- Extraction under `DrillTier.java`, `DrillMachine.java`, `SluiceEntity.java`, `ElectricDrillEntity.java` and `ElectricDrillStructure.java` in RevoExtraction. Recipes include `deep_survey_core` and `splashing/gem_sludge`.
- Emerald crafting under RevoExtraction's `emerald_from_gem_sludge` recipe. Legacy colony storage under RevoCultivation's `TradingColony.java`.
- Salt under RevoCultivation's `MachineKind.java` and `FarmMachine.java`, the `salt_crystal_starter` recipe and RevoIndustry's Salt Geode and Mineral Sludge mixing recipes.
- Recipe overrides include ExtendedAE `cobblestone_cell` and `water_cell`, AE2 `network/crafting/cpu_crafting_unit`, `pattern_providers_interface` and `molecular_assembler`, and MI `vanilla_recipes/centrifuge/lava`.

A full survival run through MV and measured factory throughput are still untested.
