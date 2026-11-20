# Direct MI / Flux power converters

RevoLib supplies the optional native MI EU bridge and DirectPowerConversion.
RevoIndustry supplies three single-block converters. They convert electrical
FE and MI EU only. They have no kinetic/RPM input, shaft support, power inventory,
internal battery or stored energy. Both energy APIs report zero stored power and
zero storage capacity. There is no StoredFE field in new block saves.

## Tiers

| Converter | EU per tick | FE per tick at the current 10 FE/EU ratio |
| --- | ---: | ---: |
| Basic (existing power_converter ID) | 32 | 320 |
| Advanced | 128 | 1,280 |
| Elite | 512 | 5,120 |

These limits apply in both directions. The FE limit follows MI's server-side
forgeEnergyPerEu setting; both limits are written explicitly on each tier's EMI
information page and in its interface. Tier upgrades require the previous
converter plus four gold ingots (Advanced) or diamonds (Elite), two copper ingots
and two redstone. After starting the LV Steam Turbine line, run the Basic
converter recipe in the MI Assembler at 8 EU/t for 200 ticks. It uses one spare
LV Steam Turbine, one Flux Core, four iron ingots, two copper ingots and two
redstone. Keep your working turbine connected to the steam line.

## Ports and wrench rotation

Teal is the FE terminal. The opposite copper terminal is MI EU. Other faces are
closed. Right-click with a Create wrench, MI wrench, or wrench-tagged tool to
rotate the terminals. All six orientations are supported, including up and down;
capabilities and neighboring cable connections are invalidated after rotation.
Sneaking reverses rotation. Open with an empty hand to change conversion mode.

FE -> EU: Flux Receiver -> teal terminal -> copper terminal -> MI machine/storage.
EU -> FE: MI generator/storage -> copper terminal -> teal terminal -> Flux
Transmitter. Connect the EU terminal directly to a native MI machine or storage.
Use that MI storage as the connection point for an MI cable network. Flux receivers
are push-only sources, so an intervening pull-only cable is not a direct destination.

Power is forwarded directly into an accepting destination. Blocked or absent
outputs accept no input. Only complete conversion quanta are accepted: an offer
of 9 FE at 10 FE/EU is left at the source. The converter does not keep a remainder.
Simulation does not transfer power; repeated calls share one per-tick EU budget.
Changing mode or rotating does not replenish the budget. A reentry guard prevents
cyclic converters from recursively calling each other. Active forwarding is also
provided for adjacent sources that expose extraction rather than pushing power.

The interface has only the standard player inventory, a mode button, tier limits
and a small black R. Jade shows mode and transfer limits, without orientation
instructions. Existing buffered converter saves retain their block ID and mode;
legacy StoredFE is deliberately ignored because converters no longer hold power.

## Art, items and verification

All converter tiers share the same three-cuboid Blockbench geometry. Their main
enclosure colours and large top numerals distinguish them at a glance:

| Tier | Main colour | Top numeral |
| --- | --- | --- |
| Basic | Blue | 1 |
| Advanced | Green | 2 |
| Elite | Red | 3 |

The teal FE and copper EU terminals retain their shared connection artwork.
`art/blockbench/power-converter/paint_tier_colours.js` paints the twelve 16px
textures in native Blockbench, updates the editable tier sources and exports the
runtime models. Top numerals use bold 6 x 10 pixel glyphs; their UVs face the FE
front. Geometry remains three cubes per tier, with no extra number/decal geometry.
The previous palette is saved in `history/pre-tier-colours`. Flux equipment retains
its coordinated steel/copper/teal texture family without warning stripes.

Clay bucket and crook art is in art/items/chapter-one-refresh, including the
imagegen prompt, original generated sheet, native Aseprite sources and 16px PNGs.
Fired clay buckets use a terracotta bucket silhouette. The crook is a slim,
diagonal wooden hook inspired by the linked Ordinary Crook reference.

A lava-filled clay bucket is consumed after successful world placement, vessel
transfer, machine fluid-container drain, or furnace fuel use. Water use returns
an empty clay bucket. Failed and simulated operations do not break it. Creative
world interactions retain normal creative item behavior. Radio/Analog Audio and
its embedded libraries were removed; Building Wands and Create: New Age remain.

As requested, verification for this revision is kept light: Java compilation,
resource/pack checks and an asset preview. Gameplay testing is left to the pack
owner. Historical live-test reports describe the previous buffered converter,
not this new direct converter. The focused shared-library tests remain available
through dev.cmd :revolib:test but are not part of the final assemble-only check.
