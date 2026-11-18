# Salt chamber animation sources

The five `geode_crystal_N.bbmodel` projects were split from the existing geode
crystals through Blockbench MCP, then natively exported as Java 1.9.0 models.
Each keeps its original pixel atlas and its stem/head geometry. The sibling JSON
files are the unmodified native exports; runtime collection adds only the cutout
render-layer metadata. The two generated bucket projects likewise use native MCP
exports and the vanilla water-bucket sprite.

`SalternAnimation` supplies smooth staggered growth and harvesting from the
machine's paid recipe progress. `SalternClient` positions each bud on the bed,
keeps the smaller corner crystal clear of neighbouring faces, and interpolates
the synchronized four-tick updates. No geometry is generated in the renderer.

Use Blockbench MCP for any geometry edits, then copy the native exports to the
corresponding runtime models. Run `scripts/check_all_machine_models.py` to check
the bed, electrodes and all five buds together through 80 animation poses.
