# Nexus Portal: first block-by-block art pass

Open `nexus_portal_assembly.bbmodel` in Blockbench 5.x. It is a **construction preview**, not a single model to render over a formed multiblock. Every ordinary structure component corresponds to a position in `portal-blueprint.json`.

- Footprint/bounds: **21 x 19 x 11** (width x height x depth).
- Blueprint: **814 individual placed blocks**; the preview has extra cubes for block-model details.
- `parts/`: **28 individually editable Java Block/Item models**, including the framed Nexus Core, six decorative block types, MI-style hatch previews and four directional fence-arm submodels.
- `java-models/`: individual 1.21.1-compatible exports, using `revoindustry:block/nexus_atlas`. They are art exports, not installed runtime resources.
- `textures/nexus_atlas.png`: authored native pixel-art tiles, with exact MI steel/hatch sprite reuse credited in `CREDITS.txt`.
- `textures/nexus_vortex.png` and `.mcmeta`: 64-frame, 9.6-second sprite animation study with interpolation. Its 256 x 16,384 strip stays within a 16,384-pixel texture dimension.
- `nexus_vortex.gif`: a quick preview of that sprite animation.
- `portal_swirl_preview`: a gentle **16-second** Blockbench rotation animation, with linear 45-degree keys every two seconds and matching start/end poses. **Only the portal surface moves.** Front/back faces are separated by 1/64 block to avoid coplanar overlap.
- `portal-three-quarter.png`, `portal-front.png`, `core-preview.png`: rendered review images.
- `asset-validation.json` and `mcp-validation.json`: geometry, model-bounds, animation and blueprint checks.

Railings connect to their actual north/east/south/west neighbours and solid tower blocks. They use a post plus two bars per connected direction. Corners turn correctly and the staircase opening stays clear. `blockstates/nexus_rail.json` is the planned multipart blockstate export; native fence neighbour updates still need runtime registration.

Texture variety follows structure: grated deck lanes, reinforced tower columns, ribbed panels, inset service access panels, lighter ceramic inner-ring blocks and chiseled foundation piers. These are substitutions at existing blueprint positions; the placed-block count remains 814.

The three production machines, recipes, hatches, Core logic and builder are specified in [NEXUS-ENDGAME.md](../../../docs/NEXUS-ENDGAME.md). They still need runtime implementation. The [machine art package](../nexus-machines/README.md) now includes the **21 x 21 footprint, five-block-high donut collider** and the two 9 x 9 machines; their geometry is separate from this portal model.

## Reproduce the art inputs

```powershell
.local/venv/Scripts/python.exe scripts/prepare_nexus_assets.py
```

This paints the textures and writes the blueprint plus `.local/nexus/*-operations.json`. Apply those operation batches through Blockbench MCP to author the assembly and individual models. The assembly uses Generic format; individual blocks use Java Block/Item. Embed the atlas, and embed `nexus_vortex_preview.png` as the assembly's second, emissive texture.

When the MCP server has a different configured output workspace, collect its resulting project directory with:

```powershell
.local/venv/Scripts/python.exe scripts/finalize_nexus_assets.py --source "C:\path\to\MCP\nexus-portal"
```

The collector copies the authored exports into this folder, makes texture paths local to the Modpack project, sets future runtime texture namespaces, and checks unique blueprint coordinates, individual block bounds and the portal-only animation target. Do not run it over a model you are actively editing in Blockbench without saving your work first.

In the current Blockbench installation, the saved camera angle **Nexus overview** frames this large construction. The original blank project tab is preserved.
