# Revo machine modeling and texturing workflow

The single-block Composter and Silk Worm Cultivator are passive, fixed-time
machines: no Create shaft, RPM input/display, speed bonus or tamping control.
Their visible fill and maturation must follow real input, work and output data.
Keep the simple dithered wooden vessels and central silk rack. See
SINGLE-BLOCK-CULTIVATION.md for the current 8-second and 20-second cycles.

Use this as the starting brief for the next Revo machine. It records the accepted
wooden drill conventions and delivery checks, rather than requiring another model
to copy every decorative part. The current editable `.bbmodel` is authoritative.

For the early single-block Composter and Silk Worm Cultivator, the owner prefers
simple wooden bucket/tray shapes and one wood material, retaining the central
silk rack. Dithering must be clearly visible: use clustered light/dark pixel
variation, broken grain and worn patches. Barely perceptible noise on otherwise
clean strips is insufficient. Inspect the texture on the model and in-game at
normal viewing distance. See `SINGLE-BLOCK-CULTIVATION.md` and the shared native
`art/blockbench/small-cultivation/wood_paint.js` palette recipe.

## 1. Start from the actual project

- Target: Minecraft Java 1.21.1, NeoForge, Create-powered block entity.
- Project: `Rassers_Revolution_Sky_Edition`; machine module: `revoextraction`.
- Preference: balanced appearance and performance. Spend geometry on silhouette,
  openings, moving parts and interaction surfaces. Paint small fasteners and wear.
- Discover the connected Blockbench MCP version, active project, exact format and
  available tools. The app uses JavaScript; Lua is not needed for this workflow.
- Inspect the live project before editing. Preserve the owner's changes, removed
  parts, lowered roof and UUIDs. Save a separate native snapshot before revisions.
- Do not rerun the historical construction scripts over an owner-edited model.
  They document individual passes; they are not an idempotent rebuild pipeline.

Relevant skills: Blockbench use, modeling, texturing, vanilla textures and animation.
Use dedicated MCP tools when available. Native API calls through `risky_eval` must
own correctly scoped Undo transactions. Do not edit the same file through both a
headless server and the live desktop app. Export/save through native codecs and
verify the complete files on disk.

## 2. Scale and coordinate contract

**A Minecraft block is 16 physical model units.** The drill occupies a 48 x 48 x 48
physical envelope. Its component blocks are authored at ordinary 0..16 block size.
The complete assembled and placement-guide sources are uniformly divided by 3:
their 3-block envelope spans 0..16 source units. Runtime rendering multiplies their
scale by 3. Keep all three coordinate systems distinct:

| Quantity | Physical model units | Full-machine source units | In-game blocks |
|---|---:|---:|---:|
| One block | 16 | 16/3 = 5.333333 | 1 |
| Full 3-block height | 48 | 16 | 3 |
| Source-to-world conversion | P | S = P/3 | W = 3*S/16 = P/16 |

Do not divide the component block models by 3: they already render at normal size.
Java runtime model coordinates are measured in sixteenths; the baked model renderer
handles that normalization before our uniform scale. This arrangement keeps the
assembled export within Java block model coordinate limits.

Source orientation: +X is the machine's right, +Y is up, front is -Z. The machine
footprint runs X/Z=0..48 physical units. The runtime rotates this source orientation
to the controller's horizontal facing. World-facing names and a screenshot's
left/right can differ; use the controller's facing as the reference.

## 3. What 'block 2 in height' means

Layers are numbered from the ground, while code indices start at zero:

| Layer | Code Y | Physical Y extent | Source Y extent | Physical centre |
|---|---:|---:|---:|---:|
| Block 1 / bottom | 0 | 0..16 | 0..5.333333 | 8 |
| Block 2 / middle | 1 | 16..32 | 5.333333..10.666667 | 24 |
| Block 3 / top | 2 | 32..48 | 10.666667..16 | 40 |

A shaft attached to the second block must be centred at **physical Y=24**, source
Y=8, world height 1.5 blocks above the machine base. Placing it at a block boundary
will visibly miss Create's shaft. The right outer attachment face is at physical
X=48, Y=24, Z=24. Its coupling spans physical Y=22..26 (4 units thick).

The controller's visible housing spans the full second layer, Y=16..32, on the
front-centre block, physical X=16..32. It has a shallow assembled housing so the
large drill flight cannot clip through its back. A separate rear mount joins the
upper body above the flight's sweep. The standalone controller remains a full
block deep for ordinary placement.

