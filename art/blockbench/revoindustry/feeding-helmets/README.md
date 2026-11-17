# Feeding Helmet models

Three native Blockbench Generic Models, built through the desktop MCP server.
Each contains an olive/steel military helmet, a side ration module, rubber feeding
tube and mouthpiece. The reinforced tier adds a second pod and controls; the elite
tier adds temple armor and three rear ration canisters. The head reference is
Minecraft's Steve skin, marked `export: false` and excluded from runtime exports.
Steve artwork is © Mojang AB. The helmet geometry and pixel art are custom.

The target is Minecraft Java 1.21.1 / NeoForge, balanced desktop detail and cost,
vanilla directional albedo lighting, opaque/cutout materials and no PBR. Textures
are 128×128 shared box-UV atlases at one pixel per model unit, with clustered
dither and wear. The larger atlas covers all parts; it does not increase the
16-pixels-per-block texture density. No animation is required: the complete
helmet follows the entity's ordinary head pose, including the mouth tube.

`scripts/build_feeding_helmet_models.py` preserves these native sources, uses the
Blockbench Java codec for item exports, and derives the worn `LayerDefinition`
from the same boxes/UV offsets. Runtime item icons use `textures/item`; worn
armor uses the same pixels under `textures/models/armor`.

See [gameplay details](../../../../docs/FEEDING-HELMETS.md) and
[in-game screenshots](../../../../screenshots/feeding-helmets).
