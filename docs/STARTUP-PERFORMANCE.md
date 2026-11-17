# Startup performance and missing optimization mods

Initial audit: 15 November 2026 for Minecraft **1.21.1 / NeoForge 21.1.265**, using the original pinned 86-mod lock, local artifacts, pack overrides and official mod/version documentation. The inventory below is historical; the subsequent [full-pack benchmark](PERFORMANCE-BENCHMARK.md) records measured results and the current configuration decision. Lithium is the tested gameplay addition; ModernFix was already installed, and optional dynamic resources remain disabled. The original audit alone did not establish seconds saved.

## What the pack already has

ModernFix 5.27.27, FerriteCore 7.0.3, Sodium 0.6.13, ImmediatelyFast 1.6.15, EntityCulling 1.11.3, Clumps and Spark are already pinned. Avoid recommending them as missing or installing duplicate renderer replacements alongside Sodium/Iris. ModernFix's currently published matching release is already the pinned version.

The Revo block optimizations in [the performance audit](PERFORMANCE-AUDIT.md) primarily reduce in-world work, allocation and network traffic. They should not be represented as measured improvements to launching Minecraft.

## Best launch-time candidate: the existing ModernFix installation

ModernFix offers optional dynamic model loading: models are loaded on demand instead of all at startup. Its author documents launch/memory benefits and explicitly keeps it disabled by default because some custom model integrations can be incompatible. The proposed **isolated test setting**, not a production change, is:

```properties
# config/modernfix-mixins.properties — in an isolated benchmark instance
mixin.perf.dynamic_resources=true
```

