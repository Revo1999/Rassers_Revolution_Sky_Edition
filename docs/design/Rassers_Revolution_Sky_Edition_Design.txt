# Rassers Revolution: Sky Edition

**Game Design & Modpack Vision Document**  
**Status:** Conceptual design specification  
**Target environment:** Minecraft 1.21.1, NeoForge  
**Core technologies:** Create, Modern Industrialization (MI), Applied Energistics 2 (AE2)  
**Tagline:** *From Nothing, Build Everything.*
**Roster revision:** Includes Iron Furnaces, Iron Chests, Trash Cans, early Crafting Stations, additional QoL and an expanded late-game AE2 addon suite.  
**Visual direction:** Vanilla+ overall; all custom Revo machine interfaces follow Modern Industrialization’s GUI style.

> **Document scope.** This document describes what the pack should feel like, contain, and allow players to do. It records the agreed direction, distinguishes proposed balancing and open design choices, and deliberately avoids code architecture, implementation instructions, sprint plans, or development tasks. Named recipes, resource tiers and figures are design intentions rather than claims about existing mod behavior.

---

## 1. Executive vision

**Rassers Revolution: Sky Edition** is a long-form, expert-style industrial skyblock pack built around a deliberate technological journey: from hand-cranked wooden machines and tiny resource yields to mechanized factories, steam, electricity, precision manufacturing, computerized logistics, and finally the **Genesis Core**.

The player begins on a minimal floating island with no conventional mining world to explore. There is no required Nether or End journey. Instead, the player builds the means of producing every necessary category of matter: minerals from the void, plants from controlled cultivation, and creature-derived materials from hatched and maintained populations. Conventional Ex Nihilo-style sieving is not the central mechanic. Resources arise from machines with visible purpose, operating costs, upgrades, and growing levels of control.

**Create** establishes the first industrial language: hand cranks, water wheels, shafts, gears, belts, fans, mills, presses, and physical assembly. **Modern Industrialization** adds deeper metallurgy, chemical processing, steam and electrical technology, industrial machines and circuits. Create machinery must not simply disappear once MI is unlocked: later electrical infrastructure can power Create through adapted existing electric-motor solutions, while physical Create processes continue to be used in selected manufacturing chains. **AE2** is intentionally gated deep into MI's electronics progression, first as storage and later as full autocrafting.

The pack's ultimate victory condition is a **single, extremely expensive final craft: the Genesis Core**. It requires meaningful progression through the whole industrial technology tree and through every core custom resource system. The goal is a difficult, rewarding industrial undertaking, not an arbitrary timer, a final machine commissioning sequence, or a mandatory sustained-throughput test.

The world is meant to be enjoyable for a solo player or a small team. Shared quests, personal and team achievement cosmetics, goofy job titles, and lightweight social mods add personality without changing the industrial balance.

### Pillars

1. **A single self-sufficient skyblock world.** All progression-critical resources are obtainable without visiting the Nether, End, or other dimensions.
2. **Earn automation, then benefit from it.** Manual effort introduces a mechanic; it should never remain the primary way to scale production.
3. **Physical first, digital later.** Create's kinetic logistics and spring-powered storage precede electronic motors and AE2.
4. **Progression through access, then control, then efficiency.** A basic machine produces mixed resources; upgrades unlock categories, exact targets and automated demand-responsive operation.
5. **Every branch matters.** Extraction, agriculture, genetics, storage, Create and MI each retain significance and contribute to the Genesis Core.
6. **Factory difficulty instead of repetitive busywork.** The hardest tasks require interconnected production, new technology and scaling, not endless hand-crafting.
7. **Play together, be ridiculous.** Cosmetic hats and absurd workplace titles celebrate progress; collaboration is supported without becoming compulsory.

### Intended audience and duration

The intended experience is for players who like ambitious Create factories, MI/GregTech-inspired manufacturing chains, expert recipe progression and long-term automation. A provisional target is **roughly 150–250 hours for an experienced solo player**, with actual duration to be judged by testing rather than treated as a fixed promise. Teams can split responsibilities and may progress faster. Solo completion must remain possible.

---

## 2. Identity, presentation and tone

### Title and naming

- **Modpack:** Rassers Revolution: Sky Edition
- **Custom mod family:** Revo
- **Custom mods:** RevoLib, RevoIndustry, RevoExtraction, RevoCultivation, RevoGenetics, RevoStorage
- **Ultimate craft:** Genesis Core
- **Tagline:** *From Nothing, Build Everything.*

The title should be recognizable in the main menu, quest book, pack icon and in-game tooltips. The Revo prefix creates a coherent family of custom systems without requiring players to learn six unrelated naming conventions.

### Overall visual direction: Vanilla+

**The modpack should look and feel like Minecraft, with restrained customization—not like a separate game launcher or a futuristic industrial dashboard.** The overall presentation is explicitly **Vanilla+**: recognizable Minecraft pixel art, familiar buttons and menus, modest branding, clear information and very little decorative UI. The pack should be pleasant and coherent without calling attention to its interface design.

The world and machine *models* can still visibly progress through industrial eras without imposing an elaborate style on menus:

- **Primitive:** Wood, stone, iron fittings and practical hand-operated machinery.
- **Mechanical Create era:** Brass, copper, gears, exposed shafts, belts and flywheels.
- **Steam and heavy industry:** Boilers, steel, pipes and reinforced machinery.
- **Electrical MI era:** Functional metal casings, wiring and electrical equipment.
- **Advanced technology:** More capable industrial machinery that still belongs naturally in a Minecraft factory.

The intention is a **cohesive mix of vanilla Minecraft, Create and Modern Industrialization textures**, not photorealism, elaborate industrial skins or flashy effects. Shaders may improve the world lighting, but are optional and should not define the visual identity.

### Revo machine interfaces: follow Modern Industrialization

**Modern Industrialization's existing machine GUIs are the primary design reference for every custom Revo machine.** RevoExtraction, RevoCultivation, RevoGenetics, RevoStorage and RevoIndustry should feel as if their machines belong beside MI machines, rather than introducing an unrelated GUI theme.

The shared interface expectations are:

- **Familiar Minecraft/MI layout:** Standard inventory slots, item/fluid input and output areas, unobtrusive progress indicators, energy or kinetic status and clear upgrade/program slots where applicable.
- **MI-like textures and density:** Modest grey industrial panels, clean pixel-art details, thin separators and practical spacing instead of oversized cards, gradients, glossy controls or stylized dashboards.
- **Clear machine status:** Readable indicators for active/idle operation, recipe or program, progress, speed or stored spring energy, and reasons a machine is stopped.
- **Consistency across Revo mods:** The Void Excavator, greenhouse, tree farm, composter, genetics habitats and Mechanical Workstation share visual conventions, even when their gameplay requires different controls.
- **Progressive complexity without a visual overhaul:** Early GUIs remain minimal; category percentages, precision settings and inventory-aware programming appear as additional ordinary MI-style controls when unlocked.
- **Minecraft usability first:** Inventory interactions, tooltips, recipe-viewer compatibility and scaling should feel familiar. No unnecessary animation or visual noise.

A workstation may have an extra spring-tension meter, a habitat a population display, and an excavator an instruction selector; these are **functional extensions of the MI-style interface**, not excuses for a separate art direction. RevoStorage's search-and-craft screen can be larger because it handles more items, but should still use standard slot-based Minecraft conventions.

### Main menu, font and branding

