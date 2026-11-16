# Nexus endgame: machines, materials and portal

## Scope and current status

This is the implementation specification for the Pressure Casting Plant, Electroplating Line, Particle Collider, Nexus Core and the inexpensive Nexus Builder. It supersedes the earlier Alloy Foundry / Astral Condenser / Lattice Assembler proposal. There are **three new processing multiblocks**, with additional processing performed in existing MI machines.

This pass delivers the specification, a block-position portal blueprint, editable Blockbench portal/core/part assets and a vortex animation study. **Controllers, recipes, quests, builder placement and in-game portal rendering are not implemented by these art files.** The three production machines below remain to be added. Nothing here changes the existing playable pack or its recipes.

User constraints:

- Structures consist of real, individually placed blocks. Formation must never hide the structure and replace it with one giant model.
- The builder places those blocks one at a time from a blueprint and a retained program.
- Only the beacon-like details, contained Nexus Core effects and portal surface/animation may use special visuals. Their surrounding structure stays built from blocks.
- Use existing MI-based hatch behavior and appearance, including native locking, configuration and auto-transfer.
- The Particle Collider is a **21 x 21 footprint, five blocks high**, forming a low stepped cone around the central containment chamber.

## Cost baseline: approximately 1.5 times the expensive proposal

The baseline is the last expensive proposal (4,096 alloy, 1,024 lattices, 2,048 essence, approximately 4,096 stars and four MI singularities), not the earlier 752-star draft. These are **whole-project balance targets**, not ingredients to add again on top of intermediate recipes. The final recipe graph must calculate gross feedstocks and avoid double-counting material embedded in lattices.

| Material / requirement | Previous target | New approximate target | Accounting |
|---|---:|---:|---|
| Nexus Alloy Ingots | 4,096 | **6,144** | Includes alloy inside carriers, castings, the core and lattices. |
| Phase Lattices | 1,024 | **1,536** | Finished components for structures, anchors, circuitry and activation. |
| Astral Essence | 2,048 | **3,072** | Includes 1,536 consumed inside the target lattice production. |
| Nether Stars | 4,096 | **6,144** | At two stars per Astral Essence; included in the essence figure, not an additional star charge. |
| MI Singularities | 4 | **6** | Installed native recipe uses 64 nukes each: **384 nukes** before other upstream costs. |
| Builder | Relatively cheap | **Cheap LV/MV construction tool** | No Nexus alloy, essence, lattices, singularities or Nether Stars. |

These targets deliberately require parallel production. At the current 75-second hatchery cycle, 6,144 stars take 128 hours in one continuously supplied hatchery, or approximately eight hours across 16 hatcheries. Advanced extraction must also be faster than today's Deep Survey before these quantities are practical. Balance is not complete until the actual recipe ledger and an endgame throughput test are available.

## The three production machines

| Machine | Dimensions, W x H x D | Function | Automation challenge | Initial power target, subject to balancing |
|---|---|---|---|---:|
| **Pressure Casting Plant** | 7 x 5 x 5 | Casts Nexus casing panels, ring blanks, lattice carrier frames, anchor bodies and the Nexus Core housing. | Circulate reusable moulds; supply alloy, pressure gas and coolant; drain warm coolant; cool a casting before ejecting it. | 32,768 EU/t |
| **Electroplating Line** | 11 x 4 x 5 | Makes superconducting contacts and layered portal substrates. | Three separate bath stages: clean, plate and rinse. Each has dedicated fluids; spent electrolyte must leave before the next batch. | 65,536 EU/t |
| **Particle Collider** | **21 x 5 x 21** | Produces Stabilized Exotic Matter, used with plated substrates to assemble Phase Lattices. | Prepare and feed two different beam targets; latch both injections, sustain power and cryogenic cooling, and collect the output in containment cells. | 262,144 EU/t |

The collider is a horizontal machine with five stepped annular levels, tapering from a 21-block base toward a raised central containment chamber. It is not a 21-block-tall cone and not one conical mesh. Two opposed injector sections are made from blocks. Beam arcs and the small contained reaction are effects inside the existing structure. Exact accelerator cells and clear-air volumes need their own blueprint before runtime coding.