This pack needs a visual/resource-reload check of MI machines and hatches, Revo multiblocks, Create, AE2, animated water, custom fonts and recipe viewers before adopting it. See the [official Dynamic Resources FAQ](https://github.com/embeddedt/ModernFix/wiki/Dynamic-Resources-FAQ) and [1.21.1 patch reference](https://github.com/embeddedt/ModernFix/wiki/1.21.1-Summary-of-Patches). Initial item/model display may move some work later; title-screen time alone is not the whole experience.

The local asset inventory makes model loading a sensible hypothesis: the 86 third-party JARs contain **19,287 model JSON entries** and **13,178 recipe JSON entries**. These are archive-entry counts, including templates/conditional definitions; they are not measured CPU costs or counts of loaded recipes/registered blocks.

| Third-party artifact | Model JSON entries |
|---|---:|
| BetterBlockZ | 3,528 |
| Create | 2,684 |
| Macaw's Roofs | 2,683 |
| Macaw's Windows | 1,887 |
| Macaw's Doors | 1,394 |
| Create Deco | 1,359 |

These are candidates for profiling, not a recommendation to remove the building palette. Revo's editable Blockbench sources and raw QA frame sequences are already excluded from runtime resources, so deleting those development assets would not speed up a packaged game launch.

## Optimization candidates from the original inventory

Exact matching builds were checked against official Modrinth metadata filtered to **NeoForge + 1.21.1**. Compatibility declarations establish candidates, not successful integration tests with this pack.

| Priority | Candidate / verified build | Expected scope | Recommendation |
|---|---|---|---|
| Tested gameplay addition | [Lithium 0.15.4](https://modrinth.com/mod/lithium/version/DDUrRVCA) | Game logic, entities and block ticking, rather than a primary launch-time improvement. | See the [benchmark and native regressions](PERFORMANCE-BENCHMARK.md) for the adoption decision and coverage limits. [Official description](https://modrinth.com/mod/lithium). |
| Medium | [FastSuite 6.0.7](https://modrinth.com/mod/fastsuite/version/OQZRQcDM) + Placebo | Recipe matching. The 1.21.1 release is **6.x**, which uses the concurrent-matching approach; the current website's newer 7.x indexing claims must not be applied to this build. | Worth profiling with this recipe-heavy pack; not evidence of faster title-screen startup. Adds Placebo, which is not currently pinned. [Author's explanation](https://modrinth.com/mod/fastsuite). |
| Medium/low | [BadOptimizations 2.4.1](https://modrinth.com/mod/badoptimizations/version/S2qthD5S) | Client micro-optimizations such as avoiding unnecessary lightmap work. | Test after the main bottlenecks; do not promise startup savings. [Official project](https://modrinth.com/mod/badoptimizations). |
| Optional visual test | [More Culling 1.0.11](https://modrinth.com/mod/moreculling/version/ucyxnpWe) | Additional rendering culling. Cloth Config is already present. | Potential FPS improvement, not a launch-time mod. Check compatibility with the pinned Sodium/Iris versions and all custom glass/multiblock views. [Official project](https://modrinth.com/mod/moreculling). |

Lower priority/excluded:

- **FastWorkbench 9.1.3:** matching NeoForge build exists and requires Placebo. Primarily conventional crafting behavior; Revo's own terminal should not be assumed to benefit automatically. Its targeted caching/aggregation has already been optimized directly.
- **FastFurnace 9.0.1:** matching published build is a beta. Vanilla-furnace changes are not automatically improvements to Revo's MI-backed furnace controller. Defer until profiling shows a relevant need.
- **Noisium 2.3.0:** a matching build exists, but world generation is a lower priority for the intended void-island world and is separate from reaching the title screen.
- **ThreadTweak:** the official project lists Fabric/Quilt; the exact NeoForge 1.21.1 metadata query returned no versions. Do not add its Fabric JAR to this pack. Its author also explains that its old launch-speed purpose no longer applies. [Official project](https://modrinth.com/mod/threadtweak).

## Configuration/content experiments worth measuring

1. **Recipe-viewer initialization:** JEI and EMI are both pinned, with a JEI crafting-station integration. Compare only in an isolated instance and verify all recipe categories/transfer integrations; simply removing one may remove functionality. The previously recorded Applied Create integration error should be resolved independently rather than hidden by deleting a viewer.
2. **Unused scripting startup:** the packaged KubeJS folder currently contains only its README, and current gameplay recipes live in Revo data resources. KubeJS/Rhino are candidates for a separate dependency-checked lean-profile experiment if scripting is not wanted yet. They were not removed.
3. **Video menu/native media:** FancyMenu, WaterMedia and native media binaries are present, and the pack deliberately ships an animated title background. Native media initialization could contribute to startup; this is an inference to profile, not a measured bottleneck. A static-poster comparison is useful only if the video menu is optional. The existing menu is retained.
4. **Shader compilation:** the pack enables Complementary Reimagined through Iris. Measure title-screen startup separately from world entry/shader compilation. Disabling shaders may improve world-entry time but changes the intended visuals; it is not a free equivalent optimization.
5. **Java/memory:** keep Java 21. `org.gradle.jvmargs=-Xmx3G` in this repository controls the build daemon, not a player's launcher memory setting. Do not treat changing it as a game-startup optimization or add unmeasured JVM flag bundles.

## How to establish actual savings

Use an isolated copy of the **complete packaged instance** on the same Java, heap, resource packs and shader setting. Do not include Gradle compilation, asset downloads or mod downloads in the game-launch timer. Do not compare small development GameTest/modular-client launches with the complete 86-mod client.

Measure these separately:

- Java process start -> usable title screen after the loading overlay is gone.
- Title screen -> playable fresh test island.
- First opening of inventory/recipe viewer and the first view of representative Revo/MI/Create models.
- Peak/settled memory and any new errors or missing models.

Warm up each configuration once, then perform at least three alternating baseline/candidate runs and compare medians. Test one change at a time: baseline, ModernFix dynamic resources, then any additional mod. Check a resource reload as well as initial launch. Only adopt a change after both timings and compatibility checks pass.

**Follow-up decision:** the [full-pack comparison](PERFORMANCE-BENCHMARK.md) found lower server tick cost with Lithium, but no reliable launch-time saving. Retain normal ModernFix and leave optional dynamic resources disabled. FastSuite remains a separately measured future candidate.

The local artifact inventory and exact matching version records are saved in [startup audit evidence](../art/startup/).
