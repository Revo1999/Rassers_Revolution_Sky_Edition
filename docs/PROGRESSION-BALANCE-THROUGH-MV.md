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

## Remaining problems and shortcuts

| Priority | Finding | Evidence and impact | Suggested change |
| --- | --- | --- | --- |
| P1 | The first rain reward can run out before the clay bootstrap | **Catch a Cloud** automatically runs `/weather rain 300` when the player has a barrel. Minecraft 1.21.1 reads a bare duration as ticks, so this sets 15 seconds of rain even though the quest promises five minutes. A barrel needs 200 collecting ticks per clay block, and the crucible and first bucket together need 10 clay balls, or three clay blocks. Players can also spend the one-time shower while the barrel is still in their inventory. Natural rain can recover the route. | Use `300s` and trigger or claim the shower after the barrel is placed under open sky. Verify a fresh island can make the crucible, bucket and water source with the first shower. |
| P1 | Gem Sludge makes diamonds very fast once Steel forms | The Steel Drill yields two Gem Sludge in every 16 outputs. Each washes into one diamond and two lapis, with a 25% emerald chance. At the minimum 64 RPM, 600 work per output implies a theoretical **960 diamonds per hour** per drill. This assumes continuously supplied coolant, drained sediment and enough washing capacity. One output batch of two diamonds takes about 7.5 seconds of drill time. | Reduce Gem Sludge frequency or the guaranteed diamond yield before lowering the whole drill rate. The lead and antimony cadence supports first LV batteries. |
| P1 | The Mechanical Sluice bypasses the sludge-only gem rule | The Create-stage Sluice uses andesite casings, iron bars, a shaft, a water-fed gravel feeder and rotation. It produces an emerald on cycle 64 and a diamond on cycle 128 with zero drill sludge. One diamond takes about 10 min 40 s at 32 RPM or 2 min 40 s at 128 RPM. | Replace the two direct gem drops with non-gem outputs available at that stage. Route diamond and emerald production through drill sludge. |
| P1 | A pre-MV power line can do advanced Electric Drill surveys | The drill's hatch accepts native LV power and charges a 32,000 EU buffer. A Crystalline or Deep Survey tick checks the stored 128 EU and saves partial work across pauses. One steady 32 EU/t input can therefore pay for a 1,200-work-tick survey in about four minutes, before refining. The Deep Survey Core needs LV aluminum, silver, nickel, lead and a diamond. An MV Hull or an MV energy hatch is absent from that path. Certus and the first slow Ancient Debris route can begin before the MV power tier. | If MV is meant to be a hard gate, require an MV part in the retained core or an MV-rated hatch for those programs. Buffered operation is reasonable if 128 EU/t is just the full-speed rate. |
| P1 | The legacy Iron Greenhouse makes finished salt directly | The retained Salt Crystal Starter still runs as program 19. A powered Iron Greenhouse turns 250 mB of plain water into one Salt Dust every 200 active ticks at 8 EU/t. This avoids the drill sediment, Mixer, Salt Geode Chamber and Centrifuge. Building the greenhouse still requires the steel-era converter route. | Decide whether the old greenhouse should remain a compatibility route for existing saves. To keep the chamber as the main progression route, retire new starter crafts or give the greenhouse a role in the chamber's supply chain. |
| P2 | FE can replace the first steam turbine route | Create: New Age has steel-era power components, and the ordinary Iron Furnaces **Generator Augment** is craftable. Only the Factory Augment was disabled. A Copper or Iron Furnace defaults to 40 FE/t, while an Emerald Furnace defaults to 320 FE/t, equivalent to 32 EU/t through the Basic Revo Converter. The converter needs a Flux Core and the destination still needs its MI hull. This bypasses steam generation and scales with multiple generators. | Keep the route as an earned alternative or put the FE-to-EU bridge behind the first turbine if steam is intended to be mandatory. Its sustained fuel and startup costs deserve a side-by-side playtest. |
| P2 | AE2's storage and autocrafting can arrive at the same milestone | The server overrides correctly require MV Electronic Circuits for the processors, including the MI Assembler alternatives. The native Crafting Unit, Pattern Provider and Molecular Assembler then use that same chain, with no distinct HV part. ExtendedAE's Infinity Cobblestone and Water Cells are also craftable through a 16k component. The 16k requires nine Logic and four Calculation Processors via the component tree, plus glowstone. The Infinity Cells still draw network power. | Keep basic storage at MV. If later digital manufacturing is still intended, gate the first Crafting Unit or processing equivalent separately. Disable or move Infinity Cells later if they should never replace the early fluid and cobblestone lines. |
| P2 | Lava centrifuging is a strong secondary ore source | The native MI recipe yields 17 Sulfur Dust and 11 Copper Nuggets per 1,000 mB of lava, with chances for aluminum and chromium nuggets, at 32 EU/t for 600 base ticks. The primitive crucible turns four cobblestone into a bucket of lava and can run off its own lava-heated setup with ongoing cobblestone input. This provides a renewable repeatable sulfur and copper route outside drilling. It requires the Centrifuge, so it does not bypass the first aluminum or electronics. Create's early gravel wash also gives one Iron Nugget at 12.5% per gravel, a weaker non-drill source. | Keep sulfur access for MV chemistry. Review the metal byproducts against the drill sourcing rule before changing the baseline chemistry. |

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
- Salt under RevoCultivation's `MachineKind.java` and `FarmMachine.java`, the `salt_crystal_starter` recipe and RevoIndustry's Salt Geode recipes.
- Pinned upstream recipes include ExtendedAE `cobblestone_cell` and `water_cell`, AE2 `network/crafting/cpu_crafting_unit`, Iron Furnaces `augments/augment_generator` and MI `vanilla_recipes/centrifuge/lava`.

The remaining findings come from code, recipes and configuration. A full
survival run through MV and measured factory throughput are still untested.
