# Steel Drill sediment output

The output is a real subclass of MI `FluidHatch`, using its native tank, locking,
menu, automatic fluid extraction and model loader. `runtime-model.json` records
the loader metadata: the original MI steel casing and fluid hatch/output/auto
overlays. The zero-cube Blockbench project and native export are the imported MI
model template; MI supplies the runtime casing geometry.

The existing production drill body was edited through Blockbench MCP to clear
the **rear-top centre** cell. Its current source/export remain in
`art/blockbench/surface-repairs/revoextraction/steel_drill_body.*`, as recorded in
that folder's manifest. The normalized hatch cell is X 16/3..32/3,
Y 32/3..16, Z 32/3..16. It must not be added to the production body: the native
MI block renders there separately.

`steel_drill_with_sediment_preview.bbmodel` is an MCP-authored review assembly
with a preview cube for the native hatch. Its textures compose original MI
sprites at nearest-neighbour scale for the shared atlas resolution. It is never
shipped as a replacement body. `steel-drill-sediment-preview.png` shows the back.

The structure now requires 12 supports, 8 placed casings, core, controller,
shaft input, item output, water tank, and this fluid hatch. Keep the bottom centre
air. The hatch's MI output direction points away from the front controller.
