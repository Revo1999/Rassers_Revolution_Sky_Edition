# Current production surface repairs

`manifest.json` maps each runtime model to its authoritative editable Blockbench
source and sibling **native MCP Java export**. The 48 projects were imported,
edited, exported, and validated through Blockbench MCP. Original family assembly
projects remain useful design references, but their older geometry is not the
production override for a model listed here.

Hidden portions of overlapping faces have been removed. Partly exposed faces
use UV-preserving rectangular partitions (`__visible_...`). These are individual
visible faces, not extra solid blocks. Some completely covered cubes are omitted
by Blockbench's native exporter. The extra partitions increase quad counts in
some models; this repair addresses surface correctness and does not claim an FPS
improvement.

The greenhouse panes reach inside the timber frame. Vacuum ring sections have
clear axial steps. Sluice gear faces have real axial separation, with clearance
for the bearing and yoke. The geode rock bed is trimmed around the sockets, and
the water volume has no internal shared interface faces. Primitive vessel fluids
submit only their exposed top face.

## Editing and collecting

1. Open the manifest's `.bbmodel` through Blockbench MCP. If the server limits
   file access, copy it into the server workspace first.
2. Edit and export through MCP, preserving a Java 1.9.0 export suitable for
   Minecraft 1.21.1. Copy the final source and native export back here.
3. Run `python scripts/collect_machine_surface_repairs.py --refresh`.
4. Run `python scripts/check_all_machine_models.py`. For sluice motion changes,
   also run `python scripts/check_sluice_clearance.py --runtime`.
5. Build the production mods and package with the normal project tooling.

The collector preserves runtime loader metadata, particle references, and render
layers. Embedded preview water/lava sprites are explicitly rebound to Minecraft's
animated atlas sprites in `textureBindings`; it never authors geometry.

`native-validation.json` records MCP checks for all 48 final projects. The full
scope and limits are in `docs/ALL-MACHINE-MODEL-AUDIT.md`.