## 4. Accepted wooden drill dimensions

These values include the owner's manual edits. They are reference dimensions,
not a reason to reset an edited model to an earlier version.

| Part | Physical Y extent / centre | Notes |
|---|---:|---|
| Cobblestone feet | 0..4.5 | Square 12 x 12 footprints |
| Iron footing caps | 4.5..5.5 | Touch stone; do not leave overlapping side faces |
| Oak-log pillars | 5..43 | 8 x 8 cross-sections |
| Lower stone-brick collars | 16..21 | Square 12 x 12, matching foot footprint |
| Upper stone-brick collars | 39..44 | Preserve the full square collar, not L-shaped strips |
| Lower timber rails | 17..22 | Keep the hatch connector attached to the underside |
| Upper timber rails | 38.25..43.25 | Lowered by the owner |
| Roof crossbeams | 38..40 | Owner's lowered roof |
| Roof metal straps | 40..41 | Raised detail above the crossbeams |
| Top bearing cap | 41.75..42.75 | Owner's lowered bearing |
| Drive socket / gear axis | centre Y=24 | Match Create's middle-block shaft centre |
| Controller housing | 16..32 | Full middle-layer face |
| Central drill axle | 2.4..24.5 | Reaches into the lowest cutting disc |

The full logical top layer still extends to Y=48 even when the visible roof is
lower. Never infer multiblock layer positions solely from a decorative beam.

## 5. Assembly blueprint and runtime alignment

All grids are top views; front is the bottom row:

```
Bottom       Middle       Top
L C L        L C L        L C L
H . C        C D I        C C C
L C L        L M L        L C L
```

L = custom Wooden Drill Support; C = casing; D = drill core; I = Create input;
H = output hatch; M = controller; . = air for the bit. Twelve supports, ten casings,
one core, input, hatch and controller: 26 blocks plus one air space.

The output hatch is the lower side block opposite the drive input. Its visual
housing reaches the ground. Keep its inward depth outside the drill's rotational
sweep and connect it to the upper support rail. Custom support blocks allow formed
states to hide placed geometry without replacing or losing player-owned logs.

`DrillLayout.part` is the blueprint shared by validation and the EMI layer guide.
`DrillMachine.at(x,y,z)` uses controller-relative logical coordinates: X=-1..1,
Y=0..2, Z=-1..1, with the controller at (0,1,1). Its offset along the outward facing
must be **z-1** so that the controller maps to itself. Test all four orientations.

## 6. Moving-part pivots

| Moving group | Physical pivot | Source pivot | Renderer normalized pivot |
|---|---|---|---|
| `drill_bit` | (24,18,24) | (8,6,8) | (0.5,0.375,0.5) |
| `drive_gear` | (46,24,24) | (15.333333,8,8) | (46/48,24/48,0.5) |

- The bit rotates around Y; the side gear rotates around X in the opposite sign.
- Group origins are pivots, not additional translations to add to cube bounds.
- Moving a gear or shaft requires updating the cube geometry, native group pivot,
  preview animation and runtime renderer together. UI inspection alone cannot
  establish in-game alignment.
- The Blockbench `working` clip is a four-second linear loop, with quarter-turn
  keys at 0,1,2,3,4 seconds. Runtime animation follows Create's working RPM instead.
- Keep the central axle embedded in the lowest disc. Use a slightly narrower axle
  so it does not leave coincident side faces on the final disc.
- Test the full swept radius, not just the rest pose. For a square flight rotating
  about its centre, its maximum radius is the distance from centre to a corner.
  Check that static housings and connectors remain outside this radius at every
  overlapping height. The drive stops animating when no work can be performed.

## 7. Minecraft materials and pixel density

The accepted style uses Mojang oak log sides/end grain, oak planks, cobblestone,
stone brick and stone. Import actual vanilla PNGs using the vanilla-texture
resolver and retain its manifest and Mojang attribution. Preserve the original
files; bake edited/tiled copies into the machine atlas.

- Native density is **16 pixels per block**: 1 pixel per physical unit.
- Full-machine sources therefore use **3 pixels per source unit**.
- Component models use 1 pixel per source unit.
- One shared 512 x 512 atlas contains the active machine's materials and panel
  patches. Unused atlas space is deliberate room for isolated revisions.
