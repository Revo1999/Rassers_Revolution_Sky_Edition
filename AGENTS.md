# Model workflow

Always use Blockbench MCP to author, edit and export models. If Blockbench MCP
is unavailable, stop model work and ask the user; do not use another modeling
tool or write replacement model geometry directly to runtime files.

Whenever using Blockbench MCP, show the user a rendered preview of the result
in the conversation. Machine additions must be checked against moving parts
through their full motion, not only against static body faces.

Keep editable `.bbmodel` sources and native exports with the asset. Scripts may
collect exports, adapt generated code to the project, validate assets and author
pixel textures. Geometry changes must go through Blockbench MCP.

For models listed in `art/blockbench/surface-repairs/manifest.json`, the listed
`.bbmodel` and sibling native export are the current production sources. Earlier
family assemblies are reference previews and can contain the old overlapping
surfaces. Edit the listed sources through MCP; do not overwrite their runtime
models with an older generator. Collect updated native exports with
`python scripts/collect_machine_surface_repairs.py --refresh`, then run
`python scripts/check_all_machine_models.py` and the runtime sluice clearance
check if moving sluice parts changed.
