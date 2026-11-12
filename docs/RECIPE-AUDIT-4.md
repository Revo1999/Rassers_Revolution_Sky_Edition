# Recipe audit 4 - 12 November 2026

Rechecked 13,468 local recipe definitions after implementing audit 3 items 1-10. Definitions include absent-mod compatibility recipes; those are not treated as usable routes. Native production and source inspection complement the recipe index. The intended controlled island routes remain the scope, not every possible spawning, trading, fishing or world-generation fallback.

Audit 3 is implemented: Certus extraction, first presses, Sky Stone, retained salt starter/water growth, advanced ore programs, Nether wart/Ghast, End Stone/chorus/Shulker, Wither skulls, smithing template and poppies. Exact costs and gates are in [the implementation guide](CRYSTAL-AND-LATE-RESOURCES.md).

## Next list - proposals only

| # | Remaining route / limitation | Suggested follow-up |
|---|---|---|
| 1 | Platinum feedstock | Add a high-tier platinum deposit to geological extraction. The drill currently lacks it; native raw-metal block conversion and recycling do not establish a first ore. Gate after tungsten, without requiring platinum in the first deposit core. |
| 2 | Iridium feedstock | Add a separate very high-tier deposit after nuclear power with a retained survey core. Keep the existing native processing chain. Distinguish this from worldgen/quarry fallbacks. |
| 3 | Controlled lignite | Add a lower-tier sedimentary deposit or deliberate organic compression recipe for MI lignite coal. It is an alternate chemistry/fuel branch, not an immediate power deadlock because ordinary coal exists. |
| 4 | Ink sacs | Add Squid to a water-fed colony or a small aquatic harvester, with 1 ink sac per cycle. Existing haunting makes glow ink from ordinary ink, so it still needs the first controlled ink source. |
| 5 | Honey / honeycomb starters | Add an apiary with a retained colony starter crafted from flowers and an early biological resource. Keep bottle/honeycomb extraction and native honey processing. Fluid-to-bottle recipes alone do not establish a honey source. |
| 6 | Rabbit resources | Add a later biological colony for rabbit hide and rabbit feet. Leather already works; this is specific recipe/potion coverage rather than another leather fix. |
| 7 | Aquatic resource automation | Add a water-fed fishing program for cod/salmon/pufferfish. Vanilla fishing remains a manual fallback, so classify this as automation coverage. |
| 8 | Turtle scutes | Add a slow hatchery program after an aquatic milestone. Preserve the distinction between shell equipment inputs and ordinary mob drops. Natural breeding is a fallback where turtles exist. |
| 9 | First sponge | Add a high-tier wet-sponge synthesis or aquatic colony unlock, then keep ordinary furnace drying. Check installed recipes before choosing the exact cost; no new controlled sponge-producing program is implemented. |
| 10 | Progression guidance beyond the nuclear survey core | Add connected quests for platinum/iridium chemistry and nuclear construction once those routes are chosen. The current uranium gate is a Nether-Star/lead/tungsten survey core plus 512 EU/t; it is not a claim that every nuclear fuel-machine chain has been played through. |

Suggested next focus: **1-2** for industrial completeness, then **4-5** for distinct biological resources. Rows 6-9 are optional branch coverage until a required recipe is identified.

## Specifically available, not missing

- Prismarine shards/crystals: native Create haunting of lapis. Soul sand and lapis are available now.
- Sulfur: native MI lava centrifuging. Bauxite electrolysis supplies aluminum and titanium tiny dust.
- Gunpowder: native washing of crushed zinc is an available route; a Creeper colony would be an optional biological alternative.
- Nether Star: three rare Wither skulls plus soul sand allow the normal Wither encounter. The machine does not spawn a boss for free.
- Salt: the new greenhouse program retains its crystal starter, consumes water and outputs salt. It does not consume salt to start.

## Verification limits

17 native regression tests passed after implementation, covering all added mob/plant routes, recipe loading, geological gates, saved counters and prior storage behavior. The screenshot run uses a separate gallery save, verifies formation before capturing every custom machine tier, and includes separate new-program views. These automated checks are not a full fresh-island survival playthrough or a nuclear balance benchmark.
