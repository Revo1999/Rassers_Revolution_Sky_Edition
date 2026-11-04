# Revo machine and interface conventions

Use this document when implementing or reviewing future Revo machines. It records
the working wooden-drill pattern: a diagnostics controller, dedicated inventory
ports, useful formation errors, automatic output, Jade and an EMI construction guide.

## Controller and hatch responsibilities

The controller opens a diagnostics window with operational state, actual input
RPM and cycle progress. It exposes **no machine inventory or player inventory**.
Do not add fake output slots to a controller screen to represent items stored in
a port. Right-clicking a formed item output hatch opens its output slots above the
normal player inventory. Opening the hatch never silently extracts a stack.

The primitive drill has nine persistent output slots. Its output menu is
extraction-only: ordinary clicks and shift-click move items to the player, while
insertion and player-to-output shift-click are rejected. Closing a menu must not
copy, clear or recreate storage. The server's real persistent buffer is the sole
source of truth; menus and capabilities refer to it rather than owning duplicates.

Menus verify distance to the block being used. A hatch window becomes invalid
when its hatch or controller is removed or the structure is dismantled. A controller
may remain open on an incomplete machine so the player can read the problem.
Preserve standard Minecraft cursor-stack, drag, number-key and tooltip behaviour
by using real `Slot` objects and `AbstractContainerScreen`.

## Normal player inventory layout

