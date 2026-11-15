# Full-pack performance benchmark

15–16 November 2026. This benchmark compares the original pack with Lithium 0.15.4, then with Lithium plus ModernFix dynamic resources. It uses the complete client, shaders and seven Revo modules. Measured results and the configuration decision are recorded below.

## Fixed setup

- Windows 11, AMD Ryzen AI 7 350 (8 cores / 16 threads), integrated Radeon 860M, approximately 31.3 GiB usable system RAM.
- Java 21; **10 GiB maximum heap**, 2 GiB initial heap in every client run.
- 1280×720, render distance 8, simulation distance 6, VSync disabled, 120 FPS cap.
- The pack's Complementary Reimagined shader and resource packs remain enabled.
- A dedicated creative save, **Revo Performance Benchmark**, under the Git-ignored `.local/performance-benchmark/instance`. Player saves are never opened or modified.
- The initial warm-up runs are excluded. Repeated configurations alternate to reduce cache/order/temperature bias. Startup is measured from the game JVM's uptime, excluding Gradle compilation and downloads.

This is the complete pinned mod set in the development client with a test-only measurement helper. `titleReadyMs` ends when the loading overlay disappears and the title screen is present; the video menu can still initialize asynchronously. `worldEntryMs` also includes a deliberate three-second title-screen delay, so it is a diagnostic rather than a clean world-load benchmark. No cold-disk-cache comparison is claimed.

## Saved workload

The platform contains 32 electric drills, 12 Steel Harvesters, eight greenhouses, four real-tree growers, eight drives with nine diamond chests each (**7,776 slots**), a crafting terminal, 128 vanilla hoppers, 64 cows, two salt-geode chambers and native MI centrifuges, and a real Wither hatchery. Native creative MI sources power the machines; these are intentionally creative benchmarking fixtures.

Each run rebuilds the same scene and clears reset-generated loose items before measuring. Production checks confirm harvester cycles, salt cycles and at least one Nether Star. The reset originally exposed legitimate drops from dismantled filled machines; that contaminated trial is excluded rather than compared with the clean workload.

The scene warms up for **600 server ticks**, then samples **1,200 server ticks**. Server tick duration, achieved tick rate, frame intervals, heap usage and resource-reload time are recorded separately. Ten additional Revo models are checked before/after a resource reload, with factory and geode screenshots for visual inspection.

Tick times below 50 ms generally mean the server can maintain 20 TPS; lower tick time adds capacity rather than raising the game's tick rate above 20. FPS results reflect this integrated-GPU/shader scene, not a guarantee for every player's hardware or factory. Two runs per configuration are indicative, not a statistical confidence interval. Heap-used snapshots are not a controlled memory-footprint benchmark.

The automated model checks cover ten Revo special models, with visual inspection of the rendered factory, water, glass and crystal chamber before/after reload. This does not exhaustively test every mod's GUI, Create contraption or AE2 network. Existing EMI synthetic-Create-recipe warnings also occur in the baseline and are not hidden by these checks. Runs interrupted by computer suspension are excluded.

## Running or opening the benchmark

The exported `Revo-Performance-Benchmark.zip` contains a normal Minecraft world folder. Extract **Revo Performance Benchmark** into an instance's `saves` directory and open it with the matching pack. Creative mode and commands are enabled. If another player profile starts on the original island, use `/tp 0 170 24` to reach the platform.

The saved snapshot uses finite water/input/output buffers; it is not a perpetual factory or a survival progression save. The test harness resets inputs and the scene before every automated comparison. The dev-only measurement helper is excluded from the distributable Revo JARs.

Reproduce the automated comparison from the checkout:

```powershell
.local/venv/Scripts/python.exe scripts/run_performance_benchmark.py --runs warmup-new baseline-new-1 lithium-new-1 both-new-1 baseline-new-2 both-new-2 lithium-new-2
.local/venv/Scripts/python.exe scripts/export_benchmark_save.py
```

Use fresh run labels. `both` means Lithium plus dynamic model loading; `baseline` means the original optimization stack; `lithium` changes only Lithium. The setup script downloads the exact candidate and verifies its published SHA-512. Local logs, screenshots, per-run JSON and the save remain under `.local/performance-benchmark/`; selected shareable results are copied to `art/performance-benchmark`.

## Measured results and configuration decision