### Starting controller crafts

These are proposed MI Assembler crafts, each producing one controller. Keep intermediate stacks within native limits; the installed native Assembler recipes demonstrate up to eight item-input types. Shell blocks and hatches are additional construction materials, to be counted from each machine's actual blueprint.

| Controller | Proposed ingredients |
|---|---|
| Pressure Casting Plant | 2 Quantum Machine Hulls, 12 Quantum Circuits, 6 Large Motors, 6 Pumps, 6 Robot Arms, 12 Titanium Gears, 32 Superconductor Wire |
| Electroplating Line | 2 Quantum Machine Hulls, 18 Quantum Circuits, 6 Large Motors, 12 Pumps, 6 Robot Arms, 24 pressure-cast frame components |
| Particle Collider | 4 Quantum Machine Hulls, 24 Quantum Circuits, 12 Robot Arms, 32 Superconductor Wire, 16 pressure-cast frame components, 16 plated contacts, 8 native MI Fusion Chambers, 8 Large Motors |

This bootstraps the caster entirely from existing MI technology, then requires casting for the plating line and both casting and plating for the collider. None of these controller recipes requires its own machine's output. The final bill must include these native components and the machines' shell/port recipes rather than treating the main material target as their complete cost.

### Pressure Casting Plant stages

1. MI **Mixer**: blend iridium, tungsten and superconductor feedstocks with Netherite. A starting batch preserves the proposed expensive ratio: 16 iridium dust, 32 tungsten dust, 16 superconductor dust and two Netherite Ingots -> eight units of Nexus Alloy Blend. Exact feedstock item IDs must be validated when adding recipes.
2. MI **Electric Blast Furnace**: eight blend plus UU Matter -> eight hot alloy ingots. One item input keeps this inside the native furnace's recipe shape.
3. MI **Vacuum Freezer**: hot alloy -> cooled Nexus Alloy Ingots. Fusion-produced Helium Plasma remains an additional required endgame process input elsewhere in the chain.
4. Pressure Casting Plant: install a retained casting program; accept a reusable mould from the mould hatch; fill, pressurize, cool and eject the selected rough casting. The mould is a real item returned through a mould output hatch.
5. MI **Compressor / Assembler**: finish the cast panel or add reinforcement, insulation and mounts. A rough casting cannot be placed directly as a finished portal block.

Create moulds in the native MI Assembler from processed steel/titanium components. The plant must be constructible with native MI casings, circuits, motors and pumps; its first controller and moulds cannot require its own castings. Initial moulds have no random failure or wear mechanic.

### Electroplating Line stages

1. Existing salt geode -> salt water -> MI **Centrifuge** -> salt -> MI **Electrolyzer** -> sodium/chlorine chemistry. This makes the geode part of the endgame supply chain.
2. MI **Chemical Reactor**: metal feedstock, native sulfuric acid and water -> a proposed plating electrolyte. Produce cleaning solution through a separate sodium/chlorine-based recipe. These are new recipes/fluids, not claims that those custom solutions already exist.
3. MI **Wiremill**: prepare native superconductor wire for contacts. MI **Assembler** combines a cast carrier with Create Precision Mechanisms, diamonds and Certus to make an unplated substrate.
4. Electroplating Line: clean the substrate, deposit its conductor layer, then rinse. Quantum Circuits and AE2 Engineering Processors are installed as part of the plated substrate recipe. Inputs are reserved once; each bath advances a saved stage.
5. MI **Centrifuge**: recover part of the dissolved metal from spent electrolyte and output wastewater. A treatment recipe recovers some water with sludge loss. The loop must never return more metal or fluid than it consumed.

Clean, plating, rinse and waste tanks are separate. A fluid hatch refers to one configured tank; it must not silently mix incompatible solutions. Piping waste to the input does not count as fresh electrolyte.

### Particle Collider and Phase Lattices

