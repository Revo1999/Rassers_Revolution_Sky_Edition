# First-tier multiblocks implementation

This document records the wooden first tiers and their balanced Minecraft/Create-like art. Metal upgrades are documented separately in [METAL-FARMS.md](METAL-FARMS.md).

- Tree Grower: 3x3x1 base, oak sapling retained as starter, daylight and 3x3x3 air above; 40 s cycle, logs/leaves/sticks/saplings. Growing tree is a visual, not placed world blocks.
- Greenhouse: 3x3x3 oak/stone/glass frame, retained crop starter (wheat/carrot/potato/beetroot), daylight, water tank/bucket input, 20 s cycle with 250 mB water per harvest.
- Mob Harvester: 3x3x3 oak/stone/barred cage, passive production with the actual selected vanilla Zombie/Skeleton/Spider inside, 15 s cycle. The machine owns and saves its contained mob, without spawning a roaming entity.
- Controllers: diagnostics only, small R bottom-right. Separate I/O hatch inventories use vanilla player slot layout and no R/footer. Auto-output uses outward face, retains rejected items, retries when blocked.
- Detailed facing errors stay in controller; Jade remains compact. All machines get interactive 3D EMI construction and production information.
- Cultivation owns shared machine engine, Tree/Greenhouse controllers and parts; Genetics owns the Mob Harvester controller and depends on Cultivation.
- Editable native Blockbench sources and runtime exports live under art/blockbench/first-tier-machines; target native density 16 px/block, 512px atlas, <=96 cubes per assembled machine including growth visual, cutout transparency, no PBR dependency.

## Build and operate

Craft the controller and shared parts in a crafting table. Find the controller
in EMI, select its Assembly category and open the interactive 3D guide. It includes
materials, layers, rotation, zoom, single-layer mode and port highlighting. Its
blueprint is the same one the server checks for formation.

Layouts below are viewed from above, with the controller/front at the bottom.
The hatch goes on the right and faces outward; controller and filter face front.
Formation is automatic. The Tree Grower uses only its bottom layer.

```
Tree / Greenhouse bottom     Harvester bottom
    S W S                       S W S
    W D H                       W G H
    S C S                       S C S

Greenhouse middle           Harvester middle
    S V S                       S B S
    V . V                       B . B
    S V S                       S F S

Greenhouse top              Harvester top
    S W S                       S W S
    W V W                       W W W
    S W S                       S W S
```

S = Reinforced Timber Support, W = Wooden Casing, D = Cultivation Soil Bed,
H = Timber I/O Hatch, C = selected controller, G = Collection Grate,
V = Greenhouse Glass Panel, B = Barred Cage Panel, F = Mob Species Filter,
`.` = air.

| Material | Tree Grower | Greenhouse | Mob Harvester |
| --- | --- | --- | --- |
| Timber support | 4 | 12 | 12 |
| Wooden casing | 2 | 6 | 7 |
| Soil bed | 1 | 1 | 0 |
| Collection grate | 0 | 0 | 1 |
| Glass panel | 0 | 5 | 0 |
| Barred panel | 0 | 0 | 3 |
| Mob species filter | 0 | 0 | 1 |
| I/O hatch | 1 | 1 | 1 |
| Controller | 1 | 1 | 1 |
| Total | 9 | 26 | 26 |

Right-click the controller for status, selected program, cycle percentage,
greenhouse water amount and an EMI guide button. Harvester controllers also have a retained mob-spawner slot and player inventory. Other controllers have no inventory.
A backwards controller, hatch or filter is explained there with the part,
relative position and correct direction. Jade remains compact without facing hints.

Right-click the formed hatch to access actual storage. Tree has one retained oak
sapling slot; Greenhouse has a retained crop-starter slot, water-bucket slot and
water gauge; Harvester has outputs only. Input slots hold one item, and starters
are removable. A water bucket fills 1,000 mB and leaves an empty bucket to extract.
The greenhouse tank holds 8,000 mB and accepts piped water via the hatch. This
fluid port is fill-only and rejects other fluids. Only a completed harvest uses water.

