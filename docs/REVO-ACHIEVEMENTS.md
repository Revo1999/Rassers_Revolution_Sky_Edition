# RevoAchievements and Revo Wardrobe

Minecraft 1.21.1 / NeoForge 21.1.265. RevoAchievements is the seventh local Revo
module. It connects FTB quest rewards to permanent personal cosmetic unlocks and
provides an independent screen for selecting one title and one hat.

## Player controls

- **K** opens Revo Wardrobe; the key is remappable under Controls → Revo Cosmetics.
- The **R button beside survival or creative inventory** opens the same screen.
- `/revoachievements wardrobe` is available without operator permission.
- Switch between **Titles** and **Hats**, search, browse pages, and toggle All/Owned.
- Click a reward to preview it, then **Equip** to wear it. **Clear** removes only
  that category's selection. Locked rewards can be previewed but cannot be equipped.
- The screen shows what is locked, unlocked and equipped, plus the unlock hint.

Titles appear through PlayerName Styler in chat, the tab list and nameplates. The
pack's formats include `%revo_title%` while retaining nickname and LuckPerms
prefix/suffix fields. Hats use the installed items' actual head models. They are
cosmetic render layers, so they grant no stats and do not occupy, replace or alter
armor slots. Physical Simple Hats helmets take visual priority to avoid overlapping
two hats. The wardrobe preview uses a separate client-only player, never changes
real inventory, and does not commit a selection until Equip is confirmed by the server.

Unlocks and selected equipment survive death, respawn, dimension changes, logout
and world restarts. Nothing automatically equips when awarded. Repeating a reward
does not duplicate items or unlock records. Clearing a selection retains ownership.
Earned hats are not physical item rewards and cannot be extracted or scrapped.

## Included content

The default catalogue has **9 titles and 8 hats**. The small **Revo Employee
Handbook** FTB chapter demonstrates the actual integration:

| Quest | Task | Title | Hat |
| --- | --- | --- | --- |
| Clock in at Revo | Checkmark | Unpaid Factory Intern | Off-duty Cap |
| First Wooden Drill | Obtain controller | Certified Crank Operator | Miner Helmet |
| A Greener Workplace | Obtain controller | Senior Dirt Inspector | Flower Crown |
| Contained Curiosity | Obtain controller | Director of Questionable Science | Scientist Hat |
| Branch Office | Obtain controller | Deforestation Supervisor | Propeller Hat |

The four machine tasks branch from the welcome quest. They detect controllers,
do not consume them and do not claim that a formed machine was detected. Both
cosmetic rewards auto-claim separately for each player after quest completion.
The remaining catalogue entries are ready for future progression quests or the
administrator's own reward assignments; no full technology quest overhaul is implied.

Simple Hats 0.4.1 contains miner-hat and backwards-cap model assets without
registering corresponding items in its default list. RevoAchievements registers
`revoachievements:miner_helmet` and `revoachievements:off_duty_cap` when Simple Hats
is installed. Their item models reference those existing parent models. No Simple
Hats art is copied or redistributed, and its configuration is preserved. Other
defaults reference the real registered Simple Hats items. None has a crafting recipe
or is given to inventory by cosmetic rewards.

## Add rewards in FTB's editor

Add either **Revo Title Unlock** (`revoachievements:title`) or **Revo Hat Unlock**
(`revoachievements:hat`) to a quest. Set **Cosmetic ID** to a catalogue ID from
`/revoachievements list`, for example:

```snbt
{
    id: "5E71000000000001"
    type: "revoachievements:title"
    cosmetic: "revoachievements:title/certified_crank_operator"
    team_reward: false
    auto: "enabled"
}
```

For a hat, use `type: "revoachievements:hat"` and an ID such as
`revoachievements:hat/minerhat`. A hat item registry ID and a cosmetic catalogue ID
are different concepts. Use the cosmetic ID in the reward configuration.

The reward classes implement FTB's actual `Reward` persistence, network and config
interfaces. Ordinary FTB readiness and claim accounting still apply. A missing ID
or wrong cosmetic kind returns failure rather than silently consuming the reward.
Awarding an already-owned valid cosmetic succeeds harmlessly, allowing repeatable
quests without giving duplicate physical items.

Rewards default to personal ownership. With **Team reward = true**, one claim
unlocks the cosmetic separately for all current FTB team members, including offline
members, and for the claimant. Team membership is captured at claim time. Later
team joiners do not inherit old cosmetic claims automatically. These unlocks remain
personal if a player later leaves the team. Shared quest progress and equipped
cosmetics are separate: every player chooses their own loadout.

## Add or change catalogue definitions

