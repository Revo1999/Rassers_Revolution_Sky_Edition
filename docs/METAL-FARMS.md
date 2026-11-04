# Metal farm upgrades — pack 0.1.26

Rebuild older Steel Tree Grower and Iron Greenhouse structures using the updated EMI guide. Wooden machines, the Steel Mob Harvester and single-block Composter/Silk Worm Cultivator keep their previous layouts.

| Machine | Outer size | Bed | Cycle | Requirements |
| --- | --- | --- | --- | --- |
| Steel Tree Grower | 5 x 5 x 1 | 3 x 3 | 20 s | Oak sapling, daylight, 500 mB water, 5 x 5 x 7 clear above |
| Steel Mob Harvester | 3 x 3 x 3 | Grate | 7.5 s | Selected Zombie/Skeleton/Spider; passive |
| Iron Electric Greenhouse | 5 x 5 x 3 | 3 x 3 | 10 s | Crop starter, 250 mB water, 8 EU/t |

Yield is unchanged: oak gives six logs, two leaves, two sticks and a sapling; crops give three produce plus existing seed output. Starters are retained.

## Construction

EMI derives layers, required counts, orientations and port highlights from the same blueprint used by server validation. Mixed wooden/metal frames do not form.

| Part | Steel Tree | Steel Harvester | Iron Greenhouse |
| --- | --- | --- | --- |
| Tier supports | 4 | 12 | 11 |
| Tier casings | 9 | 7 | 21 |
| Tier soil beds | 8 | 0 | 9 |
| Vanilla dirt | 1 | 0 | 0 |
| Tier grate | 0 | 1 | 0 |
| Tier barred panels | 0 | 3 | 0 |
| Tier species filter | 0 | 1 | 0 |
| Tier glass | 0 | 0 | 20 |
| Tier hatch | 1 | 1 | 1 |
| Controller | 1 | 1 | 1 |
| Tier water tank | 1 | 0 | 1 |
| Basic Power Converter | 0 | 0 | 1 |
| Iron Grow Light | 0 | 0 | 1 |
| Required air positions | 0 | 1 | 9 |

Local coordinates use the base centre, front at negative Z. Both wide farms put the controller at (0,0,-2) and tank at (-2,0,0). Tree hatch: (2,0,0). Greenhouse hatch: (2,0,1), immediately in front of converter (2,0,2). Hatches face right. Greenhouse middle layer has a 3 x 3 empty chamber; roof has eight interior glass around the central light.

The tree soil meets at one height inside a continuous steel ribbon. A visible collection duct connects bed and hatch; this is an item chute, not a kinetic port. Greenhouse steel borders frame seamless glass and support three top light rails above nine native crop models. Both attached tanks accept buckets/pipes into one persistent 16000 mB reservoir. Each tank recipe uses four matching plates, four glass and a bucket.

## Real oak growth

The Steel Tree Grower charges 500 mB when growth begins and saves the payment with progress. Halfway through the cycle it places six real vanilla oak logs and a persistent leaf canopy. Daylight is checked above the canopy so its own leaves do not stop it.

Owned positions survive reloads. Automatic harvest removes only recorded matching blocks, then gives normal output once. Manual harvesting/replacement cancels automatic output and clears the remaining owned tree. Unrelated blocks are never harvested. Dismantling or changing the starter clears owned growth. Keep the 5 x 5 x 7 growing volume clear.

## Electric greenhouse

The required Basic Power Converter remains a functional separate block. Forming hides its chunk model; the assembly supplies the fitted housing, rear FE terminal, hatch bus and roof conduit. Dismantling restores its standalone model. Wrench, interface, rates and modes remain.

Face FE rearward, opposite the controller facing; EU touches the adjacent hatch. Use FE to EU mode. Supply FE/Flux through the rear converter face, or native MI EU through the hatch. Converter remains required either way.

Consumption: 8 EU/t, or 80 FE/t at default 10 FE/EU. EMI uses the actual MI ratio. Each 200-tick harvest uses 1600 EU; the greenhouse buffers 1600 EU. Converter stores zero power and keeps its 32 EU/t limit. No kinetic power is accepted.

Water is consumed on completed harvests; energy only on productive ticks. Missing power/starter/water or full output pauses work and switches lights off. The centre roof light emits level 12. Three diffusers render full-bright while working. Artificial lighting permits night/indoor operation. Wheat/carrot/potato/beetroot native models show synchronized real growth stages across the full bed. Wider bed keeps established yield.

## UI, storage and Jade

Controllers show state, progress, water and greenhouse EU/load, with black R bottom-right and no inventory. Hatch screens have normal player inventory geometry, nine output slots and no R/footer. Water appears in UI/Jade and as animated biome-tinted vanilla water in the attached tank. Orientation corrections remain in controller diagnostics, not Jade. Cycle-end auto-ejection uses the hatch's outward face; all products must fit before output is granted.

The Steel Harvester keeps its persistent selected vanilla mob, without world AI, despawning or additional entity drops.

## Native modeling and texture contract

Sources, atlases, manifests, builders and front/rear renders live under `art/blockbench/metal-farms`. Refinements were built/painted/exported through desktop Blockbench MCP with native undo and codecs. Previous models are preserved in `history/before-connected-glass`.

Java 1.21.1 desktop, balanced cubes, albedo/cutout, 16 texels per world block. Iron is blue-grey; steel muted green/cast grey. Clustered dithering, small fasteners and wear are painted with proportional UV density. The refinement atlas is 512-square. Original Mojang textures/attribution remain in `vanilla/manifest.json`. Glass removes tile-border lines; FarmGlass also suppresses internal matching-block faces.

Wide footprints are 80 physical units, source geometry divided by five and restored by runtime scale five. Components use ordinary 16-unit blocks. Tree base height: 16; greenhouse height: 48. Harvester keeps 48-wide/divide-by-three. Front: negative Z. Wide controller cell: (32..48,0..16,0..16); tree hatch: (64..80,0..16,32..48); greenhouse hatch: (64..80,0..16,48..64); converter: (64..80,0..16,64..80).

Bed tops Y=16; ribbon Y=11..18. Greenhouse glass Y=16..43; roof Y=44..44.15; supported light rails Y=44.5..46.5. Diffusers are separated from glass to avoid coplanar flicker. Tree centre dirt is a real world block: its editor proxy is excluded from runtime body. Tree/crop/water preview groups are also excluded; the game supplies real world tree blocks, native age-based crops and live water. Current runtime cubes: greenhouse 107 including three active diffusers; tree base 62; each tank 12; connected glass one.

## Regeneration and verification

Generator order: first chapter, Create/steam, steel drill, metal farms, then `python scripts/refine_greenhouse_tree_content.py` LAST. The last step retains wide layouts, tanks, converter states and updated quest instructions without changing quest IDs. Native art uses `refine_greenhouse_tree.js`; preserve user edits before regeneration.

Checks: native renders, UV scale/preview exclusion, blueprint counts, converter states, compile/assemble, synced dev overrides and exact packaged files. No Minecraft or GameTests were launched; gameplay and shaders are left for owner playtesting.
