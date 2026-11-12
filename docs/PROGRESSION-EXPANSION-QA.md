# Progression expansion QA - 12 November 2026

## Results

- Full native regression suite: **16 / 16 required GameTests passed** on NeoForge 21.1.265, Minecraft 1.21.1, pinned Create, MI, Iron Chests, Iron Furnaces and Flux Networks.
- Full installed modpack client: **passed** in a separate copied save named `Revo Progression Expansion QA`. No user survival save was used for the fixtures.
- Actual EMI handler initiated a crafting recipe transfer over the client/server payload. Actual client shift-click consumed 16 stored oak logs, created 64 planks and reduced spring charge from 256 to 240. The visible search box filtered the network to diamonds.
- Chest integration: nine installed native diamond chests expose **972 slots**, equal to **62,208 items at 64 per slot**. Vanilla chests expose 27 each. Nonempty bay removal, simulated extraction, actual extraction and save/load passed.
- A powered native Create motor recharged the terminal spring, and the registered hopper item capability inserted into the connected drive. Actual terminal crafting returned the water bucket.
- Crop/mob programs, species guards, Blaze credit persistence, bucket remainders, native recipe loading, electric power consumption, deep rare output, full-output backpressure, trading and the first powered greenhouse harvest passed.
- The greenhouse milestone produced three cane using 1600 EU and 250 mB, retaining its starter; filling output storage paused further water and energy use.
- Static verification passed for all quest/task/reward IDs and dependencies, the 18 new quests, source/runtime model UV density, textures, loot and recipes. Existing first-chapter bootstrap validation also passed.

## Fixes found during verification

Vanilla chest items carried an empty container component, which the initial portable-content guard rejected. The guard now permits empty container components and preserves the refusal of filled portable chests. The first test-player fixture triggered an unrelated Flux Networks login-packet guard; a NeoForge fake player replaced that fixture. Visual review removed drive-only control overlap and made the terminal search field render explicitly above the background.

## Reproduction

```powershell
.\dev.cmd :revoextraction:runProgressionTests -PrevoProgressionTests --no-watch-fs --offline
python scripts/verify_progression_expansion.py
python scripts/validate_first_chapter.py
.\dev.cmd :revolib:runClient -PrevoExpansionUiQa --no-watch-fs --offline
```

The client QA command requires the isolated QA save and runs destructive fixtures only inside that specifically named copy. Its screenshots and result note are in [art/progression-expansion/ingame](../art/progression-expansion/ingame). The QA classes are excluded from production jars.

## Limits and unrelated warnings

These are automated world tests plus a real client interaction pass, not a full fresh-island survival playthrough or a benchmark of hundreds of storage blocks. The first-chapter reachability check and recipe audit complement the powered endpoint test; later progression gaps are listed separately. Existing modpack warnings include untranslated EMI tags and Applied Create's missing AE2 JEI integration class. No new Revo recipe/model loading errors were found in the client log.