1. MI **Compressor** produces dense tungsten targets. MI **Polarizer** prepares charged iridium targets. Different targets go to the two injector hatches.
2. Produce Astral Essence in existing MI machines, as described below. No fourth custom processing multiblock is needed.
3. Produce native **Helium Plasma** through fusion and native **Cryofluid** through its MI processing chain.
4. Particle Collider: eight Astral Essence, eight left targets and eight right targets, plus plasma, Cryofluid and power -> eight Stabilized Exotic Matter. Exact fluid amounts and cycle duration remain balancing work.
5. MI **Assembler**: eight plated substrates + eight Stabilized Exotic Matter -> **eight Phase Lattices**.

An injected target waits in its own saved latch until the opposite injector is ready. This rewards balanced supply without requiring unreliable same-tick item delivery. Output cells must be available before a reaction begins. A power/coolant interruption pauses safely; it does not delete the expensive batch or explode the factory.

Across one batch of eight lattices, retain at least the earlier recipe's eight alloy, eight essence, eight Quantum Circuits, eight AE2 Engineering Processors, eight Create Precision Mechanisms, 16 diamonds and 16 Certus. Distribute these through carriers, plating and collision rather than charging the same ingredients twice. Targets and process fluids are the requested additional MI work.

## Astral Essence using existing MI machines

The proposed starting ratio remains **16 Nether Stars -> eight Astral Essence**, plus 64 each of Rotten Flesh, Bones, String, Leather, Slimeballs, Blaze Rods, Ender Pearls, Ghast Tears and Magma Cream, and eight Biological Catalysts.

Avoid a nine-input Chemical Reactor recipe. Compact each drop separately, assemble three groups of three compacted drops, then combine the three groups into one Mob Concentrate. Macerate stars into a new star-dust intermediate. The final native Chemical Reactor recipe then needs only three item inputs: star dust, Mob Concentrate and Biological Catalysts.

Eight Biological Catalysts require 64 Compost, 64 Silk Cocoons, 64 Paper and 64 MI Rubber Sheets, processed in an MI Assembler. Crop-derived nutrients and tree-derived carbon reinforcement can be added as separate prepared intermediates. All of these custom recipes/items remain to be registered.

For the 3,072-essence target, this ratio means 384 batches, 6,144 stars and 24,576 of **each** listed mob drop. Biological feedstocks also need large-scale automation. Do not put these totals directly into an oversized crafting-grid stack: use batches and actual intermediate items.

## Nexus Core and cheap builder

| Component | Planned construction route |
|---|---|
| **Nexus Ignition Cell**, six required | Each uses 16 Nexus Alloy, eight Phase Lattices, 16 Astral Essence, four Quantum Circuits and one MI Singularity, split into prepared components as necessary. |
| **Nexus Core**, one | Pressure-cast housing -> electroplated contacts -> MI Assembler with six Ignition Cells, containment components and control processors. Final core therefore contains 96 alloy, 48 lattices, 96 essence, 24 Quantum Circuits and six singularities before housing/finishing costs. |
| **Nexus Builder**, one | Eight Steel Plates, four native Motors, two Electronic Circuits and one Chest, through an ordinary machine crafting/assembly recipe. No endgame materials. |
| **Nexus Construction Program**, retained | Cheap blank program and a server-verified final progression unlock. The builder may preview the structure before that unlock, but cannot start the final project. |

The Core is a separate placed block with a framed, contained glowing centre. It is not the whole portal in one item/model. Fusion, nuclear processing, superconductors, quantum circuits, UU Matter, the three new processing machines and the required Revo production milestones gate the program.

## Hatches: reuse the existing native MI approach

Use the established `revoindustry.furnace.FurnaceItemHatch` / `Furnaces` integration: native MI `ItemHatch`, `MIInventory`, configurable slots, locking, auto-transfer, MI menus and matched casing artwork. Use native MI fluid and energy hatch implementations for their respective ports, rather than inventing another parallel hatch system. The older hatchery/geode ports provide useful behavior examples, but the furnace's native MI pattern is the requested foundation.

- Casting: item feed, mould input, mould return, finished output, pressure-fluid input, coolant input, warm-coolant output and energy.
- Plating: substrate/component input, finished output, separate cleaning/electrolyte/rinse inputs, dedicated waste outputs and energy.
- Collider: separate left/right target inputs, essence/container input, containment output, plasma/coolant inputs, spent-coolant output and energy.
- Portal builder: material input, return/output hatch, program slot and an energy connection. Portal stabilization can have separate coolant input/output.

