package dk.revo.revocultivation;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/** Vanilla mob-specific spawner items are retained controller programs. */
@EventBusSubscriber(modid=RevoCultivation.MOD_ID)
public final class MobSpawnerPrograms {
    public static final Map<String,Integer> PROGRAMS=Map.ofEntries(
        Map.entry("minecraft:zombie",20),Map.entry("minecraft:skeleton",21),Map.entry("minecraft:spider",22),
        Map.entry("minecraft:cow",23),Map.entry("minecraft:blaze",24),Map.entry("minecraft:slime",25),
        Map.entry("minecraft:enderman",26),Map.entry("minecraft:magma_cube",27),Map.entry("minecraft:ghast",28),
        Map.entry("minecraft:shulker",29),Map.entry("minecraft:wither_skeleton",30));
    public static int program(ItemStack stack){
        if(!stack.is(Items.SPAWNER))return -1;
        var data=stack.get(DataComponents.BLOCK_ENTITY_DATA);if(data==null)return -1;
        var tag=data.copyTag();if(!tag.getString("id").equals("minecraft:mob_spawner"))return -1;
        return PROGRAMS.getOrDefault(tag.getCompound("SpawnData").getCompound("entity").getString("id"),-1);
    }
    public static boolean accepts(MachineKind kind,ItemStack stack){
        int program=program(stack);return kind.isHarvester()&&Arrays.stream(kind.harvesterPrograms()).anyMatch(p->p==program);
    }
    public static int selected(MachineKind kind,ItemStack spawner,ItemStack retainedStarter){
        if(!accepts(kind,spawner))return -1;int program=program(spawner);
        return (program==29||program==30)&&!retainedStarter.is(Items.NETHERITE_INGOT)?-1:program;
    }
    public static void disableEggDispensing(){
        for(var egg:SpawnEggItem.eggs())DispenserBlock.registerBehavior(egg,new DefaultDispenseItemBehavior());
    }
    private static boolean egg(ItemStack stack){return stack.getItem() instanceof SpawnEggItem;}
    @SubscribeEvent public static void blockEgg(PlayerInteractEvent.RightClickBlock event){
        if(egg(event.getItemStack())){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);}
    }
    @SubscribeEvent public static void airEgg(PlayerInteractEvent.RightClickItem event){
        if(egg(event.getItemStack())){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);}
    }
    @SubscribeEvent public static void entityEgg(PlayerInteractEvent.EntityInteract event){
        if(egg(event.getItemStack())){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);}
    }
    @SubscribeEvent public static void entitySpecificEgg(PlayerInteractEvent.EntityInteractSpecific event){
        if(egg(event.getItemStack())){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);}
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event){
        if(egg(event.getItemStack()))event.getToolTip().add(Component.translatable("tooltip.revocultivation.egg_ingredient").withStyle(ChatFormatting.GRAY));
        int program=program(event.getItemStack());if(program>=0)event.getToolTip().add(Component.translatable("tooltip.revocultivation.spawner_program",Component.translatable("program.revocultivation."+MachineKind.programName(program))).withStyle(ChatFormatting.AQUA));
    }
    private MobSpawnerPrograms(){}
}
