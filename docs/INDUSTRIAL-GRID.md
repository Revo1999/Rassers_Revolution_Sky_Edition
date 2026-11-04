# Industrial Power Grid

The pack retains Flux Networks 8.0.0, Create: New Age 1.2.0 and the existing
Building Wands 2.14. Analog Audio/radio and its embedded libraries are removed.
There are now 86 external mod jars.

The enabled revo-industrial-grid resource pack provides coordinated cool steel,
muted copper contacts, ceramic parts and teal indicators. There are no warning
stripes or heavy grime. Network-coloured LED masks still respect the chosen Flux
network colour; capacities and native Flux device behavior remain unchanged.
Flux Plug is named Power Transmitter, Flux Point is Power Receiver, Flux
Controller is Grid Controller, and the three storage tiers are Capacitor Banks.
The configurator is a Grid Multimeter. Search @fluxnetworks in EMI to find them.

Three direct electrical converter tiers now link MI EU and Flux FE through the
shared RevoLib compatibility code. See POWER-CONVERSION.md for limits, wrench
rotation, ports, the no-storage rule and the lighter verification scope.

Editable Flux texture sources, exports and prompts live in art/flux-industrial.
Old gritty textures and earlier radio integration documents are archived in its
history directory. Run scripts/build_flux_industrial.py --install-dev to stage
the texture pack while preserving other selected resource packs. Create: New Age
provides its own generators, motors, wiring, energisers and heat systems, with
Ponder documentation; no kinetic input is exposed by the Revo power converters.
