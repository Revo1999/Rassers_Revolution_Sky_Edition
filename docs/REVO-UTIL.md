# RevoUtil playtest commands

| Command | In game |
| --- | --- |
| `/log "The Steel Drill output hatch is hard to see"` | Saves a note and replies with its number. Quotes are optional. |
| `/revonotes` | Shows the last five notes in chat. Hover over a note to see who wrote it, when and where. |
| `/revonotes 10` | Shows up to ten recent notes. The command accepts 1–20. |
| `/revostatus` | Toggles the upper-right HUD with the pack version, FPS and average server tick time in milliseconds. The HUD starts hidden when you join. |
| `/revohelp` | Shows the commands and the current pack version. |

Open `saves/<world>/revo-playtest/notes.md` inside the launcher instance for
single-player notes. For a dedicated server, the same file is inside its world
folder. Every entry includes a stable number, a UTC timestamp, player name,
dimension and block coordinates. Anyone playing in that world can log and
review its notes. Share the Markdown file when the playtest runs on a different
computer.

`pack/version.txt` supplies the version to both the Modrinth package and the
HUD. Bump it before building a new version.
