# Create Sluice — fresh reference rebuild

Built from an empty Generic Model project in Blockbench 5.2.2 through the
desktop MCP server, using the owner's `Create-sluice med vandstrøm og blueprint.png`.
The earlier stepped model remains preserved separately.

The new geometry uses a broad two-block-width arrangement and three-block length:
one continuous 22.5-degree bed, open full-length lattice, solid timber side infills,
boxed intake, brass corners, square steel fittings, front outlet surround and a
copper side vessel. Small pipes, brackets and trim overhang the nominal block grid.
The 22.5-degree angle is a Minecraft-compatible cube rotation.

Files:
- `create_sluice_reference.bbmodel`: editable source, textures and native animation embedded.
- `from-scratch-animation.gif`: native Blockbench animation preview.
- `contact-sheet.png`: front, drive side, side profile and rear connections.
- `create_sluice_reference.gltf`: embedded-texture model with rigid animation, one unit per block.
- `clearance.json`: sampled moving-assembly clearance report.

The trough and its side panels move vertically by ±0.3 model units. The housing,
water, copper vessel and feet stay stationary. The eccentric pin follows the
guided yoke; bearing openings clear the square shaft throughout a turn. Suspension
pads scale around their fixed lower anchors. Preview gravel is a separate group.

107 cubes, 3 meshes, 9 groups, 5 textures; 1,306 exported triangles. The two solid
side infills use trapezoidal meshes, and the waterfall uses one quad. This retains
the reference's continuous silhouette with fewer elements than the prior model.

Balanced desktop Minecraft art target, native directional albedo lighting, no PBR.
Wood, steel, brass, copper and rubber now use a custom 256×256 pixel-art atlas,
painted in Blockbench through MCP at one pixel per model unit (16 pixels per
Minecraft block). Grain, dithering, rivets and the sight gauge were repainted
at this standard density after the earlier pass was too dense. The current wood
uses Minecraft Java 1.21.1's original oak-plank texture as its tiled base, with a
sparse dithered wear overlay. Existing metal fittings remain over the planks.
`textures/oak-base-atlas.png` and `textures/oak-wear-overlay.png` preserve the base
and transparent painted overlay separately. It includes
clustered dithering, wood seams and knots, edge highlights, metal wear, painted
rivets, copper panel seams, subtle patina and a small decorative tank sight gauge.
Create 6.0.10 supplies the unaltered shaft texture (`axis.png`) and end cap
(`axis_top.png`), including the correct opaque UV strips. The one-piece long shaft
intentionally stretches the original 16-unit shaft strip across 27 units.
Minecraft Java 1.21.1 supplies the unchanged original gravel and water textures.
The water retains its 32×1024 flipbook. Original artwork belongs to Mojang and
the Create authors. glTF carries geometry animation; Minecraft
water flipbook playback requires the target renderer's texture animation support.

Validation: 65 convex-SAT poses, including trapezoidal side panels, with no contacts
between separate solid moving assemblies beyond the 0.025-unit touch tolerance.
Native editor world bounds were checked at the turn's extrema to confirm the pin
and yoke rise together. Water has no transform animation. The native project and
glTF both contain the working clip.

## Texture overhaul and flicker cleanup

`before-texture-overhaul/` preserves the previous embedded model and preview.
The texture pass preserves the animation exactly and keeps the same element count.
Join faces were cleaned up to remove flicker: grate crossings have distinct surface
heights, the intake joints no longer duplicate exterior planes, sill and collar
edges are separated, side infill normals face outward, and hidden infill bottom
faces are removed. The yoke's transfer reaches the moving timber box.
`coplanar-audit.json` reports zero overlapping same-facing solid planes.
`texture-verification.json` records stock texture byte comparisons, custom UV
density, outward normals, unchanged water/gravel/animation, and clearance checks.
`tank-detail-v6.png` shows the copper treatment close up.

The current standard-density model is also saved as `sluice-standard-v7.bbmodel`.
`before-standard-density/` preserves the earlier two-pixel-per-unit model. The
larger atlas size combines many parts; each individual block-length surface uses
16 texture pixels. Water, gravel and Create's original shaft textures are retained.

The oak-base revision is `sluice-oak-v8.bbmodel`; `before-oak-base/` preserves the
preceding model. Its geometry, UV density, animation and non-wood materials are
unchanged. Oak planks retain their original 16×16-pixel grain and plank seams.

This is the new model/animation source. Its wider footprint, continuous water
surface and mesh side panels require a new runtime rendering/placement integration;
the previous three-block mod build is not a runtime export of this rebuild.
