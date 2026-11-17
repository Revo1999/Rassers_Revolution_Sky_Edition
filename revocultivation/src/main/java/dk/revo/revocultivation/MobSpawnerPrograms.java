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
    public static int tier(ItemStack stack){
        if(program(stack)<0)return 0;
        var data=stack.get(DataComponents.CUSTOM_DATA);
        if(data==null)return 1;var tag=data.copyTag();if(!tag.contains("revo_spawner_tier"))return 1;
        int tier=tag.getInt("revo_spawner_tier");return tier>=1&&tier<=3?tier:0;
    }
    public static ItemStack spawner(int program,int tier){
        String mob=PROGRAMS.entrySet().stream().filter(e->e.getValue()==program).findFirst().orElseThrow().getKey();
        var tag=new net.minecraft.nbt.CompoundTag();tag.putString("id","minecraft:mob_spawner");
        var entity=new net.minecraft.nbt.CompoundTag();entity.putString("id",mob);var spawn=new net.minecraft.nbt.CompoundTag();spawn.put("entity",entity);tag.put("SpawnData",spawn);
        var stack=new ItemStack(Items.SPAWNER);stack.set(DataComponents.BLOCK_ENTITY_DATA,net.minecraft.world.item.component.CustomData.of(tag));return tier==1?stack:upgrade(stack,tier);
    }
    public static int cycleTicks(int tier){return switch(tier){case 2->1200;case 3->400;default->4800;};}
    public static int maxTier(MachineKind kind){return kind==MachineKind.STEEL_HARVESTER?2:kind.isHarvester()?1:0;}
    public static ItemStack upgrade(ItemStack source,int tier){
        var result=source.copyWithCount(1);var old=result.get(DataComponents.CUSTOM_DATA);
        var tag=old==null?new net.minecraft.nbt.CompoundTag():old.copyTag();tag.putInt("revo_spawner_tier",tier);
        result.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
        result.set(DataComponents.CUSTOM_NAME,Component.translatable("spawner.revocultivation.tier."+tier).withStyle(tier==2?ChatFormatting.YELLOW:ChatFormatting.AQUA));return result;
    }
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
        int program=program(stack),tier=tier(stack);return kind.isHarvester()&&tier>0&&tier<=maxTier(kind)&&Arrays.stream(kind.harvesterPrograms()).anyMatch(p->p==program);
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
        int tier=tier(event.getItemStack());if(tier>0){
            event.getToolTip().add(Component.translatable("tooltip.revocultivation.spawner_tier",Component.translatable("spawner.revocultivation.tier."+tier),cycleTicks(tier)/20).withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("tooltip.revocultivation.spawner_machine."+tier).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    private MobSpawnerPrograms(){}
}
