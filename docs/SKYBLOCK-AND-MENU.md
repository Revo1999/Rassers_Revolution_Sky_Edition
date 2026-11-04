# Skyblock spawn and Vanilla+ menu

New worlds default to **Skyblock**, through Default World Type and Skyblock Builder.
The Revo Starting Island is the sole template. Existing worlds are not converted.
Its 323 blocks form a small tapered grass/dirt/stone island, with one oak tree,
a Crafting Station, a torch and a little vegetation. The safe spawn is
9.5, 69, 10.5, facing north, above grass with clear headroom.

The only starting item is the Revolution Book, which opens FTB Quests.
There is no starter chest or free water/lava. Chapter 1 teaches the renewable
crook, compost, hammer, rain barrel and clay crucible route to the wooden drill.
See `docs/FIRST-CHAPTER.md` for recipes, timing and the first-rain quest reward.
Chapters 2 and 3 continue through Create water power, pressed iron and the new
Iron Drill into MI bronze and steam. See `docs/CREATE-IRON-STEAM.md`.

The overworld has plains biomes, void terrain, and no generated structures/features.
Spawn protection is off so players can build on the shared initial island.
Skyblock Builder's team commands remain available for additional islands; integration
with FTB Teams is not implemented yet. Island spacing is 1,024 blocks.

## Files

- `pack/overrides/config/skyblockbuilder`: world/template configuration and island NBT.
- `pack/overrides/config/defaultworldtype/client-config.toml`: default world preset.
- `pack/overrides/config/fancymenu`: native FancyMenu layout and local video assets.
- `pack/mods.requested.json` and `pack/mods.lock.json`: pinned mods and dependencies.
- `pack/shader.lock.json`: pinned Complementary shader download and checksum.

Restore the pinned client mods and shader with `scripts/sync-mods.ps1`.
Then launch `dev.cmd :revolib:runClient`. Pack overrides are copied on launch.
Iris shader selection is preconfigured; users can disable shaders in Video Settings.
The menu film is prerecorded, so its shader appearance does not require runtime
shader rendering on the title screen. Watermedia V3 and its binaries play the MP4;
the still poster provides a background if video playback is unavailable.

An importable `Rassers-Revolution-Sky-Edition-0.1.26-dev.mrpack` is generated beside
the repository. Import it with a launcher that supports Modrinth packs. It downloads
the pinned third-party mods/shader from their original Modrinth URLs, and includes
the seven local Revo jars, config, font and film. RevoAchievements adds permanent
FTB quest rewards for titles and hats, with the independent Revo Wardrobe opened
using K, the inventory R button or `/revoachievements wardrobe`. The starter
Employee Handbook chapter demonstrates five cosmetic milestone rewards.
The wooden first-tier Tree Grower,
Greenhouse and Mob Harvester include crafting, production, hatch interfaces, Jade
and interactive EMI construction guides; see `docs/FIRST-TIER-MULTIBLOCKS.md` in
the source project. The Harvester contains the selected native Minecraft mob,
saved with the machine and rendered using its normal species-specific renderer.
The wooden drill retains Create power support. This is a development pack,
not the finished progression pack. Java 21 and a licensed Minecraft account are needed
for ordinary launcher play. The development client remains available without importing.

FancyMenu provides the title-screen background and the owner's supplied transparent
Rassers Revolution logo. The original Minecraft logo and yellow splash are hidden.
Button labels, widget positions, Realms and footer retain vanilla/NeoForge behavior.
Create's main-menu config button is disabled through its supported
`client.mainMenuConfigButtonRow = 0` setting. The in-game Create config button is unchanged.
The supplied logo is preserved unchanged in `art/branding/revo_logo.png` and copied
to FancyMenu's local assets. It is centered above the vanilla button stack.
There are no custom text elements. The background is
a centered, character-free 20-second orbit without the old sidebar gradient. Shader
wind and torch animation are recorded against elapsed time and played at 1x speed;
view bobbing is disabled during recording. FancyMenu's authoring bar is enabled for
the pack owner; Ctrl+Alt+C toggles it. Modpack mode is off for editing.

FancyMenu also sets the window title to `Rassers Revolution: Sky Edition` and uses
the supplied stone R as its window icon. Its supported Windows/Linux inputs use
16px and 32px PNGs; macOS uses the multi-resolution ICNS. The icon assets also include
transparent PNGs at 16, 20, 24, 32, 40, 48, 64, 96, 128, 256, 512 and 1024 pixels,
plus a multi-size Windows ICO for launcher shortcuts. Larger assets are supplied
for external launcher use; FancyMenu itself exposes only the two PNG slots and ICNS.
Run `scripts/build_window_icons.py` before `scripts/build_menu.py` to rebuild them.

## Rebuild

Install the Python requirements in `scripts/world-requirements.txt`, plus font
requirements if rebuilding the font. Run `scripts/build_starting_island.py` and
`scripts/build_menu.py` to regenerate the configuration.

The optional cinematic source directory is compiled only with `-PrevoCapture=true`.
It is development tooling, excluded from normal mod builds. Launch with that flag,
enter the isolated Revo Island QA world, and allow the camera to finish its orbit.
Set `.local/menu-frames/ffmpeg-path.txt` to the FFmpeg executable's absolute path.
The recorder streams frames directly to FFmpeg at a wall-clock 30 fps, producing
600 frames over 20 seconds. Elapsed time and repeated-frame statistics are written
to `complete.txt`; then run `scripts/encode_menu_video.py` to package the film/poster.
The player is temporarily a spectator, hiding the character and shadow, and is restored
after recording. Camera yaw has no lateral offset. Recording changes time/weather/mob
spawning only inside the QA world. Do not use capture mode in a real survival save.

Run `scripts/package_modpack.py` after a normal `dev.cmd build collectMods` to
regenerate the importable pack. Dedicated-server operation and death/respawn have
not been play-tested; a dedicated server needs `level-type=skyblockbuilder:skyblock`
in server.properties. The integrated single-player spawn and menu were tested.

Sources: https://wiki.chaotictrials.de/docs/1.21.x/wiki/skyblock-builder/
and https://docs.fancymenu.net/docs/en-US/video
