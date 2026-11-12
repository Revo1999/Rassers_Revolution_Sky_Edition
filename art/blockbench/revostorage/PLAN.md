# Storage and progression block asset plan

Preference: balance appearance and performance. Target: Minecraft Java 1.21.1, desktop, repeated static blocks. Vanilla albedo lighting, one opaque material per block, no PBR or animation. Shared 64x64 atlas per block; 1 image pixel per model unit, proportionate UV spans. Geometry gives bay and display depth; fine hardware is painted. Working budget: around 20 elements per block.

Built in live Blockbench with native Undo transactions. Editable sources and Java runtime models are both saved. Chest drive: 18 cubes / up to 108 quads; terminal: 10 / 60; electric drill: 13 / 78; trading colony: 9 / 54. Boundary cullfaces are included. Triangles are at most twice the quad counts before culling. Draw calls and runtime FPS have not been benchmarked. Cube UV spans equal face lengths on the 64-pixel logical grid; Java export scales these by 16/64.
