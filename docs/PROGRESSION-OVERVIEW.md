# Rassers Revolution: Sky Edition â€” Progression Overview

*Baseline checkout: `a80e627` plus local Wither Hatchery, salt geode and drill-mineral implementations Â· Pack version: `0.1.39-dev-performance`*

From a small skyblock island, build renewable resources, automate them with Create, establish Modern Industrialization (MI) steam processing, then move into electricity and more specialized production. Biological production and storage develop alongside the main industrial route.

This overview distinguishes the **implemented progression** from the **planned endgame**. The original design is broader than the current playable quest chain. The local Wither Hatchery update adds five containment quests after the electric/deep-resource branch.

## Progression at a glance

```text
Leaves and wood
  -> Compost, dirt, pebbles and cobblestone
  -> Water, clay, ceramic vessels and lava
  -> Wooden Drill and first renewable iron
  -> Create machinery and Iron Drill
  -> Bronze processing and MI steam workshop
  -> Coke, Steam Blast Furnace and steel
  -> Water-cooled Steel Drill and renewable diamonds
  -> Rubber, circuits, motors and LV electricity
  -> Electric geology, powered farming and deeper resources

Alongside this route:
  Wooden farms -> upgraded farms and expanded mob programs
  Physical chests -> mechanical chest-drive crafting network

Planned continuation:
  Advanced MI -> gated AE2 -> industrial mastery -> Genesis Core
```

## 1. From nothing to the first drill

**Quest chapter:** Chapter 1: From Nothing â€” 35 quests.

New islands provide a Revolution Book and a placed Crafting Station. The book opens the quest guide.

1. Gather wood and use a crook to obtain leaves and replacement saplings.
2. Feed organic materials into the passive Composter. Turn its compost into dirt.
3. Hammer dirt into Stone Pebbles, craft cobblestone, and upgrade your hammer.
4. Process cobblestone into gravel, sand and dust.
5. Collect rainwater in a Rain Barrel; combine water and dust to make clay.
6. Fire clay buckets and a crucible in a furnace.
7. Heat the crucible and melt cobblestone into lava. Establish a cobblestone generator.
8. Assemble the Wooden Drill and power it with a Create hand crank.

The Wooden Drill produces a repeating mixture of cobblestone, gravel, sand and raw iron. This is the first renewable metal supply.

**Milestone:** The island can sustain its own basic construction materials and iron production.

## 2. Create machinery and reliable ore production

**Quest chapter:** Chapter 2: The Create Revolution â€” 29 quests.

Smelt the Wooden Drill's iron, make andesite alloy, and build a small Create workshop. Shafts, gears, water wheels and a Mechanical Press introduce power transmission, speed and stress capacity.

Press iron sheets and upgrade to the **Iron Drill**. It requires at least **32 RPM** and supplies raw iron, copper, tin, zinc and gold, plus coal and redstone. Every 128th completed output is Flux Dust instead of a normal resource.

Hand power remains useful during setup. Water power and automatic hatch output turn the workshop into an unattended production line. Increasing RPM also requires enough generating capacity to support the resulting stress load.

**Milestone:** Automated metals and the machinery needed to manufacture bronze components.

## 3. Renewable biology â€” a parallel branch

**Quest coverage:** Employee Handbook milestones and the Biology to Electricity branch.

These systems support the factory rather than forming a separate numbered age.

| Machine | Main role |
|---|---|
| Wooden Tree Grower | Renewable oak logs, leaves, sticks and saplings using daylight and a retained sapling. |
| Wooden Greenhouse | Renewable crops, sugar cane, kelp and cactus using water, daylight and retained starters. |
| Wooden Mob Harvester | Selectable Zombie, Skeleton, Spider, Cow and Slime resources without capturing a mob. |
| Composter | Organic inputs into compost for dirt, starters and cultivation. |
| Silk Worm Cultivator | Leaves and a retained silkworm into cocoons, silk and string. |

The important connections are practical: **sugar cane supplies paper for rubber**, **kelp supplies early Create belts**, **Slime supplies Super Glue**, and **Cow supplies leather for drill repairs**.

**Milestone:** A controlled supply of biological ingredients without relying on exploration or random mob encounters.

## 4. Bronze and the MI steam workshop

**Quest chapter:** Chapter 3: MI - The Steam Age â€” 28 quests.

Use Create milling to obtain copper and tin dust, then mix them into bronze dust and smelt bronze ingots. The unheated Create Mixer needs at least **32 RPM**. Press plates and manufacture the parts for MI machinery.

The central route is:

```text
Copper + tin -> bronze -> Bronze Boiler -> steam pipes
  -> Bronze Compressor -> Bronze Mixer / Bronze Water Pump
  -> Coke Oven
```

