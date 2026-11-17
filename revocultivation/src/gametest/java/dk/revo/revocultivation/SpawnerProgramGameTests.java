package dk.revo.revocultivation;

import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(RevoCultivation.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpawnerProgramGameTests {
    static ItemStack spawner(String mob){
        var tag=new CompoundTag();tag.putString("id","minecraft:mob_spawner");
        var entity=new CompoundTag();entity.putString("id","minecraft:"+mob);
        var spawn=new CompoundTag();spawn.put("entity",entity);tag.put("SpawnData",spawn);
        var stack=new ItemStack(Items.SPAWNER);stack.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(tag));return stack;
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void nativeRecipesKeepMobIdentity(GameTestHelper h){
        var manager=h.getLevel().getRecipeManager();
        for(var entry:MobSpawnerPrograms.PROGRAMS.entrySet()){
            String mob=entry.getKey().substring(10);
            var egg=BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(mob+"_spawn_egg"));
            var eggRecipe=manager.byKey(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"harvester/egg_"+mob));
            h.assertTrue(eggRecipe.isPresent()&&eggRecipe.get().value().getResultItem(h.getLevel().registryAccess()).is(egg),"Craftable vanilla egg: "+mob);
            var grid=new java.util.ArrayList<ItemStack>();for(int i=0;i<9;i++)grid.add(new ItemStack(i==4?egg:Items.IRON_BARS));
            var input=CraftingInput.of(3,3,grid);var recipe=manager.getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
            h.assertTrue(recipe.isPresent(),"Native shaped spawner recipe: "+mob);
            var result=recipe.get().value().assemble(input,h.getLevel().registryAccess());
            h.assertTrue(MobSpawnerPrograms.program(result)==entry.getValue(),"Crafted spawner retains typed mob NBT: "+mob);
            h.assertTrue(MobSpawnerPrograms.accepts(MachineKind.STEEL_HARVESTER,result),"Steel accepts program: "+mob);
        }h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void retainedControllerProgramAndTierGates(GameTestHelper h){
        var m=FarmGameTests.build(h,MachineKind.HARVESTER,Direction.NORTH);
        FarmGameTests.ticks(m,20);h.assertTrue(m.program()==-1&&m.buffered()==0,"No free default program without spawner");
        var port=h.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,m.getBlockPos(),Direction.UP);
        h.assertTrue(port!=null&&port.insertItem(0,spawner("blaze"),false).getCount()==1,"Wooden rejects steel-tier programs through automation");
        h.assertTrue(port.insertItem(0,new ItemStack(Items.ZOMBIE_SPAWN_EGG),false).getCount()==1,"Controller rejects eggs");
        h.assertTrue(port.insertItem(0,spawner("zombie"),false).isEmpty(),"Hopper-compatible controller program insertion");
        FarmGameTests.ticks(m,300);h.assertTrue(m.program()==20&&m.data(11)==1&&MobSpawnerPrograms.program(port.getStackInSlot(0))==20,"Spawner retained across production");
        var saved=m.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored=(FarmMachine)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),saved,h.getLevel().registryAccess());
        h.assertTrue(MobSpawnerPrograms.program(restored.programSpawner.getStackInSlot(0))==20,"Program item survives save/reload");
        port.extractItem(0,1,false);FarmGameTests.ticks(m,20);h.assertTrue(m.program()==-1,"Removing spawner stops program");
        h.assertTrue(MobSpawnerPrograms.selected(MachineKind.STEEL_HARVESTER,spawner("wither_skeleton"),ItemStack.EMPTY)==-1,"Retained Netherite gate remains");
        h.assertTrue(MobSpawnerPrograms.selected(MachineKind.STEEL_HARVESTER,spawner("wither_skeleton"),new ItemStack(Items.NETHERITE_INGOT))==30,"Netherite unlocks existing late program");h.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void placedSpawnerCannotTickOrSpawn(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(3,2,3));h.getLevel().setBlockAndUpdate(pos,Blocks.SPAWNER.defaultBlockState());
        var be=(SpawnerBlockEntity)h.getLevel().getBlockEntity(pos);var tag=spawner("zombie").get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
        tag.putShort("Delay",(short)0);tag.putShort("RequiredPlayerRange",(short)32767);
        be.loadWithComponents(tag,h.getLevel().registryAccess());
        var player=h.makeMockServerPlayerInLevel();player.setPos(pos.getX()+1,pos.getY(),pos.getZ());
        h.assertTrue(h.getLevel().hasNearbyAlivePlayer(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,32767),"Spawner has an activation player; this is not a no-player false positive");
        var before=be.saveWithoutMetadata(h.getLevel().registryAccess());
        for(int i=0;i<1000;i++)be.getSpawner().serverTick(h.getLevel(),pos);
        var after=be.saveWithoutMetadata(h.getLevel().registryAccess());
        player.setPos(0,200,0);player.discard();
        h.assertTrue(before.equals(after),"Actual vanilla spawner tick is fully disabled, including delay and spawn state");h.succeed();
    }
}