The pack retains a **custom main menu**, but the customization should be **subtle Vanilla+**. Keep the conventional Singleplayer, Multiplayer, Options and Quit experience recognizable. Use the title *Rassers Revolution: Sky Edition*, a modest logo and perhaps a simple skyblock or industrial background. Avoid cinematic montages, prominent industrial HUD overlays, large animated machinery, futuristic navigation or complicated custom menu layouts.

**FancyMenu** remains an option for this restrained menu treatment; it is not a reason to make the menu elaborate. A vanilla-style menu with subtle branding is preferable to a technically impressive but visually intrusive one.

The previously requested **custom font** remains part of the identity, but it should preserve the proportions, readability and pixel-compatible character of Minecraft's interface. It may be used sparingly for the title and branding, or broadly only if it still feels Vanilla+. Do not impose a highly stylized technical or modern sans-serif font across machine GUIs if doing so would clash with Modern Industrialization. Keep sensible glyph fallbacks and legibility at ordinary GUI scales.

Quests, titles, achievement hats, icons and inventory overlays should follow the same understated direction. Humor belongs primarily in quest wording, cosmetic rewards and player titles—not in distracting UI decoration.

**Iris + Sodium** and **Complementary Reimagined** remain the recommended optional shader stack. The pack should look good without shaders and remain fully playable on a dedicated server regardless of client graphics choices.

---

## 3. The world and the skyblock premise

### One playable world

The experience centers on one skyblock **Overworld**. The player is not expected to enter the Nether, End or other dimensions, and no mandatory recipe, quest, mob drop, plant, ore or boss reward may depend on visiting them. If third-party content normally expects a portal, structure, meteorite or worldgen feature, the pack must provide an intentional equivalent through its machines or recipes.

The skyblock is not divided into geological zones, special mining biomes or coordinates that determine ore availability. **Location does not gate mineral types.** Access is governed by technology: drill heads, instruction programs, machine upgrades and processing capability. The same principle applies to agricultural and genetic resources.

The starting island is deliberately small. It establishes survival essentials and a route toward the first hand-powered extractor and manual organic processing. The initial conditions must support a complete, non-circular route to stone, wood, basic metals, Create equipment and the first sustained automation. The exact starter inventory is not defined yet.

### Skyblock world content

- No conventional underground mining progression.
- No reliance on natural structure loot or chest loot for required technology.
- No necessity for AE2 meteorites or found inscriber presses.
- No mandatory boss fights for resources required by the Genesis Core.
- Conventional mob farms, farming and construction may remain possible as optional Minecraft play, but all progression-critical biological drops receive machine-based alternatives.
- Nether and End blocks and materials remain legitimate resources; they are **manufactured or extracted in the Overworld**.
- Renewable water, lava and other foundational fluids need deliberate routes.
- Animals, villagers, bees and rare creature resources need explicit acquisition or production paths if any recipe uses them.

The aim is not to remove Minecraft's character. It is to make the *industrial creation of resources* the primary adventure.

---

## 4. The industrial progression

The ages below describe the intended player experience, not rigid quest chapters with hard-coded timers. Players may develop several branches concurrently where their technology allows it.

### Age 0 — From Nothing: primitive survival

The player has almost no industrial infrastructure. Wood, compostable organic matter and a primitive extraction method enable the first stone, gravel and eventually trace minerals. The earliest composting path creates renewable dirt. The first steps should be comprehensible with minimal UI and very few specialized items.

**Experience:** A short, tangible bootstrap phase that shows the player how matter can be made in the void. Manual cranking is novel rather than a long-term grind.

**Milestones:** First renewable dirt; first stone and mineral material; first shafts, cogwheels and hand-powered tools.

### Age 1 — Mechanical Beginnings

The player uses Create kinetic power to replace manual cranking. Water wheels and early mechanical networks supply the Void Excavator, tree production and the first useful processing facilities. **RevoStorage** introduces the spring-powered Mechanical Workstation: a central inventory/crafting station that must be wound to function until a shaft is connected to continuous power. Early RevoGenetics habitats begin to provide basic animal and hostile-mob resources.

**Experience:** Progression from an isolated set of hand devices to one connected small factory.

**Milestones:** Continuous rotation; automatic basic extraction; a functional Workstation; elementary crop, tree or mob production.

**Early convenience:** An ordinary **Crafting Station** provides persistent manual 3×3 crafting and convenient access to nearby inventories before the player earns RevoStorage. Early **Iron Chests** act as expanded physical containers, not a substitute for the powered, searchable Workstation. Neither needs an advanced power network or unlocks remote autocrafting.

### Age 2 — The Create Revolution

The player builds real production lines: crushing, washing, pressing, mixing, belts, filters and transport. Brass and more precise mechanical components improve the extractor and its programming. Basic category targeting makes it possible to bias production toward stone, metals or other broad groups. Agriculture and mob cultivation become more useful and less manual; the Workstation gains storage and crafting upgrades.

**Experience:** Players solve layout, logistics and throughput problems rather than merely crafting the next block.

**Milestones:** Reliable basic metal supply; broad-category resource programs; automated factory loops; more capable farming and hatching.

**Factory conveniences:** More substantial **Iron Chests** and **Storage Drawers** provide physical stockpiles for bulky outputs such as cobblestone, mineral chunks, logs and ingots. **Iron Furnaces** offer a recognizable upgrade path for conventional smelting; the fastest tiers must not leapfrog MI furnaces, industrial chemistry or the intended metallurgy milestones. **Trash Cans** provide filtered disposal of unwanted byproducts where excess inventory would otherwise stall a production line.

### Age 3 — Steam Industry

Create steam power and MI steam-era processing connect the two core technology mods. Steel and industrial materials enable stronger drills, better habitats, refined cultivation, more elaborate component production and the first meaningful MI manufacturing chains. Certain Nether-origin materials become available through advanced mineral, plant or creature production without travel.

**Experience:** The factory grows beyond hand-built assembly lines into early heavy industry. Steel and steam are turning points, not merely recipe ingredients.

**Milestones:** Repeatable steel; early MI machines; advanced mineral and biological inputs; stronger resource generation.

### Age 4 — Electrification

MI introduces electricity, electrical generators, solar generation where supported by the chosen MI ecosystem, cables and energy storage. Existing Create factory networks can be driven by electric motors through a suitable MI-energy-to-motor-energy compatibility path. Players do not need to dismantle their mechanical machines; they can retrofit them.

**Experience:** The factory shifts from local water/steam rotation to centrally generated electricity, while Create still consumes that rotation.

**Milestones:** Stable electrical supply; electric motor running Create machinery; industrial electronics and more powerful machines.

### Age 5 — Precision Manufacturing

Industrial MI technology enables exact-target resource programming, more specialized genetics and cultivation, advanced alloys, fluids, chemistry and circuit production. Players can request specific ores and drops rather than broad randomized categories. The strongest processing routes improve yields or throughput over earlier Create methods, but Create remains required for selected mechanical assemblies.

**Experience:** The player becomes an industrial engineer designing several coordinated resource and processing systems.

**Milestones:** Precision drill programs; targeted biological yields; advanced MI production; improved automation and industrial storage.

### Age 6 — The Digital Revolution

**AE2 arrives late**, after substantial MI electronics and manufacturing progression. Its basic storage network is one unlock; **autocrafting comes later**, after more advanced MI requirements. The first terminal should feel like a huge leap from the Mechanical Workstation, but not instantly bypass the complexity of production lines.

**Experience:** A digital control layer finally organizes the vast factory built during the mechanical and electrical ages.

**Milestones:** First ME network and cells; later crafting CPUs, pattern providers, assemblers and complex automated crafting.

**Digital convenience:** AE2 begins with meaningful wired storage. **AE2 Wireless Terminals** are a later quality-of-life unlock, not part of the first storage recipe. Digital manufacturing additions such as **Applied Create** are earned with the corresponding autocrafting technology, ensuring Create mechanical crafting remains part of advanced factories.

### Age 7 — Advanced Industrialization

High-tier MI processing, large resource capacity, advanced resource programs and AE2 manufacturing combine. All major Revo resource systems reach efficient, programmable forms. Resource demand can be coordinated with stored inventory; factories begin producing the many high-tier components used by the final objective.

**Experience:** Scaling and orchestrating complete industrial systems, not struggling with inventory management.

**Milestones:** Advanced alloys, circuits, rare materials, high-end cultivation and habitat capability, and Genesis component production.

**Advanced logistics:** **Extended AE**, **AdvancedAE**, **MEGA Cells** and selected **ae2helpers** conveniences serve the complex production lines needed for Genesis. Their high-performance storage, crafting and pattern-management abilities are rewards of deep MI progress, not alternate routes around it.

### Age 8 — Genesis

The endgame is a deeply interdependent crafting tree culminating in **one Genesis Core**. Its inputs represent geological resources, biological production, mechanical manufacturing, metallurgy, power, computing and digital automation. Crafting it completes the principal progression. There is no further mandatory commissioning, charging or sustained-production test.

**Experience:** A definitive achievement that recognizes the entire industrial journey.

---

## 5. Shared resource-machine philosophy

RevoExtraction, RevoCultivation and RevoGenetics should feel like members of one family, but not like three reskinned item generators.

### Shared principles

- **Accessible entry:** The first machines can be understood and used without configuring complicated programs.
- **Technology unlocks resource access:** Tooling, habitat biology or cultivation capability determines what can be produced.
- **Programming improves gradually:** Fixed outputs → broad categories and proportions → individual targets → storage-aware automation.
- **Power and inputs matter:** A resource has an operating cost; rare items should not emerge at the same rate and cost as dirt or stone.
- **Renewability:** Resources do not permanently deplete because of world location.
- **Automation interface:** Machines support item/fluid movement and can cooperate with Create and later MI and AE2 logistics.
- **Visible state:** The machine makes its installed tool/program, output category, production status and bottlenecks understandable.
- **No needless micromanagement:** A new upgrade should remove repetitive tasks, not introduce obligatory manual check-ins.

### Three layers of production intelligence

| Intelligence level | Player control | Example |
|---|---|---|
| Basic | Fixed useful output mixture or simple preset | A beginner's drill produces stone, gravel and early minerals |
| Mechanical programmable | Select broad classes and their percentage weights | 50% common metals, 30% stone, 20% industrial minerals |
| Precision industrial | Select exact materials; eventually respond to inventory targets | Prioritize iron until a storage threshold is met |

These levels describe **control**, while drill heads, cultivation hardware and habitat upgrades describe **what resources are unlocked**. The two progressions should be related but distinct.

Percentages refer to allocation of attempts, cycles or work, not a guarantee that a unit of diamond costs as little as a unit of cobblestone. Resource-specific work costs maintain balance. User-facing controllers should normalize weights and warn clearly when a requested target is unavailable.

---

## 6. RevoLib — shared Revo identity and behavior

**Role:** The common foundation underlying the Revo mod family.

RevoLib represents consistent concepts players see across machines: instruction cards, upgrade categories, shared energy/status language, interface patterns and machine feedback. It is primarily an enabling system rather than a mod that gives players an entirely separate progression tree.

### Player-facing concepts

**Instruction/program cards** encapsulate choices such as a resource group, exact species, harvest priority or extraction target. Early machines offer straightforward default behavior; later machines unlock more complex programs. Reusable and copyable configurations make it practical to scale a factory without configuring every block from scratch.

**Upgrades** increase capacity, speed, efficiency or control without forcing players to discard their prior factories unnecessarily. Common interaction patterns make learning a new Revo machine easier.

**Machine diagnostics** explain operational state in plain language. For example: *No rotational drive*, *Program exceeds drill tier*, *Waiting for nutrient feed*, *Colony at population cap*, *Spring depleted*, *Output blocked*.

**Visual language** follows Modern Industrialization machine interfaces: familiar inventory slots, compact status information and restrained pixel-art panels. Machine families may be distinguished through their icons and functional controls, not radically different layouts or elaborate theming.

This shared behavior should not erase unique loops: drills mine, farms grow, habitats breed and the Workstation indexes inventory.

---

## 7. RevoExtraction — matter from the void

### Purpose

RevoExtraction completely replaces the progression role of MI's standard quarry resource generation. It provides the primary renewable supply of minerals, stone, geological materials and appropriate Nether/End blocks, all from an unrestricted void environment.

### The Void Excavator

The Void Excavator is powered by Create rotation. It accepts a **drill head** and a **mining instruction**; later upgrades allow broader control and faster production. Machines are not tied to a chunk, deposit or special world biome. A player's factory layout and available infrastructure determine where excavators can be built.

The earliest version is crankable. Later versions are driven by water, wind, steam or an MI-powered electric motor, using Create's speed and stress mechanics. Higher-tier drill heads unlock deeper classes of materials; they do not require the player to move somewhere else.

### Proposed drill capability ladder

| Drill level | Thematic material | Primary new capability |
|---|---|---|
| Primitive | Wood/stone | Stone, gravel, sand, basic mineral feedstock |
| Early mechanical | Iron | Iron, copper, coal and other common metals |
| Precision mechanical | Brass | Tin, zinc, gold, redstone and industrial minerals |
| Heavy industrial | Steel | Stronger mineral classes, quartz, specialized metals and rare materials |
| Advanced industrial | High-tier alloy | Advanced MI resources and difficult-to-obtain materials |
| Ultimate | Exotic industrial head | All available resource categories at endgame throughput |

The precise placement of MI materials such as nickel, aluminum, titanium and tungsten must follow the agreed MI recipe progression. This is a thematic ladder, not a finalized ore unlock table.

### Instruction system

**Basic:** The machine produces a practical mixture automatically. A new player does not need to choose a percentage to obtain early iron.

**Category:** A mechanical controller allocates production work among broad categories such as *Stone & Soil*, *Common Metals*, *Industrial Minerals*, *Precious Metals* and *Rare Minerals*. The actual materials inside a category depend on installed drill technology. Focus choices trade flexibility for efficiency or more targeted yields.

**Precision:** MI electronics unlock individual resource selection and reusable resource programs. The player can target iron specifically, rather than hoping to receive it from a general metals program. More precise extraction may require additional work, energy or processing.

**Advanced:** A programmable controller supports saved configurations, multiple targets, prioritization and eventually inventory-aware requests when AE2 exists.

### What it produces

- Stone, gravel, sand and other simple geological feedstock.
- Raw ore-bearing materials for Create crushing and washing.
- MI metals and special minerals according to technology tier.
- Nether-origin geological materials: Netherrack, soul-related mineral feedstock, quartz, glowstone materials, ancient debris when justified by late progression.
- End-origin geological materials: End Stone, exotic crystalline feedstock, and other geological equivalents needed for recipes.
- Certus Quartz and Sky Stone or valid crafted equivalents for AE2 progression.

**Processing matters:** Early extraction should generally output mineral chunks or raw feedstock rather than fully refined ingots. Create crushing, washing and smelting are the first meaningful processing route; MI later offers stronger industrial metallurgy and refinement, which can recover additional materials or offer better efficiency.

### Intended feeling

The Void Excavator must become a familiar, reliable industrial asset. A player starts by hand-cranking a few stone resources and eventually runs a bank of programmable excavators supplying a vast MI factory. Technology improves **access**, **targeting**, **efficiency** and **throughput** independently.