The boiler consumes water and fuel. Steam powers the processing machines, while the Water Pump supports a more continuous water supply. The Coke Oven introduces multiblock processing and makes coke for steel.

The first steam workshop does not require diamonds, brass, quartz or a Blaze Burner. The diamond-dependent Bronze Macerator comes later.

**Milestone:** Working steam infrastructure and a renewable coke supply.

## 5. Steel, cooling and diamonds

**Quest chapter:** Chapter 4: Steel and Coolant â€” 13 quests.

Mill coke into coke dust, obtain iron dust with MI's Forge Hammer, and follow the native uncooked-steel recipe and **Steam Blast Furnace** route. This establishes steel before obtaining diamonds.

Upgrade to the **Steel Drill**:

- Requires at least **64 RPM**, with **1,024 SU** of demand at that speed.
- Consumes cooling water and can seize after five seconds of dry running.
- Adds **Gem Sludge** for washed lapis/diamonds, plus lead and antimony for pre-LV battery alloy.
- Retains the rare Flux Dust output cadence.

Diamonds unlock the Bronze Macerator and further processing. Keep the drill supplied with water and prepare repair kits using steel plates, copper and leather.

Steel also supports faster farming and more resources:

- **Steel Tree Grower:** larger growing area, real oak growth and water consumption.
- **Steel Mob Harvester:** faster passive cycles; adds Blaze, Enderman and Magma Cube programs.
- **Iron Electric Greenhouse:** water, retained crop starters and powered grow lights; requires electricity rather than shaft power.

**Milestone:** Renewable diamonds, stronger resource production and the materials for electrification.

## 6. Resource bridges and first electricity

**Quest chapter:** Progression Routes: Biology to Electricity â€” 18 quests spanning several stages.

This chapter is a set of supporting routes, not a strict fifth age. Some entries are available before steel; others require the Steel Harvester or electric machines.

| Route | What it unlocks |
|---|---|
| Mill raw gold, then wash crushed raw gold | Native Create quartz production before crushing wheels. |
| Complete a Steel Harvester Blaze cycle, then use an Empty Blaze Burner | Filled burners for heated Create processing. |
| Steel Harvester Enderman program | Ender Pearls, Eyes of Ender and Flux components. |
| Steam processing of coke dust and water, then paper and synthetic oil | Rubber sheets. |
| Rubber, tin wiring, hand-crafted circuits and motors | The first LV Steam Turbine. |

The electrical path is **steam -> LV Steam Turbine -> tin cables -> electric machines**. The first circuits and motors have crafting-table recipes; an electric assembler is not needed to bootstrap them.

Power converters connect MI EU and FE systems. They generate no electricity and store no energy. Create: New Age provides an optional electrical branch with a steel-gated magnetite recipe.

The Iron Greenhouse uses **8 EU/t** while producing, plus water. Its integrated converter allows an FE supply as an alternative to native EU input.

**Milestone:** A functioning electrical supply and a completed powered-machine production cycle.

## 7. Electric geology and deeper resources

The compact **Electric Geological Drill** introduces selectable deposits:

| Program | Requirement | Main output |
|---|---|---|
| Mixed / individual metal deposits | 16 EU/t, 200 powered ticks per output | Silver, nickel, lead, bauxite dust and antimony. |
| Glacial Ice | Standard electric cycle plus a water bucket | Ice; returns the bucket. Ice can become packed and blue ice. |
| Deep Survey | Survey core crafted with an MV Advanced Machine Hull, 128 EU/t, 1,200 powered ticks per output | Terrain materials and glowstone, with Ancient Debris every 64th output. |

The survey core requires silver, nickel, lead, aluminum and diamond. Deep Survey is a substantial power step beyond basic LV extraction.

Craft four Steel Drill Gem Sludge in a 2x2 grid for one guaranteed emerald. Other routes include Magma Cube production for magma cream and heated mixing for netherrack and soul sand.

**Milestone:** Controlled industrial metals, additional furnace materials and a higher-power route to Ancient Debris.

### Wither containment

Build the ordinary Wither T to obtain an egg. A 5 x 5 x 5 [Electric Wither Hatchery](WITHER-HATCHERY.md) incubates it behind dark glass, then hatches and kills a real contained Wither. The 75-second cycle requires 8,192 EU/t and yields one Nether Star. Block placers, hopper collection and hatch capabilities support automation. Controlled Wither Skeleton skull production is still a separate gap.

### Submerged salt geology

