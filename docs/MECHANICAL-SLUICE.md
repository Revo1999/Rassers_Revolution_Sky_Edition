# Mechanical Sluice and Gravel Feeder

Minecraft Java 1.21.1, NeoForge 21.1.265, Create 6.0.10. Implemented in
RevoExtraction on the `feat/mechanical-sluice` branch.

The sluice is a three-block open channel with an exposed iron grille and a
shaft-driven eccentric crank. The separate feeder fits above its upstream block.
Andesite casings, Mixer wood panels, Basin steel, brass trim and wooden cog
textures resolve directly from the installed Create resource pack. The embedded
Blockbench textures come from the pinned Create 6.0.10 jar and preserve its pixel art.
The editable source has 181 cubes, including editor-only water and feeder
geometry; its upper bound is 2,172 triangles. Actual frame cost is not measured.

## Placement and connections

Place the Mechanical Sluice while looking downstream. Placement reserves all
three channel blocks and refuses an obstructed footprint. Place the Gravel
Feeder above its first block; it automatically matches the channel direction.
Both items have survival crafting recipes.

- **Rotation:** connect a Create shaft to either side of the first channel block.
  The shaft axis crosses the channel at the ordinary block center: physical
  Y=8, Z=8. Both shaft faces carry the same kinetic network.
- **Water:** attach a Create Fluid Pipe to the feeder's rear face. Its center is
  physical Y=24 above the channel base. Supply it with a Mechanical Pump and
  water source or tank. The pump uses Create's normal cogwheel connection; the
  sluice has its own transverse drive.
- **Gravel:** feed the upper face of the feeder using a hopper, chute or funnel.
  It has one gravel-only slot and a persistent 4,000 mB water tank. Water buckets
  also work for initial setup; lava and other fluids are rejected.
- **Products:** the last channel block exposes four extraction-only slots.
  Right-click it to open its collection window. It also pushes accepted output
  to inventories beside, below or downstream of that block, retaining and
  retrying rejected items.

Right-click the first or middle channel block for diagnostics. Its screen has
no inventory slots, and carries the usual small Revo R. The separate collection
window uses standard player inventory geometry and supports normal and shift-click
extraction. The feeder has its own one-slot inventory window.

## Motion and processing

The revised design uses the original Mixer side UV strip for warm framed wood,
the Basin atlas for the wet feeder, brass casing edging, industrial iron for the
grille, and Create's original small cog geometry scaled onto the drive axle.
The owner's latest supplied design is adapted as a boxed wooden intake, full-length
steel lattice, brass corners, side service hatch, low timber chassis and fixed copper
side vessel. The existing three-block channel and rear fluid/shaft ports are retained;
the image's footprint and sludge annotations do not add new gameplay mechanics.
The copper vessel represents fluid housing. The design uses the existing stepped
water levels rather than introducing a new slope or changing the working fluid pass.
The earlier designs are preserved under `preserved-v1`, `preserved-v2` and `preserved-v3`.
Research and exact material sources are recorded in the art folder's research notes.

Rotation runs the shaft and eccentric even when no gravel is present.
The entire box, including its bed, riffles and grate, moves **vertically only**:

`lift = 0.5 * sin(angle)`

There is no forward/backward box travel. The eccentric pin slides horizontally
inside a guided yoke, which raises and lowers the box. Front and rear rubber pads
compress with that motion; their lower ends remain on the fixed supports.
The native working clip is two seconds per shaft revolution. Runtime follows
actual Create RPM. Gravel advances downstream during processing.

Water is rendered separately using Minecraft's animated, biome-tinted water
atlas. UV scrolling runs downstream over each bed section, with vertical sheets
at the two steps, inlet and outlet. **The water surface and inlet stay steady** as
the box moves. The bed clears the rotating shaft, and the shallow riffles remain
submerged at every phase. Water appears only while the server has a real
water stream. It does not create source blocks or return water to the world.

Supplied water flows at **5 mB per tick**, including while the shaker is stopped,
empty or output-blocked. Pump throughput and shaker RPM are independent.
Processing needs at least **16 RPM**; the nominal rate is one gravel in
**100 ticks at 32 RPM**, capped at the processing rate for 128 RPM. Stress is
8 SU per RPM. At nominal speed a completed cycle uses 500 mB of flowing water.

The gravel wash uses a saved 128-cycle sequence:

| Output | Completed cycles per 128 gravel |
|---|---:|
| Flint | 96 |
| Quartz | 24 |
| Coal | 6 |
| Raw iron | 1 |
| Raw copper | 1 |

Wash Steel Drill Gem Sludge for diamonds and lapis, or craft four sludge into
one emerald. The sluice sequence remains stable across saves and blocked buffers.
Gravel is consumed only when a result fits and the cycle finishes. Partial work,
cycle cursor, water, input and output inventories persist. Both rotation
directions work. Dismantling the channel drops its saved products exactly once;
the independent feeder retains its own input and water until it is removed.

## Sources and verification

- `art/blockbench/revoextraction/mechanical-sluice/models/mechanical_sluice.bbmodel`
  is the editable model with independently pivoted groups and a working loop.
- The model was refined and exported in **Blockbench 5.2.2 through MCP 1.10.1**.
  `scripts/export_sluice_runtime.py` exports its saved native source through
  Blockbench's Java codec. The historical `generate_sluice_assets.py` blockout
  builder refuses to overwrite that source without an explicit reset flag.
- The preview water uses the actual Minecraft Java 1.21.1 flowing-water
  flipbook: **32 frames, 32×32 pixels per frame**, with ordinary blue biome
  tint. Untouched vanilla originals and attribution are in `assets/vanilla`.
- `scripts/validate_sluice_assets.py` checks all port centers, Java bounds, UV
  spans, texture references, all direction variants and 1,441 motion samples.
  Checks include steady water, vertical-only box travel, shaft/floor clearance,
  the pin inside its slot, submerged riffles and pad endpoints.
- `scripts/check_sluice_clearance.py` samples 129 poses using full oriented-box
  collision tests between separate moving assemblies. The current report has
  no conflicts. Static construction joints, parts rigidly joined within one
  assembly and fluid volumes are excluded; contacts below 0.025 units are ignored.
- `dev.cmd :revoextraction:runSluiceTests -PrevoSluiceTests --offline --no-watch-fs`
  runs six native regression tests, including an actual Create tank, pump,
  fluid pipes and two hand-crank networks.
- `dev.cmd :revolib:runClient -PrevoSluiceQa --offline --no-watch-fs` runs the
  visual fixture only in a copied save named **Revo Sluice QA**. It captures
  multiple views and moving frames into `screenshots/sluice`.

Test and visual-fixture classes are excluded from production jars. Run a normal
production build after optional test builds.
