# Quiet machine audio and directional furnace hatches

The approved sound plan is implemented with **26 continuous work loops and 19 short cues**.
The furnace family now has motor/turbine hums rather than a repeating fire-noise burst.
Wooden, iron, steel and electric drills have distinct work sounds. Organic machines
use sparse rustles rather than constant chatter.

[Audition the sounds](machine-sound-preview.html). The preview never autoplays, starts at
22% volume, and pauses other samples when one starts. Actual game playback additionally
uses distance falloff, fades and a shared machine-room volume budget.

| Blocks | Sound treatment |
| --- | --- |
| Copper / iron furnaces | Low 60 Hz hum, gentle combustion texture |
| Silver / gold / diamond / emerald furnaces | Smoother 80 Hz turbine hum |
| Obsidian / netherite furnaces | Deeper 45 Hz motor and muffled combustion |
| Crystal furnace | Clean electrical hum with a restrained resonant layer |
| Rainbow furnace | Layered turbine hum, kept at the same restrained level |
| Furnace vent | Shares the main furnace's combustion/exhaust bed; no duplicate loop |
| Item hatches, all grades | Rate-limited transfer clunk and opening latch |
| Solid / liquid / gas fuel hatches, all grades | Fuel-feed rattle / valve click during real transfers |
| Wooden / iron / steel / electric drills | Wood chatter / gear grind / heavy motor / electric motor |
| Drill drive blocks | Share their controller's sound; interaction latch |
| Drill output hatches and coolant tank | Transfer clunks and filling sounds |
| Sluice / gravel feeder | Water wash and shaker rhythm / actual gravel-feed rattle |
| Wooden tree grower | Irrigation, branch snap at harvest |
| Steel tree grower | Pump/motor with a short saw harvest |
| Wooden / iron greenhouses | Irrigation / circulation fan and water pump |
| Kelp / salt geode programs | Submerged bubbling / quiet crystal-bath circulation and harvest tick |
| Wooden / steel harvesters | Wooden mechanism / low motor and collection stroke |
| Composter / silkworm cultivator | Sparse settling / leaf rustle; short collection cue |
| Trading colony | Sparse handling sounds during work |
| Rain barrel / crucible | Water collection / bubbling; pouring feedback |
| Power converters, three tiers | Progressively deeper electrical hum during real energy forwarding |
| Vacuums, three tiers | Distinct, restrained fan/suction tones while pulling items |
| Storage terminal / chest drive | Charging/winding/crafting / rate-limited transfer and latch |
| Grow lights / mob filters | Switching / selector clicks only |
| Casings, supports, beds, grates, panels, dust and unfired crucible | Existing material placement/break/step sounds; no independent working loops |

The old farm harvest block-break event has been replaced with silent particles plus
the quiet harvest cue. This removes the full-volume block-breaking noise from every
crop/harvester cycle. Random furnace crackles no longer stack over the furnace loop.

## Volume and lifecycle

- All sounds respect Minecraft's **Blocks** and **Master** volume controls.
- `config/revolib-client.toml`: `machineVolume = 0.65`, adjustable from 0 to 1;
  `maximumMachineLoops = 8`, adjustable from 1 to 16.
- Nearby loops share an equal-power mix; distant loops attenuate over 12 blocks.
- Loops fade in over about 0.5 seconds and fade out after their work heartbeat expires.
  The stop lease plus fade is at most approximately 1.65 seconds after the last heartbeat.
- Chunk removal, changed worlds, resource reloads and muted sound categories cannot
  leave a permanently attached loop. Loops can restart when work becomes audible again.
- Transfers are silent during simulation. Committed transfers are rate-limited, with
  an additional client-wide short-cue limit, so hopper banks cannot click every tick.
- One main loop per controller; casing/hatch/vent blocks do not multiply that loop.

All audio is original procedural foley. `scripts/build_machine_sounds.py` creates the
Vorbis files and validates decoded peaks/RMS, registered voice coverage and loop seams.
`machine-audio-validation.json` records the measurements. These checks constrain level
and playback behavior; the preview is provided for subjective listening preferences.

## Directional furnace hatches

All six item input/output hatches and nine solid/liquid/gas fuel hatches now show their
port on **one selected face**. The remaining faces use the matched furnace casing.
Hoppers and pipes connect only through that marked face. Native MI inventory locks,
automatic item import/export, casing links and menus remain in use.

**Sneak-use an MI wrench on the desired face to select the port.** All six directions
are supported. The chosen direction is saved. Existing setups may need their port
pointed toward the attached hopper or pipe after updating. Furnace shape membership
does not require rebuilding simply to rotate the hatch port.

## Verification

- Eight native furnace GameTests pass, including all 15 hatch variants, every port
  direction, forbidden-face rejection, model direction and saved orientation.
- Eighteen native progression GameTests pass after the audio/transfer changes.
- The real Minecraft client plays both a working furnace and a Create-driven drill.
- A synthetic twelve-source load alongside real machinery respects the eight-loop cap.
- Expired work, blocked outputs, restarting work and block removal pass sound-engine checks.
- All 45 sound events resolve in the client; all 45 Vorbis files decode and pass level checks.

Client evidence: [`screenshots/audio-check/results.txt`](../screenshots/audio-check/results.txt)
and [`directional-hatches.png`](../screenshots/audio-check/directional-hatches.png).

Reproduce the isolated client check with a copied `Revo Audio QA` save:

```powershell
.\dev.cmd :revoindustry:runFurnaceTests -PrevoFurnaceTests --offline --no-watch-fs
.\dev.cmd :revoextraction:runProgressionTests -PrevoProgressionTests --offline --no-watch-fs
.\dev.cmd :revolib:runClient -PrevoAudioQa --offline --no-watch-fs
.\dev.cmd build collectMods --offline --no-watch-fs
```

The fixture only modifies its bounded area in that named copied save, restores the
pause preference and closes the client. It is excluded from production jars. Run
the client separately from builds/tests, because they share compiled resource outputs.
