## Current power and convenience update

The current lock has 86 external mod jars. Analog Audio and its embedded radio
libraries are removed. Building Wands, Flux Networks and Create: New Age remain.
See [POWER-CONVERSION.md](POWER-CONVERSION.md) for the three direct converter tiers.

# Installed mod roster

Source: `Rassers_Revolution_Sky_Edition_v2.md`, section 18. Target: Minecraft 1.21.1 / NeoForge 21.1.265.

Installed 84 external jar files, including dependencies, plus seven Revo modules. Embedded library mods may make the in-game count higher.

The third-party design roster is installed, with Simply Graves replacing Soul Gravestone at the owner's request, including the optional Sophisticated Backpacks and the performance candidates. MI Tweaks is selected for the design document's unnamed MI EU-to-FE compatibility role; its Flux Transformer provides a configurable bridge. Energy flow and charging still need gameplay validation.

The Revo modules now include the wooden drill, three first-tier farming/harvesting multiblocks, and RevoAchievements with quest-granted titles/hats and an independent wardrobe. RevoIndustry and RevoStorage still contain scaffolding; wider progression work remains for later. Shader and menu assets are preserved.

JEI is installed as an addon dependency alongside EMI. FTB jars come from FTB's official Maven; Iron Chests, Signboards, Noticeboard, Simply Light and Simply Graves come from CurseForge's CDN. Their pinned external manifest is `pack/mods.external.lock.json`.

Restore with `scripts/sync-mods.ps1`. Resolve Modrinth dependencies again with `scripts/sync-mods.ps1 -Resolve`; external pins are merged automatically. All downloaded artifacts are checked with SHA-512.

## Artifacts

