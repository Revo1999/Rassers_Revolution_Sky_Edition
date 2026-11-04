# Setup status — 2026-10-31

- Portable Eclipse Temurin JDK 21.0.13+9 installed under the chat workspace's work/tools/jdk21 directory; checksum verified against Adoptium release metadata. The project's local configuration points to it. Keep that directory while using this setup. Existing system Java 17 was retained.
- VS Code Java Extension Pack and Gradle extension installed; project opened in VS Code.
- Gradle 9.2.1 wrapper, ModDevGradle 2.0.148 and NeoForge 21.1.265 pinned from the official 1.21.1 MDK; wrapper distribution checksum recorded.
- All six Revo entry-point projects compiled successfully and produced separate jars in build/mods.
- Core integration artifacts installed from Modrinth and SHA-512 verified; exact versions in pack/mods.lock.json. Bundled Create dependencies are discovered by NeoForge itself.
- Development client started and loaded all six Revo mods plus Create, MI, AE2, KubeJS, EMI, Jade and dependencies. Client resource reload, sound engine and texture initialization completed. No world creation, gameplay balancing or dedicated-server gameplay test was performed.
- Git initialized locally with the requested private repository as origin. The GitHub connector confirms the repository is empty. Local Git authentication needs the user's device sign-in through scripts/github-login.cmd. No commits or remote writes were made.
- Blockbench is installed. Its MCP tools are not exposed in this chat and no listener was detected on the documented default localhost:3000 endpoint. Asset folders and workflow instructions are prepared in docs/BLOCKBENCH.md.
- The full proposed mod roster is not an installed modpack. Skyblock setup, quests, optional addons, progression recipes and API integration dependencies remain implementation work.

References: https://docs.neoforged.net/docs/1.21.1/gettingstarted/
and https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle
