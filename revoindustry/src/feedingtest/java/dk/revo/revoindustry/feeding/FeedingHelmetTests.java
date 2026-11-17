package dk.revo.revoindustry.feeding;

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoindustry") @PrefixGameTestTemplate(false)
public final class FeedingHelmetTests {
    static ServerPlayer player(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel(); p.setGameMode(GameType.SURVIVAL);
        p.getAbilities().instabuild = false; p.getInventory().clearContent();
        return p;
    }
    static ItemStack wear(ServerPlayer p, int tier, ItemStack... food) {
        var helmet = new ItemStack(switch(tier) { case 0 -> FeedingHelmets.BASIC.get(); case 1 -> FeedingHelmets.ADVANCED.get(); default -> FeedingHelmets.ELITE.get(); });
        var items = FeedingHelmets.contents(helmet, tier);
        for (int i = 0; i < food.length; i++) items.set(i, food[i]);
        FeedingHelmets.save(helmet, items); p.setItemSlot(EquipmentSlot.HEAD, helmet); return helmet;
    }
    static void hunger(ServerPlayer p, int level) { p.getFoodData().setFoodLevel(level); p.getFoodData().setSaturation(0); }
    static void check(GameTestHelper h, boolean condition, String message) { h.assertTrue(condition, message); }

    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void exactHalfDrumstickThreshold(GameTestHelper h) {
        var p=player(h); var helmet=wear(p,0,new ItemStack(Items.CARROT,3));
        hunger(p,18);check(h,!FeedingHelmets.feed(p,helmet),"A 1.5-drumstick food must wait when only one drumstick is missing");
        check(h,FeedingHelmets.contents(helmet,0).get(0).getCount()==3,"Waiting does not consume food");
        hunger(p,17);check(h,FeedingHelmets.feed(p,helmet),"Eat at exactly three missing half-points");
        check(h,p.getFoodData().getFoodLevel()==20&&FeedingHelmets.contents(helmet,0).get(0).getCount()==2,"Restore exactly 1.5 drumsticks and consume one carrot");h.succeed();
    }
    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void chooseLargestFittingFoodAndIgnoreWaste(GameTestHelper h) {
        var p=player(h);var helmet=wear(p,2,new ItemStack(Items.BREAD,2),new ItemStack(Items.CARROT,2),new ItemStack(Items.APPLE,2));
        hunger(p,16);check(h,FeedingHelmets.chooseFood(FeedingHelmets.contents(helmet,2),p)==2,"Choose the four-point apple instead of wasting a five-point bread");
        hunger(p,15);check(h,FeedingHelmets.chooseFood(FeedingHelmets.contents(helmet,2),p)==0,"Choose the largest fitting five-point meal");
        hunger(p,19);check(h,FeedingHelmets.chooseFood(FeedingHelmets.contents(helmet,2),p)==-1,"Never eat a portion larger than missing hunger");
        hunger(p,20);check(h,!FeedingHelmets.feed(p,helmet),"Never feed a full player");h.succeed();
    }
    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void capacitiesFoodFilteringAndComponentRoundTrip(GameTestHelper h) {
        var p=player(h);
        for(int tier=0;tier<3;tier++){
            var helmet=wear(p,tier);var items=FeedingHelmets.contents(helmet,tier);
            check(h,items.size()==new int[]{1,4,25}[tier],"Exact tier capacity");
            for(int i=0;i<items.size();i++)items.set(i,new ItemStack(Items.APPLE,i+1));FeedingHelmets.save(helmet,items);
            var restored=ItemStack.parseOptional(h.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)helmet.save(h.getLevel().registryAccess()));
            var loaded=FeedingHelmets.contents(restored,tier);
            for(int i=0;i<loaded.size();i++)check(h,loaded.get(i).is(Items.APPLE)&&loaded.get(i).getCount()==i+1,"Every slot survives item save/load");
        }
        check(h,!FeedingHelmets.accepts(new ItemStack(Items.STICK),p)&&!FeedingHelmets.accepts(new ItemStack(FeedingHelmets.BASIC.get()),p),"Reject non-food and nested helmets");
        var dynamic=new ItemStack(Items.PAPER);dynamic.set(DataComponents.FOOD,new FoodProperties.Builder().nutrition(3).saturationModifier(.6f).build());
        check(h,FeedingHelmets.accepts(dynamic,p),"Read stack-sensitive food components, including modded food");h.succeed();
    }
    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void onlyEquippedHelmetAndNormalTickCadence(GameTestHelper h) {
        var p=player(h);var helmet=wear(p,0,new ItemStack(Items.APPLE,4));hunger(p,16);
        p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);p.getInventory().setItem(0,helmet);p.tickCount=20;
        helmet.getItem().inventoryTick(helmet,p.level(),p,0,true);check(h,p.getFoodData().getFoodLevel()==16,"Held helmets do not feed");
        p.getInventory().setItem(0,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.HEAD,helmet);p.tickCount=19;
        helmet.getItem().inventoryTick(helmet,p.level(),p,39,false);check(h,p.getFoodData().getFoodLevel()==16,"Wait for feeding cadence");
        p.tickCount=20;helmet.getItem().inventoryTick(helmet,p.level(),p,39,false);
        check(h,p.getFoodData().getFoodLevel()==20&&FeedingHelmets.contents(helmet,0).get(0).getCount()==3,"Worn helmet feeds on a real inventory tick");h.succeed();
    }
    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void normalEffectsSaturationAndContainerReturns(GameTestHelper h) {
        var p=player(h);var helmet=wear(p,0,new ItemStack(Items.MUSHROOM_STEW));hunger(p,14);
        check(h,FeedingHelmets.feed(p,helmet)&&p.getFoodData().getFoodLevel()==20,"Soup supplies its ordinary hunger");
        check(h,p.getInventory().countItem(Items.BOWL)==1&&FeedingHelmets.contents(helmet,0).get(0).isEmpty(),"Soup leaves exactly one bowl in player inventory");
        helmet=wear(p,0,new ItemStack(Items.GOLDEN_APPLE));hunger(p,16);FeedingHelmets.feed(p,helmet);
        check(h,p.hasEffect(MobEffects.ABSORPTION)&&p.hasEffect(MobEffects.REGENERATION),"Normal beneficial food effects are retained");
        check(h,p.getFoodData().getSaturationLevel()>0,"Normal saturation is retained");h.succeed();
    }
    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void menuPersistenceShiftClickAndHelmetSwapLock(GameTestHelper h) {
        var p=player(h);var helmet=wear(p,2);p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);p.getInventory().setItem(0,helmet);
        var menu=new FeedingHelmetMenu(1,p.getInventory(),0,2);
        check(h,menu.slots.size()==61&&menu.width==5,"Five by five slots plus complete player inventory");
        check(h,!menu.slots.get(0).mayPlace(new ItemStack(Items.STICK))&&menu.slots.get(0).mayPlace(new ItemStack(Items.APPLE)),"Food-only menu slots");
        menu.slots.get(0).set(new ItemStack(Items.APPLE,8));check(h,FeedingHelmets.contents(helmet,2).get(0).getCount()==8,"Each edit is persisted immediately");
        menu.clicked(menu.foodSlots,0,ClickType.SWAP,p);check(h,p.getInventory().getItem(0)==helmet,"Number-key swap cannot move the bound helmet through another slot");
        check(h,!menu.slots.get(52).mayPickup(p),"Direct pickup of the helmet is locked");
        menu.quickMoveStack(p,0);check(h,FeedingHelmets.contents(helmet,2).get(0).isEmpty()&&p.getInventory().countItem(Items.APPLE)==8,"Shift-click returns all food exactly once");
        p.getInventory().setItem(0,helmet.copy());check(h,!menu.stillValid(p),"Even an identical replacement closes the old binding");h.succeed();
    }
    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void editingTheWornHelmetPausesFeeding(GameTestHelper h) {
        var p=player(h);var helmet=wear(p,0,new ItemStack(Items.APPLE,2));hunger(p,16);
        var menu=new FeedingHelmetMenu(1,p.getInventory(),39,0);p.containerMenu=menu;
        check(h,!FeedingHelmets.feed(p,helmet)&&FeedingHelmets.contents(helmet,0).get(0).getCount()==2,"An open editor cannot race auto-feeding");
        p.containerMenu=p.inventoryMenu;check(h,FeedingHelmets.feed(p,helmet),"Feeding resumes after editing");h.succeed();
    }
    @GameTest(template="feeding_test", timeoutTicks=100)
    public static void recipesAndThreeArmorItemsLoad(GameTestHelper h) {
        for(var id:FeedingHelmets.IDS)check(h,h.getLevel().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("revoindustry",id)).isPresent(),"Craftable tier: "+id);
        check(h,FeedingHelmets.BASIC.get().getEquipmentSlot()==EquipmentSlot.HEAD,"Real armor slot");h.succeed();
    }
}