---

## 8. RevoCultivation — producing life and soil

### Purpose

RevoCultivation makes plants, wood, soil and related organic resources renewable from the skyblock, replacing the common Ex Nihilo reliance on manual leaf/dirt processes and traditional biome exploration. It includes the **Tree Farm**, **Greenhouse**, **Composter** and **Silkworm Cultivation**.

### 8.1 Tree Farm / Arboretum

A controlled machine-based way to grow and harvest trees. It begins simply and becomes an increasingly productive mechanical wood source.

**Loop:** Establish a tree type from a sapling → provide growing conditions and power → harvest logs, leaves, saplings and sticks → use the output for construction, compost, fuel and other machines.

Progression introduces tree-type selection, output priorities, more productive cultivation and advanced industrial operation. Higher-tier control might choose specific wood species or prioritize leaves over logs. The farm should never require constant replanting of a consumed starter sapling unless an explicit recipe is meant to do so.

Traditional Create-built saw contraptions may remain a player-created alternative, making physical designs worthwhile even when the compact Revo farm exists.

### 8.2 Greenhouse

A general-purpose, upgradeable enclosed cultivation machine that grows agricultural and specialized plants. It uses seeds, water and appropriate power, with fertilizer as a useful productivity tool rather than an obligatory chore in the basic stage.

Its program ladder follows the rest of the Revo machines:

- Early: common food crops and straightforward seed cultivation.
- Mechanical: broad agricultural and botanical groups.
- Industrial: exact crop selection, more output, richer plant materials.
- Advanced: exotic fungi, Nether Wart, Crimson/Warped plant resources, Chorus plants and other biome- or dimension-specific plants where required.
- Late: inventory-driven crop targeting and large-scale supply for industrial chemistry and Genesis materials.

The player should not have to find an inaccessible biome or dimension to unlock a crop: the first seed or research material must itself have an achievable crafting or production route.

### 8.3 Composter

The Composter is an essential **early** source of renewable dirt. It converts leaves, excess saplings, seeds, plant waste and other compatible organic material into compost, then dirt or fertilizer depending on the product chain.

The first composting experience is manual and inexpensive. Kinetic automation allows steady dirt production from tree-farm waste. Later upgrades improve efficiency or handle industrial quantities of biomass.

The Composter is meant to connect resource loops: tree leaves become soil, greenhouse byproducts become fertilizer, and creature-derived biological material may be useful when appropriate. It must not become a bottleneck preventing the player from expanding the first island.

### 8.4 Silkworms / Sericulture

Silkworms supply early **string and silk** without relying on mob farms or Ex Nihilo infestation-and-sieving mechanics.

**Loop:** Acquire silkworms through a valid early recipe or leaf-related process → place them in a cultivation tray → supply leaves → obtain cocoons → process into string/silk → automate supply and harvesting.

Mechanical sericulture eventually connects directly to tree-farm outputs. The system stays compact and understandable; it should not force the player to repeat an infestation minigame endlessly.

### Intended feeling

Cultivation is not simply a side activity for food. It is an industrial resource branch producing wood, soils, fibres, organic feedstock, specialized crops and some materials otherwise locked behind the Nether and End. It makes a distinct contribution to the Genesis Core.

---

## 9. RevoGenetics — hatcheries, populations and mob resources

### Purpose

RevoGenetics replaces mandatory traditional mob grinding and inaccessible dimension-specific mob drops with a system of **craftable species eggs, incubation, growing colonies and resource harvesting**.

The key rule is: **Craft an egg to establish a species; develop a population to scale production.** The mob population is a meaningful investment, not a consumable resource that gets destroyed with each item output.

### Core loop

1. **Manufacture an egg** using accessible ingredients appropriate to the species' technological tier.
2. **Incubate and hatch** the egg in a basic hand-cranked or mechanically powered hatchery.
3. **Establish a population** in a habitat.
4. **Feed and grow** the population using inputs from RevoCultivation, RevoExtraction or industrial processing.
5. **Collect resources** produced by the established colony.
6. **Upgrade, specialize and automate** habitats, harvesting priorities, feeding and population management.

A creature-derived ingredient should never be the only way to make the first egg of that creature; inaccessible species must have a self-contained initial manufacturing path.

### Virtual colonies, visible personality

A habitat represents a persistent population number rather than spawning large numbers of real mobs for the server to simulate. Small decorative or animated representations may help players understand the species being cultivated, but resource output comes from the habitat and its internal population state.

Different species can have different growth rates, population caps, nutrient needs and harvest outputs. Basic species should be easy to establish; exotic species may require elaborate reagents, industrial conditions or advanced electronics.

### Example species ladder

| Stage | Example colonies | Primary resources |
|---|---|---|
| Primitive/early mechanical | Chicken, basic livestock | Eggs, feathers, meat, leather, wool/milk equivalents |
| Mechanical | Skeleton, spider, creeper | Bones, arrows, string, eyes, gunpowder |
| Brass/advanced mechanical | Slime, Enderman | Slimeballs, ender pearls |
| Steam/industrial | Blaze, magma cube, ghast | Blaze rods, magma cream, ghast tears |
| Advanced industrial | Wither skeleton, shulker | Wither skulls, shulker shells |
| Late industrial synthesis | Exotic materials and boss-associated products | Endgame genetic reagents, Nether-star-related ingredients |

The precise tier assignments can change to preserve the MI dependency tree.

### Population and harvesting

- A hatched egg creates a small initial population.
- Food/nutrients support growth; the population expands toward its habitat capacity.
- More population generally means more output, with configurable or species-dependent efficiency.
- **Harvesting should not routinely kill population members.** Lapses in food or power reduce or pause production instead of continuously wiping out hours of growth.
- Players can choose between breeding/growth emphasis and item-harvest emphasis when the relevant controller is unlocked.
- Advanced controllers permit target populations and targeted drops.
- Colonies should be movable or recoverable without destroying the player's long-term investment.

### Integration with other resource systems

Simple colonies use simple feed. Advanced ones combine several industries; for example a Blaze habitat may need thermal nutrients synthesized from volcanic mineral output, greenhouse biomass and MI processing. Skeleton-derived bones can return to fertilizer production. Spider colonies may support silk production; slime materials support Create glue and components.

**This is an intentionally different loop from mining.** The Void Excavator improves by tools and work capacity; RevoGenetics improves by nurturing and sustaining a growing colony.

### Boss-equivalent resources

Wither-related and Dragon/End-related resources used by pack recipes must be provided by specific biological/chemical or geological production routes. Mandatory boss fights are not part of the progression. Where a unique trophy item has no recipe role, it can remain optional or be absent; where a material is needed for progress, it must have a deterministic obtainable path.

---

## 10. RevoStorage — a clockwork crafting inventory

### Purpose

RevoStorage fills the large gap between vanilla chests and intentionally late-game AE2. It is an **expandable, unified crafting-and-storage Workstation** that feels physical and mechanically constrained rather than like a cheap digital storage network.

### The spring battery

The defining component is a **crankable spring battery** storing winding energy. Initially the player attaches a hand crank and winds the station. Stored tension lets the station operate for a short period.

**When the spring is depleted, the main storage interface cannot be viewed or used for crafting.** This is a deliberate mechanic, not merely a crafting-speed penalty. The player must rewind the spring or later connect continuous Create rotation.

To avoid a softlock, a limited emergency way to retrieve stored items should exist without power. This emergency access is not the full searchable/crafting UI; it is a safety valve for stranded resources.

### Workstation behavior