Modern Industrialization uses the standard 176-pixel window width, player inventory
X=8 and 18-pixel slot spacing. The three main rows use inventory indices 9–35;
the separate hotbar row uses indices 0–8 and sits 58 pixels below the first main
row. Refer to MI's [MachineGuiParameters](https://github.com/AztechMC/Modern-Industrialization/blob/1.21.x/src/main/java/aztech/modern_industrialization/machines/gui/MachineGuiParameters.java),
[MachineMenuCommon](https://github.com/AztechMC/Modern-Industrialization/blob/1.21.x/src/main/java/aztech/modern_industrialization/machines/gui/MachineMenuCommon.java)
and [MachineScreen](https://github.com/AztechMC/Modern-Industrialization/blob/1.21.x/src/client/java/aztech/modern_industrialization/client/machines/gui/MachineScreen.java).

Use the shared `RevoLib.PlayerInventoryLayout` instead of inventing slot coordinates
per machine. For a content-panel height `H`:

| Element | Position in logical GUI pixels |
| --- | --- |
| Window width | 176 |
| Player inventory label | X=8, Y=H−94 |
| First main-row item | X=8, Y=H−82 |
| Main-row slot spacing | 18 horizontally and vertically |
| Hotbar item row | Y=H−24; 58 below the first main row |
| Item artwork | 16×16, inside an 18×18 slot frame |

The drill hatch uses `H=184`: total **176×184**, with no branding footer.
Its main player rows begin at `(8,102)`, hotbar at `(8,160)`, inventory label at
`(8,90)`, and 3×3 output grid at `(62,27)`. Output slots precede player slots in
the menu, so shift-click ranges are machine `[0,9)` and player `[9,45)`.

Render normal player slots with `VanillaMachineGui.playerInventory`, which references
Minecraft's existing `generic_54.png` player-inventory region. Use
`VanillaMachineGui.slot` for ordinary machine slots. Do not hand-paint player-slot
shadows in a different palette or stretch the inventory to match a wider panel.
The server slot coordinates and client slot artwork must agree exactly.

Keep the small black **R at the bottom-right** of controller and construction-guide
windows, without a shadow. Inventory hatch windows have **no R and no extra footer**;
they end at the normal inventory border below the hotbar.

## Automatic item output

The drill automatically exports after each completed cycle. Use the hatch's
outward-facing neighbor, as in MI's [ItemHatch](https://github.com/AztechMC/Modern-Industrialization/blob/1.21.x/src/main/java/aztech/modern_industrialization/machines/blockentities/hatches/ItemHatch.java).
Query `Capabilities.ItemHandler.BLOCK` on the adjacent position and the neighbor's
**opposite face**. A sided furnace or other restricted inventory decides what that
face accepts. Never bypass that decision by writing directly into its container.

Production first enters the persistent machine buffer. The hatch then tries every
output slot, including earlier buffered items. Simulate acceptance, extract only
the accepted amount, insert it and return any rejected remainder to its source
slot. A reentrancy guard prevents nested automatic exports. If the neighbor is
absent, unloaded, full or rejects the item, retain output in the buffer. Do not
drop items into the world, load neighbor chunks or discard rejected stacks.

If the next result will not fit, retry export before declaring the output blocked.
This lets a powered machine recover after space is freed in a destination even
though it cannot finish another cycle while its buffer is full. Missing rotational
power still stops production. Hoppers and pipes may continue to pull from the
extraction-only hatch capability; that capability resolves the live machine on
every operation so cached wrappers cannot access a dismantled structure.

## Formation and orientation errors

Use the server's formation blueprint for formation, error reporting and EMI. Check
block types separately from their orientation. If all required blocks occupy the
right positions, a backwards input or hatch must report **Wrong block orientation**,
not **Incomplete multiblock**. Report the part, controller-relative coordinates and
required outward direction. Example: `Hatch (1, −1, −1): face east.`

A backwards controller changes the frame used to interpret the structure. Try the
other horizontal fronts with block-type checks before reporting missing blocks.
If another front matches, tell the player to rotate the controller to that front.
Dismantle the original footprint too, so previously formed placement blocks become
visible again. Missing/wrong block types remain an incomplete-structure error;
an unloaded footprint is reported separately and must not force-load chunks.

The current synchronized operational states are:

| Code | Meaning |
| --- | --- |
| 0 | Incomplete structure |
| 1 | Working |
| 2 | No rotational drive |
| 3 | Output blocked |
| 4 | Structure chunks not loaded |
| 5 | Correct block layout, wrong orientation |

The drill checks formation before each server production tick. A missing or wrongly
facing part stops production and animation, without exposing its output capability.
Fixing the block or orientation should allow automatic formation again.

## Shared server diagnostics and Jade

`DrillDiagnostics` supplies the controller menu and the compact Jade status summary. Do not
infer operational state from decorative textures, an animation angle or a client
inventory copy. Actual Create input RPM and cycle progress come from the server.

Its eight integers are progress, RPM, operational state, faulty part ID, offset X,
offset Y, offset Z and required direction. Part IDs are blueprint enum ordinal+1;
zero means no faulty part. Direction uses Minecraft's 3D direction index. Offsets
are deliberately small and safe for menu data synchronization; do not send large
world coordinates through 16-bit menu data slots. If this protocol changes, update
both reader and writer together.

The optional `DrillJadePlugin` registers providers on controller, hatch, kinetic
input and frame/casing/core block classes. Jade requests the snapshot from the
server, including on parts without a block entity. Resolve the nearby controller
within the known 3×3×3 footprint, without loading chunks. Jade displays state,
RPM and cycle percentage. Orientation details and corrective hints stay in the
controller; a facing error maps to generic incomplete status in Jade. Its payload
contains only progress, RPM and compact operational state. The hatch
also shows buffered item count and an automatic-output hint. An unlinked part
points the player to EMI rather than inventing a running state.

Jade is a compile-only integration and optional mod dependency. Keep its providers
behind Jade's plugin discovery; the base machine must work without Jade installed.
The current API artifact is the same 15.10.6 release pinned by the pack. Register
only the machine's block classes, not a provider on every block in the game.

## First-tier cultivation and harvester machines

The Tree Grower, Greenhouse and Mob Harvester follow these same responsibilities.
Their controller is diagnostics-only, with cycle percentage, status, selected
program and greenhouse water amount; passive machines have no invented RPM value.
The Tree Grower hatch has one retained sapling slot, the Greenhouse has a retained
crop starter and water-bucket slot, and the Mob Harvester has no input slots.
All three expose nine persistent output slots. Starter slots accept one item;
empty buckets remain available for extraction. Player inventory always uses the
shared vanilla layout. Tree/Greenhouse hatch panels are 176×220; Harvester is
176×184. None has a branding footer or R.

Farm controller synchronization uses 13 small integers: kind, state, progress,
duration, water, program, faulty part, relative XYZ, required direction, cycle
counter and buffered item count. Cycle counters are informational. The five-int
Jade payload is compact state, progress, duration, water and buffer count, with
wrong orientation mapped to incomplete. Detailed part/offset/facing data is
exclusive to the controller. Each machine's EMI guide comes from its actual
formation blueprint. See [FIRST-TIER-MULTIBLOCKS.md](FIRST-TIER-MULTIBLOCKS.md).

## EMI construction guidance

Provide the shared interactive RevoLib guide for each multiblock. Include actual
placement block states/facings, full material counts including the controller,
bottom-up and single-layer views, highlighted ports, block offsets, camera controls
and the larger guide window. Follow [REVO-MULTIBLOCK-GUIDES.md](REVO-MULTIBLOCK-GUIDES.md).
When changing formation or port rules, update the guide's data and instructions
in the same change. Reference [MACHINE-MODELING-WORKFLOW.md](MACHINE-MODELING-WORKFLOW.md)
for the separate physical-model coordinates, heights and animation pivots.

## Required checks before delivery

Run the targeted game tests for conservation of items, blocked-output recovery,
sided destination rules, menu transfers, formation in every horizontal orientation,
misoriented input/hatch/controller diagnostics, and blueprint/guide agreement.
Check that the machine also works without the optional Jade/EMI integrations.

Use the opt-in copied-save client QA to inspect the real inventory layout, wrong-
orientation window, Jade's compact real server-data HUD and EMI guide. Confirm
orientation details appear only in the controller and the hatch has no R/footer.
Keep controller/guide branding clear of items and text at normal GUI scale. Then build normal production jars, confirm
test-only classes/templates are excluded and rebuild the importable pack.

```powershell
.\dev.cmd -PrevoDrillTests :revoextraction:runDrillTests
.\dev.cmd -PrevoDrillUiQa :revolib:runClient
.\dev.cmd :revoextraction:build collectMods
python scripts/package_modpack.py
```

## Single-block cultivation

The Composter and Silk Worm Cultivator are passive fixed-time machines. Their
176 by 184 menu integrates real slots with fill/process percentage, status and
cycle duration. There is no RPM line or tamping button. Player rows start at 102,
the hotbar at 160, and the black R is at 158,82 above player inventory. Jade shows
only compact status and percentage. See SINGLE-BLOCK-CULTIVATION.md for hand
feeding, visible processing, automation, persistence and verification.