| Project | Version | Source |
|---|---|---|
| advancedae | 1.6.12-1.21.1 | [Source](https://modrinth.com/mod/advancedae) |
| ae2 | 19.2.18 | [Source](https://modrinth.com/mod/ae2) |
| ae2helpers | 1.0.1 | [Source](https://modrinth.com/mod/ae2helpers) |
| appleskin | 3.0.9+mc1.21 | [Source](https://modrinth.com/mod/appleskin) |
| applied-create | 1.1.8 | [Source](https://modrinth.com/mod/applied-create) |
| applied-energistics-2-wireless-terminals | 19.5.1 | [Source](https://modrinth.com/mod/applied-energistics-2-wireless-terminals) |
| architectury-api | 13.0.11+neoforge | [Source](https://modrinth.com/mod/architectury-api) |
| athena-ctm | 4.0.6 | [Source](https://modrinth.com/mod/athena-ctm) |
| balm | 21.0.66+neoforge-1.21.1 | [Source](https://modrinth.com/mod/balm) |
| better-advanced-tooltips | 2101.1.0-build.5 | [Source](https://modrinth.com/mod/better-advanced-tooltips) |
| betterblockz | 0.2.10-1.21.1-neoforge | [Source](https://modrinth.com/mod/betterblockz) |
| building-wands | 2.14_release_neoforge_1.21 | [Source](https://modrinth.com/mod/building-wands) |
| cloth-config | 15.0.140+neoforge | [Source](https://modrinth.com/mod/cloth-config) |
| clumps | 19.0.0.1 | [Source](https://modrinth.com/mod/clumps) |
| controlling | 19.0.5 | [Source](https://modrinth.com/mod/controlling) |
| crafting-station-jei-edition-updated | 2.2.1 | [Source](https://modrinth.com/mod/crafting-station-jei-edition-updated) |
| crafting-tweaks | 21.1.11+neoforge-1.21.1 | [Source](https://modrinth.com/mod/crafting-tweaks) |
| create | 6.0.10+mc1.21.1 | [Source](https://modrinth.com/mod/create) |
| create-deco | 2.1.3 | [Source](https://modrinth.com/mod/create-deco) |
| create-jetpack | 5.2.1 | [Source](https://modrinth.com/mod/create-jetpack) |
| createaddition | neoforge-1.21.1-1.6.0 | [Source](https://modrinth.com/mod/createaddition) |
| cucumber | 8.0.16 | [Source](https://modrinth.com/mod/cucumber) |
| defaultworldtype | 1.21-5.0.4 | [Source](https://modrinth.com/mod/defaultworldtype) |
| emi | 1.1.24+1.21.1+neoforge | [Source](https://modrinth.com/mod/emi) |
| emotecraft | 2.4.12+1.21.1-forge | [Source](https://modrinth.com/mod/emotecraft) |
| entityculling | 1.11.3 | [Source](https://modrinth.com/mod/entityculling) |
| extended-ae | 1.21-2.2.35-neoforge | [Source](https://modrinth.com/mod/extended-ae) |
| fancymenu | 3.9.14-1.21.1-neoforge | [Source](https://modrinth.com/mod/fancymenu) |
| ferrite-core | 7.0.3-neoforge | [Source](https://modrinth.com/mod/ferrite-core) |
| ftb-chunks | 2101.1.23 | [Source](https://maven.ftb.dev/releases/dev/ftb/mods/) |
| ftb-library | 2101.1.39 | [Source](https://maven.ftb.dev/releases/dev/ftb/mods/) |
| ftb-quests | 2101.1.37 | [Source](https://maven.ftb.dev/releases/dev/ftb/mods/) |
| ftb-teams | 2101.1.11 | [Source](https://maven.ftb.dev/releases/dev/ftb/mods/) |
| ftb-ultimine | 2101.1.15 | [Source](https://maven.ftb.dev/releases/dev/ftb/mods/) |
| geckolib | 4.9.3 | [Source](https://modrinth.com/mod/geckolib) |
| glodium | 1.21-2.2-neoforge | [Source](https://modrinth.com/mod/glodium) |
| guideme | 21.1.22 | [Source](https://modrinth.com/mod/guideme) |
| immediatelyfast | 1.6.15+1.21.1-neoforge | [Source](https://modrinth.com/mod/immediatelyfast) |
| iris | 1.8.12+1.21.1-neoforge | [Source](https://modrinth.com/mod/iris) |
| iron-chests | 16.0.7 | [Source](https://www.curseforge.com/minecraft/mc-mods/iron-chests/files/5491156) |
| iron-furnaces | 4.3.2 | [Source](https://modrinth.com/mod/iron-furnaces) |
| iron-jetpacks | 8.0.11 | [Source](https://modrinth.com/mod/iron-jetpacks) |
| jade | 15.10.6+neoforge | [Source](https://modrinth.com/mod/jade) |
| jade-addons-forge | 6.1.2+neoforge | [Source](https://modrinth.com/mod/jade-addons-forge) |
| jei | 19.64.0.455 | [Source](https://modrinth.com/mod/jei) |
| konkrete | 1.9.9-1.21-neoforge | [Source](https://modrinth.com/mod/konkrete) |
| kotlin-for-forge | 5.12.0 | [Source](https://modrinth.com/mod/kotlin-for-forge) |
| kubejs | 2101.7.2-build.377 | [Source](https://modrinth.com/mod/kubejs) |
| libx | 1.21.1-6.0.9 | [Source](https://modrinth.com/mod/libx) |
| macaws-doors | 1.1.5 | [Source](https://modrinth.com/mod/macaws-doors) |
| macaws-fences-and-walls | 1.2.1 | [Source](https://modrinth.com/mod/macaws-fences-and-walls) |
| macaws-roofs | 2.3.2 | [Source](https://modrinth.com/mod/macaws-roofs) |
| macaws-stairs | 1.0.2 | [Source](https://modrinth.com/mod/macaws-stairs) |
| macaws-windows | 2.4.2 | [Source](https://modrinth.com/mod/macaws-windows) |
| mega | 4.11.0 | [Source](https://modrinth.com/mod/mega) |
| melody | 1.0.10-1.21-neoforge | [Source](https://modrinth.com/mod/melody) |
| mezzconfig | 0.6.10 | [Source](https://modrinth.com/mod/mezzconfig) |
| mi-tweaks | 1.9.6-1.21.1 | [Source](https://modrinth.com/mod/mi-tweaks) |
| modern-industrialization | 2.5.11 | [Source](https://modrinth.com/mod/modern-industrialization) |
| modernfix | 5.27.27+mc1.21.1 | [Source](https://modrinth.com/mod/modernfix) |
| mouse-tweaks | 1.21-2.26.1-neoforge | [Source](https://modrinth.com/mod/mouse-tweaks) |
| noticeboard | 1.1.0 | [Source](https://www.curseforge.com/minecraft/mc-mods/noticeboard/files/9295781) |
| owo-lib | 0.12.15.6-beta.1+1.21 | [Source](https://modrinth.com/mod/owo-lib) |
| playername-styler | 2.0.3+mc1.21.1 | [Source](https://modrinth.com/mod/playername-styler) |
| polymorph | 1.1.0+1.21.1 | [Source](https://modrinth.com/mod/polymorph) |
| rhino | 2101.2.7-build.85+Rhino-1.21 | [Source](https://modrinth.com/mod/rhino) |
| searchables | 1.0.2 | [Source](https://modrinth.com/mod/searchables) |
| signboards | 1.0.0 | [Source](https://www.curseforge.com/minecraft/mc-mods/signboards/files/9223720) |
| simple-hats | 1.21.1-Neo-0.4.1 | [Source](https://modrinth.com/mod/simple-hats) |
| simply-graves | 1.1.4+mc1.21-b23 | [Source](https://www.curseforge.com/minecraft/mc-mods/simply-graves/files/5552933) |
| simply-light | 1.5.3+1.21.1-b4 | [Source](https://www.curseforge.com/minecraft/mc-mods/simply-light/files/6219029) |
| skyblock-builder | 21.1.37 | [Source](https://modrinth.com/mod/skyblock-builder) |
| sodium | mc1.21.1-0.6.13-neoforge | [Source](https://modrinth.com/mod/sodium) |
| sophisticated-backpacks | 1.21.1-3.26.18.2235 | [Source](https://modrinth.com/mod/sophisticated-backpacks) |
| sophisticated-core | 1.21.1-1.5.21.2439 | [Source](https://modrinth.com/mod/sophisticated-core) |
| spark | 1.10.124-neoforge-1.21.1 | [Source](https://modrinth.com/mod/spark) |
| storagedrawers | 1.21.1-13.11.4 | [Source](https://modrinth.com/mod/storagedrawers) |
| supermartijn642s-config-lib | 1.1.8-neoforge-mc1.21 | [Source](https://modrinth.com/mod/supermartijn642s-config-lib) |
| supermartijn642s-core-lib | 1.1.26-neoforge-mc1.21 | [Source](https://modrinth.com/mod/supermartijn642s-core-lib) |
| tesseract-api | 1.12.19-1.21.1 | [Source](https://modrinth.com/mod/tesseract-api) |
| trash-cans | 1.1.2-neoforge-mc1.21 | [Source](https://modrinth.com/mod/trash-cans) |
| trashslot | 21.1.11+neoforge-1.21.1 | [Source](https://modrinth.com/mod/trashslot) |
| watermedia | 3.0.0.23 | [Source](https://modrinth.com/mod/watermedia) |
| watermedia-binaries | 3.0.0.6 | [Source](https://modrinth.com/mod/watermedia-binaries) |

## Simply mods update

Simply Light 1.5.3+1.21.1-b4 and Simply Graves 1.1.4+mc1.21-b23 are pinned from their official CurseForge files for NeoForge 1.21.1. Soul Gravestone was removed from the requested roster, active lockfile and run/mods so only Simply Graves handles deaths. Its former jar is retained under `.local/retired-mods`; the cached artifact and existing configuration are also preserved. Existing Soul Gravestone graves are not automatically converted to Simply Graves.

## Building and decoration additions

Macaw's Fences and Walls, Doors, Windows, Roofs and Stairs, Create Deco, and BetterBlockZ are included in the NeoForge 1.21.1 pack. Their exact official Modrinth releases and SHA-512 hashes are pinned in the requested roster and active lockfile. Create Deco requires Create 6.0.7–6.0.x and is compatible with the existing 6.0.10 pin. No existing dependency version was changed for these additions. Athena 4.0.6 is included to enable BetterBlockZ connected textures; no other libraries were needed.
