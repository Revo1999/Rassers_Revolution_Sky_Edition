# RevoIndustry Vacuums

Three passive, single-block item collectors. Each has a large hollow octagonal intake pipe, a recessed fan and a mechanical motor housing. Four-tone palettes and classic single-pixel checker dithering shade the painted housing, metal flange and dark duct at 16 pixels per block. Red, blue and violet identify Mk 1, Mk 2 and Mk 3. Their pipe diameters grow from 10 to 12 to 14 pixels. Mk 2 gains side cooling panels; Mk 3 adds braces, top rails and a second indicator.

| Tier | Pickup radius | Buffer slots | Transfer interval | Output rate |
| --- | --- | --- | --- | --- |
| Mk 1 | 3 blocks | 1 | 8 ticks | 2.5 items/s |
| Mk 2 | 5 blocks | 3 | 4 ticks | 5 items/s |
| Mk 3 | 7 blocks | 9 | 1 tick | 20 items/s |

Place on a chest or another item inventory. The rear plate outlet points into the clicked block, with all six directions supported. A Create/MI or tagged wrench rotates the outlet; sneak reverses the rotation. The pipe mouth is always opposite this output face. The entire model and tier-specific collision shape follow the outlet orientation. Right-click opens the buffer.

Loose items within the spherical pickup radius move toward the pipe mouth. White end-rod particle streams show the suction direction while items are being pulled. Items enter the buffer near the mouth. Full buffers stop attracting stacks they cannot accept; partial pickup leaves the remaining items in the world. Items are pulled physically and can be blocked by walls. Only already-loaded nearby entities are searched; the vacuum does not load chunks. A scan handles at most 12 eligible entities every four ticks.

The vacuum transfers one item per interval into the attached inventory, respecting its sided item capability. Full or missing destinations preserve the buffered items. The inventory and transfer cooldown survive saves. Breaking a vacuum drops its block and all buffered contents. No fuel, FE or EU is required.

Recipes appear in EMI. Mk 1 uses iron, hoppers and redstone. Mk 2 upgrades it with steel; Mk 3 upgrades Mk 2 with diamonds.

## Validation

- Five native server GameTests cover all tiers, exact slot counts, transfer cadence in all six directions, full destinations, partial pickup, save/load, range boundaries, break drops and actual world-tick attraction/collection.
- Real pack-client screenshots show each revised tier, all three menus, the rear output plate and live suction particles.
- Editable Blockbench projects and native Java block exports are under `art/blockbench/vacuums`. The tiers use 24/26/31 cubes and seven 16px textures each and proportionate per-face UVs; all geometry stays within one block.
- A coplanar-face overlap check verifies that same-facing surfaces do not overlap, preventing z-fighting at ring seams and trim.
- Production jars exclude the opt-in test and screenshot helpers.

Rebuild assets with `python scripts/generate_vacuums.py`, then run the emitted `art/blockbench/vacuums/build_native.js` in Blockbench through MCP, then `python scripts/update_vacuum_shape.py`. The native script saves editable projects, runtime models and previews. Run tests with `:revoindustry:runVacuumTests -PrevoVacuumTests` and graphical checks with `:revolib:runClient -PrevoVacuumUiQa` against the isolated copied `Revo Vacuum QA` save.

To repaint textures without rebuilding geometry, run `python scripts/vacuum_textures.py`, then run `art/blockbench/vacuums/retexture_native.js` through Blockbench MCP. This updates the native texture bitmaps while preserving cube UUIDs, geometry, UVs and the outliner.
