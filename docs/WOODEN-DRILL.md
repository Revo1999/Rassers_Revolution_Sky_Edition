# Wooden drill - implementation and assembly

Implemented in RevoExtraction for the pinned Minecraft 1.21.1 / NeoForge pack.
The design source is docs/design/Rassers_Revolution_Sky_Edition_v2.md section 7,
plus the supplied Wooden Drill concept image. Reference text is design material;
it does not authorize unrelated environment changes.

## Assembly

All grids below are top views, with the machine front at the bottom.
Craft twelve Wooden Drill Supports from logs and cobblestone. Place the controller
facing outward, the kinetic input facing out of the right side, and the output
hatch facing out of the lower left side. Right-click the controller to inspect.
The machine also detects valid assemblies automatically.

```
Bottom       Middle       Top
L C L        L C L        L C L
H . C        C D I        C C C
L C L        L M L        L C L
```

L = Wooden Drill Support; C = wooden drill casing; D = drill core; I = kinetic input;
H = output hatch; M = controller; . = air for drill-bit clearance.
Total: 12 supports, 10 casings, one core, one input, one hatch, one controller.
EMI provides the same three layers using the same blueprint definition as gameplay.
All six custom parts have crafting recipes and appear in Functional Blocks.

## Operation

Attach an existing Create hand crank or shaft to the right-side input. Create
propagates the actual network speed, including loss of drive and overstress.
The input consumes 4 SU per RPM; speed contributes up to 256 work per tick.
One item requires 1200 work: at 32 RPM that is 38 ticks for the first item.
The repeating provisional mixture is cobblestone, cobblestone, gravel, sand,
raw iron. Rates and unlock balance remain provisional. Later-tier programs and
replaceable head upgrades from the broader design document are future work.

The machine preserves nine persistent output slots. Right-click the hatch to
open its inventory; hoppers and other item handlers can extract through the hatch.
Insertion through the hatch is disabled. Full output pauses progress, preventing
voiding or rerolling the next result. Breaking the controller drops its inventory;
breaking other parts retains the controller inventory for rebuilding.
The structure is revalidated before every production tick. Missing or unloaded
parts stop production. A missing part resets work; unloading pauses production.

All six placed part types switch to invisible models on formation. The renderer
uses the Blockbench-exported body, bit and drive gear, with the latter two rotating
from synchronized working RPM. The 3x3x3 collision/interaction grid remains in place.
Supports are custom blocks so assembly never replaces or loses player-owned logs.
The bit's lower centre space stays air. Do not replace the supports with vanilla logs.

## Blockbench assets

All eight editable sources are in `art/blockbench/revoextraction`:
six component blocks, a complete pre-assembly structure, and the assembled machine.
The assembled source includes a four-second linear `working` preview loop for
`drill_bit` and `drive_gear`; runtime speed follows Create rather than this preview.
Sources and runtime exports use one shared, embedded 512x512 atlas with no PBR
requirement. Fasteners are painted; major fittings, feet, hatch depth and gear
silhouette are modeled. The machine has 76 cubes, 456 quads / 912 triangles.

The full-machine source and exports span 16 model units (one third of physical
size), which the renderer scales uniformly by three. The texture density is three
pixels per source unit / one pixel per physical unit, matching 16px Minecraft blocks.
This keeps runtime Java block geometry within its coordinate limits. Body, bit and
gear are exported separately from named native groups. The assembled model is not
a standalone placeable block; the registered controller renders it after validation.

`build_models.js` contains the native Blockbench construction helper. Sources are
the authoritative editable deliverables. Native project and Java codecs were used
through Blockbench MCP JavaScript; no Lua interpreter was needed. The session did
not expose the dedicated geometry/export tools, so native API calls managed explicit
Undo transactions and exported with the installed codecs.

## Verification

- Gradle build and three blueprint tests.
- `python scripts/validate_drill_assets.py`: checks embedded textures, positive
  dimensions, runtime bounds, Java rotations, all face UV densities and atlas bounds.
- Blockbench previews from multiple angles and animation samples at 0, 0.5, 1,
  3.999 and 4 seconds; bit and gear preserve continuous linear rotation at the seam.
- Full pack client smoke launch: model and registry loading reaches the main menu.
- Isolated NeoForge game tests cover every horizontal orientation and dismantling,
  output blocking/extraction, inventory serialization, and a real Create hand-crank
  network producing items and stopping when released.

Run the isolated game tests with:

    .\dev.cmd -PrevoDrillTests :revoextraction:runDrillTests

Test sources and empty template are only included when `revoDrillTests` is set.
A normal build excludes them. Game tests use a separate ephemeral test world; they
do not modify a player save. Tests do not measure FPS or replace in-game visual QA
under the pack's shader. Later head upgrades, programs and final rates remain future
progression work.

Development API references:
https://wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1
https://docs.neoforged.net/docs/1.21.1/misc/gametest/
Create/EMI runtime jars remain supplied by the existing pack lock and sync script.

## Texture revision

The texture pass uses fitted face patches rather than cropping generic swatches.
Wood has directional grain, board seams and shaded edges; metal has smaller
fasteners and worn bevels. The output hatch has a charcoal MI-inspired shell,
a recessed port and steel rim. All eight sources and runtime exports share the
repacked 512px atlas at 32 pixels per physical block. Geometry and pivots are
preserved. `retexture.js` is the native Blockbench repaint/export helper; it is
the final pass after `build_models.js`. Earlier textures are backed up in
`.local/drill-textures-v1`.

