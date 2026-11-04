# Blockbench workflow

Save editable .bbmodel sources under art/blockbench/<modid>/.
For static blocks/items choose Java Block/Item format; export JSON into
<modid>/src/main/resources/assets/<modid>/models/block or models/item.
Export PNGs into textures/block or textures/item in the same namespace.
Use texture references such as revoextraction:block/void_excavator.
Blockstate JSON belongs in blockstates; Java registration is still required.
Animated entities or block entities need a runtime renderer/animation library decision;
Blockbench exports alone do not implement machines or animation at runtime.

Blockbench desktop MCP is available and used for the delivered machine models.\nInspect current projects and capabilities before editing, preserve saved user\nwork, and use native Undo scopes and codecs for edits and exports. See\n[SINGLE-BLOCK-CULTIVATION.md](SINGLE-BLOCK-CULTIVATION.md) for the compact\nmachine coordinates, texture density and reproducible native builder.\n