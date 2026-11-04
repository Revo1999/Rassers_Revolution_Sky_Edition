# Single-block cultivation machines

The Composter and Silk Worm Cultivator are 1 x 1 x 1 passive machines in
RevoCultivation. Fill them and wait. There is no shaft connection, RPM input,
stress cost, speed bonus or manual tamping.

## Composter

A batch of four identical compostable plants, seeds, saplings or leaves runs
automatically for 160 server ticks (8 seconds), then produces one Compost.
Accepted inputs follow vanilla's composting table, excluding Revo Compost.
Use Compost as bone meal or craft it into one dirt. EMI shows the machine recipe.

## Silk Worm Cultivator

Install one retained Silkworm and add leaves. Two leaves run automatically for
400 server ticks (20 seconds) and produce two Silk Cocoons. The starter remains
inside. No water or power is needed. Four leaf blocks craft the starter worm;
leaf blocks can come from shears or the existing Tree Grower output.

One cocoon crafts into two string. Two cocoons craft into one Silk; one Silk can
be unwound into four string. Silk has the c:strings and c:silks item tags.

## Hand feeding and visible filling

Right-click with a valid input to add one item to the same inventory used by the
interface and automation. Right-click with a Silkworm to install the starter.
Survival consumes one held item only on successful insertion; creative preserves
its stack. If the item cannot fit, normal interface interaction remains available.
Empty-hand right-click opens the interface.

- Empty: the vessel is visibly empty; leaves, worms and cocoons are not static
  decorations in the world model.
- Loading: adding inputs raises the surface smoothly. The Composter has four
  quarter-batch steps; the cultivator has half and full steps. A full cultivator
  waits if its starter is missing.
- Working: a full valid batch starts automatically. Cultivator leaves change
  through four increasingly silky surface stages, while cocoons grow downward
  from the central rack. Its actual worm follows the surface. Composter contents
  darken as the fixed timer progresses.
- Ready: completed contents and a small product display remain visible while
  output waits. The interface and Jade show readiness and 100%. Extraction or
  auto-export clears the contents if no input is queued; otherwise the next batch
  continues normally.

The display represents one active batch. Extra feed queues later batches without
overflowing the vessel. The server maintains fill=0..8 and stage=0..3 properties
for empty/loading/working/ready. These follow real inventory and work, including
hand feeding, hoppers, extraction, save/reload and blocked outputs. The client
interpolates the surface; rendering cannot consume inputs or create products.

The behavioral reference is Ex Deorum's 1.21.1
[barrel renderer](https://github.com/thedarkcolour/ExDeorum/blob/1.21.1/src/main/java/thedarkcolour/exdeorum/client/ter/BarrelRenderer.java),
where content height follows fill amount and appearance changes during processing.

## Automation and interfaces

Front and bottom expose only output. Other faces expose feed and starter inputs.
Invalid insertion is rejected and only one starter worm fits. Unsided access uses
the same real three slots. Outputs reject insertion.

At each completed batch, output is offered to the inventory directly in front,
then retried once per second while anything remains. Rejected items stay buffered.
Inputs are consumed only when the whole result fits. Full output or missing inputs
pause the timer. Breaking the block drops its real inventory.

Both machines advance one work unit per loaded server tick. No chunks are forced
to stay loaded. Inventory, work and cycles retain their existing NBT keys, so old
machines keep their contents/progress; legacy kinetic metadata is ignored.

The menus are 176 x 184 and use RevoLib's vanilla/MI player layout: rows at y=102,
hotbar at y=160. They show fill/process percentage, status, cycle duration and real
slots, with a small black R at x=158,y=82 above player inventory. There is no RPM
display or manual-work button. Existing multiblock hatch interfaces retain their
footer-free layout. EMI shows passive production recipes; Jade shows compact
status and percentage without RPM or orientation.

## Native Blockbench assets and texture preference

Editable sources are art/blockbench/small-cultivation/*.bbmodel. Blockbench MCP
and scoped native Undo operations author the geometry and compile Java block JSON.
The Composter is five wooden cubes. The cultivator has eight baked body cubes
and its inventory-driven contents; its editable assembly preview has 12 cubes.

Both structures use one visibly dithered wood material. The shared padded atlas
is 128 x 128, with wood at [2,42] to [18,58], mapped at 16 pixels per block.
wood_paint.js paints five-tone clustered dithering, broken stave grain, knots and
rubbed patches through the native canvas. Keep this visible wear in future edits.
Leaves and cocoons retain green and cream content colors. There are no stone
feet, metal bands, control panels or drive fittings.

build_models.js edits saved native projects and invokes build_contents.js. The
latter authors six top-only native surfaces and exports the real worm/cocoons
separately. Content-stage tiles occupy the atlas row at y=62; wood is preserved.
Do not bake those live components into the body too, or they appear twice.
Preserve user edits before running the recipes; earlier revisions are in archive/.

All runtime geometry stays inside one block. North is the source front, with
four placement facings for the item output. Full-block selection/collision is
intentional. Source UVs preserve proportional pixel density; the fill is one
moving top quad, while growing cocoons deliberately scale vertically.

| Part | Height in sixteenths |
| --- | --- |
| Wooden floor | 0..2 |
| Composter walls | 2..16 |
| Cultivator walls | 2..10 |
| Cultivator moving surface | 2..8.5 |
| Worm | follows surface |
| Rack supports | 8..14 |
| Cocoons | grow down to 11..14 |
| Central crossbar | 14..16 |

## Verification and delivery

19 server GameTests cover nine existing farm regressions and ten single-block
checks: fixed timing, ignored old speed data, retained starter, full output,
lossless export, persistence, direct feeding, real fill transitions, automated
export clearing the visual state and ordinary non-kinetic item-port facing.

Full client QA checks real network feeding, empty/half/full/working/ready/collected
appearances, passive Composter output, both menus, inventory coordinates, EMI and
Jade. Twelve screenshots and the result are in the model folder's ingame directory.
validate_small_cultivation.py checks bodies, absence of shaft geometry, atlas
equality, bounds, UV density, preserved rack and six native surface exports.

Production excludes QA classes/templates. Release 0.1.17-dev retains seven Revo
jars, 84 pinned external mods and the existing shader. Create remains available
elsewhere in the pack, including the drill; these two machines do not use it.

Run from the project root:

    .\dev.cmd :revocultivation:runFarmTests -PrevoFarmTests
    python scripts/validate_small_cultivation.py
    python scripts/validate_first_tier_assets.py
    .\dev.cmd build collectMods
    python scripts/package_modpack.py
    python scripts/verify_first_tier_delivery.py
    python scripts/verify_small_cultivation_delivery.py

Client QA requires the copied save Revo Single Block QA and
.\dev.cmd :revolib:runClient -PrevoSmallUiQa. It modifies only that test world
and stops the client after completing its checks.