The chosen native energy hatch tiers must actually sustain the machine's combined draw; do not reuse the small hatchery's fixed rate limit accidentally. Use long EU accounting, correct FE conversion limits if FE is exposed, simulation-safe operations and a shared per-tick cap across repeated calls. Structure checks, inventory contents, reserved inputs and stage progress must survive save/reload. Breaking blocks returns owned/reserved items exactly once. No forced chunk loading or repeated full 21 x 21 scan per hatch per tick.

## Portal: placed-block art blueprint

The first art blueprint is **21 wide x 19 high x 11 deep**, comprising **814 placed blocks**. Coordinates and part counts are recorded in [portal-blueprint.json](../art/blockbench/nexus-portal/portal-blueprint.json). These are proposed art parts, not registered block IDs or a finished executable builder blueprint. The budget above is not yet a solved per-block recipe ledger.

| Part | Count | Rendering |
|---|---:|---|
| Dark Nexus casing | 281 | Ordinary individual block |
| Foundation blocks | 178 | Ordinary individual block |
| MI-style ring segments | 48 | Ordinary individual block |
| Ceramic inner-ring panels | 44 | Ordinary individual decorative block |
| Grated deck blocks | 48 | Ordinary individual decorative block |
| Reinforced column blocks | 20 | Ordinary individual decorative block |
| Ribbed panels | 20 | Ordinary individual decorative block |
| Service access panels | 4 | Ordinary individual decorative block |
| Chiseled foundation blocks | 11 | Ordinary individual decorative block |
| Hazard blocks | 48 | Ordinary individual block |
| Trim/stair components | 27 | Individual block-sized models |
| Railings | 51 | Individual posts with two bars per connected direction; multipart exports cover all four directions |
| Blue / amber / violet lamps | 10 / 2 / 4 | Individual placed lamps |
| Beacon detail blocks | 5 | Small special details only |
| Vent blocks | 6 | Ordinary individual block |
| Core, controller, item input/output, fluid input/output, energy | One each | Separate placed blocks; hatch behavior remains native MI-based |

The purple swirl occupies the opening only. The Blockbench assembly is a construction/art preview, **never a single runtime model to replace the placed structure**. The revised preview makes one turn in **16 seconds**, with linear 45-degree steps and matching loop endpoints. Its front/back faces are separated by 1/64 block rather than placed on the same plane. An alternative 64-frame, 9.6-second interpolated texture study demonstrates surface flow; runtime should choose one animation strategy, not play both on top of each other accidentally. Portal effects exist only while formed/activated and use the controller's actual bounds for rendering/culling.

The 51 fence blocks connect according to their cardinal neighbours, including corners and connections to solid tower blocks. The centre stair entrance remains open. The six decorative block types replace existing casing/foundation/ring positions and leave the total at 814 placed blocks. Their final craft costs belong in the pending recipe ledger.

## Remaining implementation work

1. Resolve high-tier drill feedstocks (especially tungsten, iridium and nuclear materials), faster debris extraction, reliable Wither Skeleton/Ghast programs, AE2 unlocks and any missing gem-sludge routes. Quarries remain excluded.
2. Turn the budget into a complete recipe ledger, including batch rounding, native component costs and construction of all three new machines. Check every ingredient has an obtainable upstream route.
3. Create and validate individual-block blueprints and models for the caster, plating line and stepped-cone collider.
4. Register blocks/items/fluids, native MI-based controllers/hatches, recipes, EMI guides, Jade diagnostics and progression milestones.
5. Implement builder preview, obstruction checking and incremental placement. Reserve/consume each real block once, preserve unrelated world blocks, persist the placement cursor, and pause on missing power/materials.
6. Implement the Core and portal-only visual effects, then test rotation, save/reload, structure breaks, blocked outputs, input simulation, native auto-transfer and power limits.

This goal connects the existing Revo systems through production. It does not yet establish that every old machine tier is mandatory; explicit production milestones are still needed for storage, Trading Colonies, vacuums and other optional branches.
