# Skyblock promotional base

Six compositions in one connected showcase world:

1. A morning homestead with an oak/spruce lodge, tree grower, composting and silk production.
2. The glass conservatory by day, with two automated electric greenhouses.
3. A copper/andesite workshop at golden hour, with a working iron drill and pump-fed Create sluice.
4. The greenhouse at night, using its actual grow lights and warm hanging lanterns.
5. The night-shift foundry, with a functioning custom furnace, input/fuel hoppers and collected output.
6. A wider blue-hour view of the connected islands, skybridges, foundry and storage hall.

The base is built creatively on five shaped floating islands. The showcase uses
native creative motors and Create Addition's creative FE supplies for reliable
demonstration power. Inputs are stocked into real inventories; ordinary hoppers,
item hatches, fluid tanks/pump/pipe connections and machine ticks perform the work.
The screenshot originals are native 2560×1440 PNG captures with Complementary
Reimagined shaders and no HUD. No machine products are fabricated for the shots.

The fixture only loads under `-PrevoPromo` and checks for an isolated copied save
named `Revo Promo Skybase` before modifying its own region around (1000, 210, 1000).
Normal player worlds and production jars do not include the builder.

```
.\dev.cmd :revolib:runClient -PrevoPromo --offline --no-watch-fs
python scripts/package_promo_screenshots.py
```

The screenshots, scene descriptions and machine-counter proof are in
[`screenshots/promo`](../screenshots/promo). The contact sheet and HTML gallery
are previews; the six original PNGs are unchanged. The reusable base remains in
`revolib/run/saves/Revo Promo Skybase` and is intentionally outside Git.