While powered, the station presents one searchable storage interface and a crafting grid that draws from its own inventory. Storage upgrades expand capacity. Recipe memory and batch crafting upgrades allow players to produce repeated quantities of an item from ingredients already inside.

The Workstation is deliberately **not AE2 autocrafting**. It should not automatically infer recursive dependencies and manufacture a chain of missing components across other machines. Its crafting queue is local and limited. True network-scale recursive crafting arrives much later with AE2.

### Progression

| Stage | Experience |
|---|---|
| Wooden Spring Workstation | Modest integrated inventory; hand winding; short active time |
| Iron/Reinforced Workstation | More slots and spring capacity; better usability |
| Brass/Mechanical Workstation | Continuous rotational recharging; saved recipes and batch crafting |
| Steel/Industrial Workstation | Larger storage; queueing and restricted logistics |
| Electrical-era Workstation | Electrically driven mechanical system; improved automation |
| AE2 transition | Existing stored items can be transferred or exposed to a digital network |

An early spring should last long enough for ordinary crafting and searching, not force constant winding during a single inventory session. The thrill is the first transition from manual winding to permanent mechanized storage. Later spring and power upgrades should make physical storage a reliable foundation until AE2.

### Complementary physical storage and crafting

The pack intentionally includes several conventional storage and crafting conveniences alongside the Workstation. They solve different problems and should not erase RevoStorage's progression role.

- **Crafting Station: J/EMI Edition Updated** is a useful, **early, unpowered manual crafting station**. Its persistent crafting grid, recipe-viewer integration and access to adjacent chests make early building pleasant. It is not a global searchable inventory, does not batch-craft from an indexed storage pool and has no automatic multistep crafting. Thus it coexists with the spring-powered Workstation rather than replacing it.
- **Iron Chests** offer larger individual chest inventories and straightforward upgradeable containers. They make building and buffer storage more comfortable, while the Workstation remains the central powered search-and-craft interface.
- **Storage Drawers** provide compact, high-volume storage for a small number of item types, suited to bulk outputs from the Void Excavator, tree farms and industrial processes. They are physical factory buffers rather than an early digital network.
- **Crafting Tweaks** improves the ordinary crafting-grid experience through rotation, balancing and similar small controls. It is a convenience feature rather than an automation mechanic.
- **Sophisticated Backpacks**, if included, are optional portable inventory conveniences. High-capacity and automated upgrades should be restricted as necessary so they do not undermine RevoStorage or progression into AE2.

A player can therefore place a Crafting Station beside a few Iron Chests in the early ages, later organize bulk resource buffers with Storage Drawers, then use the Mechanical Workstation for unified searchable storage and powered batch crafting. AE2 finally brings digital network storage and automated crafting across machines. These layers should feel complementary, not redundant.

### Story role

The storage story mirrors the whole pack: a player once needed to wind a clockwork chest to search for materials, and eventually manages an industrial digital manufacturing network. The mechanical workstation remains meaningful in selected late recipes, including the ultimate crafting tree.

---

## 11. RevoIndustry — industrial integration and final technology

### Purpose

RevoIndustry defines the distinctive industrial components and progression identity connecting Create, MI, the Revo resource machines and the Genesis Core. It also hosts the **cosmetic employee/title identity** associated with milestones.

It is not intended to replace MI's entire industry or reinvent an independent electricity system. MI remains the technological heart of steam, electrical engineering, advanced materials, chemistry, circuits and high-end manufacturing.

### Cross-mod production

The desired feeling is a sequence of interconnected processes:

**Void-extracted raw mineral → Create crushing/washing → MI metallurgy → Create precision assembly → MI advanced manufacturing → AE2-managed production.**

Not every item must traverse all stages, but important milestones should deliberately depend on machinery from multiple systems. The deeper MI technology goes, the more control players gain over extraction, cultivation and genetics.

### Smelting and industrial furnaces

**Iron Furnaces** supplies accessible upgrades to conventional furnaces, giving players tangible improvements between the primitive furnace and larger industrial processing systems. Its basic tiers are useful for everyday smelting, charcoal and small batches. Advanced furnace tiers should belong to later technology stages, so they remain a convenience without replacing the distinctive capabilities of MI's smelting, alloying and high-tier industrial machines. The intended progression is more convenient ordinary furnaces first, followed by deeper industrial metallurgy and specialized automated processing.

### Byproducts and safe disposal

**Trash Cans** is part of the factory-management toolkit. It handles unwanted items and, where supported, fluid or energy overflow. This is particularly important for continuous Void Excavator and cultivation production, where plentiful gravel, stone or plant waste could stop more valuable output. Filtered disposal should be a deliberate player choice rather than the default expectation: composting, crushing, recycling and useful byproduct recovery remain preferable when those routes matter to progression. Unrecoverable disposal must be clear enough to avoid accidentally voiding valuable Genesis ingredients.

### Existing electric motors, adapted

Power generation belongs primarily to MI. Create power generation belongs to Create. **The pack reuses an existing Create electric motor solution, rather than developing its own motor/power ecosystem.** The chosen motor should allow MI-generated electricity, including solar setups and other MI generation where available, to supply Create rotation through a compatible energy connection. An MI-to-FE bridge may be necessary if the motor accepts FE while MI provides EU.

The preferred candidate is **Create Crafts & Additions** for electric motors. Specific versions, conversion compatibility and balance remain to be verified. The resulting motor is a normal part of the game's technological progression: it should be unlocked after a genuine MI electrical milestone, not available as a primitive bypass.

**Power design:**

- Electric motors make Create infrastructure easier to power and scale, not obsolete.
- Mechanical load, speed and electricity consumption remain related.
- Converting rotation back into electricity cannot generate a free-energy loop or bypass MI's intended progression.
- Solar energy may be a useful production choice, with daytime intermittency and storage trade-offs as relevant to the selected generation systems.
- Players are rewarded for retrofitting water-wheel and steam-powered systems to an electrical drive rather than rebuilding every machine.

### Cosmetic employee/title system

RevoIndustry includes an optional, silly **employee-title identity**: players can wear achievement titles such as *Chief Crank Officer*, *Senior Dirt Inspector*, *Unpaid Factory Intern*, *Director of Questionable Science* or *Chief Egg Hatcher*. Titles are primarily earned through the quest system and have **no gameplay advantages**. Players can select from titles they've unlocked, with appropriately readable display behavior on multiplayer servers.

---

## 12. Applied Energistics 2 — deliberately late

AE2 should be as transformative as a new age, not a convenient mod added at the beginning. The Mechanical Workstation remains the primary compact crafting-storage interface throughout substantial Create and MI progression.

### Two separate milestones

**First AE2 storage network:** Requires significant MI electrical and electronics progress, refined silicon/Certus processing, advanced circuits and industrial materials. Players can use terminals, storage cells and digital storage after a serious technology investment.

**AE2 autocrafting:** Comes later, gated by more advanced MI assembly, circuits and precision manufacturing. Crafting CPUs, pattern providers and molecular assemblers should not simply arrive alongside the first ME terminal.

### Materials that must be replaced or reinterpreted

Traditional AE2 access may depend on naturally generated meteorites, Sky Stone, Certus Quartz and inscriber presses. In a pure skyblock world, these must have custom resource and fabrication paths. Other AE2 components and alternative power connections must not unintentionally skip the intended gate.

### Relationship to RevoStorage

RevoStorage is a comfortable, power-limited early personal/industrial terminal. AE2 is the first genuinely global, digital and eventually recursive manufacturing network. Migration of existing Workstation inventory should be straightforward, so the AE2 milestone feels like liberation instead of requiring painful manual item moves.

### AE2 extension ecosystem

