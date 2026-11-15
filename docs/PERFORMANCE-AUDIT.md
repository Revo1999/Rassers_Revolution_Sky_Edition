# Revo block performance audit

15 November 2026. Reviewed the block entities, ports and custom renderers across all seven Revo modules after the drill/sludge update. Changes target repeated work and allocation; recipes, production rates, energy costs and structure-break checks retain their existing behavior.

## Implemented improvements

| System | Change |
|---|---|
| Mechanical drills | Cache the fixed resource pools instead of constructing arrays and resolving every optional item for each output check. Coordinate transforms now construct one position rather than chaining intermediate positions. Item hatches cache their live controller and recheck removal, loaded chunk, identity, formation and port position on each use. |
| Electric drill | Reuse the metal program table instead of constructing a new table per output/tick. Existing program IDs, buffer limits and blocked-output behavior remain intact. |
| Farms, greenhouses and harvesters | Reuse product definitions; cache the atomic multi-product capacity result until outputs/program change. Reapply formation flags only when a validated part actually needs them. Stop periodic idle packets and dirty marking when nothing has changed. Inventory/tank mutations still trigger updates, and working animations keep periodic synchronization. |
| Farm item/fluid ports | Cache the controller reference with live identity, loaded-chunk, removal, formation and exact port-position checks. This avoids repeated broad controller searches during hopper/pipe operations. |
| Composters and silkworm nurseries | Reuse the output template for capacity tests, preserving component-sensitive comparisons. Stop idle progress packets; inventory changes and completion still synchronize. |
| Storage drives | Replace one address object per slot with one span per installed chest. Nine diamond chests use **9 spans for 972 slots**, rather than 972 address objects. Sequential reads reuse the last span; random access uses binary search. Topology checks still invalidate the map. |
| Storage terminal | Aggregate stacks with component-sensitive hash keys instead of a growing linear search. Reuse results for repeated requests within the same tick, invalidating on content/topology/search/sort changes. Stop marking the crafting grid dirty just because an unchanged menu broadcasts. Cross-thread revision counters are atomic. |
| Integrated power converters | Cache the live host and stop searching the full 9×9 area every tick when the host remains valid. Conversion remains direct, sided and rate-limited. |
| Multiblock furnaces | Use Minecraft's cached smelting lookup. Validate in-progress recipes against their current recipe-holder identity, repeating full ingredient/output checks after reload/replacement rather than assembling a comparison result every working tick. |
| Wither hatchery | Iterate a prefiltered list of eight lamp/controller/roof positions for light updates, rather than searching all 98 shell cells. Full structure and real-Wither safety checks remain immediate. |
| Salt geode | Reuse the immutable 27-cell blueprint; avoid repeatedly creating cells, positions and lists during capability validation. Stop unchanged idle dirty marking. |
| Vacuums | Avoid an unnecessary copied stack during internal simulated acceptance checks, reuse the intake position per scan, remove redundant cooldown-decrement dirty marks and reuse tier tables. Collection radius, cadence, nearest-first ordering and per-scan limit remain unchanged. |
| Primitive vessels | Stop unconditional idle heartbeat packets/dirty marks. Inventory/fluid changes and active work still synchronize. |
| Client water rendering | Eliminate temporary point and UV arrays for every fluid face in farm/drill water rendering. Vertex coordinates, winding, UVs, tint, light and transparency remain the same. Reuse water bounds, crop grids and model identifiers; actual baked models are still obtained from the current model manager after resource reload. |

## Reviewed without a speculative rewrite

- Trading Colony: its bounded inventory scan remains immediate and preserves existing trade/persistence behavior.
- Managed real trees: retain obstruction, ownership and integrity checks rather than harvesting unverified world blocks.
- Contained mobs/Withers: retain existing entity ownership and lifecycle rules; contained ordinary mobs already avoid roaming AI ticks.
- Create kinetic drive: delegates to Create's kinetic system; stress and speed behavior remain unchanged.
- Passive frames, casing, glass, decorative blocks and non-ticking ports do not gain tickers.
- RevoGenetics currently shares the cultivation machinery rather than adding a separate ticking block subsystem. RevoAchievements does not introduce a production block ticker. RevoLib supplies shared energy/rendering infrastructure.

## Verification

Native tests cover storage span boundaries and same-tick topology changes, component-sensitive aggregation and cache invalidation, full-output recovery, removed-host capability guards, all six farm tiers in all four rotations, ordinary progression/storage/charging, early vessels, farms/lighting, furnaces, vacuums, Wither containment and salt processing.

Commands:

```powershell
.\dev.cmd :revoextraction:runProgressionTests -PrevoProgressionTests -PrevoOptimizationTests
.\dev.cmd :revocultivation:runCultivationPerformanceTests -PrevoCultivationPerformanceTests
.\dev.cmd :revoindustry:runIndustryPerformanceTests -PrevoIndustryPerformanceTests
.\dev.cmd build collectMods
```

Combined test runs isolate batches that manipulate global world time. Legacy first-tier farm fixtures are scoped to their three original tiers; a new full-width fixture verifies every current tier and rotation. The first powered greenhouse fixture explicitly establishes clear weather because rain intentionally makes idle LEDs consume energy.

See [verification results](../art/performance/validation.json). These checks establish behavior preservation and the stated reductions in repeated work; they are not a measured large-factory TPS/FPS percentage. Large multiplayer factories still need representative profiling. Safety-critical structure and inventory checks were not replaced with delayed polling.