The [Salt Geode Chamber](SALT-GEODE.md) consumes water and 16 EU/t to grow and harvest submerged crystal buds. Its salt-water output is centrifuged by native MI machinery into Salt Dust for sodium/chlorine chemistry. The route needs no manual salt bootstrap.

## 8. Storage and furnace expansion

### Mechanical storage

Chest Drives and Mechanical Crafting Terminals provide a local storage network. The current Chest Drive quest depends on the filled-Blaze-Burner milestone.

Each drive accepts nine physical chests and uses their native capacities. Connected terminals provide search, a shared crafting grid, EMI recipe transfer and ingredient refill. Crafting spends spring charge: wind it manually or recharge it from adjacent Create rotation at **16 RPM or higher**. Item insertion and extraction cost no charge.

This establishes the mechanical storage stage. AE2 storage opens at MV. AE2 autocrafting and ExtendedAE Infinity Cells open with HV Digital Circuits.

### Furnace expansion

Ten implemented furnace multiblock tiers extend processing capacity: **Copper, Iron, Silver, Gold, Diamond, Emerald, Obsidian, Crystal, Netherite and Rainbow**. They add parallel smelting, graded hatches and fuel handling as their materials become available.

These are available systems, but their existence does not establish a fully finished late-game progression route.

## 9. Quest structure and rewards

The current quest files contain **287 quests**. The [LV/MV continuation](QUESTS-LV-MV.md)
adds metallurgy, electronics, power and renewable chemistry guidance, plus optional
early running shoes. It adds quest guidance for existing machines and recipes.

| Chapter | Quests |
|---|---:|
| Chapter 1: From Nothing | 35 |
| Chapter 2: The Create Revolution | 29 |
| Chapter 3: MI - The Steam Age | 28 |
| Chapter 4: Steel and Coolant | 13 |
| Progression Routes: Biology to Electricity | 25 |
| Chapter 5: The LV Factory | 48 |
| Chapter 6: Electronics and Separation | 25 |
| Chapter 7: MV - Power and Chemistry | 25 |
| Chapter 8: HV - Refining, Coils and Logic | 26 |
| Crystals and Distant Resources | 10 |
| Field Equipment: Running Shoes | 7 |
| Wither Containment | 5 |
| Tips & Guides | 6 |
| Revo Employee Handbook | 5 |

Tips provide freely accessible manuals and guidance. The Handbook awards permanent cosmetic titles and hats for early milestones. These are chosen through the wardrobe and do not increase production power.

## 10. Planned continuation and remaining gaps

The design document intends the journey to continue through **advanced MI metallurgy, chemistry and electronics**, then **gated AE2 storage**, wireless convenience, autocrafting and increasingly capable digital manufacturing.

The planned finish is to **craft the Genesis Core once**, combining advanced extraction, Create manufacturing, cultivation, industrial processing, electronics, energy and automation. Its nine proposed components and recipes remain design concepts; this is not an implemented completion objective in the current source.

The pulled [crystal and late-resource work](CRYSTAL-AND-LATE-RESOURCES.md) now supplies AE2 starter presses/Sky Stone, tungsten/uranium programs, controlled skulls, Netherite templates and later crops/mob drops. [MV AE2 gates](MACHINE-POLISH-AND-MV-AE2.md) are implemented. See the refreshed [remaining-gap table](PROGRESSION-GAPS-AFTER-MV.md) for unresolved feedstocks and endgame work.

The implemented material routes and targeted tests do not establish a complete fresh-island survival playthrough through the planned endgame.

## Source notes

This overview uses the current [quest definitions](../pack/overrides/config/ftbquests/quests/chapters), machine source, and these progression documents:

- [First chapter](FIRST-CHAPTER.md), [Create and steam](CREATE-IRON-STEAM.md), and [Steel Drill](STEEL-DRILL.md).
- [Progression expansion](PROGRESSION-EXPANSION.md), [metal farms](METAL-FARMS.md), and [power conversion](POWER-CONVERSION.md).
- [Furnace progression](REVO-FURNACES.md), [latest recipe audit](RECIPE-AUDIT-3.md), and [verification scope](PROGRESSION-EXPANSION-QA.md).
- [Original design and planned Genesis endgame](design/Rassers_Revolution_Sky_Edition_v2.md).

Older audits and machine notes describe earlier versions; the current quest files, source and progression expansion take precedence where they differ.

## Drill minerals and sludge

The [drill mineral update](DRILL-MINERALS-AND-SLUDGE.md) closes the lead/antimony first-battery bootstrap. Mechanical drills supply mineral feedstocks and sludge; gems are recovered by washing. Deep Survey Core-gated electric Crystalline Sludge supplies Certus through MI centrifuging. Native quarry controller recipes are disabled.