The Digital Age should have real depth after the first functional ME network. The following addons expand its usefulness, but **the unlock sequence remains dependent on MI technology**, and addons should never act as a shortcut around Create, RevoExtraction or RevoStorage.

| Addon | Desired experience | Intended progression |
|---|---|---|
| **AE2 Wireless Terminals** | Use storage and crafting terminals while moving around large factories instead of returning to a wired terminal for each item | After basic AE2 storage and a further electronics investment |
| **Applied Create** | Coordinate AE2 patterns with Create mechanical crafting and keep physically assembled components useful well into digital manufacturing | Alongside AE2 autocrafting and appropriate MI assembly technology |
| **Extended AE** | Higher-throughput digital interfaces, pattern management and advanced crafting capacity for large-scale industrial production | Advanced MI and established AE2 autocrafting |
| **AdvancedAE** | Sophisticated AE2 automation, processing and late-stage digital conveniences | Advanced to late MI |
| **MEGA Cells** | Very large digital item/fluid storage and large-scale crafting infrastructure | Late MI / Genesis preparation |
| **ae2helpers** | Targeted quality-of-life improvements for AE2 pattern and crafting workflows | As each underlying AE2 feature becomes available |

The name and existence of an addon do not grant access to every one of its items. A player should unlock **basic network storage first**, **local and wireless convenience next**, **standard autocrafting after further MI progression**, and **large-capacity or accelerated crafting much later**.

**Preserve resource-generation identity.** Any addon features that create infinite water, cobblestone or other resources for free must be excluded, disabled or gated so strongly that they cannot bypass the intentional RevoExtraction and renewable-fluid systems. Similarly, high-capacity storage cells and advanced terminals should not become cheaper alternative starting routes to RevoStorage.

The AE2 ecosystem is there to make a mature, complicated factory pleasant to control, not to replace Create machines, MI processing or Revo resource-production lines.

---

## 13. Complete renewable-resource design

Every required item must fit into a reachable and intentional production route. This table is a **coverage concept**, not an exhaustive list of all modded item IDs.

| Resource family | Intended source | Notes |
|---|---|---|
| Stone, gravel, sand | RevoExtraction | Accessible from the beginning; Create processing may transform one into another |
| Dirt | RevoCultivation Composter | Extremely early and renewable |
| Clay | Extraction and/or compost/water processing | Obtainable before clay-dependent recipes |
| Common ores/metals | RevoExtraction → Create/MI | Iron, copper, coal, tin, zinc as progression allows |
| Precious/rare metals | Higher extraction tiers → MI | Gold, rare MI metals and special materials |
| Redstone, quartz, gems | Appropriate mining programs | Rates and availability reflect progression |
| Nether geological blocks | RevoExtraction | Netherrack, quartz, glowstone feedstock, soul materials, ancient debris |
| End geological blocks | RevoExtraction | End Stone and other special required materials |
| Wood and leaves | RevoCultivation Tree Farm | Supports building, fuel, compost, sericulture |
| Dirt/biomass/fertilizer | RevoCultivation Composter | Connects multiple biological loops |
| Food and ordinary crops | RevoCultivation Greenhouse | Normal farming may also work |
| Fungi, Nether Wart, Chorus | Higher-tier Greenhouse | No dimension travel required |
| String and silk | Silkworms and selected RevoGenetics colonies | Accessible early |
| Bones, gunpowder, slime, leather | RevoGenetics | Cultivated populations |
| Ender pearls, blaze rods, ghast tears | Advanced RevoGenetics | Species eggs and habitats replace dimension farming |
| Shulker shells, wither skulls | High-tier RevoGenetics | Expensive species progression |
| Nether Stars/boss derivatives | Advanced genetic/industrial synthesis | No mandatory boss encounter |
| Certus and Sky Stone | RevoExtraction/processing | Supports gated AE2 |
| AE2 inscriber presses | Industrial fabrication | No meteorite dependency |
| Water | Starter access and renewable infrastructure | Must never be progression-blocking |
| Lava | Deliberate mechanical/thermal processing route | Supports heating, obsidian and industrial use |
| Other fluids/chemicals | MI and suitable Create processes | Must be included in the dependency audit |
| Villager/bee/animal-dependent items | Cultivation, Genetics or explicit alternatives | No inaccessible biome/entity-only recipe |

### Anti-deadlock rule

Any ingredient used to manufacture the first machine capable of producing a resource must already be obtainable by some earlier method. This applies especially to eggs, drill heads, seeds, catalysts, fluid loops and electronics.

### Avoid unnecessary universal generation

RevoExtraction should not produce every biological material. RevoCultivation should not produce high-grade ores. RevoGenetics should not randomly output arbitrary industrial components. Each branch has a meaningful job; late resources can require **combined** products from several branches.

---

## 14. Gameplay balance and accessibility

### Intended difficulty

The pack is challenging because of **recipe dependencies, manufacturing depth, production scale and integrated automation**. It should not be difficult primarily because of waiting, repeated cranking or navigating deliberately inconvenient GUIs.

### Balance principles

- Hand-powered stages are short introductions to automation.
- The first usable kinetic power network should feel achievable early.
- Ordinary dirt, basic wood and building resources should be inexpensive to produce.
- Higher-tier ores and creature drops need more work, specialist inputs and machinery.
- Category targeting improves control without circumventing material tiers or resource cost.
- Exact targeting is powerful, but can have a modest efficiency premium.
- New processing stages give worthwhile advantages: better yield, valuable byproducts, speed or automation.
- Vanilla/modded alternative recipes must not make gated technologies obtainable prematurely.
- Valuable automation upgrades remove chores instead of adding obligatory maintenance.
- A large factory is encouraged, but a player should not be required to produce absurd block counts with no engineering benefit.

### Spring battery experience

The first spring can be wound manually for a short session. Without energy, the Workstation's normal view and craft functions are inaccessible; emergency recovery prevents item trapping. Once a water wheel is available, most players should naturally power storage continuously. This creates a satisfying milestone while preventing the winding mechanic from becoming a recurring nuisance.

### Population experience

Hatching the first egg should feel momentous. A growing colony is a persistent asset. Feeding should be meaningful but not micromanagement-heavy. Basic species are approachable; exotic species require advanced industry. Normal resource production should not regularly decimate the population.

### Multiplayer experience

Difficulty and resource quantities should be fair for solo players while allowing teams to divide labor. Quests may share progress, but personal cosmetics should remain available to each eligible team member. Players should be able to specialize in extraction, farming, genetics, Create machinery or MI without a forced class system.

### Convenience without bypassing progression

The modpack should be generous with **building and inventory convenience**, especially because it takes place over a void and asks players to build large factories. **Building Wands** makes placing platforms and factory walls less tedious. **Soul Gravestone** is a candidate for recovering belongings after a death, especially a fall into the void; a void-death recovery route should be dependable but need not remove every consequence of mistakes. **Clumps** reduces the cost of dense experience-orb collections. **Jade Addons** gives richer machine and contraption information so complicated production lines are easier to diagnose.

These QoL features should save clicks and frustration rather than fabricate items or circumvent progression gates. Their purpose is to make building, operating and recovering a complex factory more comfortable, not to lower the requirements for advanced materials.

---

## 15. Teams, dedicated-server aspirations and social identity

### Intended multiplayer

The pack supports solo play and cooperative teams. The desired long-term multiplayer model allows teams to share progression, claim/maintain industrial spaces and potentially operate their own islands. A shared island must also be a valid playstyle.

**FTB Teams**, **FTB Quests** and **FTB Chunks** are the intended foundation for teams, quests, claims and chunk-loading rules. Team-island allocation, shared resources, permissions and any separate-island experience remain gameplay choices to refine; the presence of FTB Teams alone does not guarantee automatic island provisioning.