## Minecraft material revision

The final pass uses Mojang oak log sides/end grain, oak planks and cobblestone
from `textures/vanilla/vanilla_manifest.json`. These are baked at native 16px/block
scale into the shared 512px atlas. Long faces tile rather than stretching a block
texture. Metal fittings retain custom pixel art. The assembled output hatch fills
its lower side-block position to ground level; metal footing caps sit over the four
cobblestone feet. The assembled asset is 76 cubes. The latest native helper is
`minecraft_materials.js`; earlier texture passes are preserved in `.local` backups.
Texture attribution: Mojang AB; Minecraft EULA/Usage Guidelines apply.

## Bracing and clearance refinement

Eight full square stone-brick collars replace the L-shaped corner braces; their
12x12 footprints match the cobblestone feet. The output housing is shallower and
joined to the upper wooden support rail by a casing connector. An analytic bound
on the drill bit's complete rotational sweep leaves about 2.83 physical model
units of clearance to the housing and connector; see `clearance-check.json`.
Two raised oak roof beams with iron straps add top detail. Sparse low-contrast
ordered dithering, small chips and edge wear soften the clean surfaces while
keeping the native Minecraft material patterns. All eight sources share the updated
atlas. `refine_machine.js` is the latest native Blockbench construction/texture pass.
Stone-brick attribution is recorded in `textures/stone_bricks/vanilla_manifest.json`.

## Owner model finishing pass

Preserved the owner's 61-cube assembly, including their deleted pieces and lowered
roof. Drive socket and gear pivot are centred at y=24 physical model units: the
centre of the second block. Runtime pivot matches. The vertical axle extends into
the lowest disc with a small surrounding rim. Removed coplanar overlap at foot
caps, hatch lip, connector and drive bracket. The axis-aligned exposed-face check
finds no remaining coincident faces. See `user-final-checks.json`. Shared UV patches
are intentional and do not cause in-game flicker. The owner's pre-edit native model
is saved in `.local/user-drill-final/before.bbmodel`. Earlier construction helpers
must not overwrite this owner-edited source.

## Industrial controller panel

Added the controller to the assembled machine. Its charcoal housing and steel rim
frame a green decorative display, selector and amber control. It spans the entire
second layer (physical y=16..32) and matches the standalone block and pre-assembly
guide. The assembled housing is shallow enough to clear the rotating drill; its
rear mount joins the upper body. The owner's 61 cubes remain intact, plus nine
controller pieces: 70 total. The display is decorative; right-click still reports
operational status. `controller-checks.json` verifies height, rotational clearance
and coplanar surfaces. Native pre-edit backups are in `.local/controller-before-redesign`.

## Early-tier stone / analogue controller

The active wooden drill uses a vanilla-stone enclosure with a weathered dark rim,
ivory analogue meter, brown switch and ceramic fuse cap. Meter graphics remain
decorative; right-click diagnostics still report machine state. The controller
continues to fill the second layer and keeps the established drill clearance.
The modern green-screen version is preserved under
`art/blockbench/revoextraction/variants/modern-industrial`: three embedded-texture
editable sources (controller, assembly and placement guide), matching atlas, four
runtime exports, previews and a SHA-256 manifest. It is reserved art for future
higher-tier machines, not a newly registered gameplay tier. The native paint pass
for the active variant is `stone_analogue_controller.js`. Mojang's stone texture
attribution is recorded under `textures/stone_panel/vanilla_manifest.json`.

## In-game interfaces

Right-click the controller for RPM, operational status and cycle progress. The
controller exposes no machine or player inventory slots. Right-click the lower
left-side output hatch to open the nine output slots above your player inventory.
Output slots reject insertion; click or shift-click to extract. Hoppers still
extract through that same hatch. Opening a hatch does not take a stack automatically.
Dismantling the structure closes an open hatch window; no stored items are migrated
or duplicated by this interface split.

A small black R sits at the bottom-right of the controller and EMI assembly
page. The hatch has no R or branding footer. EMI has a rotatable 3D placement preview, bottom-up or single-layer views,
port highlighting, controller-relative block tooltips and the full material list.
Open the controller's recipes in EMI, choose Wooden Drill Assembly and select
Open 3D guide for the larger construction window. The guide uses the same blueprint
as the formation check; it includes all 26 required blocks, including the controller.
See [REVO-MULTIBLOCK-GUIDES.md](REVO-MULTIBLOCK-GUIDES.md) for controls and the shared
implementation to use for future machines.

See [MACHINE-MODELING-WORKFLOW.md](MACHINE-MODELING-WORKFLOW.md) for the reusable
modeling process, exact heights/pivots and current interface conventions. Nine
isolated game tests cover the machine, menu separation, transfers and diagnostics. These include a blueprint test that checks every guide offset and port facing
against an assembled drill.

The hatch pushes buffered output to an inventory touching its outward face after
each completed cycle, and retries when blocked. Rejected output remains buffered;
sided inventory rules are respected. The controller distinguishes wrong block orientation from missing blocks and
shows the part offset and required facing. Jade keeps only basic status, RPM and
cycle progress; orientation details stay in the controller. The hatch uses normal vanilla
player inventory artwork and the standard 176-pixel width. See
[MACHINE-INTERFACES.md](MACHINE-INTERFACES.md) for the full interface conventions.
