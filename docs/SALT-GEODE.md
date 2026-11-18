# Submerged Salt Geode Chamber

The renewable salt route is:

**Steel Drill cooling water -> Sediment-laden Water -> native MI Mixer -> Seawater -> powered Salt Geode Chamber -> Salt Water -> native MI Centrifuge -> MI Salt Dust.**

This uses the existing MI Centrifuge, as chosen for the pack. No extra MI addon or custom Fluid Solidifier is required.

## Progression

The Steel Drill returns its cooling water through a new native MI fluid output hatch. The existing MI Mixer leaches the minerals into seawater, using 8 EU/t for 200 base ticks per bucket. Vanilla water has no salinity distinction; the two new fluids are pack additions, and no ocean biome is required.

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
| Geode Seawater Input Hatch | 1 | Left centre of the base, facing outward. |
| Geode Salt Water Output Hatch | 1 | Right centre of the base, facing outward. |
| Geode Energy Hatch | 1 | Rear centre of the base, facing outward. |

The middle centre must remain air. The renderer supplies the submerged rock bed, growing halite buds, electrodes and live water without inserting hidden world blocks. All four horizontal orientations work.

The casing, glass and ports follow the existing MI-style Revo machines. Editable models and the pixel-art atlas are in [art/blockbench/salt-geode](../art/blockbench/salt-geode); geometry was authored through Blockbench MCP. The Wither Hatchery's approved glass assets are reused without modification.

## Operate

1. Fill the left seawater hatch by bucket or pipe.
2. Feed native MI EU or equivalent FE into the outward face of the rear energy hatch.
3. Watch crystal buds grow under animated water, with flickering electrical arcs. The final portion of the cycle harvests/dissolves the buds into brine.
4. Extract salt water from the right hatch, by pipe or bucket. The hatch also automatically pushes into a compatible fluid inventory immediately beside its outward face.
5. Feed it to the existing MI Centrifuge.

| Process | Input | Base power | Duration | Output |
|---|---|---:|---:|---|
| Drill coolant recovery | 100 mB ordinary water per work cycle | Create shaft | Alongside drill work | 100 mB sediment-laden water |
| Native MI Mixer | 1,000 mB sediment-laden water | 8 EU/t | 200 recipe ticks / 10 seconds | 1,000 mB seawater |
| Geode growth and harvest | 1,000 mB seawater | 16 EU/t | 400 productive ticks / 20 seconds | 1,000 mB salt water |
| Native MI centrifuging | 1,000 mB salt water | 8 EU/t | 200 recipe ticks / 10 seconds before MI speed behaviour | 2 MI Salt Dust |

At the pack's current 10 FE/EU ratio, the chamber's working draw is 160 FE/t. Its batch consumes 6,400 EU. The Centrifuge retains native MI energy, recipe-efficiency and overclock behaviour.

Each fluid reservoir holds 8,000 mB; the energy buffer holds 6,400 EU. The energy hatch accepts up to 64 EU/t, shared across repeated FE/EU input calls. The geode is retained rather than consumed.

Controller and port interactions open inventory-free diagnostics. Jade summarizes water, brine, power and progress. The fluid hatches expose real sided fluid capabilities, rather than item-only bucket recipes. Seawater, Sediment-laden Water and Salt Water are distinct registered fluids with a filled bucket and native pipe compatibility.

## Animation

The five existing crystal buds are exported separately through Blockbench MCP. Each is anchored in the rock bed, grows with a staggered smooth curve, then harvests and dissolves in sequence during the last 70 productive ticks. Small mineral particles mark harvesting. Electrical pulses use the same paid recipe clock. Client rendering interpolates between the four-tick state packets; a stopped machine holds its pose instead of jumping or continuing work visually.

Existing saved freshwater buffers upgrade once to the same volume of seawater; reserved batches and progress survive. New fluid insertion always requires seawater.

## Pause, save and automation behaviour

- The input accepts seawater only; fresh water and raw sediment are rejected. The brine hatch rejects insertion and exposes extraction only.
- Starting a batch reserves exactly one bucket of seawater. That reserved batch remains visible in the basin even when the input reservoir is empty.
- Power loss, full brine storage, a missing part, wrong port orientation or an obstructed centre pauses processing without spending further cycle energy.
- Input/output tanks, reserved water, growth progress, stored energy and completed-cycle count survive saves. Resuming a batch does not consume another bucket.
- Cached port capabilities validate the current structure before moving fluids or energy.
- Automatic output respects the neighbouring inventory's acceptance and retains rejected fluid.
- The machine does not force-load chunks. Breaking the controller loses its fluids and in-progress batch, as with the pack's other tank machines; it does not drop a free salt result or duplicate water.

## Quests and verification

**Capture the Cooling Sediment** extends the Steel Drill chapter; **Seawater from Drill Sediment**, **A Geode Under Water** and **Salt from the Centrifuge** teach the processing chain in **Progression Routes: Biology to Electricity**. Existing quest IDs and progression are preserved.

```powershell
.\dev.cmd :revoindustry:runSalternTests -PrevoSalternTests
.\dev.cmd :revoindustry:runSalternUiQa -PrevoSalternUiQa
.\dev.cmd build collectMods
```

The native server suite verifies rotations, sided/filtered capabilities, simulation and shared power limits, exact water/energy/output balance, pause/reload, full output and broken structures. Its integration test exports chamber brine into a real MI Centrifuge and checks that the machine produces exactly two native Salt Dust.

The opt-in graphical fixture creates a new uniquely named **Revo Salt Geode QA** save. It captures water, crystal growth, electrical effects, harvesting and actual native MI salt output, then closes the client. It never opens an existing player save. Test helpers are excluded from production JARs.

## Graded and dry sediment

The Electric Drill is now a 3x3x3 native-hatch multiblock. LV geology produces Good Sediment Water; MV/HV geology produces Excellent Sediment Water. The optional 1x1 HV Digital Drill produces dry sediment with configurable item/EU faces and no cooling fluids. See [ELECTRIC-AND-DIGITAL-DRILLS.md](ELECTRIC-AND-DIGITAL-DRILLS.md) for the construction layers, recipes, dilution ratios and side settings.
