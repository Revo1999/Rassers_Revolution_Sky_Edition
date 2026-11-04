# Wooden Drill asset plan

Preference: balanced appearance/performance. Target: Minecraft Java 1.21.1 desktop,
NeoForge block models and a block-entity renderer, typically seen from 2–12 blocks.
Use vanilla diffuse/cutout shading; no shader or PBR requirement. One shared 256x256
pixel atlas. Material swatches are 64x64; map at one pixel per physical model unit
(16 pixels per block). The assembled source uses 48 units across; runtime exports
are uniformly divided by three and the renderer scales them back by three.
UVs retain physical texel density. No out-of-swatch wrapping.

Budget: under 120 cubes / 1440 triangles for the assembled machine, under 24 cubes
per component block. Hidden faces may be removed when they remain internal.
Bolts and fine grain live in the atlas. Large metal fittings, open hatch depth,
legs, gears and drill silhouette use geometry. One rigid rotating bit bone, one
gear bone. Preview animation is linear, four seconds per revolution; runtime
motion follows Create working RPM. Collision/interaction stays on the 3x3x3 grid.

Use a custom craftable wooden support for the twelve corner positions so its
formed state can hide the unassembled log-shaped blocks without replacing or
losing vanilla player blocks. The assembled asset contains all visible posts.

Deliver six component .bbmodel sources, one assembled .bbmodel with loop preview,
exported runtime models and atlas, and multi-angle previews. Validate native source
and runtime export counts, dimensions, UV density, texture links and animation
pivots. In-game throughput and FPS remain separate from editor visual checks.