- Oak log sides run vertically; end grain belongs on the caps. Oak plank patterns
  cover rails and panels. Cobblestone belongs on the feet; stone brick on collars.
- Long surfaces repeat the actual tile at native scale. Do not stretch a single
  16px square across a whole beam, and do not assume atlas subregions wrap.
- Map proportionate rectangles to each face. For cube dimensions (X,Y,Z), face
  sizes are X/Y for north/south, Z/Y for east/west and X/Z for up/down.
- Pixel spans equal face lengths times the density above. Account for UV rotation.
  Fractional thin geometry may use a fractional span within a padded patch.
- Reserve padding and extend edge pixels to prevent neighbouring material bleed.
  Use nearest-neighbour sampling for authored pixel art.
- Dithering and wear are sparse and low contrast: occasional one-pixel dark/light
  variation, chips along rims, directional scratches and shaded fasteners. Keep
  the underlying vanilla material readable. Paint small bolts; don't add a cube
  for every screw.
- Shared UV patches are intentional texture reuse. They do not cause z-fighting.

When changing geometry, remeasure its face dimensions and update UV spans. Allocate
new patches if the old patch cannot hold the revised footprint; don't stretch the
old swatch or overwrite another part's patch. In native scripts assign a **copy**
of cached UV arrays (`uv.slice()`): shared mutable arrays can apply an adjustment
more than once when two faces reference the same object.

## 8. Controller tiers and visual language

The active primitive controller is stone-faced with an ivory analogue meter,
a brown switch and ceramic fuse cap. Those outside meter graphics are decorative;
actual machine state is shown in the right-click interface.

The charcoal/green-screen electronic controller is archived under
`art/blockbench/revoextraction/variants/modern-industrial`. It includes embedded
editable sources, runtime exports, atlas, previews and hashes. Preserve that art
for a future higher tier. It is not a registered tier yet. Before a redesign the
owner wants to keep, save an independent atlas and exports as well as `.bbmodel`:
changing one shared PNG can otherwise silently change a preserved variant.

## 9. Intersections and texture flicker

Structural parts can intersect internally. The problem is **exposed coplanar
faces** occupying the same plane, especially with different textures. Those can
flicker in-game as well as in Blockbench.

Prefer matching geometry boundaries, removing completely hidden faces, or slightly
recessing a bracket. Don't move parts far enough to create a visible gap. Here,
stone feet terminate at the metal cap's lower plane; hidden touching faces are
omitted. The output connector joins the rail from underneath; the lip and bracket
are offset so their visible faces do not coincide. The rear controller mount has
a small recess at the body seam.

After edits, check same-facing axis-aligned planes with overlapping rectangles.
Rotated parts need a separate visual/swept-volume check; a simple plane audit is
not proof for arbitrary rotated geometry. Inspect front, both sides, top and the
underside, then sample the working loop including 3.999 and 4 seconds.

## 10. Runtime and interface handoff

Deliver component block models and the assembled model. The assembly exports
three groups separately: body, bit and gear. Register the body/bit/gear as extra
client models and render the last two around their pivots. Editable `.bbmodel`
files alone do not implement registration, rotation, inventory or multiblocks.

Runtime files live under `revoextraction/src/main/resources/assets/revoextraction`:
`models/block`, `models/item`, `blockstates`, `textures/block` and `lang`. Recipes,
loot and tags are under `data`. Save editable sources under `art/blockbench`.
Use the native project codec for `.bbmodel` and the Java block codec for runtime
JSON; preserve embedded textures, per-face UVs, allowed cube rotations and AI
provenance metadata. Include texture references that resolve in Minecraft.

The controller is a server-synchronized diagnostics interface with **zero inventory
slots**. It shows Create RPM, operational status and cycle progress. The output
hatch opens its own storage interface: nine extraction-only machine slots above
the player's inventory. Normal item clicks and shift-click extraction work there;
player items cannot be inserted into output-only slots. Validate proximity and the
hatch's continued connection to the same machine on the server. Dismantling must
invalidate an already-open hatch window. Preserve saved output during UI changes;
never create a second cosmetic inventory in a client screen.

