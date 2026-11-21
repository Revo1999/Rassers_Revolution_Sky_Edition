package dk.revo.revoextraction;
import dk.revo.revocultivation.*;
import dk.revo.revostorage.*;
import aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("revoextraction") @PrefixGameTestTemplate(false)
public class ProgressionExpansionTests {
 static Item item(String s){return ProgressionBridgeTests.item(s);}
 static void check(GameTestHelper h,boolean ok,String why){h.assertTrue(ok,why);}
 static ElectricDrillEntity electricFixture(GameTestHelper h,BlockPos pos){var l=h.getLevel();l.setBlockAndUpdate(pos,RevoExtraction.ELECTRIC.get().defaultBlockState());var e=(ElectricDrillEntity)l.getBlockEntity(pos);for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++)l.setBlockAndUpdate(ElectricDrillStructure.at(e,x,y,z),ElectricDrillStructure.block(x,y,z).defaultBlockState());e.sedimentHatch().orientation.outputDirection=Direction.SOUTH;var tank=ElectricDrillStructure.hatch(e,-1,-1).getInventory().getFluidStacks().getFirst();tank.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant.of(net.minecraft.world.level.material.Fluids.WATER));tank.setAmount(8000);check(h,ElectricDrillStructure.validate(e),"Native electric multiblock forms");return e;}
 static net.neoforged.neoforge.items.IItemHandler electricOutput(ElectricDrillEntity e){return ElectricDrillStructure.hatch(e,1,0).getInventory().itemStorage.itemHandler;}
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void starterBatteryAndHullNeedNoElectricDrillMetals(GameTestHelper h){
  var level=h.getLevel();var manager=level.getRecipeManager();
  var battery=(CraftingRecipe)manager.byKey(ResourceLocation.parse("revoextraction:bootstrap_redstone_battery")).orElseThrow().value();
  var cable=new ItemStack(item("modern_industrialization:tin_cable"));var plate=new ItemStack(item("modern_industrialization:copper_plate"));var zinc=new ItemStack(item("create:zinc_ingot"));var redstone=new ItemStack(Items.REDSTONE);
  var input=CraftingInput.of(3,3,java.util.List.of(cable,plate,cable,zinc,redstone,zinc,plate,redstone,plate));
  check(h,battery.matches(input,level),"Early copper/zinc/tin/redstone recipe matches actual registered tags");
  var cell=battery.assemble(input,level.registryAccess());check(h,cell.is(item("modern_industrialization:redstone_battery"))&&cell.getCount()==1,"One actual MI battery per craft");
  var hull=(CraftingRecipe)manager.byKey(ResourceLocation.parse("modern_industrialization:electric_age/hull/lv_machine_hull_asbl")).orElseThrow().value();
  var hullInput=CraftingInput.of(3,3,java.util.List.of(ItemStack.EMPTY,new ItemStack(item("modern_industrialization:analog_circuit")),ItemStack.EMPTY,cell,new ItemStack(item("modern_industrialization:steel_machine_casing")),cell,cable,cable,cable));
  check(h,hull.matches(hullInput,level)&&hull.assemble(hullInput,level.registryAccess()).is(item("modern_industrialization:basic_machine_hull")),"Starter batteries feed the unchanged native LV hull recipe");h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void mvAe2RecipesAndPoweredSaltGate(GameTestHelper h){
  var level=h.getLevel();var ops=net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,level.registryAccess());
  for(String id:new String[]{"ae2:inscriber/logic_processor","ae2:inscriber/engineering_processor","ae2:inscriber/calculation_processor","ae2:network/blocks/inscribers","ae2:network/blocks/energy_energy_acceptor","ae2:network/blocks/controller","modern_industrialization:compat/ae2/logic_processor","modern_industrialization:compat/ae2/engineering_processor","modern_industrialization:compat/ae2/calculation_processor"}){
   var recipe=level.getRecipeManager().byKey(ResourceLocation.parse(id)).orElseThrow();
   String encoded=Recipe.CODEC.encodeStart(ops,recipe.value()).getOrThrow().toString();
   check(h,encoded.contains("modern_industrialization:electronic_circuit"),"Actual loaded recipe needs MV circuit: "+id);
  }
  var seed=MachineKind.starter(19);check(h,!MachineKind.GREENHOUSE.acceptsStarter(seed)&&MachineKind.GREENHOUSE.program(seed,0)==-1,"Wood cannot grow salt");
  check(h,MachineKind.IRON_GREENHOUSE.acceptsStarter(seed)&&MachineKind.IRON_GREENHOUSE.program(seed,0)==19,"Electric salt geode program");
  check(h,MachineKind.products(19).size()==1&&MachineKind.products(19).getFirst().is(RevoExtraction.MINERAL_SLUDGE.get()),"Greenhouse grows seawater feedstock instead of finished salt");
  var recipe=(CraftingRecipe)level.getRecipeManager().byKey(ResourceLocation.parse("revoextraction:salt_crystal_starter")).orElseThrow().value();
  var input=CraftingInput.of(2,2,java.util.List.of(new ItemStack(Items.QUARTZ),new ItemStack(Items.SAND),new ItemStack(Items.WATER_BUCKET),new ItemStack(item("revocultivation:compost"))));
  check(h,recipe.matches(input,level)&&recipe.assemble(input,level.registryAccess()).is(seed.getItem()),"Salt-free geode bootstrap");
  check(h,recipe.getRemainingItems(input).stream().anyMatch(v->v.is(Items.BUCKET)),"Geode returns bucket");h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void surveyAndDigitalAutomationRecipesRequireTheNextTier(GameTestHelper h) {
  var recipes = h.getLevel().getRecipeManager();
  var survey = (CraftingRecipe)recipes.byKey(ResourceLocation.parse("revoextraction:deep_survey_core")).orElseThrow().value();
  var mvHull = new ItemStack(item("modern_industrialization:advanced_machine_hull"));
  check(h, survey.getIngredients().stream().anyMatch(ingredient -> ingredient.test(mvHull)), "New survey cores require an MV hull");
  var digital = new ItemStack(item("modern_industrialization:digital_circuit"));
  for (String id : new String[]{"ae2:network/crafting/cpu_crafting_unit", "ae2:network/blocks/pattern_providers_interface", "ae2:network/crafting/molecular_assembler",
          "extendedae:cobblestone_cell", "extendedae:water_cell"}) {
   if (id.startsWith("extendedae:") && !net.neoforged.fml.ModList.get().isLoaded("extendedae")) continue;
   var recipe = (CraftingRecipe)recipes.byKey(ResourceLocation.parse(id)).orElseThrow().value();
   check(h, recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.test(digital)), "Loaded automation recipe requires a Digital Circuit: "+id);
  }
  var drive = (CraftingRecipe)recipes.byKey(ResourceLocation.parse("ae2:network/blocks/storage_drive")).orElseThrow().value();
  check(h, drive.getIngredients().stream().noneMatch(ingredient -> ingredient.test(digital)), "Basic ME storage is still available at MV");
  h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void nativeMachinesApplyConverterSaltAndLavaBalance(GameTestHelper h) {
  var assembler = placeMiMachine(h, new BlockPos(2,2,2), "assembler");
  var converterInputs = assembler.getInventory().getItemStacks();
  var ingredients = java.util.List.of(new ItemStack(Items.IRON_INGOT,4), new ItemStack(Items.REDSTONE,2),
          new ItemStack(Items.COPPER_INGOT,2), new ItemStack(item("fluxnetworks:flux_core")));
  for(int i=0;i<ingredients.size();i++){
   var ingredient=ingredients.get(i);
   converterInputs.get(i).setKey(ItemVariant.of(ingredient));
   converterInputs.get(i).setAmount(ingredient.getCount());
  }
  var converter=item("revoindustry:power_converter");
  var recipes=h.getLevel().getRecipeManager();
  check(h, recipes.byKey(ResourceLocation.parse("revoindustry:power_converter")).orElseThrow().value().getType()
          == aztech.modern_industrialization.machines.init.MIMachineRecipeTypes.ASSEMBLER, "Converter has a native Assembler recipe");
  power(assembler,250);
  check(h, amount(assembler,converter)==0,"The old converter ingredients are insufficient");
  converterInputs.get(4).setKey(ItemVariant.of(item("modern_industrialization:lv_steam_turbine")));
  converterInputs.get(4).setAmount(1);
  power(assembler,250);
  check(h, amount(assembler,converter)==1,"The powered Assembler produces a converter with a spare LV turbine");

  var mixer=placeMiMachine(h,new BlockPos(4,2,2),"electric_mixer");
  var sludgeInput=mixer.getInventory().getItemStacks().getFirst();
  sludgeInput.setKey(ItemVariant.of(RevoExtraction.MINERAL_SLUDGE.get()));
  sludgeInput.setAmount(1);
  var fluids=mixer.getInventory().fluidStorage.fluidHandler;
  var execute=net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
  fluids.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),execute);
  power(mixer,250);
  var seawater=new net.neoforged.neoforge.fluids.FluidStack(dk.revo.revoindustry.saltern.Saltern.SEA_WATER.get(),1000);
  check(h, fluids.drain(seawater,execute).getAmount()==1000 && sludgeInput.isEmpty(),"One greenhouse feedstock and water make seawater in the native Mixer");
  check(h, amount(mixer,item("modern_industrialization:salt_dust"))==0,"The Mixer still requires chamber and centrifuge processing to obtain salt");

  var centrifuge=placeMiMachine(h,new BlockPos(6,2,2),"centrifuge");
  centrifuge.getInventory().fluidStorage.fluidHandler.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA,1000),execute);
  power(centrifuge,650);
  check(h, amount(centrifuge,item("modern_industrialization:sulfur_dust"))==17,"Lava still yields 17 sulfur");
  check(h, amount(centrifuge,item("modern_industrialization:copper_nugget"))==2,"Lava now yields two copper nuggets");
  h.succeed();
 }

 private static ElectricCraftingMachineBlockEntity placeMiMachine(GameTestHelper h,BlockPos relative,String name){
  var level=h.getLevel();
  var pos=h.absolutePos(relative);
  var block=BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization",name));
  level.setBlockAndUpdate(pos,block.defaultBlockState());
  return (ElectricCraftingMachineBlockEntity)level.getBlockEntity(pos);
 }

 private static void power(ElectricCraftingMachineBlockEntity machine,int ticks){
  for(int i=0;i<ticks;i++){
   machine.getEnergyComponent().insertEu(32,Simulation.ACT);
   machine.tick();
  }
 }

 private static long amount(ElectricCraftingMachineBlockEntity machine,Item item){
  return machine.getInventory().getItemStacks().stream().filter(slot->slot.toStack().is(item)).mapToLong(slot->slot.getAmount()).sum();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void allNewMobsAndBurnerCapture(GameTestHelper h){
  for(var kind:new MachineKind[]{MachineKind.HARVESTER,MachineKind.STEEL_HARVESTER})for(int i=0;i<kind.harvesterPrograms().length;i++){
   var m=ProgressionBridgeTests.farm(h,kind,i);check(h,!m.captureBurner(),"No instant burner fills");if(i>=9)m.inputs.setStackInSlot(0,new ItemStack(Items.NETHERITE_INGOT));ProgressionBridgeTests.ticks(m,m.cycleTicks()*(i==10?64:1));var expected=MachineKind.products(kind.harvesterPrograms()[i]);for(var s:expected)check(h,ProgressionBridgeTests.count(m.outputs,s.getItem())>=s.getCount(),"Actual mob product "+s);check(h,m.containedMob()!=null,"Every mob renders a valid contained entity");
   if(m.program()==24){var saved=m.saveWithoutMetadata(h.getLevel().registryAccess());m.loadWithComponents(saved,h.getLevel().registryAccess());check(h,m.captureBurner()&&!m.captureBurner(),"One saved credit per Blaze cycle");}
  }h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void allNewGreenhouseProgramsRetainStarters(GameTestHelper h){
  h.getLevel().setDayTime(1000);for(int program:new int[]{14,15,16,31}){var m=ProgressionBridgeTests.farm(h,MachineKind.GREENHOUSE,0);m.inputs.setStackInSlot(0,MachineKind.starter(program));m.water.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);ProgressionBridgeTests.ticks(m,400);check(h,m.program()==program&&ProgressionBridgeTests.count(m.outputs,MachineKind.products(program).getFirst().getItem())==(program==19?1:3),"Three crop outputs");check(h,m.inputs.getStackInSlot(0).getCount()==1&&m.water.getFluidAmount()==750,"Retained starter and paid water");}h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void loadedRecipeBridgesAndBucketRemainder(GameTestHelper h){
  for(String id:new String[]{"milling/raw_gold","empty_blaze_burner","sugar_cane_starter","kelp_starter","cactus_starter","beetroot_starter","electric_drill","deep_survey_core","synthetic_netherrack","synthetic_soul_sand"})check(h,h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("revoextraction:"+id)).isPresent(),"Recipe loads: "+id);
  var recipe=(CraftingRecipe)h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("revoextraction:kelp_starter")).orElseThrow().value();var input=CraftingInput.of(3,1,java.util.List.of(new ItemStack(item("revocultivation:compost")),new ItemStack(Items.WHEAT_SEEDS),new ItemStack(Items.WATER_BUCKET)));check(h,recipe.matches(input,h.getLevel()),"Kelp uses actual registered compost");check(h,recipe.assemble(input,h.getLevel().registryAccess()).is(Items.KELP),"Kelp result");check(h,recipe.getRemainingItems(input).stream().anyMatch(s->s.is(Items.BUCKET)),"Returns empty bucket");
  for(String id:new String[]{"revostorage:chest_drive","revostorage:crafting_terminal","modern_industrialization:electric_age/machine/lv_steam_turbine_asbl","modern_industrialization:electric_age/circuit/craft/lv_circuit_asbl"})check(h,h.getLevel().getRecipeManager().byKey(ResourceLocation.parse(id)).isPresent(),"Native crafting route "+id);h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void electricGeologyPowerBlockingDeepAndPersistence(GameTestHelper h){var l=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));var e=electricFixture(h,pos);
  ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,e.work==0,"No free power");
  for(int program=0;program<=4;program++){for(var slot:ElectricDrillStructure.hatch(e,1,0).getInventory().getItemStacks())slot.empty();e.program=program;e.work=0;e.energy=32000;var expected=e.output().getItem();check(h,expected!=Items.AIR,"Real registered ore");for(int t=0;t<200;t++)ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,ProgressionBridgeTests.count(electricOutput(e),expected)>0&&e.energy==28800,"16 EU/t mining program "+program);}
  e.program=5;e.work=0;e.energy=32000;e.items.setStackInSlot(1,new ItemStack(Items.WATER_BUCKET));for(int t=0;t<200;t++)ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,ProgressionBridgeTests.count(electricOutput(e),Items.ICE)==1&&e.items.getStackInSlot(1).is(Items.BUCKET),"Glacial water paid and bucket returned");
  for(var slot:ElectricDrillStructure.hatch(e,1,0).getInventory().getItemStacks())slot.empty();e.sedimentHatch().getInventory().getFluidStacks().getFirst().empty();e.program=6;e.work=0;e.energy=32000;ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,e.work==0,"Deep core required");e.items.setStackInSlot(0,new ItemStack(RevoExtraction.SURVEY_CORE.get()));e.cycle=63;e.work=1199;ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,ProgressionBridgeTests.count(electricOutput(e),Items.ANCIENT_DEBRIS)==1&&e.cycle==64,"Rare deep output");var tag=e.saveWithoutMetadata(l.registryAccess());e.loadWithComponents(tag,l.registryAccess());check(h,e.cycle==64&&e.program==6,"Deep cursor saved");for(var slot:ElectricDrillStructure.hatch(e,1,0).getInventory().getItemStacks()){slot.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(Items.COBBLESTONE));slot.setAmount(slot.getTotalCapacityFor(Items.COBBLESTONE));}int power=e.energy;ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,e.energy==power&&e.work==0,"Full output pauses energy and work");h.succeed();}
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void emeraldCraftingReplacesTradingColony(GameTestHelper h) {
  var level = h.getLevel();
  var recipes = level.getRecipeManager();
  var recipe = (CraftingRecipe) recipes.byKey(ResourceLocation.parse("revoextraction:emerald_from_gem_sludge")).orElseThrow().value();
  var sludge = new ItemStack(item("revoextraction:gem_sludge"));
  var input = CraftingInput.of(2, 2, java.util.List.of(sludge, sludge, sludge, sludge));
  check(h, recipe.matches(input, level), "Four Gem Sludge match the emerald recipe");
  var result = recipe.assemble(input, level.registryAccess());
  check(h, result.is(Items.EMERALD) && result.getCount() == 1, "Each craft yields one emerald");
  var incomplete = CraftingInput.of(2, 2, java.util.List.of(sludge, sludge, sludge, ItemStack.EMPTY));
  check(h, !recipe.matches(incomplete, level), "Three Gem Sludge are insufficient");
  check(h, recipes.byKey(ResourceLocation.parse("revocultivation:trading_colony")).isEmpty(), "The colony recipe is retired");

  var pos = h.absolutePos(new BlockPos(2, 2, 2));
  var colony = RevoCultivation.TRADE.get();
  var state = colony.defaultBlockState();
  level.setBlockAndUpdate(pos, state);
  var entity = (TradingColony.Entity) level.getBlockEntity(pos);
  entity.items.setStackInSlot(0, new ItemStack(Items.PAPER, 32));
  entity.items.setStackInSlot(26, new ItemStack(Items.EMERALD, 2));
  var saved = entity.saveWithoutMetadata(level.registryAccess());
  saved.putInt("Work", 199);
  entity.items.setStackInSlot(0, ItemStack.EMPTY);
  entity.items.setStackInSlot(26, ItemStack.EMPTY);
  entity.loadWithComponents(saved, level.registryAccess());
  check(h, colony.getTicker(level, state, RevoCultivation.TRADE_ENTITY.get()) == null, "Existing colonies cannot trade");
  check(h, entity.items.insertItem(0, new ItemStack(Items.PAPER), false).getCount() == 1, "Automation cannot insert new items");
  check(h, entity.items.extractItem(0, 32, false).getCount() == 32, "Saved input can be recovered");
  var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
  var menu = new TradingColony.Menu(1, player.getInventory(), entity);
  player.getInventory().setItem(9, new ItemStack(Items.EMERALD));
  check(h, menu.quickMoveStack(player, 27).isEmpty(), "Shift-clicking cannot merge into saved emeralds");
  check(h, menu.quickMoveStack(player, 26).getCount() == 2 && entity.items.getStackInSlot(26).isEmpty(), "Saved emeralds can be recovered through the menu");
  h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void nineDiamondChestsNetworkAndPersistence(GameTestHelper h){var l=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));l.setBlockAndUpdate(pos,RevoStorage.DRIVE.get().defaultBlockState());l.setBlockAndUpdate(pos.east(),RevoStorage.TERMINAL.get().defaultBlockState());var e=(StorageEntity)l.getBlockEntity(pos);var terminal=(StorageEntity)l.getBlockEntity(pos.east());var diamond=item("ironchest:diamond_chest");check(h,diamond!=Items.AIR,"Iron Chests integration installed");for(int i=0;i<9;i++)e.chests.setStackInSlot(i,new ItemStack(diamond));check(h,e.capacity()==972&&terminal.networkItems().getSlots()==972,"Nine native diamond chest capacities");check(h,terminal.insert(new ItemStack(Items.DIAMOND,64),false).isEmpty(),"Network insert");check(h,e.chests.extractItem(0,1,false).isEmpty(),"Nonempty bay locks chest");check(h,terminal.take(new ItemStack(Items.DIAMOND),32,true).getCount()==32&&terminal.take(new ItemStack(Items.DIAMOND),32,false).getCount()==32,"Simulation does not extract");var tag=e.saveWithoutMetadata(l.registryAccess());e.loadWithComponents(tag,l.registryAccess());check(h,ProgressionBridgeTests.count(terminal.networkItems(),Items.DIAMOND)==32,"Real saved inventory");terminal.take(new ItemStack(Items.DIAMOND),32,false);check(h,!e.chests.extractItem(0,1,false).isEmpty()&&terminal.networkItems().getSlots()==864,"Empty bay removes without phantom slots");h.succeed();}
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void terminalTransferCraftRemaindersChargeAndSharedGrid(GameTestHelper h){var l=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));l.setBlockAndUpdate(pos,RevoStorage.DRIVE.get().defaultBlockState());l.setBlockAndUpdate(pos.east(),RevoStorage.TERMINAL.get().defaultBlockState());var drive=(StorageEntity)l.getBlockEntity(pos);drive.chests.setStackInSlot(0,new ItemStack(Items.CHEST));check(h,drive.capacity()==27,"Vanilla chest capacity="+StorageEntity.chestCapacity(new ItemStack(Items.CHEST))+" components="+new ItemStack(Items.CHEST).getComponents());var e=(StorageEntity)l.getBlockEntity(pos.east());check(h,e.insert(new ItemStack(Items.OAK_LOG,3),false).isEmpty(),"Initial log insert; slots="+e.networkItems().getSlots());check(h,ProgressionBridgeTests.count(e.networkItems(),Items.OAK_LOG)==3,"Initial stored logs");var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(l);player.teleportTo(pos.getX()+1,pos.getY(),pos.getZ());var menu=new StorageMenu(1,player.getInventory(),e);check(h,l.getRecipeManager().byKey(ResourceLocation.parse("minecraft:oak_planks")).isPresent(),"Vanilla oak planks recipe exists");check(h,e.networkItems().getSlots()==27&&ProgressionBridgeTests.count(e.networkItems(),Items.OAK_LOG)==3,"Logs inserted into connected drive: slots="+e.networkItems().getSlots()+" logs="+ProgressionBridgeTests.count(e.networkItems(),Items.OAK_LOG));menu.transfer(ResourceLocation.parse("minecraft:oak_planks"),false);check(h,menu.craft.getItem(0).is(Items.OAK_LOG),"Recipe transfer from whole network");check(h,menu.slots.getFirst().getItem().is(Items.OAK_PLANKS),"Native crafting result");menu.clicked(0,0,ClickType.PICKUP,player);check(h,menu.getCarried().isEmpty(),"No crafting without winding");seedSpring(l,placeSpring(l,pos.east(2)));menu.clicked(0,0,ClickType.PICKUP,player);check(h,menu.getCarried().is(Items.OAK_PLANKS)&&menu.getCarried().getCount()==4&&e.springCharge()==255,"Craft produces four planks and spends one charge");check(h,menu.craft.getItem(0).is(Items.OAK_LOG),"Grid refills after craft");menu.setCarried(ItemStack.EMPTY);menu.quickMoveStack(player,0);check(h,player.getInventory().countItem(Items.OAK_PLANKS)==4,"Shift-click crafting");e.insert(new ItemStack(Items.DIAMOND),false);menu.search="@minecraft";check(h,!menu.entries().isEmpty(),"Mod search");menu.search="impossible";check(h,menu.entries().isEmpty(),"Search hides unmatched items");var tag=e.saveWithoutMetadata(l.registryAccess());e.loadWithComponents(tag,l.registryAccess());check(h,e.craftingItems.getFirst().is(Items.OAK_LOG)&&e.springCharge()==254,"Persistent shared grid and spring");h.succeed();}
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void firstPoweredGreenhouseHarvestAndBackpressure(GameTestHelper h){var l=h.getLevel();l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);l.setRainLevel(0);var kind=MachineKind.IRON_GREENHOUSE;var pos=h.absolutePos(new BlockPos(4,2,3));l.setBlockAndUpdate(pos,kind.controller().defaultBlockState().setValue(FarmController.FACING,Direction.NORTH));var m=(FarmMachine)l.getBlockEntity(pos);
  for(int x=-2;x<=2;x++)for(int y=0;y<3;y++)for(int z=-2;z<=2;z++){var part=kind.part(x,y,z);var state=kind.block(part).defaultBlockState();if(part==MachineKind.Part.CONTROLLER)state=state.setValue(FarmController.FACING,Direction.NORTH);if(part==MachineKind.Part.HATCH)state=state.setValue(FarmHatch.FACING,Direction.EAST);if(part==MachineKind.Part.POWER)state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,Direction.SOUTH);l.setBlockAndUpdate(m.at(x,y,z),state);}
  check(h,m.checkStructure(),"First electric greenhouse forms");m.inputs.setStackInSlot(0,new ItemStack(Items.SUGAR_CANE));m.water.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);m.receiveEu(1600,false);ProgressionBridgeTests.ticks(m,200);check(h,ProgressionBridgeTests.count(m.outputs,Items.SUGAR_CANE)==3&&m.water.getFluidAmount()==750&&m.energy()==0,"One real harvest consumes 1600 EU and 250 mB");for(int i=0;i<9;i++)m.outputs.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));m.receiveEu(1600,false);ProgressionBridgeTests.ticks(m,20);check(h,m.energy()==1600&&m.water.getFluidAmount()==750&&m.state()==5,"Full greenhouse pauses power and water");for(int program:new int[]{17,18,19}){for(int i=0;i<9;i++)m.outputs.setStackInSlot(i,ItemStack.EMPTY);m.inputs.setStackInSlot(0,MachineKind.starter(program));l.setBlockAndUpdate(m.at(0,-1,0),(program==17?net.minecraft.world.level.block.Blocks.SOUL_SAND:net.minecraft.world.level.block.Blocks.END_STONE).defaultBlockState());m.receiveEu(1600,false);ProgressionBridgeTests.ticks(m,200);check(h,ProgressionBridgeTests.count(m.outputs,MachineKind.products(program).getFirst().getItem())==(program==19?1:3),"Gated greenhouse harvest "+program);}h.succeed();
 }
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void terminalKineticChargingAndHopperCapability(GameTestHelper h) {
  var level=h.getLevel();
  var pos=h.absolutePos(new BlockPos(2,2,2));
  level.setBlockAndUpdate(pos,RevoStorage.DRIVE.get().defaultBlockState());
  var terminalPos=pos.east();
  level.setBlockAndUpdate(terminalPos,RevoStorage.TERMINAL.get().defaultBlockState());
  var drive=(StorageEntity)level.getBlockEntity(pos);
  drive.chests.setStackInSlot(0,new ItemStack(Items.CHEST));
  var terminal=(StorageEntity)level.getBlockEntity(terminalPos);
  var handler=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,terminalPos,Direction.DOWN);
  check(h,handler!=null&&handler.getSlots()==27,"Hopper sees connected storage without a spring");
  check(h,net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(handler,new ItemStack(Items.IRON_INGOT,12),false).isEmpty(),"Hopper inserts items");
  check(h,ProgressionBridgeTests.count(terminal.networkItems(),Items.IRON_INGOT)==12,"Hopper items arrive in drive");
  var spring=placeSpring(level,terminalPos.east());
  var motor= com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState()
      .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,Direction.SOUTH);
  level.setBlockAndUpdate(spring.getBlockPos().north(),motor);
  h.succeedWhen(()->check(h,Math.abs(spring.getSpeed())>=SpringEntity.MIN_RPM && spring.charge()>=32,
      "The front socket receives powered rotation through the real Create network"));
 }

 private static SpringEntity placeSpring(net.minecraft.world.level.Level level,BlockPos pos) {
  level.setBlockAndUpdate(pos,RevoStorage.SPRING.get().defaultBlockState());
  return (SpringEntity)level.getBlockEntity(pos);
 }

 private static void seedSpring(net.minecraft.world.level.Level level,SpringEntity spring) {
  var saved=spring.saveWithoutMetadata(level.registryAccess());
  saved.putInt("Charge",256);
  spring.loadWithComponents(saved,level.registryAccess());
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void nativeHandCrankWindsSpring(GameTestHelper h) {
  var level=h.getLevel();
  var spring=placeSpring(level,h.absolutePos(new BlockPos(3,2,3)));
  var crankPos=spring.getBlockPos().north();
  var crankState=com.simibubi.create.AllBlocks.HAND_CRANK.getDefaultState()
      .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,Direction.NORTH);
  level.setBlockAndUpdate(crankPos,crankState);
  var crank=(com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity)level.getBlockEntity(crankPos);
  check(h,crankState.canSurvive(level,crankPos),"The Create Hand Crank mounts directly on the socket");
  h.onEachTick(()->crank.turn(false));
  h.succeedWhen(()->check(h,spring.charge()>=256,"Turning the actual Create Hand Crank winds the coil"));
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void terminalCraftReturnsWaterBucket(GameTestHelper h){var l=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));l.setBlockAndUpdate(pos,RevoStorage.DRIVE.get().defaultBlockState());l.setBlockAndUpdate(pos.east(),RevoStorage.TERMINAL.get().defaultBlockState());var drive=(StorageEntity)l.getBlockEntity(pos);drive.chests.setStackInSlot(0,new ItemStack(Items.CHEST));var e=(StorageEntity)l.getBlockEntity(pos.east());seedSpring(l,placeSpring(l,pos.east(2)));for(var item:new Item[]{item("revocultivation:compost"),Items.WHEAT_SEEDS,Items.WATER_BUCKET})e.insert(new ItemStack(item),false);var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(l);player.getInventory().clearContent();var menu=new StorageMenu(1,player.getInventory(),e);menu.transfer(ResourceLocation.parse("revoextraction:kelp_starter"),false);menu.clicked(0,0,ClickType.PICKUP,player);check(h,menu.getCarried().is(Items.KELP),"Actual terminal crafts kelp");check(h,menu.craft.getItems().stream().anyMatch(stack->stack.is(Items.BUCKET))||player.getInventory().countItem(Items.BUCKET)>0,"Native ResultSlot returns bucket");check(h,e.springCharge()==255,"One operation charged");h.succeed();}
 @GameTest(template="drill_test",timeoutTicks=100)
 public static void auditThreeBootstrapRecipesAndGates(GameTestHelper h){var l=h.getLevel();for(String id:new String[]{"logic_press_blank","calculation_press_blank","engineering_press_blank","silicon_press_blank","pressing/logic_press","pressing/calculation_press","pressing/engineering_press","pressing/silicon_press","sky_stone_starter","salt_crystal_starter","tungsten_survey_core","nuclear_survey_core","nether_wart_starter","end_stone_starter","chorus_starter","netherite_template_blank","pressing/netherite_template","poppy_starter"})check(h,l.getRecipeManager().byKey(ResourceLocation.parse("revoextraction:"+id)).isPresent(),"Loaded bootstrap "+id);
 var pos=h.absolutePos(new BlockPos(2,2,2));var e=electricFixture(h,pos);for(int program=7;program<=10;program++){for(var slot:ElectricDrillStructure.hatch(e,1,0).getInventory().getItemStacks())slot.empty();e.sedimentHatch().getInventory().getFluidStacks().getFirst().empty();e.select(program);e.energy=32000;check(h,!e.output().isEmpty(),"Registered geological output "+program);if(program>=8){ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,e.work==0,"High tier core required");e.items.setStackInSlot(0,new ItemStack(program==8?RevoExtraction.SURVEY_CORE.get():program==9?RevoExtraction.TUNGSTEN_CORE.get():RevoExtraction.NUCLEAR_CORE.get()));}e.work=e.duration()-1;var output=e.output().getItem();ElectricDrillEntity.tick(l,pos,e.getBlockState(),e);check(h,ProgressionBridgeTests.count(electricOutput(e),output)>0,"Geological production "+program);}
 e.select(6);e.cycle=63;e.select(7);e.cycle=1;e.select(6);check(h,e.cycle==63,"Program-specific rare cursor survives switching");
 check(h,MachineKind.HARVESTER.program(ItemStack.EMPTY,8)==-1,"Wooden tier cannot select Ghast");check(h,MachineKind.STEEL_HARVESTER.program(ItemStack.EMPTY,9)==-1&&MachineKind.STEEL_HARVESTER.program(new ItemStack(Items.NETHERITE_INGOT),9)==29,"Shulker requires Netherite catalyst");h.succeed();}
}