Custom machines must behave predictably when multiple players interact, and machinery should not require an open GUI or active player to perform its normal loaded-world operations. Offline processing and chunk-loading policies should be fair to server performance and to teams of different sizes.

### Hats as achievements

**Simple Hats** is an achievement system, not a random loot-grind focus. Hats are earned *mainly through FTB Quests*, especially major milestones, with some optional funny side achievements. Hats are cosmetic and offer no power bonuses. A player can show off their journey through increasingly silly hats.

**Possible milestone rewards:**

| Milestone | Suggested cosmetic concept |
|---|---|
| First Void Excavator | Miner Helmet |
| First Spring Workstation | Propeller Beanie |
| Automated Create factory | Cogwheel Hat |
| Automated Greenhouse | Flower Crown |
| First stable mob population | Scientist Hat |
| MI Steam Age | Steampunk Top Hat |
| First MI electricity | Lightbulb Hat |
| First AE2 network | Computer Hat |
| Advanced MI mastery | Industrial Crown |
| Genesis Core | Exclusive Golden Revo Crown |

These are **thematic reward concepts**, not a promise that all named hats already exist as assets in Simple Hats. Exclusive designs may be unique Revo cosmetics.

### Funny titles

Quest achievements also grant selectable employee titles such as:

- *Certified Crank Operator*
- *Senior Dirt Inspector*
- *Chief Egg Hatcher*
- *Licensed Shaft Technician*
- *Unpaid Factory Intern*
- *Executive Cobblestone Manager*
- *Director of Questionable Science*
- *Master of Industry*

Titles are a playful parallel progression. They need no stats and should not create rank-based gameplay permissions.

### Other lightweight social mods

**Emotecraft** supports dancing and gestures. **PlayerName Styler** can provide nickname and name-display customization alongside the unlocked title concept, subject to how display rules coexist. The pack may also include aesthetic factory signage and collaborative notice boards.

**Explicit exclusions:** Simple Voice Chat, Piggyback, Sit and Lay and Exposure are *not* in the planned modpack roster.

---

## 16. Quests and presentation of progression

**FTB Quests** is the main story and learning structure. It should explain what players are trying to achieve, demonstrate how systems connect, award tasteful milestone rewards, and provide a useful sense of direction without replacing discovery and experimentation.

### Suggested questbook chapters

| Chapter | Main topic | Representative milestone |
|---|---|---|
| A World from Nothing | Starter resources and composting | First renewable dirt and stone |
| Mechanical Beginnings | Create crank/kinetics and Workstation | First powered storage station |
| Extraction Revolution | Raw materials and Create processing | Automated iron supply |
| Cultivation | Tree farm, greenhouse, compost, silk | Renewable plant/wood materials |
| Life in the Machine | Eggs, hatchery and habitats | First growing mob colony |
| Brass & Automation | Resource percentages, mechanical logistics | Category-based extraction |
| Steam Industry | MI steam and steel | First major MI production line |
| Electrification | MI electricity and motors | Electric-powered Create network |
| Precision Industry | Exact targets, advanced MI, exotic materials | Programmable resource production |
| Digital Manufacturing | Gated AE2 storage, wireless access, autocrafting and Create-linked patterns | First fully functional digital manufacturing chain |
| Industrial Mastery | Late MI and high-end production | Genesis ingredient branches |
| Genesis | Final ultimate craft | Craft the Genesis Core |

The detailed quest book may branch so players can work on cultivation, extraction or genetics in parallel, while meaningful technology gates keep major progression coherent.

Rewards should mostly consist of guidance, cosmetics, titles and occasional convenience; quests should not routinely grant large quantities of advanced materials that bypass the associated manufacturing loops.

---

## 17. The Genesis Core — the final single craft

### Narrative meaning

The Genesis Core is the logical culmination of the opening premise: beginning with almost nothing, the player has built an industrial civilization capable of manufacturing matter, sustaining biological systems and producing advanced technology. The Genesis Core is a compact symbolic result of that industrial civilization.

### The victory condition

**Craft the Genesis Core once.** It is not a machine the player must subsequently power, commission or feed at a sustained rate to finish the pack. Its challenge is the extremely demanding **crafting dependency tree**. The craft requires endgame tools, sophisticated processing and scalable production in nearly every branch.

### Nine proposed ultimate components

| Component | Represents | Example demands |
|---|---|---|
| **Void Singularity** | RevoExtraction | Rare mineral feedstock, advanced drill programs and extensive extraction |
| **Mechanical Singularity** | Create | Precision mechanisms, sequenced manufacturing and high-tier mechanical components |
| **Biosphere Core** | RevoCultivation | Tree, crop, compost and silk production, including exotic materials |
| **Industrial Matrix** | MI | High-tier metallurgy, chemistry and advanced machines |
| **Genesis Heart** | All systems | Deeply cross-linked intermediate components |
| **Quantum Processor** | Electronics + AE2 | High-grade circuits, precision technology and digital components |
| **Material Singularity** | Large-scale material refining | Alloys, specialized resources and advanced processing |
| **Energy Singularity** | MI energy industry | Complex power-generation and electrical components |
| **Automation Matrix** | RevoStorage + AE2 | The evolution from spring-workstation parts to digital factory automation |

These are **design names and roles**, not finalized recipes. Their recipes should overlap rather than form nine completely isolated checklist tasks. An advanced electronic component could use Create mechanisms; biological materials could rely on MI chemistry; digital components could require RevoStorage-derived mechanisms. That interdependence ensures all main systems remain important.

### Cost philosophy

The final item must be expensive enough that players need capable resource generation and mostly automated production. Some mass quantities are appropriate, but the main burden should be *manufacturing complexity and technological reach*. Avoid letting the endgame collapse into idle waiting for giant piles of one basic material.

### Reward

Crafting the Genesis Core completes the main quest progression and can provide a trophy or placeable visual memento, an announcement to the team, and a distinctive **Golden Revo Crown** and title. Any creative-tier abilities, if included as optional postgame celebration, must not be required for completion.

---

## 18. Selected existing mod roster

This is the **intended mod roster**, not a verified, fully pinned compatibility manifest. All intended mods and dependencies need suitable NeoForge 1.21.1 builds before release; optional cosmetic/QoL additions may change if compatibility is poor.

### Core industry and digital systems

| Mod | Intended place in pack |
|---|---|
| Create | Kinetic power, mechanical automation and physical manufacturing |
| Modern Industrialization | Steam, electricity, materials, industrial manufacturing and high technology |
| Applied Energistics 2 | Deeply gated digital storage and autocrafting |
| AE2 Wireless Terminals | Wireless terminal convenience unlocked after the first wired network |
| Applied Create | Links AE2 pattern automation with Create mechanical crafting in the digital manufacturing era |
| Extended AE | Advanced AE2 crafting and pattern handling, gated to later MI progression |
| AdvancedAE | Advanced digital processing and automation, gated to later MI progression |
| MEGA Cells | Very large storage and crafting capacity reserved for late MI / Genesis |
| ae2helpers | AE2 crafting and pattern-management quality of life; follows its underlying technology gates |
| Iron Furnaces | Upgradeable conventional furnaces, with advanced tiers balanced against MI metallurgy |
| Create Crafts & Additions | Existing electric motor solution, adapted for MI-based factory power |
| MI–FE energy compatibility solution | Connect MI EU to motor FE where required; exact choice unconfirmed |
| Create Jetpack | Early mechanical-flight convenience |
| Iron Jetpacks | Later electrical flight; actual power/charging compatibility must fit MI |

