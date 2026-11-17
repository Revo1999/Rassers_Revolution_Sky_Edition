# Revo machine model audit — 2026-11-17

Checked every shipped Revo model definition: **452 JSON models, including 203
with their own geometry**. Repaired **48 exports** using Blockbench MCP. This
includes legacy standalone machine parts still shipped in the jars, alongside
the models actually used by the assembled renderers.

| Machine family | Coverage and result |
| --- | --- |
| Wooden greenhouse | Wall glass reaches the pillars and headers; roof glass reaches the roof frame. Hidden pane edges and conflicting frame faces removed. Glass material retained. |
| Iron greenhouse | Body, glazing, active lights, and tank reviewed. Glazing around the tank meets at clean seams. Covered structural faces trimmed. |
| Wooden and steel tree growers | Body surfaces repaired. Steel body and separate tank glass checked together. Existing block-based miniature tree rendering retained. |
| Wooden and steel mob harvesters | Cage, collection grate, filter, body, and supports checked; covered surface portions removed. Native mob models remain supplied by Minecraft. |
| Wooden, iron, and steel drills | Bodies, standalone structural parts, cores, bits, ports, and gears checked. Each assembled body/bit/gear sampled at 36 angles. Iron braces have proper depth separation. |
| Electric drill | Body repaired; body plus rotating fan checked at 36 angles. |
| Mechanical sluice and feeder | Three world segments, feeder, drive, crank, yoke, trough, shaker, and springs checked at 36 angles. Gear depth steps corrected; hidden axle end removed. Actual exported solids also pass 129-position oriented-box clearance checks. |
| Saline geode chamber | Electrode surfaces repaired; bed trimmed around the sockets. Five crystal growth states checked with bed and electrodes. Internal transparent water interfaces removed. Solid parts now use back-face culling. |
| Wither hatchery | Casing, glass, ports, egg stages, and roof lamps included in the asset scan. Egg rocking and the three lamps sampled at 240 times; egg stays above the floor and below the lamps. |
| Primitive vessels | Rain barrel and crucibles checked; water and lava submit one exposed top face each. Native animated fluid textures retained. |
| Composter and silkworm cultivator | Bodies and fill/worm/cocoon contents checked together across 16 states each. |
| Power converters | All three tiers and integrated variants included in the resource scan. No new surface repair needed. |
| Vacuums | All three tiers checked and repaired; duct and flange segments use clear axial steps instead of nearly coincident faces. |
| Trading colony | Overlapping body surfaces repaired. |
| Storage | Chest drive repaired; crafting terminal checked. |
| Furnace multiblocks | All ten tiers, casings, controllers, and hatches included. Their geometry uses the native MI machine loader rather than an overlapping replacement body. |
| Pressure Casting Plant | All 161 placed blueprint cells checked; native MI hatches retained. |
| Electroplating Line | All 199 placed blueprint cells checked; native MI hatches retained. |
| Particle Collider | All 784 placed blueprint cells checked; native MI hatches retained. |
| Nexus portal | All 814 placed cells checked, including connected rail parts. Existing separated front/back animated portal faces retained. |
| Nexus builder | All four blueprint cells checked; native MI input/output/energy parts retained. |
| Shared library, genetics, achievements | Included in repository resource coverage. No additional custom machine geometry in these modules; inherited vanilla models remain inherited. |

## Evidence

- `all-machine-model-audit.json`: production model counts, exact and near-plane
  checks, 21 renderer/blueprint assembly checks, export provenance, and results.
- `sluice-runtime-clearance.json`: 129 samples of the actual runtime solid
  exports, including renderer scale, pivots, lifts, and spring deformation;
  **zero conflicts** between separate moving assemblies.
- `art/blockbench/surface-repairs/native-validation.json`: all 48 current native
  sources pass outliner, node listing, positive geometry, and Java block bounds
  checks through MCP.
- `art/blockbench/surface-repairs/previews/`: selected native MCP render reviews.

The assembly checker uses the real renderer transformations. Rotated cubes are
included in the face checks; sluice solid clearance uses oriented boxes. Common
facing rotations do not change the relative clearances. Individual Nexus blocks
stay individual blocks, and connected railing parts are included at their placed
positions. External MI hatch geometry is provided by MI's loader; it is not
reimplemented or modified here.

## Scope and limits

This pass uses static geometry checks, sampled motion, native MCP render reviews,
and a production build. **It does not claim a fresh graphical Minecraft or
shader sweep.** Near-plane spacing is a conservative heuristic, not proof for
every camera distance or shader. Opposing faces at normal structural joints are
not treated as duplicate outward surfaces. Moving contact beyond the sampled
states is not exhaustively proven.

Two steel-drill tank glass/port pairs are retained deliberately: transparent tank
walls sit in front of opaque ports. A separate near-plane finding in the elite
feeding **helmet item** is recorded as an equipment follow-up, outside this
machine task.

The older first-tier/drill asset validators also have pre-existing assumptions
about atlas families, geometry counts, and historical UV density. They are not
reported as passing here. The new production audit validates the shipped exports,
their texture references, bounds, visible faces, and current native provenance.

Face partitions add quads in some models to preserve the visible silhouette and
UVs while removing competing surfaces. No new performance benchmark was run.

## Repeat the checks

```powershell
.local/venv/Scripts/python.exe scripts/collect_machine_surface_repairs.py --refresh
.local/venv/Scripts/python.exe scripts/check_all_machine_models.py
.local/venv/Scripts/python.exe scripts/check_sluice_clearance.py --runtime
.\dev.cmd build collectMods :revolib:syncPackOverrides --console=plain
.local/venv/Scripts/python.exe scripts/package_modpack.py
```

Use the manifest's current sources for future edits. Running older asset
generators directly over these repaired exports would reintroduce the defects.
