# Submerged Salt Geode Chamber

The renewable salt route is:

**Water + electricity -> submerged crystal growth and harvesting -> salt water -> native MI Centrifuge -> MI Salt Dust.**

This uses the existing MI Centrifuge, as chosen for the pack. No extra MI addon or custom Fluid Solidifier is required.

## Progression

The chamber uses steel, quartz, redstone, analog circuits, motors and ordinary MI pipes/cables. Its saline geode core contains no salt ingredient. It becomes usable with LV power; the finishing step uses the native Centrifuge's normal electronic-circuit, large-motor and pump progression.

In the pinned MI recipes, the first industrial salt consumer is the electric Electrolyzer: two Salt Dust become sodium dust and chlorine. Its construction uses the same general electronic-component tier as the Centrifuge. The first LV turbine, analog circuits, ordinary motors and the geode chamber do not require salt. Native electronic circuits have undoped silicon routes that do not require chlorine. There is therefore no salt-specific pre-electricity bootstrap loop, and no tedious manual salt recipe has been added.

This closes the salt feedstock gap. It does not claim to resolve the other advanced-metal or chemistry gaps listed in the progression audit.

## Build: 3 x 3 x 3

Use the controller's EMI assembly guide; it uses the same blueprint as server validation.

| Part | Count | Location |
|---|---:|---|
| Wither Containment Casing | 17 | Four base corners, four middle corner pillars and all nine roof positions. Reuses the common MI-steel containment shell. |
| Smoked Containment Glass | 4 | Front, rear, left and right centres of the middle layer. |
| Salt Geode Chamber controller | 1 | Front centre of the base, facing outward. |
| Saline Geode Core | 1 | Centre of the base, beneath the crystal-growth space. |
| Geode Water Input Hatch | 1 | Left centre of the base, facing outward. |
| Geode Salt Water Output Hatch | 1 | Right centre of the base, facing outward. |
| Geode Energy Hatch | 1 | Rear centre of the base, facing outward. |

The middle centre must remain air. The renderer supplies the submerged rock bed, growing halite buds, electrodes and live water without inserting hidden world blocks. All four horizontal orientations work.

The casing, glass and ports follow the existing MI-style Revo machines. Editable models and the pixel-art atlas are in [art/blockbench/salt-geode](../art/blockbench/salt-geode); geometry was authored through Blockbench MCP. The Wither Hatchery's approved glass assets are reused without modification.

## Operate

1. Fill the left water hatch by bucket or pipe.
2. Feed native MI EU or equivalent FE into the outward face of the rear energy hatch.
3. Watch crystal buds grow under animated water, with flickering electrical arcs. The final portion of the cycle harvests/dissolves the buds into brine.
4. Extract salt water from the right hatch, by pipe or bucket. The hatch also automatically pushes into a compatible fluid inventory immediately beside its outward face.
5. Feed it to the existing MI Centrifuge.

| Process | Input | Base power | Duration | Output |
|---|---|---:|---:|---|
| Geode growth and harvest | 1,000 mB water | 16 EU/t | 400 productive ticks / 20 seconds | 1,000 mB salt water |
| Native MI centrifuging | 1,000 mB salt water | 8 EU/t | 200 recipe ticks / 10 seconds before MI speed behaviour | 2 MI Salt Dust |

At the pack's current 10 FE/EU ratio, the chamber's working draw is 160 FE/t. Its batch consumes 6,400 EU. The Centrifuge retains native MI energy, recipe-efficiency and overclock behaviour.

Each fluid reservoir holds 8,000 mB; the energy buffer holds 6,400 EU. The energy hatch accepts up to 64 EU/t, shared across repeated FE/EU input calls. The geode is retained rather than consumed.

Controller and port interactions open inventory-free diagnostics. Jade summarizes water, brine, power and progress. The fluid hatches expose real sided fluid capabilities, rather than item-only bucket recipes. Salt Water is a registered fluid with a filled bucket and native pipe compatibility.

## Pause, save and automation behaviour

- The input accepts water only. The brine hatch rejects insertion and exposes extraction only.
- Starting a batch reserves exactly one bucket of water. That reserved batch remains visible in the basin even when the input reservoir is empty.
- Power loss, full brine storage, a missing part, wrong port orientation or an obstructed centre pauses processing without spending further cycle energy.
- Input/output tanks, reserved water, growth progress, stored energy and completed-cycle count survive saves. Resuming a batch does not consume another bucket.
- Cached port capabilities validate the current structure before moving fluids or energy.
- Automatic output respects the neighbouring inventory's acceptance and retains rejected fluid.
- The machine does not force-load chunks. Breaking the controller loses its fluids and in-progress batch, as with the pack's other tank machines; it does not drop a free salt result or duplicate water.

## Quests and verification

Two quests extend **Progression Routes: Biology to Electricity**: **A Geode Under Water** and **Salt from the Centrifuge**. Existing quest IDs and progression are preserved.

```powershell
.\dev.cmd :revoindustry:runSalternTests -PrevoSalternTests
.\dev.cmd :revoindustry:runSalternUiQa -PrevoSalternUiQa
.\dev.cmd build collectMods
```

The native server suite verifies rotations, sided/filtered capabilities, simulation and shared power limits, exact water/energy/output balance, pause/reload, full output and broken structures. Its integration test exports chamber brine into a real MI Centrifuge and checks that the machine produces exactly two native Salt Dust.

The opt-in graphical fixture creates a new uniquely named **Revo Salt Geode QA** save. It captures water, crystal growth, electrical effects, harvesting and actual native MI salt output, then closes the client. It never opens an existing player save. Test helpers are excluded from production JARs.
