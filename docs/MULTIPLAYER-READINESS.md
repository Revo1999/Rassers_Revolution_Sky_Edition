# Multiplayer readiness

Use release `0.1.47-dev` on the server and on every client. This release changes
RevoStorage's terminal packet and menu data, so use the matching archives when
upgrading. Back up the world before changing versions.

| Mod | Multiplayer audit |
| --- | --- |
| RevoLib | Reviewed the shared networking, recipe sync and machine feedback. Machine menus now verify the original block entity, loaded chunk, player distance and dimension through a common check. |
| RevoExtraction | Reviewed drill and sluice menus, output hatches, and server-side program and side switching. Menus validate the active machine before allowing use. |
| RevoCultivation | Farm and small-machine menus bind to their original server-side inventory. Hatch access checks the connected controller, and merchant access checks the original colony. |
| RevoGenetics | Both Mob Harvesters register on the dedicated server. Their spawner and inventory interactions use the audited Cultivation machinery. |
| RevoIndustry | Reviewed the Nexus Builder, hatchery, saltern, converter, vacuum and primitive vessel menus. Their server access checks reject removed, replaced, unloaded or distant machines. |
| RevoStorage | The displayed grid has a revision per viewer. Withdrawals and deposits apply to the active terminal and the view the client clicked. Stale requests trigger resync. The GameTest uses two players and checks reordering, withdrawal, deposit and replacement of a viewed terminal. |
| RevoAchievements | Wardrobe grants and profiles stay on the server. Dedicated-server clients receive cosmetics through packets, and an integrated client leaves its server's catalogue in server control. |
| RevoUtil | Notes are stored once per world and identify the author. The status HUD subscribes per player. Two clients joined the same test world and notes recorded under both names persisted in the shared file. |

## Verification

- A packaged NeoForge dedicated server loaded all eight Revo mods, loaded
  17 chapters with 373 quests, generated the Revo starter island and saved cleanly.
- Two local Minecraft clients joined together, received the FTB Quests sync
  and exercised the shared note file. Both opened the same crafting
  terminal, withdrew separate items, and one returned the item through the
  grid. The isolated test server used loopback and local QA identities.
  The client test runner matched the production oωo setting because it used
  NeoForge's development launcher.
- Run the targeted storage regression with
  `dev.cmd :revostorage:runStorageTests -PrevoStorageTests` on Windows.
  Its local working directory is `.local/revo-storage-tests`. The GameTest
  needs an accepted Minecraft EULA there and its fixture is excluded from
  the release jars. It also covers large stacks above the grid's display cap.

A full co-op progression run, sustained large-factory load, public access
and automation across separate player claims remain outside this verification.
