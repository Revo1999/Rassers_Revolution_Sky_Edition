# Electric and Digital Geological Drills

## Tiers and salt route

| Machine/program | Size | Power | Sediment per geological cycle |
| --- | --- | --- | --- |
| Create Steel Drill | 3x3x3 | Create shaft, 64 RPM minimum | 100 mB Sediment-laden Water from 100 mB coolant |
| Electric Drill, LV programs | 3x3x3 | 16 EU/t | 100 mB Good Sediment Water from 100 mB coolant |
| Electric Drill, advanced survey/tungsten | 3x3x3 | 128 EU/t | 100 mB Excellent Sediment Water from 100 mB coolant |
| Electric Drill, nuclear survey | 3x3x3 | 512 EU/t | 100 mB Excellent Sediment Water from 100 mB coolant |
| Digital Drill | 1x1x1 | 512 EU/t | One Dry Drill Sediment; no coolant or fluid output |

The existing Glacial Ice bucket program is not geological drilling and produces no sediment. Existing survey cores, rare-output cursors and geological output tables are retained. Gems still come from washing/centrifuging sludge, never directly from the drills.

## Electric construction

Use 11 Steel Drill Supports, 8 Steel Drill Casings, one Steel Drill Core, one Electric Geological Drill controller, one Drill Sediment Output Hatch, native MI steel item input/output hatches, a native MI steel fluid input hatch, and an LV or better native MI energy input hatch. The bottom centre remains empty.

Layers below are viewed from above, with the controller/front towards the bottom. S = support, C = casing, K = core, X = controller, . = air.

```text
Top             Middle       Bottom
Water Sed S     S C S        S C S
EU    C   Items S K C        C . C
S     Input S   S X S        S C S
```

The sediment hatch faces backwards. All five MI hatches occupy the upper layer, safely above the swept bit and gear. Hatch role validation accepts the appropriate native MI hatch type; a higher-tier energy input can power advanced programs. EMI includes an interactive layer-by-layer construction guide derived from the same server layout. Native hatch GUIs, fluid/item locks, sided transfer and automatic extraction are retained. Ore output appears both in the native item hatch and the controller's output view.

Fresh water enters the native fluid input. Geological work conserves coolant exactly: 100 mB in and 100 mB out per full cycle, distributed across productive ticks. Full item/fluid outputs, wrong sediment grade, missing water or incomplete structure stop productive work. Empty the sediment output before switching from Good to Excellent. Power may still charge the buffer while work is stopped.

An old single-block electric controller retains its inventory, energy, core and saved program when rebuilt into the new structure. Any old buffered coolant migrates into available native input capacity. Existing controllers require the surrounding structure before resuming.

## Digital construction and configurable sides

Craft with two Highly Advanced Machine Hulls, two Advanced Motors, two Digital Circuits, two Titanium Plates and an Electric Geological Drill. This is an optional HV compact upgrade. It does not bypass retained survey-core gates.

Open **Sides** for the six-face configuration panel. Each button cycles Off, blue Item Input, orange Item Output, yellow EU Input. Directions are absolute world directions, labelled U/D/N/S/E/W. Default top = input, bottom = output, south = EU. The block renders coloured versions of native MI item/energy hatch textures on assigned faces. Settings persist and capability caches update when a face changes.

Both geological output and dry sediment must fit before any work or energy is spent. Item input accepts retained cores and the Glacial Ice water bucket; item output exposes completed products only. There is no cooling-fluid capability.

## Native MI processing

| MI Mixer input | Output | EU/t | Base ticks |
| --- | --- | --- | --- |
| 1000 mB Sediment-laden Water | 1000 mB Seawater | 8 | 200 |
| 1000 mB Good Sediment Water + 1000 mB Water | 2000 mB Seawater | 16 | 200 |
| 1000 mB Excellent Sediment Water + 3000 mB Water | 4000 mB Seawater | 32 | 200 |
| 1 Dry Drill Sediment + 1000 mB Water | 1000 mB Excellent Sediment Water | 32 | 200 |

Pipe seawater to the existing Salt Geode Chamber: 1000 mB seawater becomes 1000 mB salt water in 400 productive ticks at 16 EU/t. The native MI Centrifuge then produces two Salt Dust from 1000 mB salt water at 8 EU/t for 200 base ticks. MI overclock/efficiency rules apply to native recipes. No starting salt or ocean biome is required.

Quests: Capture the Cooling Sediment, Seawater from Drill Sediment, Better Sediment Water, Compact Digital Drilling. Existing quest/task IDs remain intact.

## Verification

- Fourteen native extraction GameTests cover steel conservation/backpressure, native MI hatch persistence, electric LV/MV grades, digital atomic co-products/configuration persistence, shift-click extraction from native MI storage and actual native Mixer recipes for every added grade.
- Eight salt chamber GameTests cover the real Mixer-to-chamber route, exact balance, saves, ports, interruptions and the native MI Centrifuge.
- Model audit checks native MCP export provenance, competing surfaces and transformed assemblies, including five native hatch casings and all six Digital Drill face orientations. Continuous vertical bounds and full-turn samples prove that top-layer hatches clear rotating steel/electric drill parts.
- The opt-in root graphical fixture creates a fresh isolated QA save, captures the complete steel-to-salt pipeline plus electric/digital models and the side UI, and closes the game. It supplies QA shaft speed and EU directly.

The 19-test progression regression suite also passes with fixtures updated for the current spawner-program selectors and native electric hatch storage.
