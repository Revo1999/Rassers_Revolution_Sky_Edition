# Rassers Revolution dedicated server

This archive pairs with the client pack version in `install.json`. Install Java
21 and Python 3.11 or later on the host. Internet access is needed during setup.

1. Extract the archive into a **new directory**. Run `py -3 install.py` on
   Windows or `python3 install.py` on Linux. Use `--java` with the Java 21
   executable path for setup if Java 21 is outside your PATH. The launch
   scripts also need Java 21 on PATH. Setup checks the hashes of the
   downloaded mods and NeoForge installer before using them.
2. Check `server.properties` before generating the world. The supplied preset
   selects Skyblock Builder and enables the whitelist and online authentication.
   The installer preserves an existing `server.properties`.
3. Start in an interactive terminal with `run.bat nogui` on Windows or
   `bash run.sh nogui` on Linux. The first launch creates `eula.txt` and
   owo-lib prompts for an answer. Review the Minecraft EULA and enter
   `true` if you agree. For an unattended host, you can review the EULA
   and set `eula=true` in that file before launching. Configure server
   heap in the generated `user_jvm_args.txt` if needed.
4. Type `whitelist add YourMinecraftName` in the server console. Import the
   matching client pack on each playing PC, then join from Multiplayer.
   Computers on the same home network can use the host's local IP address.

RevoUtil records shared playtest notes in `<level-name>/revo-playtest/notes.md`
on the server. The default level directory is `world`.

Use `stop` in the console to shut down before copying the world. For upgrades,
back up the old server, prepare the new archive in a separate directory, and
copy the existing world and the whitelist across before starting. Carry over
any intentional server settings and review version-specific config changes.

[NeoForge server setup](https://docs.neoforged.net/user/docs/server/) ·
[Skyblock Builder server settings](https://wiki.chaotictrials.de/docs/1.21.x/wiki/skyblock-builder/admin/)