Use a datapack file at
`data/<namespace>/revo_cosmetics/<path>.json`. Its resource path is the cosmetic ID.
For example, `data/my_pack/revo_cosmetics/title/shift_supervisor.json` defines
`my_pack:title/shift_supervisor`:

```json
{
  "kind": "title",
  "name": "Shift Supervisor",
  "description": "Responsible for absolutely everything after five.",
  "unlock_hint": "Complete the late-shift quest.",
  "item": "minecraft:name_tag",
  "color": "#D6AD5A",
  "order": 90
}
```

For hats use `kind: "hat"` and a registered item with the intended HEAD model,
for example `simplehats:tophat`. Optional `requires_mod` skips a definition cleanly
when its asset-providing mod is absent. Title text is limited to 64 characters,
descriptions/hints to 256, IDs to normal resource locations, and the catalogue to
512 entries. Invalid entries are logged and skipped. `/reload` refreshes clients.

Removing a definition unequips that selection while keeping its earned ID in saved
history. If the same definition returns later, the player still owns it. Changing
an ID creates a different cosmetic, so keep IDs stable across pack updates.

The default generator is `scripts/generate_revo_cosmetics.py`. It regenerates
default definitions, this starter chapter and the pack's name-format configuration;
preserve author edits before rerunning it. External API code is pinned to the same
versions as the pack and compiled from official FTB/Modrinth Maven artifacts.

## Commands

```text
/revoachievements wardrobe
/revoachievements list
/revoachievements equip <cosmetic_id>
/revoachievements clear title
/revoachievements clear hat
/revoachievements unlock <players> <cosmetic_id>
/revoachievements revoke <players> <cosmetic_id>
```

Only unlock/revoke require operator permission level 2. Equip requests always
validate the current server catalogue and the player's ownership. The client cannot
grant rewards or select another player's loadout. Unlock/revoke target online
players via Minecraft selectors; FTB's team reward implementation also handles
offline members by UUID. Revoke clears that cosmetic if currently selected.

## Storage, synchronization and integrations

Authoritative data is in the overworld's `data/revoachievements_wardrobe.dat`, keyed
by player UUID, with an independent unlocked set and title/hat selections. It is not
stored on the player entity or in an inventory slot. Back it up with the world.

Private catalogue/unlock/loadout snapshots go only to the owner. Other clients
receive visible title text/color and the hat item for each online player. Login,
respawn, dimension changes, equip/revoke and datapack reload refresh state. Client
caches clear on disconnect. Requests have bounded IDs and a short server-side
cooldown. The server accepts only open/equip/clear actions from the wardrobe packet.

FTB Quests, Simple Hats and PlayerName Styler are optional integrations. The
installed pack includes all three. Without PlayerName Styler, vanilla name/tab
events provide titles. Without FTB, operator commands or other server code can grant
catalogue entries. Simple Hats-backed definitions are omitted when its assets are
unavailable. Third-party code/assets are not bundled in the Revo jar.

## Verification

```powershell
.\dev.cmd -PrevoAchievementsTests :revoachievements:runAchievementTests
.\dev.cmd -PrevoAchievementsUiQa :revolib:runClient
.\dev.cmd -PrevoAchievementsUiQa -PrevoAchievementsReloadQa :revolib:runClient
.\dev.cmd build collectMods
python scripts/package_modpack.py
python scripts/verify_revo_achievements.py
```

Server tests cover UUID isolation, idempotent/no-auto-equip unlocks, locked/unknown
ID rejection, independent slots, revocation, save/reload, definition removal and
restoration, offline awards, operator permissions and private/public packet data.
They also exercise the base module without the optional integrations installed.

Full-pack visual QA uses only a copied `Revo Wardrobe QA` save. It completes real
FTB tasks and claims, clicks real wardrobe buttons through server packets, checks
locked previews, retains real inventory/armor, verifies PlayerName Styler output,
captures the real quest book and world hat renderer, saves the loadout and starts
the client/world again to verify persistence. Screenshots/results are under
`art/revoachievements/ingame`. A normal build must follow optional QA to remove its
classes from the desktop development classpath. QA classes/templates are also
explicitly excluded from shipping jars.

API references: [FTB reward types](https://github.com/FTBTeam/FTB-Quests/blob/1.21.1/main/common/src/main/java/dev/ftb/mods/ftbquests/quest/reward/RewardTypes.java),
[Simple Hats](https://github.com/fonnymunkey/SimpleHats/tree/MultiLoader-1.21.1),
[PlayerName Styler placeholders](https://github.com/programmer1o1/playername-styler/tree/main/src/main/java/sierra/thing/playernamestyler/api).
