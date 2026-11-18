# Nexus survival material ledger

Generated from the production recipe JSON by `scripts/audit_nexus_progression.py`. Includes all three production multiblocks, one portal and the builder/program. Native MI component manufacture, fluids, power and factory infrastructure remain additional.

| Gross material | Proposed target | Actual |
| --- | ---: | ---: |
| nexus alloy ingot | 6,144 | 6,101 |
| phase lattice | 1,536 | 1,536 |
| astral essence | 3,072 | 3,072 |
| nether stars | 6,144 | 6,144 |
| mi singularities | 6 | 6 |

The alloy batch requirement is **6,104 ingots**, including three spare. Lattices and essence embedded inside the Core, ring, beacons, contacts and controller are included once. Six singularities retain the native MI requirement of **384 nukes**, plus the native ultradense-ball and nuclear costs.

## Automation chain

1. Mixer -> EBF with UU Matter -> Vacuum Freezer -> cooled Nexus Alloy.
2. Pressure Casting Plant with the selected physical mould, nitrogen and Cryofluid -> rough casting + returned mould + warm coolant. Native MI Compressor finishes the casting.
3. MI Chemical Reactor prepares cleaning solution and copper/sulfuric electrolyte. Electroplating Line performs clean, plate and rinse as three saved native recipes. Return each output intermediate to its input hatch; use MI slot/fluid locks for the dedicated baths and waste outlets.
4. Compressor and Polarizer prepare different targets. Collider accepts both targets, Astral Essence, reusable containment cells, native fusion Helium Plasma and Cryofluid. Unbalanced injections wait in their native saved hatch inventories. No same-tick timing is required.
5. MI Centrifuge opens the stabilized exotic-matter cells and returns their empty containment cells. The MI Assembler combines the recovered matter with rinsed substrates into Phase Lattices.
6. Six Ignition Cells and finished cast/plated components assemble the Core. The server-side MI program recipe requires that completed Core with consumption probability zero: the Core remains available for the portal. A blank program cannot operate the builder.
7. Build with individual supplied blocks, then power the full portal at 1,024 EU/t. The server awards the final stabilization advancement; the final quest uses that advancement.

## Waste recovery

Spent electrolyte centrifuging returns one copper dust per 1,000 mB, at most25% of the copper originally used to make it. Wastewater treatment returns 750 mB water per 1,000 mB. Warm coolant freezing returns 750 mB Cryofluid per 1,000 mB. These are lossy loops; fresh feed is always required.

## Renewability and gates

Platinum drilling is Nuclear Survey Core-gated at 512 EU/t. Native MI sulfuric solution/centrifuging supplies iridium; no quarry is required. A dedicated core-gated Deep Debris program supplies one Ancient Debris per 1,200 ticks, replacing the impractical 64-result wait for bulk endgame work. Existing program indices 0–10 are unchanged. Monazite is also available as a late drill program. Gems remain sludge products.

Astral Essence compacts nine mob-drop streams separately. Each Essence costs two Nether Stars and eight each of rotten flesh, bones, string, leather, slimeballs, blaze rods, pearls, ghast tears and magma cream. At 3,072 essence this is 24,576 of each drop. Biological Catalysts additionally consume compost, silk cocoons, paper, rubber, crop nutrients and charcoal/cotton reinforcement. These costs connect cultivation, trees, harvester programs and Wither processing to MI chemistry.

## Builder controls

Place the builder at the centre of the bottom layer. Place the MI input hatch left of it, output hatch right of it, and power hatch immediately behind it. Right-click the controller for the materials interface. Click a slot with matching items to deposit or with an empty cursor to withdraw up to 64. Shift-click inventory stacks to deposit. Counts include already placed blocks while stored quantities remain available for withdrawal. The Box toggle shows the build volume. Clear the entire volume apart from the builder and its hatches before starting. Charge 100,000,000 EU, insert the retained program and fill the bill before pressing Build. Construction places 810 supplied blocks around its four existing blocks.

Use native MI recipe locking from EMI when ingredients match multiple Assembler recipes. A higher-tier energy hatch charges the builder faster. At a sustained 8,192 EU/t, the precharge takes about ten minutes.

## Required native MI processing

Mixer, Electric Blast Furnace, Vacuum Freezer, Compressor, Assembler, Chemical Reactor, Macerator, Wiremill, Polarizer, Centrifuge, Electrolyzer, nuclear processing, fusion, UU Matter, superconductors and quantum circuits all have explicit inputs in this graph. Optional MI machines, quarrying, Revo storage and cosmetics sit outside this recipe chain.

The current Wither recipe takes 75 seconds per star. 6,144 stars require 128 h in one chamber or 8 h across 16 supplied chambers. This is a calculated throughput estimate, not a complete survival playthrough.
