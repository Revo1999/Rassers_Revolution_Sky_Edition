# Revo multiblock construction guides

Use an interactive 3D EMI construction guide for Revo multiblocks, including future
machines. The interaction reference is [MI Multiblock Guide for EMI](https://www.curseforge.com/minecraft/mc-mods/mi-multiblock-emi):
rotate the actual placement blocks, build from the foundation upward, inspect one
layer, identify ports and gather the required materials. Revo's implementation is
original code in RevoLib; it does not require MI or that addon to recognize a Revo
structure.

## Shared implementation

`revolib/src/main/java/dk/revo/revolib/client/guide/` contains:

- `MultiblockGuide`: immutable block states, controller-relative offsets, port roles,
  layer instructions and power information. Material counts come from these blocks.
- `MultiblockPreview`: vanilla baked block rendering in GUI space, rotation, zoom,
  ray-picked block tooltips, bottom-up/single-layer views and port filtering.
- `MultiblockGuideScreen`: larger construction scene, materials, power information,
  selected-layer instructions and navigation back to EMI.
- `emi/MultiblockEmiRecipe`: the common EMI recipe, ordinary material ingredient slots,
  compact scene controls and the button that opens the larger guide.

The classes are loaded from client integrations only. The EMI adapter has a
compile-only EMI API dependency; RevoLib does not force EMI onto a server.
Previewing a structure does not place blocks or send construction packets.

## Registering the next machine

1. Use the same blueprint as the server's formation check. Do not maintain an
   independent array of positions in the client integration.
2. Pick one canonical facing and convert positions to offsets from the controller.
   The controller must be `(0, 0, 0)`. Supply unformed placement block states with
   their actual facing properties. Formed states can intentionally have empty models.
3. Give each part a role: `BLOCK`, `CONTROLLER`, `OUTPUT` or `POWER`. Include required
   air positions in the data; they are omitted from materials and solid rendering.
4. Provide one short, useful instruction for each vertical layer, bottom first,
   plus the machine's power description. The constructor rejects duplicate positions
   or a mismatched layer-instruction count.
5. Register an EMI category and workstation, then add a `MultiblockEmiRecipe` with
   the category, recipe resource ID, guide data and controller stack. It advertises
   the controller as the guide's result, but `supportsRecipeTree()` is false because
   assembly is a construction reference rather than a crafting transaction.
6. Verify the material total, port positions, actual facing and every layer in-game.
   Keep a small black **R at the bottom-right** of controller and guide interfaces;
   inventory hatches have no branding footer.

Example registration (inside the machine's EMI plugin):

```java
registry.addRecipe(new MultiblockEmiRecipe(
    category,
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "assembly/my_machine"),
    guideFromFormationBlueprint(),
    EmiStack.of(controller)
));
```

The wooden drill's example is `DrillGuideDefinition.create()`. It reads `DrillLayout.part()`
and maps local `(x, y, z)` to controller-relative `(-x, y - 1, z - 1)`. Its canonical
controller faces south, shaft input west and output hatch east, matching the
server's clockwise/counterclockwise side convention. There are 26 placed
blocks: 12 supports, 10 casings, one core, controller, shaft input and output hatch.
The remaining bottom-centre cell stays air. The materials list includes the
controller, even though it is also the item used to find the guide in EMI.

## Player controls

In EMI, open the controller's recipes and select **Wooden Drill Assembly**.

- Drag the 3D scene with the left mouse button to rotate; right-click it to reset.
- `<` and `>` select a layer. **Build up / Single layer** switches cumulative and
  isolated-layer views.
- `+`, `-` and **Reset** control the compact scene's zoom and camera.
- The port button cycles off, all ports, controller, output hatch and shaft input.
  Matching blocks have green outlines; other blocks dim.
- Hover a block for its name, role and offset from the controller. Material slots
  remain normal EMI ingredients with recipe/use navigation.
- **Open 3D guide** opens the larger view. Scroll over that scene to zoom; left/right
  arrow keys change layers, `H` cycles port highlighting and `R` resets the camera.
  **EMI recipes** returns to the controller's recipe entries. **Back** or Escape
  restores the previous EMI screen.

## Verification and scope

The placement scene shows the blocks to build before activation. The assembled
machine's decorative body and rotating drill continue to use the runtime renderer;
the construction guide does not substitute that decoration for required blocks.
Future machines with special block-entity-only placement visuals should extend
the preview renderer for those visuals and verify them before shipping.

Use the optional drill UI QA in a copied save:

```powershell
.\dev.cmd -PrevoDrillUiQa :revolib:runClient
```

It opens both machine interfaces by interacting with their real blocks, captures
the EMI scene and larger guide, checks the 26-block materials total, and exercises
single-layer, port-highlight, rotation and zoom views. Evidence is saved under
`art/blockbench/revoextraction/`. Follow with a normal build so optional QA classes
are excluded from shipped jars.

For inventory layout, automatic output and shared controller/Jade diagnostics,
follow [MACHINE-INTERFACES.md](MACHINE-INTERFACES.md).
