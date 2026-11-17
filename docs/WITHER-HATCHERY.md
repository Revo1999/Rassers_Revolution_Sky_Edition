# Electric Wither Hatchery

Build the ordinary Wither summoning shape to obtain a **Wither Egg**, then incubate and neutralize it inside a powered **5 x 5 x 5 containment chamber**. The Wither is a real server-side entity rendered with Minecraft's native Wither model; the result is not granted by an egg-to-star timer alone.

## Make an egg

Place four Soul Sand/Soul Soil blocks and three Wither Skeleton skulls in the normal upright Wither T. Either horizontal axis works, including mixed Soul Sand and Soul Soil. Keep the two spaces beside the bottom centre empty.

```text
skull  skull  skull
soul   soul   soul
air    soul   air
       hopper
```

Completing the seven-block shape consumes it and drops one Wither Egg at the bottom centre instead of summoning a roaming Wither. Incomplete shapes consume nothing. The ritual works in Peaceful too, because the result is an item rather than a roaming hostile mob.

No player click or player-owned tool is required. Vanilla skull placement, dispensers and block placers using world block placement can complete it. The final ingredient can be a skull or a base block. An ordinary hopper below the centre collects the dropped egg. Hoppers/pipes can insert eggs into the chamber's input hatch and extract stars from its output hatch. This provides the interfaces for automation; a hopper alone does not place the ritual blocks.

Skulls remain consumed ingredients. This feature does not add the separately planned controlled Wither Skeleton skull-production program, nor does it add a Netherite Upgrade template recipe.

## Build the chamber

Use the controller's **EMI construction guide**. The same blueprint is used by the server and supports all four horizontal orientations.

| Part | Count | Position |
|---|---:|---|
| Wither Containment Casing | 81 | Remaining shell positions, including the whole roof and floor except ports/controller. |
| Smoked Containment Glass | 9 | Centred 3 x 3 front facade, one block above the base. |
| Red Hatching Light | 4 | Front left/right outer columns, at heights 2 and 4. |
| Electric Wither Hatchery controller | 1 | Front centre of the bottom layer, facing outward. |
| Wither Egg Input Hatch | 1 | Left centre of the bottom layer, facing left/outward. |
| Nether Star Output Hatch | 1 | Right centre of the bottom layer, facing right/outward. |
| Wither Chamber Energy Hatch | 1 | Rear centre of the bottom layer, facing backward/outward. |

Outer dimensions are **5 wide x 5 deep x 5 high**. Leave the entire **3 x 3 x 3 interior air**. The shell has 98 blocks. The window uses translucent smoked glass; it does not require amethyst or geodes to craft.

The shell uses MI's exact steel casing artwork. Item hatches use MI's native
recess/extraction sprites, and the energy hatch matches the existing Revo furnace
energy/fuel-hatch treatment. The red warning lenses have authored pixel dithering.
Three simple red downlights are fitted to the inside of the roof, aimed down at
the egg; these are included in the formed machine and need no extra placed blocks.
They illuminate the chamber during powered operation and turn off when paused.

Controllers provide diagnostics without inventory slots. Input/output hatches each have nine real persistent slots, with the standard 36-slot player inventory below them. Outputs reject insertion and automatically export through the outward face. Jade summarizes the stage, progress and energy; detailed formation errors remain on the controller.

## Power and visible cycle

Initial balance:

| Stage | Powered ticks | Duration at 20 TPS | Visible behaviour |
|---|---:|---:|---|
| Incubation | 1,200 | 60 seconds | Egg rocks, develops cracks and glows; four red lights flash. |
| Hatching | 100 | 5 seconds | The egg opens into a real contained Wither that grows to chamber scale. |
| Neutralization | 200 | 10 seconds | The chamber damages the Wither every second until it dies. |

The chamber draws **8,192 EU per productive tick** throughout all three stages: **12,288,000 EU per Nether Star**. It accepts native MI EU or FE through the same outward energy terminal. At the pack's default 10 FE/EU ratio this is **81,920 FE/t** and **122,880,000 FE per star**. The FE ratio follows MI's configuration.

The internal buffer holds 327,680 EU, or two seconds of operation. The energy hatch accepts up to 32,768 EU/t into that buffer, shared across FE and EU reception. Input is rate-limited across repeated capability calls. A weak supply can charge the buffer and advance intermittently; it does not obtain free progress. Basic/Advanced/Elite Revo converters alone cannot sustain the machine's full draw; use adequate native EU infrastructure or a sufficiently strong FE source.

The contained Wither is scaled to fit the three-block-high interior. It cannot fire skulls, create a spawn explosion, damage terrain, wander away, change dimensions or yield an extra loose Nether Star. Its actual health is reduced by chamber hits, and the output is committed only after a lethal hit. The normal death animation remains visible. External damage cannot bypass the production sequence.

## Interruptions and recovery

- Missing power pauses stage progress, egg rocking and Wither damage.
- Full output pauses processing and power use. Free space or connect an output chest to resume.
- A missing/misoriented part or obstructed interior pauses the machine. The contained Wither remains immobilized.
- Progress, stored energy, escrowed egg and owned entity identity survive saves. The machine reconnects to its owned Wither on reload.
- Breaking the controller removes its contained entity and returns one in-flight egg. Already spent electricity is not refunded. Completed stars remain in the output hatch.
- Breaking an item hatch drops its own stored contents. Cached automation handlers validate the current structure before transferring items.
- The machine and ritual do not force-load chunks.

## Models and source

Editable models were authored and exported through **Blockbench MCP**. Delivered sources, the shared native pixel-art atlas and reference assembly are in [art/blockbench/wither-hatchery](../art/blockbench/wither-hatchery). Runtime Java block models use those exports. `scripts/hatchery_assets.py` paints the atlas and writes recipes/data; it does not construct model geometry.

## Verification

```powershell
.\dev.cmd :revoindustry:runHatcheryTests -PrevoHatcheryTests
.\dev.cmd :revoindustry:runHatcheryUiQa -PrevoHatcheryUiQa
.\dev.cmd build collectMods
```

The native server suite covers both ritual axes and last-base placement, hopper suction, incomplete-shape preservation, all chamber rotations, orientation/cached-capability guards, FE/EU simulation and shared input limits, one-egg escrow, real Wither spawning/damage/death, exact cycle energy, pause/recovery, reload identity, break recovery, output export and loaded crafting recipes.

The opt-in graphical fixture creates a **new uniquely named Revo Wither Hatchery QA save**, captures real egg/crack/hatch/neutralization frames and hatch/controller menus, verifies the actual output star and closes the client. It does not open or alter an existing player save. Test fixtures are excluded from production JARs.
