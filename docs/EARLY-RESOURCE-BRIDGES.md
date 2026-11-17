# Early resource bridges - 6 November 2026

- Create Millstone: one MI Coke becomes one MI Coke Dust, processing time 100. Use native Forge Hammer iron dust, 7 iron dust + 2 coke dust, then the MI Steam Blast Furnace. This route is available before diamonds.
- Wooden and Steel Mob Harvesters: insert a crafted Cow spawner into the controller. Each cycle yields 1 leather and 2 raw beef. Wooden cycles take 300 ticks; steel cycles take 150 ticks. Craft a Cow egg from wheat, compost and wheat seeds, then surround it with eight iron bars to make the retained spawner program. Eggs and placed spawners cannot spawn mobs.
- Steel Mob Harvester only: select Blaze for 1 blaze rod per 150-tick cycle. Wooden filters have four modes; steel filters have five. Existing Zombie/Skeleton/Spider mode IDs are preserved.
- Iron and Steel Drills: every 128th completed output is 1 Flux Dust instead of the regular item. This is a deterministic saved cycle, not a random bonus roll. A full output pauses the cycle; reload does not reroll it. Wooden Drills never produce Flux Dust. Without Flux Networks installed the normal output remains.
- Contained Cow and Blaze use vanilla renderers and remain machine-owned, saved, harmless entities. They do not spawn into the world. Blaze capture into a Create Burner is not implemented.

EMI descriptions and Chapter 3/4 instructions document these routes. Existing quest IDs are retained. Ender Pearls, filling Blaze Burners and the other proposals in EARLY-PROGRESSION-AUDIT-2.md remain separate work.

## Verification

Six native GameTests passed with pinned MI, Create, Iron Furnaces and Flux Networks: actual milling recipe loading; Cow production and reload; steel-only Blaze production; tier filter wrapping and full-buffer behavior; rare-drop cadence; and a formed Iron Drill with blocked output/save/reload/Flux production. Opt in with `:revoextraction:runProgressionTests -PrevoProgressionTests`.

The isolated graphical fixture uses `:revolib:runClient -PrevoProgressionUiQa` and a copied save named Revo Resource Bridge QA. Test helpers are excluded from shipped jars.
