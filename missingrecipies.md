# Missing machine-block recipes

Audited 166 registered Revo blocks against the native recipe manager and packaged recipes. See [machine-recipe-audit.json](docs/machine-recipe-audit.json) for evidence.

**0 Nexus block recipes are missing.** The [Nexus material ledger](docs/NEXUS-MATERIAL-LEDGER.md) tracks the full survival route, including the retained Construction Program.

Fluid blocks have buckets and processing routes. World-only crops use their seed items. Dust uses the manual sand hammering interaction. Nexus hatches use the native MI recipes.

Refresh with `dev.cmd :revoindustry:runNexusTests -PrevoNexusTests` followed by `python scripts/audit_machine_recipes.py`.
