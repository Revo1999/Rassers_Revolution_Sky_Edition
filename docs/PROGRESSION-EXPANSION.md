# Progression expansion

## Controlled resource routes

Wooden greenhouse retains starters for wheat, carrots, potatoes, beetroot, sugar cane, kelp and cactus. New plants yield three per harvest. Starter crafting uses RevoCultivation compost; kelp returns a bucket. Wooden mob harvesters add Slime; steel adds Enderman and Magma Cube. Species indices preserve the old wooden/steel selections through Cow and Blaze. One completed Blaze cycle earns one burner fill; switching away clears the credit.

Raw gold can be milled before crushing wheels, then washed using native Create quartz processing. The alternative Empty Blaze Burner recipe uses four iron sheets and a brick block. Native MI synthetic oil, paper and rubber chemistry remains unchanged. New quests explain hand crafting circuits/motors and the LV turbine, then steam -> EU -> powered machine.

## Electric geology and later materials

A compact Electric Geological Drill uses 16 EU/t for 200 powered ticks per normal resource. Mixed cycles silver, nickel, lead, bauxite dust and antimony, with individual programs available. Glacial mode uses a water bucket per ice and returns the bucket. Deep Survey needs a core made from silver, nickel, lead, aluminum and diamond, plus 128 EU/t for 1200 ticks. The saved deep cycle returns Ancient Debris every 64 outputs; terrain materials and glowstone fill the rest. This is deliberately a larger power step than LV extraction. Heated mixing supplies netherrack and soul sand earlier. The steel Trading Colony exchanges 16 wheat or paper for one emerald every 200 ticks.

## RevoStorage

Place drives and terminals face-adjacent (up to 128 connected storage blocks). Nine chest bays use native chest sizes queried from the installed mod. Nine diamond chests provide 972 slots / 62,208 items at 64 per slot. Vanilla chest: 27 slots. Filled component-bearing portable chest items must be emptied first. Each bay owns a saved 108-slot maximum bank and exposes only its installed chest capacity. A nonempty bay locks its chest in place. Breaking a drive drops all stored stacks and physical chests.

The terminal aggregates matching item components, searches names or @mod IDs, sorts by name/count and scrolls in 54-entry pages. Left/right click withdraws a full/half stack; shift-click withdraws to player inventory; shift-click player inventory deposits. Crafting has a shared persistent 3x3 grid, native recipe remainder handling, shift-click output and automatic refill from network storage. EMI recipe + requests a server-resolved recipe, shift-click + fills multiple ingredients. Missing materials leave a partial grid, never generate outputs. Clear returns ingredients to storage. The search and transfer packets are bounded and require a valid nearby open menu.

Crafting consumes one spring charge per operation (4096 maximum). Wind adds 256; an adjacent spinning Create kinetic block at >=16 RPM adds 32 per second. Item insertion/extraction does not spend spring charge. Hoppers access the connected inventory through block capabilities. This is local mechanical storage; the existing AE2 mod remains the later electrical storage system.

## Quests and verification

The stable-ID progression chapter now has 24 quests, including salt geology, mechanical battery minerals and sludge recovery. All existing quest IDs are preserved. Compile checks precede a combined native world test and isolated client UI run after implementation. See the QA report for actual results and limits.

## Drill mineral update

Mechanical Steel Drills now supply lead and antimony before electricity. Iron/Steel Mineral Sludge washes into quartz; Steel Gem Sludge washes into diamonds/lapis with emerald byproducts. Core-gated electric Crystalline Sludge centrifuges into starter Certus. Native quarry controller recipes are disabled. See [the full resource ladder](DRILL-MINERALS-AND-SLUDGE.md).