Craft a vanilla mob egg, surround it with eight iron bars to craft a mob-specific
vanilla spawner, then insert that spawner into the controller. It is retained.
The front filter remains structural and no longer cycles species. Wooden accepts
Zombie, Skeleton, Spider, Cow and Slime; Steel adds Blaze, Enderman, Magma Cube,
Ghast, Shulker and Wither Skeleton. The last two still require a retained Netherite
Ingot in the input hatch. Eggs cannot spawn mobs, change spawners or create baby
mobs; dispensers eject them as items. Placed spawners never spawn mobs.
Changing the inserted spawner changes the program and contained mob together.
Minecraft's native renderer supplies the proper species model, textures,
equipment (including the skeleton's bow), spider eyes and idle/limb animation.
The contained vanilla Mob is serialized with the controller, including its UUID
and equipment. Reloading retains the same mob; existing machines migrate on first
use. It is never added to the world's roaming-entity collection or AI/despawn tick
loop, so distance, chunk reloads, peaceful difficulty and daylight cannot remove it.
It cannot escape, attack, pick up items, burn or generate extra drops. Changing the
spawner deliberately replaces it with the newly selected species. An incomplete
machine hides the contained mob and retains its data for repair; removing the
controller removes its contained mob with it. The Harvester does not kill nearby
mobs. The wooden tree grower renders a normal oak built from miniature vanilla log and leaf blocks, with full-resolution vanilla textures and biome leaf tint. It does not place world blocks. The steel grower still grows and harvests a real tree.

| Program | Output per completed cycle |
| --- | --- |
| Oak | 6 oak logs, 2 oak leaves, 2 sticks, 1 oak sapling |
| Wheat | 3 wheat, 1 wheat seeds |
| Carrot | 3 carrots |
| Potato | 3 potatoes |
| Beetroot | 3 beetroot, 1 beetroot seeds |
| Zombie | 2 rotten flesh, 1 carrot, 1 potato |
| Skeleton | 2 bones, 1 arrow |
| Spider | 2 string, 1 spider eye |

Changing starter or species resets progress. Missing daylight, water, starter,
clearance or output capacity pauses progress. Broken/wrongly facing structures
stop and unform; fixing them restores formation. All products must fit together
before completion, avoiding partial rewards. Nine persistent output slots export
to the adjacent inventory outside the hatch after every completed cycle. Rejected
items stay buffered; full-buffer production retries export. Pipes/hoppers may
extract output, starters and empty buckets, but insert only valid input items.
Cached wrappers and open hatch menus lose access on dismantling.

## Modeling, heights and textures

Native editable Blockbench projects, previews and the native MCP builder are in
`art/blockbench/first-tier-machines`. Runtime geometry is exported by Blockbench's
Java block codec; the resource generator writes registration data only. Existing
drill art and saved higher-tier controller variants are preserved.

The builder recreates the initial sources. After manual model edits, treat saved
`.bbmodel` files as authoritative and make targeted native edits; do not rerun the
initial builder over them without preserving the edited version.

Art coordinates use 16 units per world block: the footprint is 48×48 units. Assembled
sources divide coordinates by three to fit Java export limits; the renderer restores
scale three. Standalone placement blocks use 16 units directly. Native front is −Z,
X is right, Y is up. Controller is front-centre in the ground layer; hatch is
right-centre. This differs from the historical drill's local Z convention.

| Detail | Physical art Y | World height in blocks |
| --- | --- | --- |
| Bottom sill | 0–2 | 0–0.125 |
| Cobblestone feet | 0–4 | 0–0.25 |
| Tree square stone caps | 12–16 | 0.75–1 |
| Soil surface | 14 | 0.875 |
| Tall square stone braces | 14–18 | 0.875–1.125 |
| Tall oak pillars | 4–45 | 0.25–2.8125 |
| Glass walls | 18–43 | 1.125–2.6875 |
| Cage bars | 18–42 | 1.125–2.625 |
| Tall top caps | 44–48 | 2.75–3 |
| Greenhouse roof glass | 46–47 | 2.875–2.9375 |
| Controller / hatch | 0–16 | First block's full height |
| Mature decorative tree | 14–62 | 0.875–3.875 |

Timber uses oak-log sides/end grain, walls oak planks, feet cobblestone and square
braces stone brick. The analogue controller has a stone enclosure, dark meter,
switch and amber fuse. Metal wear and bolts are pixels. Shared 512×512 cutout atlas,
nearest-neighbour sampling, 16 texture pixels per world block, tiled vanilla swatches,
no PBR dependency. Plant preview planes deliberately map one full crop sprite.
Vanilla texture sources and attribution are recorded beside the atlas.

Balanced static geometry: Tree 37 cubes, Greenhouse 52, Harvester frame 81. The cage
has four framed panels: full-width lower, waist and upper iron rails; dark inset
stiles seated into oak pillars; four spaced iron bars per face. Lower rails sit
at Y=13–15 against the Y=14 stone collars and raised plank base; top rails span
Y=41–44 into the roof beams. Bars run Y=14–42 into both rails, eliminating the old
gap below the upper structure. Waist rails at Y=25–27 attach the filter selector
while keeping the mob's face visible. Material UVs retain 16 px/world-block density;
bolts/wear remain texture details. Standalone barred panels and filters share this
framed style. Editable assembled source has 87 cubes including a hidden legacy actor.
Previous cage sources/exports are backed up under `history/pre-framed-cage`, and
`refine_harvester_cage.js` records the targeted native edit. The old
Blockbench placeholder actors remain as historical art references;
they are no longer loaded or rendered by the game. Harvester mobs use Minecraft's
entity renderer at normal mob size, centred over the collection tray at Y=5/16.
The frame's divide-by-three scale is undone before entity rendering, so mobs are
neither enlarged threefold nor squashed into a block model. Each machine caches
its own native mob instance; routine network snapshots preserve that cache and its
animation clock. Only harmless client animation values advance, never Mob.tick(),
aiStep() or checkDespawn(). No global shadow/hitbox/fire pass is run for this
machine-owned render. Tree scales from its soil pivot; Greenhouse uses vanilla
crop age states. No roaming entities, dynamic lights or crop random ticks are needed.
Selection/collision remain full-block independently of decorative gaps. Render
bounds cover the assembled structure.

Tree/Greenhouse hatch panels are 176×220; Harvester is 176×184. All use vanilla
player slots and have no R/footer. Controllers and guides retain the small black
bottom-right R. See [MACHINE-INTERFACES.md](MACHINE-INTERFACES.md) and
[MACHINE-MODELING-WORKFLOW.md](MACHINE-MODELING-WORKFLOW.md) for future machines.

## Verification

```powershell
python scripts/validate_first_tier_assets.py
.\dev.cmd -PrevoFarmTests :revocultivation:runFarmTests
.\dev.cmd -PrevoFarmUiQa :revolib:runClient
.\dev.cmd build collectMods
python scripts/package_modpack.py
```

Nine server game tests cover all three kinds/four rotations, facing errors,
guide/blueprint parity, every production program, daylight/clearance, starters,
bucket/fluid water, atomic multi-output fit, automatic export, saving, menu transfers
and stale capability invalidation. The contained-mob test additionally checks actual
species, cached instance, persistent UUID/equipment, inert flags, reload/migration,
peaceful survival, anchored position, damage rejection and allow-listed entity IDs.
All nine passed. Real client QA in the copied `Revo First Tier QA` save opened
network menus through actual block interaction, checked player slots, rendered all
three EMI guides, selected all three real filters and checked native mob types/UUIDs
plus compact Jade server data. Screenshots/results are under `ingame` in the
art folder. The final client run passed with no Revo missing-model/texture or
unregistered-category errors. Optional QA/test classes and templates are excluded
from production jars; a normal build follows QA before delivery.

The framed cage revision also passed 56 geometric attachment checks: every bar
overlaps its lower and upper rails, panel stiles reach the oak pillars, lower rails
seat into two stone collars, and upper rails reach both timber posts. The revised
cage, filter, vanilla mob visibility and EMI placement view were inspected in the
live client. Preview: `previews/harvester-framed-cage.png` under the art folder.

## Cage texture refinement

The Harvester, barred panel, species filter and Harvester controller use a dedicated
`harvester_atlas.png`, retaining the original vanilla oak, stone, dirt and other
material pixels. Tree/Greenhouse and shared-part textures keep their original atlas.
The new atlas reserves padded, proportionate iron face islands below Y=200, with
warm grey iron, restrained highlights, clustered wear and sparse oxidation.
Thin bars have their own iron regions without repeated bolt patterns; rails,
stiles and plates carry appropriately placed small mounting details. The roof vent
gets painted slots. No extra cubes are added for texture details.

The 54 shared face islands cover 354 remapped metal faces at the original density
of 16 pixels per world block. Narrow fractional-width faces keep their measured UV
span, with opaque padding to prevent dark/transparent seams. The eight wooden beams
have their 48 face UVs aligned to the same plank seams. The controller's stand-alone
item/block model matches its assembled rim. Geometry, joints and native mob rendering
are unchanged. `texture_harvester_cage.js` records the native Blockbench edit;
`cage-texture-audit.json` records affected faces and pixel extents. Sources from before
the edit are preserved under `history/pre-cage-textures`. Preview:
`previews/harvester-textures.png`.

The asset validator checks embedded/runtime atlas equality, original-material pixel
preservation, every remapped iron face's opacity and UV density, unchanged exported
geometry and all 56 cage attachment checks. In-game QA verifies the complete
resource reload, item models and visibility of the selected vanilla mobs.

The texture revision passed asset validation, the live client scenario with all
three native mobs, and the normal production build. Updated runtime assets are in
the 0.1.12 development pack. Test tooling remains excluded from normal launches.