### Skyblock, quests, teams and recipes

| Mod | Intended place in pack |
|---|---|
| Skyblock Builder | Configurable void-world and starter-island experience |
| FTB Quests | Progression, milestones, rewards, hats and titles |
| FTB Teams | Team membership and shared progression |
| FTB Chunks | Claims and configured chunk loading |
| KubeJS | Pack-level recipes, balancing and content gating |
| EMI | Discoverable recipes and production dependency browsing |
| Jade | Clear machine and block information |

### Cosmetics and playful social features

| Mod | Intended place in pack |
|---|---|
| Simple Hats | Primarily quest-earned cosmetic achievements |
| PlayerName Styler | Custom player display names |
| Emotecraft | Player emotes and dances |
| RevoIndustry title system | Selectable silly industrial job titles |

### Graphics, menu and visual presentation

| Mod or pack | Intended place in pack |
|---|---|
| FancyMenu | Restrained Vanilla+-style main menu with subtle pack branding |
| Revo visual resource pack | Readable, Minecraft-compatible custom font, modest logo/icon assets and MI-consistent machine GUI details |
| Iris + Sodium | Optional shader-capable rendering and performance |
| Complementary Reimagined | Recommended optional shader pack |

### Quality of life, signage and performance

| Mod | Intended place in pack |
|---|---|
| Mouse Tweaks | Better inventory interaction |
| Iron Chests | Upgraded physical chest inventories for machine buffers and everyday storage |
| Storage Drawers | High-volume physical storage for a few bulk resource types |
| Crafting Station: J/EMI Edition Updated | Early persistent 3×3 crafting with convenient adjacent-inventory access |
| Crafting Tweaks | Crafting-grid rotation, balancing and other small crafting conveniences |
| Trash Cans | Filtered disposal of unwanted item/fluid/energy byproducts as supported |
| Jade Addons | More detailed machine and contraption information overlays |
| Building Wands | Faster construction of platforms and factory structures |
| Soul Gravestone | Death inventory recovery, especially valuable for void falls |
| Clumps | Combine experience orbs to reduce performance overhead |
| Sophisticated Backpacks (optional) | Portable inventory convenience; avoid early high-tier automation/storage bypasses |
| FTB Ultimine | Efficient bulk harvesting, with appropriate balancing |
| Controlling | Manage key bindings |
| AppleSkin | Food/saturation information |
| Polymorph | Resolve overlapping crafting recipes |
| TrashSlot | Remove unwanted inventory items |
| Signboards | Factory signage and organization |
| Noticeboard | Shared physical notices and goals |
| ModernFix | Optimization candidate |
| FerriteCore | Memory optimization candidate |
| ImmediatelyFast | Rendering optimization candidate |
| Entity Culling | Rendering optimization candidate |
| spark | Performance diagnostics, especially for servers |

**Do not include:** Simple Voice Chat, Piggyback, Sit and Lay, Exposure. They were considered and deliberately removed from the desired roster.

### Compatibility expectations

- No mod may silently trivialize the gated Revo resources or early Create progression.
- Native MI quarries are not part of the intended resource-generation progression.
- Third-party alternatives that defeat AE2 gates or make unrestricted early storage networks available should be restricted or omitted.
- Crafting Stations, Iron Chests and Storage Drawers should complement rather than replace the search, spring power and batch-crafting identity of RevoStorage.
- Advanced Iron Furnaces, high-capacity AE2 addon items and powerful backpacks must not bypass the relevant MI/research gates.
- Infinite cobblestone/water or similarly free resource cells from AE2 addons should be disabled or excluded if they undermine RevoExtraction.
- Trash Cans should be a safe optional byproduct-disposal tool, not a source of compulsory material loss.
- Shader support is client-side and optional.
- A dedicated server should not need client-only visual modules.
- MI electricity, FE motors, and any jetpack charging interfaces are **not assumed compatible without validation**.

---

## 19. Areas deliberately left open

These are *design questions*, not an implementation checklist. They should be answered through progression design and play experience rather than prematurely inventing technical details.

1. **Starting supplies:** What is the smallest pleasant island and starter resource set that avoids deadlocks?
2. **Exact material tiers:** Where should every MI ore, circuit family, chemical reagent and exotic resource first become available?
3. **Exotic species:** Which creature populations are worth supporting as distinct colonies and which drops are better produced through rare industrial synthesis?
4. **Biology balance:** How fast should a population grow, how much should feeding cost, and how much resource production should be unlocked through each habitat upgrade?
5. **Extraction balance:** What trade-off should exist between mixed mining, category targeting and exact targeting?
6. **Mechanical storage comfort:** How many minutes should the initial spring last and when should continuous rotation naturally become available?
7. **Motor and electricity balance:** Which existing Create motor plus energy compatibility route best preserves MI's power progression?
8. **Dimensions and optional encounters:** Are vanilla portals fully unusable or simply irrelevant? Are optional boss fights available in the Overworld?
9. **Team islands:** Should teams start on one shared island by default or get separate islands in multiplayer?
10. **Final crafting scale:** What exact quantities and dependency depth make the Genesis Core feel monumental without becoming an idle-time requirement?
11. **Minor visual choices:** Which understated background, logo and font variant best support the already chosen **Vanilla+ pack appearance** and **Modern Industrialization-style custom machine GUIs**?
12. **Mobility:** How early should a jetpack be available as a construction convenience, without eliminating the satisfaction of building bridges and platforms?
13. **Physical storage balance:** How early should Iron Chests, Storage Drawers and Crafting Stations improve inventory handling without making the Mechanical Workstation feel unnecessary?
14. **Furnace progression:** Which Iron Furnaces tiers remain useful while MI metallurgy and high-tier machines are being unlocked?
15. **AE2 addon pacing:** When should wireless access, Applied Create integration, advanced pattern management and MEGA Cells each become available?
16. **Safety and QoL:** Which void-death recovery, building-wand and disposable-byproduct conveniences should be included by default, and how much should they mitigate risk?

These open choices do not change the core vision.

---

## 20. Final design summary

**Rassers Revolution: Sky Edition** is a single-world, expert-style industrial skyblock adventure. A player starts by hand-cranking primitive extraction machinery and winding a spring-powered storage chest. Create makes it possible to automate minerals, plants, soil, wood, silk and the beginnings of creature cultivation. RevoGenetics turns craftable eggs into maintained virtual populations that yield creature resources. Each resource system becomes progressively more programmable, moving from mixed output to broad percentages to exact production and ultimately network-aware targeting.

Modern Industrialization brings steel, steam, electricity, chemicals, advanced circuits and increasingly complex production. Existing electric motors connect MI power generation to Create's rotational machines. AE2 arrives only after deep MI progression, first as storage and later as real autocrafting, completing the transition from clockwork workshop to digital factory.

Alongside the serious industrial journey, familiar quality-of-life mods provide early physical crafting stations, upgraded chests, organized bulk buffers, furnace upgrades, filtered trash disposal, safer void-death recovery and comfortable large-scale building. These remain conveniences rather than alternative progression paths. Once AE2 is earned, complementary addons provide wireless access, Create-linked crafting patterns and advanced high-volume digital automation without invalidating the Revo resource systems. FTB Quests awards collectible Simple Hats and ridiculous Revo employee titles, with team-friendly progression and optional emotes. The whole experience builds to **one enormous craft: the Genesis Core**, a final object that requires meaningful mastery of Create, MI, AE2, RevoExtraction, RevoCultivation, RevoGenetics, RevoStorage and the shared Revo industrial progression.

> **From Nothing, Build Everything.**