Median of two runs per configuration (FPS uses the median of each run's average):

| Configuration | Title screen (s) | Mean server tick (ms) | Tick p95 (ms) | Average FPS | Resource reload (s) |
|---|---:|---:|---:|---:|---:|
| Original optimization stack | 48.75 | 4.77 | 6.60 | 93.4 | 10.86 |
| + Lithium 0.15.4 | 54.72 | 4.32 | 5.92 | 96.3 | 10.77 |
| + Lithium + dynamic resources | 47.86 | 4.64 | 6.28 | 104.7 | 4.84 |

All six runs maintained approximately **20 TPS**. Lithium reduced the median run-average server tick cost by **9.5%**, from 4.77 to 4.32 ms. This is extra headroom in this fixture; it does not establish the maximum factory size or multiplayer performance.

**Lithium 0.15.4 is now pinned** after the native regressions passed. **Keep the existing ModernFix 5.27.27 and leave optional dynamic resources disabled.** Dynamic resources cut reload time, but their roughly 0.9-second title-screen difference from baseline is smaller than the 5.8-second spread between baseline runs. They also recorded a 1.53-second warm-up frame in one run (123 ms in the other). This alone does not prove causation, but gives insufficient reason to enable the optional tradeoff by default.

Lithium-only title timings were 59.76 and 49.68 seconds, versus baseline 45.87 and 51.64 seconds. These tests establish no launch-time saving from Lithium; the two-run median was slower. Average FPS varied considerably (baseline 85.8–100.9, Lithium 88.6–104.1), so no reliable FPS percentage is claimed. No change is advertised as making both startup and gameplay faster.

All runs checked the same compiled measurement probe, zero reset-generated loose items, actual harvester/salt/Nether Star production and ten Revo models before/after reload. The original helper exited before EMI's asynchronous reload finished, producing shutdown-only EMI/JEI errors in baseline and candidate logs. The helper now waits for EMI readiness; the additional final validation is reported separately and is excluded from the A/B table. The timing interval itself is unchanged.

Raw selected measurements are in [comparison.json](../art/performance-benchmark/comparison.json).

## Final client validation

The corrected helper completed a further Lithium-only run with `recipeReloadReady=true`, all production/model checks passing, a clean game exit and **zero EMI shutdown-race errors**. Its full result is in [final-validation.json](../art/performance-benchmark/final-validation.json). It recorded 64.14 seconds to title, 4.22 ms average server tick, 20 TPS and 74.2 average FPS. This extra run is excluded from the fixed six-run comparison because the helper's shutdown code changed; its slower client timings further underline that no reliable startup or FPS improvement is established. Only the measured server tick reduction was consistent.

## Download the save

[Revo Performance Benchmark ZIP](../art/performance-benchmark/Revo-Performance-Benchmark.zip) is also exported to the Desktop. The [save manifest](../art/performance-benchmark/save-manifest.json) records its size and SHA-256. The archive was exported after Minecraft closed, verified for ZIP integrity, and contains the world folder without its session lock.

## Native regression reproduction

All **85 native tests passed with Lithium**: 29 progression/optimization tests, 20 cultivation tests, 29 industry tests and seven drill-mineral tests. Machine/port/storage behavior, production, structure integrity and recipe/progression routes are covered by these existing suites. See [native-regressions.json](../art/performance-benchmark/native-regressions.json) for suite counts and process results.

After preparing the benchmark once (which downloads and verifies the pinned Lithium candidate), run these separately from the client benchmark:

```powershell
.\dev.cmd :revoextraction:runProgressionTests -PrevoProgressionTests -PrevoOptimizationTests -PrevoLithiumTest
.\dev.cmd :revocultivation:runCultivationPerformanceTests -PrevoCultivationPerformanceTests -PrevoLithiumTest
.\dev.cmd :revoindustry:runIndustryPerformanceTests -PrevoIndustryPerformanceTests -PrevoLithiumTest
.\dev.cmd :revoextraction:runMineralTests -PrevoMineralTests -PrevoLithiumTest
.\dev.cmd build collectMods
.local/venv/Scripts/python.exe scripts/package_modpack.py
```

The candidate runtime hook and benchmark Java sources are opt-in development features. Packaging rejects benchmark classes and the existing QA fixtures in distributable JARs.

The normal production build and all **10 unit tests** passed. Pack `0.1.40-dev-performance-tested` contains 87 pinned third-party artifacts, the shader and seven production Revo JARs. ZIP integrity, pinned hashes, the Lithium entry, disabled dynamic-resources setting and absence of benchmark code are checked during packaging/final verification.
