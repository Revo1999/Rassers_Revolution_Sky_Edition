# Create material study

Compared the original assets in the installed `create-1.21.1-6.0.10.jar`
with the owner's four Create reference images. `create-original-textures.png`
shows the original pixels enlarged with nearest-neighbor sampling.

Primary sources:
- [Create's material roles](https://github.com/Creators-of-Create/Create/wiki/Create%27s-Materials): andesite kinetic machinery, brass logistics, copper fluid equipment.
- [Mixer geometry and UVs](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/resources/assets/create/models/block/mechanical_mixer/block.json).
- [Basin geometry and UVs](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/resources/assets/create/models/block/basin/block.json).
- [Native cog geometry](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/resources/assets/create/models/block/cogwheel.json).

Applied in Blockbench through its desktop MCP server:

| Part | Original Create asset | Mapping |
| --- | --- | --- |
| Channel side panels | mixer_base_side | UV 0,0 to 16,6; cutouts retain continuous panel coordinates |
| Shafts and frame trim | axis, axis_top | Original dithered andesite steel; proper shaft end caps |
| Feeder wet walls | basin | 32-pixel atlas, half-scale logical UV spans |
| Feeder rim | brass_casing | Gold edge strips, avoiding stretched full casing faces |
| Eccentric pin | brass_block | Small central metal region |
| Grille and bed | industrial_iron_block | Native dark steel |
| Drive cog | cogwheel | Original six non-axis elements and UVs, scale 0.38, axis rotated to X |
| Fluid inlet | copper_casing | Existing copper manifold retained |

Runtime JSON references Create resources directly. Embedded copies in the editable
model are for standalone preview. Original Create artwork belongs to its authors;
this study does not change its licensing. Motion, fluid connections and processing
logic were preserved. The new cog reduces total cubes from 131 to 127.

## Owner's supplied grate-sluice design

`owner-design.png` is the visual reference supplied subsequently. The final model
adapts its boxed timber feeder, brass opening surround, steel corner clamps,
full-length lattice and side copper vessel. It has 181 cubes. The existing
three-block channel, water levels and connection positions are retained.
The side vessel is a visual fluid housing; no sludge recipe or output is added.
The previous 127-cube material-study model is preserved under `preserved-v3`.
Native shaft-texture UVs are confined to its opaque U=6..10 strip, including
rotated strip UVs on long trims, so frames do not disappear through atlas alpha.