This follows MI's separation of item-hatch slot grids from the multiblock controller:
[MI ItemHatch source](https://github.com/AztechMC/Modern-Industrialization/blob/1.21.x/src/main/java/aztech/modern_industrialization/machines/blockentities/hatches/ItemHatch.java)
and [hatch layout documentation](https://github.com/AztechMC/Modern-Industrialization/blob/1.21.x/docs/ADDING_HATCHES.md).
The primitive hatch auto-exports at cycle completion through its outward face,
retains rejected output and retries when blocked. It does not copy MI's locking controls.

Stamp controller and construction-guide interfaces with a **small black R in the bottom-right**
(no shadow), using `RevoScreenBrand.draw`. Leave it below slot rows and clear of
status labels and controls. The drill's interactive EMI construction view and larger guide also carry their R
in the bottom-right. Keep this placement in future machine UI briefs. The controller
panel is 230x154 logical pixels; the hatch window is 176x184. Both fit normal GUI
scales and wrap hints to support the pack's font.

## 11. Verification commands

Run from the repository root:

```powershell
python scripts/validate_drill_assets.py
.\dev.cmd :revoextraction:build collectMods
.\dev.cmd -PrevoDrillTests :revoextraction:runDrillTests
```

Asset validation covers embedded images, dimensions, Java export limits/rotations,
UV bounds and effective density. Keep targeted reports for clearance, heights and
exposed coplanar faces as well. Nine isolated game tests cover formation in all
orientations, dismantling, output/save behaviour, real Create crank production and
menu transfers/diagnostics. They run in a separate generated test world.

Optional visual interface QA uses `-PrevoDrillUiQa :revolib:runClient` and a copied
save named `Revo Drill Interface QA`. It places a test drill only inside that copy,
opens controller and hatch windows through actual block interaction, captures both,
then captures the interactive EMI construction scene and the larger guide, including
single-layer and highlighted-port views, before exiting.
These test classes are excluded from normal jars. A normal production build must
follow any build using optional test source sets. Verify the jar includes the
latest models and atlas and excludes test-only classes/templates.

Use the shared RevoLib 3D guide for future multiblocks. Follow
[REVO-MULTIBLOCK-GUIDES.md](REVO-MULTIBLOCK-GUIDES.md) for blueprint data, canonical
facing, materials, port roles, layer controls and EMI registration.

## 12. Reusable brief for the next machine

Record the following before modeling:

1. Target version, block dimensions, intended tier and reference image.
2. Appearance/performance preference and a concrete cube/face budget.
3. Physical, source and world coordinate conversion; base origin and front axis.
4. Layer extents and exact centres of input shafts, ports and controller faces.
5. Static/moving group ownership, pivots and runtime animation strategy.
6. Material files, attribution, texel density, atlas size and UV padding.
7. Collision/selection requirements separate from decorative geometry.
8. Inventory/automation behaviour, diagnostics and EMI assembly layout.
9. Small black R at the bottom-right of controllers/guides; inventory hatches have no R/footer.
10. Editable sources, runtime exports, preserved variants and verification evidence.

For this drill, the latest native source and the owner's changes take precedence
over hardcoded dimensions in historical scripts. Use targeted edits, save a native
checkpoint, verify the result and preserve any version the owner wants for later.

For current machine UI, player inventory geometry, auto-output, orientation errors
and Jade conventions, follow [MACHINE-INTERFACES.md](MACHINE-INTERFACES.md).

The wooden Tree Grower, Greenhouse and Mob Harvester now provide a second worked
example: [FIRST-TIER-MULTIBLOCKS.md](FIRST-TIER-MULTIBLOCKS.md). Their native front
is −Z, their ground controller/hatch use the first full block of height, and their
48-unit assembled art uses the same divide-by-three/export/restore-scale technique.
Use that document's height table and native builder for those machines; do not
apply the historical drill's local-axis/port offsets to them.

The water-cooled Steel Drill is a further native Blockbench example. Its new portal frame,
glass reservoir, separate runtime vanilla-water renderer and repair system are documented
in [STEEL-DRILL.md](STEEL-DRILL.md). Its 84-cube editable assembly includes one editor-only
water-volume preview; the runtime water level follows the real tank contents.

Wide farm example: [METAL-FARMS.md](METAL-FARMS.md) records the 80-unit footprint,
divide-by-five exports, Y=16 soil alignment, continuous steel ribbon, item duct,
steel-framed glazing, supported ceiling rails and exclusion of editor-only
tree/crop/water proxies from runtime meshes.
