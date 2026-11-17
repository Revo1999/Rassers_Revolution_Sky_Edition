package dk.revo.revocultivation;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(RevoCultivation.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RunningShoesGameTests {
    @GameTest(template="farm_test",timeoutTicks=120)
    public static void speedFallProtectionAndRemoval(GameTestHelper helper){
        var entity=helper.spawn(EntityType.ZOMBIE,new BlockPos(3,2,3));entity.setNoAi(true);entity.setInvulnerable(false);
        entity.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
        double base=entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
        entity.setItemSlot(EquipmentSlot.FEET,new ItemStack(RunningShoes.SHOES.get("blue").get()));
        helper.runAfterDelay(3,()->{
            helper.assertTrue(Math.abs(entity.getAttributeValue(Attributes.MOVEMENT_SPEED)-base*1.25)<1e-7,"Shoes give exactly 25 percent movement speed");
            helper.assertTrue(entity.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER)==0,"Equipped shoes zero fall damage multiplier");
            float health=entity.getHealth();entity.causeFallDamage(12,1,entity.damageSources().fall());
            helper.assertTrue(entity.getHealth()==health,"Real fall damage call must not hurt equipped wearer");
            entity.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(RunningShoes.SHOES.get("red").get()));
            helper.runAfterDelay(3,()->{
                helper.assertTrue(Math.abs(entity.getAttributeValue(Attributes.MOVEMENT_SPEED)-base)<1e-7,"Removing shoes removes movement bonus");
                helper.assertTrue(entity.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER)==1,"Removing shoes restores fall multiplier");
                entity.invulnerableTime=0;entity.causeFallDamage(6,1,entity.damageSources().fall());
                helper.assertTrue(entity.getHealth()<health,"Falls hurt again after removal");helper.succeed();
            });
        });
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void allColourRecipesCraftInNativeManager(GameTestHelper helper){
        for(String color:RunningShoes.COLORS){
            var ingredients=new java.util.ArrayList<ItemStack>();
            for(int i=0;i<4;i++)ingredients.add(new ItemStack(RunningShoes.COTTON.get()));
            for(int i=0;i<2;i++)ingredients.add(new ItemStack(RunningShoes.RUBBER.get()));
            ingredients.add(new ItemStack(RunningShoes.GLUE.get()));ingredients.add(new ItemStack(Items.STRING));
            ingredients.add(color.equals("white")?ItemStack.EMPTY:new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(color+"_dye"))));
            var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,ingredients);
            var recipe=helper.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,helper.getLevel());
            helper.assertTrue(recipe.isPresent(),"Native crafting manager finds "+color+" recipe");
            var result=recipe.get().value().assemble(input,helper.getLevel().registryAccess());
            helper.assertTrue(result.is(RunningShoes.SHOES.get(color).get())&&result.getCount()==1,"Correct shoe colour and yield: "+color);
        }helper.succeed();
    }
    @GameTest(template="farm_test",timeoutTicks=100)
    public static void coloursInventoryAndCotton(GameTestHelper helper){
        helper.assertTrue(RunningShoes.SHOES.size()==12,"Exactly the twelve selected colours");
        for(var item:RunningShoes.SHOES.values()){
            var stack=new ItemStack(item.get());helper.assertTrue(stack.getMaxStackSize()==1,"Shoes are unstackable");
            helper.assertTrue(item.get().getEquipmentSlot()==EquipmentSlot.FEET,"Shoes equip as boots");
        }
        var starter=new ItemStack(RunningShoes.SEEDS.get());
        helper.assertTrue(MachineKind.GREENHOUSE.acceptsStarter(starter)&&MachineKind.IRON_GREENHOUSE.acceptsStarter(starter),"Both greenhouses accept cotton");
        helper.assertTrue(MachineKind.GREENHOUSE.program(starter,0)==17,"Cotton appends program 17 without changing existing IDs");
        helper.assertTrue(MachineKind.products(17).getFirst().is(RunningShoes.COTTON.get()),"Cotton harvest produces cotton");
        var pos=helper.absolutePos(new BlockPos(5,2,5));helper.getLevel().setBlockAndUpdate(pos.below(),Blocks.FARMLAND.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos,RunningShoes.CROP.get().getStateForAge(7));
        var drops=net.minecraft.world.level.block.Block.getDrops(helper.getLevel().getBlockState(pos),helper.getLevel(),pos,null);
        helper.assertTrue(drops.stream().anyMatch(s->s.is(RunningShoes.COTTON.get())),"Mature crop drops cotton");
        helper.assertTrue(drops.stream().filter(s->s.is(RunningShoes.SEEDS.get())).mapToInt(ItemStack::getCount).sum()==2,"Mature crop yields two renewable seeds");helper.succeed();
    }
}
