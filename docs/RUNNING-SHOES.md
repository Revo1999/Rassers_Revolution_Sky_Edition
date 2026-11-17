# Running shoes

Twelve early-game pairs in RevoCultivation: white, black, brown, red, orange,
yellow, green, cyan, blue, purple, magenta and pink. The user explicitly excluded
gray, light gray, light blue and lime.

Wear them in the **boots slot** for **25% more movement speed** and **zero fall
damage**. Holding or carrying a pair grants neither effect. The bonuses use native
equipment attributes, with shared modifier IDs across colours, so changing pairs
does not accumulate bonuses. No tick handler or permanent potion effect is used.
The shoes provide no armor points, have 256 durability and can be repaired with
Natural Shoe Rubber. They do not grant other damage immunity.

## Early crafting

| Recipe | Inputs | Output |
|---|---|---|
| Cotton starter | 1 Wheat Seeds + 1 String | 2 Cotton Seeds |
| Shoe latex | 2 Leaves + 1 Slimeball | 2 Shoe Latex |
| Cure rubber, ordinary furnace | 1 Shoe Latex + fuel | 1 Natural Shoe Rubber |
| Shoe glue | 1 Slimeball + 1 Sugar | 4 Shoe Glue |
| White shoes, shapeless | 4 Cotton + 2 Natural Shoe Rubber + 1 Shoe Glue + 1 String | 1 pair |
| Coloured shoes, shapeless | The same eight ingredients + 1 corresponding vanilla dye | 1 pair in that colour |

Cotton Seeds plant on ordinary farmland and grow like wheat, with bonemeal support.
A mature crop drops one Cotton and two Cotton Seeds; an immature crop returns one
seed. Both existing greenhouses accept retained Cotton Seeds and produce three
Cotton plus one seed per normal crop cycle. Cotton appends program **17**, leaving
existing saved program IDs unchanged.

The early route needs the existing wheat/string, leaf, Slime-program and sugar-cane
supplies, plus a furnace. It needs no steel, electricity, blaze burner or EBF.
White shoes do not need a dye. Cotton acquisition unlocks all twelve recipes in
the recipe book; EMI sees the ordinary crafting and smelting recipes normally.

Shoe Latex, Natural Shoe Rubber and Shoe Glue are dedicated early materials.
They are deliberately not MI Rubber Sheets, Create Super Glue, or globally tagged
industrial rubber substitutes. MI's rubber and electrical progression are preserved.
These simple game recipes are not intended as real-world rubber chemistry.

## Models and textures

The design takes its thick sole and low running-shoe silhouette from the supplied
reference. The simplified design uses just **two cuboids per shoe**: a solid upper
and one pale sole. Laces, ankle opening and a small side stripe are texture pixels.
Two-tone pixel dithering gives the upper a woven finish; the lace rows occupy the
two middle texture columns on top, centered across both shoes.
The shoes use native 64px pixel
atlases. There are separate 3D item models and a leg-bound wearable model.

All shoe geometry was edited and exported using **Blockbench MCP**. Editable
sources, the native Java wearable export, validation and previews live in
[the art folder](../art/blockbench/running-shoes/). Runtime wearable geometry is
extracted unchanged from the MCP-generated `createBodyLayer` method; the client
adapter supplies NeoForge's armor extension and normal leg animation.

`scripts/generate_running_shoes.py` regenerates the pixel textures, crop visuals,
recipes and names. It does not write shoe geometry. Edit models through MCP,
export Java Block/Item models for version `1.9.0`, and export the wearable source
with the Mojang-mapped `1.17` Java template. Collect those exports with:

```powershell
.local/venv/Scripts/python.exe scripts/collect_running_shoes.py --source "C:\path\to\Blockbench\running-shoes"
```

If Blockbench MCP is unavailable, stop model work and ask the user; no alternative
model authoring tool or direct geometry-writing fallback is authorized.

## Verification

Three native NeoForge GameTests pass, covering real equipment attribute changes,
fall damage while equipped and after removal, holding shoes without bonuses,
all twelve native crafting recipes, cotton starter compatibility and mature crop
loot. All twelve item models and the wearable source pass the applicable MCP
geometry checks. Build production jars without the opt-in test flag:

```powershell
.\dev.cmd :revocultivation:runShoesTests -PrevoShoesTests
.\dev.cmd build collectMods
```

The sources compile on Minecraft 1.21.1 / NeoForge 21.1.265. Headless Blockbench
renders review the item geometry; a live player/client visual inspection is a
separate check and is not claimed by the server tests.
