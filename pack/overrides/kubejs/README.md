KubeJS is pinned in `pack/mods.lock.json` for NeoForge 1.21.1.

**Revo's Creationalization** lives in `server_scripts/revo_creationalization.js`.
It adds same-cost MI Assembler alternatives for Create's Brass Hand and Whisk,
conditional on both mods being loaded. Existing recipes remain available.

Add a call to the small `assemble` helper to author another bridge, then `/reload`
in a development world. Other native recipe types can use `event.custom` with
their own JSON format. No additional Create scripting addon is required.

See [the proposal and progression rules](../../../docs/REVO-CREATIONALIZATION.md).
