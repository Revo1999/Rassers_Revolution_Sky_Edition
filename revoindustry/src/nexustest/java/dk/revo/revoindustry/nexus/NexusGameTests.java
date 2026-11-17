package dk.revo.revoindustry.nexus;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoindustry") @PrefixGameTestTemplate(false)
public final class NexusGameTests {
    static void step(NexusMachine machine,int ticks){for(int i=0;i<ticks;i++){machine.getLevel().getServer().getWorldData().overworldData().setGameTime(machine.getLevel().getGameTime()+1);machine.tick();}}
    static NexusMachine build(GameTestHelper h,String kind,Direction facing){
        var level=h.getLevel();var origin=h.absolutePos(new BlockPos(25,2,25));
        var bp=Nexus.blueprint(kind);var offset=Nexus.position(bp.getAsJsonArray("origin"));
        level.setBlockAndUpdate(origin,Nexus.block(bp.get("controller").getAsString()).defaultBlockState());
        var machine=(NexusMachine)level.getBlockEntity(origin);machine.orientation.facingDirection=facing;
        for(var raw:bp.getAsJsonArray("cells")){
            var cell=raw.getAsJsonObject();var pos=ShapeMatcher.toWorldPos(origin,facing,Nexus.position(cell.getAsJsonArray("position")).subtract(offset));
            if(pos.equals(origin))continue;
            var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(cell.get("block").getAsString()));
            h.assertTrue(block!=Blocks.AIR,"Registered blueprint block "+cell.get("block"));
            level.setBlockAndUpdate(pos,block.defaultBlockState());
        }
        for(var raw:bp.getAsJsonArray("requiredAir"))level.setBlockAndUpdate(ShapeMatcher.toWorldPos(origin,facing,Nexus.position(raw.getAsJsonArray()).subtract(offset)),Blocks.AIR.defaultBlockState());
        step(machine,1);return machine;
    }
    static void clear(GameTestHelper h,NexusMachine machine){
        machine.unlink();var bp=Nexus.blueprint(machine.kind);
        for(var raw:bp.getAsJsonArray("cells")){var p=Nexus.position(raw.getAsJsonObject().getAsJsonArray("position"));h.getLevel().setBlockAndUpdate(machine.blueprintWorld(p.getX(),p.getY(),p.getZ()),Blocks.AIR.defaultBlockState());}
    }
    @GameTest(template="nexus_test",timeoutTicks=200)
    public static void fourStructuresFourRotationsNativeHatchesAndBreaks(GameTestHelper h){
        for(String kind:Nexus.RECIPES.keySet())for(Direction facing:Direction.Plane.HORIZONTAL){
            var machine=build(h,kind,facing);h.assertTrue(machine.isShapeValid(),kind+" forms facing "+facing);
            h.assertTrue(machine.getEnergyComponents().size()==1,"One exclusively linked native MI energy hatch");
            h.assertTrue(!machine.getMultiblockInventoryComponent().getItemInputs().isEmpty()&&!machine.getMultiblockInventoryComponent().getItemOutputs().isEmpty(),"Real native MI inventory inputs and outputs");
            var cell=Nexus.blueprint(kind).getAsJsonArray("cells").get(kind.equals("nexus_builder")?1:0).getAsJsonObject();var p=Nexus.position(cell.getAsJsonArray("position"));
            h.getLevel().setBlockAndUpdate(machine.blueprintWorld(p.getX(),p.getY(),p.getZ()),Blocks.AIR.defaultBlockState());step(machine,1);
            h.assertTrue(!machine.isShapeValid(),"Breaking an ordinary structure block invalidates "+kind);clear(h,machine);
        }h.succeed();
    }
    @GameTest(template="nexus_test",timeoutTicks=100)
    public static void allRevoBlocksRegistryAudit(GameTestHelper h)throws Exception{
        var root=new com.google.gson.JsonObject();var blocks=new com.google.gson.JsonArray();
        for(var block:BuiltInRegistries.BLOCK){var id=BuiltInRegistries.BLOCK.getKey(block);if(!id.getNamespace().startsWith("revo"))continue;
            var entry=new com.google.gson.JsonObject();entry.addProperty("id",id.toString());entry.addProperty("hasItem",block.asItem()!=net.minecraft.world.item.Items.AIR);entry.addProperty("item",BuiltInRegistries.ITEM.getKey(block.asItem()).toString());blocks.add(entry);
        }root.add("blocks",blocks);var recipes=new com.google.gson.JsonArray();
        for(var holder:h.getLevel().getRecipeManager().getRecipes()){
            var recipe=holder.value();var result=recipe.getResultItem(h.getLevel().registryAccess());
            if(recipe instanceof aztech.modern_industrialization.machines.recipe.MachineRecipe mi){
                for(var output:mi.itemOutputs){var entry=new com.google.gson.JsonObject();entry.addProperty("id",holder.id().toString());entry.addProperty("type",BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString());entry.addProperty("result",BuiltInRegistries.ITEM.getKey(output.getStack().getItem()).toString());recipes.add(entry);}
                continue;
            }
            if(result.isEmpty())continue;
            var entry=new com.google.gson.JsonObject();entry.addProperty("id",holder.id().toString());entry.addProperty("type",BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString());entry.addProperty("result",BuiltInRegistries.ITEM.getKey(result.getItem()).toString());recipes.add(entry);
        }root.add("recipes",recipes);
        var path=java.nio.file.Path.of("../../.local/nexus-runtime-registry-audit.json").toAbsolutePath().normalize();java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root));h.succeed();
    }
    @GameTest(template="nexus_test",timeoutTicks=100)
    public static void builderUsesRealMaterialsPowerAndPersistentCursor(GameTestHelper h){
        var builder=build(h,"nexus_builder",Direction.NORTH);h.assertTrue(builder.isShapeValid(),"Native three-port builder forms");
        var inputs=builder.getMultiblockInventoryComponent().getItemInputs();
        inputs.get(0).setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(Nexus.PROGRAM.get()));inputs.get(0).setAmount(1);
        var first=Nexus.blueprint("nexus_portal").getAsJsonArray("cells").get(0).getAsJsonObject();
        var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(first.get("block").getAsString()));
        inputs.get(1).setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(item));inputs.get(1).setAmount(5);
        var local=Nexus.position(first.getAsJsonArray("position")).subtract(Nexus.position(Nexus.blueprint("nexus_portal").getAsJsonArray("origin"))).offset(0,0,4);
        var target=ShapeMatcher.toWorldPos(builder.getBlockPos(),Direction.NORTH,local);
        var power=builder.getEnergyComponents().get(0);power.insertEu(4096,aztech.modern_industrialization.util.Simulation.ACT);
        h.getLevel().setBlockAndUpdate(target,Blocks.STONE.defaultBlockState());long before=power.getEu();step(builder,10);
        h.assertTrue(builder.buildCursor()==0&&inputs.get(1).getAmount()==5&&power.getEu()==before,"Obstruction preserves terrain, materials and power");
        h.getLevel().setBlockAndUpdate(target,Blocks.AIR.defaultBlockState());step(builder,10);
        h.assertTrue(builder.buildCursor()==1&&h.getLevel().getBlockState(target).getBlock().asItem()==item&&inputs.get(1).getAmount()==4,"Places one real supplied block");
        h.assertTrue(power.getEu()==before-64&&inputs.get(0).getAmount()==1,"Charges exactly 64 EU and retains program");
        var saved=builder.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored=(NexusMachine)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(builder.getBlockPos(),builder.getBlockState(),saved,h.getLevel().registryAccess());
        h.assertTrue(restored.buildCursor()==1,"Placement cursor persists");clear(h,builder);h.getLevel().setBlockAndUpdate(target,Blocks.AIR.defaultBlockState());h.succeed();
    }
    @GameTest(template="nexus_test",timeoutTicks=200)
    public static void fullBuilderProducesAFormablePortal(GameTestHelper h){
        var builder=build(h,"nexus_builder",Direction.EAST);var inputs=builder.getMultiblockInventoryComponent().getItemInputs();
        inputs.get(0).setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(Nexus.PROGRAM.get()));inputs.get(0).setAmount(1);
        var cells=Nexus.blueprint("nexus_portal").getAsJsonArray("cells");
        for(int i=0;i<cells.size();i++){
            var cell=cells.get(i).getAsJsonObject();var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(cell.get("block").getAsString()));
            inputs.get(1).setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(block.asItem()));inputs.get(1).setAmount(1);
            builder.getEnergyComponents().get(0).insertEu(64,aztech.modern_industrialization.util.Simulation.ACT);step(builder,10);
            h.assertTrue(builder.buildCursor()==i+1,"Builder places supplied blueprint cell "+i+": "+cell);
        }
        var target=ShapeMatcher.toWorldPos(builder.getBlockPos(),Direction.EAST,new BlockPos(0,0,4));var portal=(NexusMachine)h.getLevel().getBlockEntity(target);
        step(portal,1);h.assertTrue(portal.isShapeValid(),"All 814 real builder-placed blocks form the native MI portal facing east");
        h.assertTrue(portal.orientation.facingDirection==Direction.EAST,"Constructed controller faces correctly");
        var energy=portal.getEnergyComponents().get(0);energy.insertEu(1023,aztech.modern_industrialization.util.Simulation.ACT);step(portal,1);
        h.assertTrue(!portal.portalPowered&&energy.getEu()==1023,"Insufficient portal power consumes nothing");
        energy.insertEu(1,aztech.modern_industrialization.util.Simulation.ACT);step(portal,1);
        h.assertTrue(portal.portalPowered&&energy.getEu()==0,"Portal animation requires and consumes exactly 1024 EU per tick");
        step(portal,1);h.assertTrue(!portal.portalPowered,"Animation stops on power loss");
        clear(h,portal);clear(h,builder);h.succeed();
    }
    @GameTest(template="nexus_test",timeoutTicks=100)
    public static void nativeProcessingAndOutputBlocking(GameTestHelper h){
        for(String kind:java.util.List.of("pressure_casting_plant","electroplating_line","particle_collider")){
            var machine=build(h,kind,Direction.NORTH);var inventory=machine.getMultiblockInventoryComponent();
            var input=inventory.getItemInputs().get(0);input.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(net.minecraft.world.item.Items.STONE));input.setAmount(1);
            var water=inventory.getFluidInputs().get(0);water.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant.of(net.minecraft.world.level.material.Fluids.WATER));water.setAmount(1000);
            for(var output:inventory.getItemOutputs()){output.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(net.minecraft.world.item.Items.DIRT));output.setAmount(output.getCapacity());}
            machine.getEnergyComponents().get(0).insertEu(10_000_000,aztech.modern_industrialization.util.Simulation.ACT);step(machine,30);
            h.assertTrue(inventory.getItemOutputs().stream().noneMatch(s->s.toStack().is(net.minecraft.world.item.Items.COBBLESTONE)),"Blocked output prevents delivery: "+kind);
            for(var output:inventory.getItemOutputs())output.setAmount(0);
            for(int i=0;i<30;i++){machine.getEnergyComponents().get(0).insertEu(machine.getBaseRecipeEu(),aztech.modern_industrialization.util.Simulation.ACT);step(machine,1);}
            h.assertTrue(inventory.getItemOutputs().stream().filter(s->s.toStack().is(net.minecraft.world.item.Items.COBBLESTONE)).mapToLong(s->s.getAmount()).sum()==1,"Native MI recipe produces exactly once after unblocking: "+kind);
            h.assertTrue(inventory.getFluidOutputs().stream().mapToLong(s->s.getAmount()).sum()==500,"Real fluid output: "+kind);
            if(kind.equals("particle_collider")){
                h.assertTrue(machine.completedReactions==1,"Exactly one completion flash per successful collider reaction");
                h.assertTrue(machine.getBlockState().triggerEvent(h.getLevel(),machine.getBlockPos(),201,0),"Minecraft block events reach the controller for the client completion flash");
            }clear(h,machine);
        }h.succeed();
    }
}
