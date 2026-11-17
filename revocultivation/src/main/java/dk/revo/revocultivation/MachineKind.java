package dk.revo.revocultivation;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum MachineKind {
    TREE("tree_grower",1,800,0,0), GREENHOUSE("greenhouse",3,400,1,0), HARVESTER("mob_harvester",3,4800,2,0),
    STEEL_TREE("steel_tree_grower",1,400,0,2), STEEL_HARVESTER("steel_mob_harvester",3,4800,2,2), IRON_GREENHOUSE("iron_greenhouse",3,200,1,1);
    public enum Part { SUPPORT, CASING, CONTROLLER, HATCH, BED, GRATE, GLASS, BARS, FILTER, AIR, POWER, LIGHT, TANK, SOIL }
    public final String id;public final int height,ticks,metal,family;
    private Supplier<? extends Block> controller;
    MachineKind(String id,int height,int ticks,int family,int metal){this.id=id;this.height=height;this.ticks=ticks;this.family=family;this.metal=metal;}
    public boolean isTree(){return family==0;}public boolean isGreenhouse(){return family==1;}public boolean isHarvester(){return family==2;}
    public boolean electric(){return this==IRON_GREENHOUSE;}
    public boolean needsDaylight(){return !isHarvester()&&!electric();}
    public int radius(){return attachedTank()?2:1;}
    public int hatchZ(){return electric()?1:0;}
    public int width(){return radius()*2+1;}
    public boolean realTree(){return this==STEEL_TREE;}
    public boolean attachedTank(){return electric()||realTree();}
    public boolean needsWater(){return isGreenhouse()||realTree();}
    public int waterPerCycle(){return realTree()?500:250;}
    public int waterCapacity(){return attachedTank()?16000:8000;}
    public int energyPerTick(){return electric()?8:0;}
    public static String seconds(int ticks){return ticks%20==0?Integer.toString(ticks/20):String.format(java.util.Locale.ROOT,"%.1f",ticks/20d);}
    public Component tierText(){return Component.translatable("gui.revocultivation.tier."+(electric()?"electric":metal==2?"steel":"wooden"));}
    public void bind(Supplier<? extends Block> block){controller=block;}
    public Block controller(){return controller.get();}
    public boolean registered(){return controller!=null;}
    public Component title(){return Component.translatable("machine.revocultivation."+id);}
    public int inputSlots(){return needsWater()?2:isTree()||this==STEEL_HARVESTER?1:0;}
    public int hatchHeight(){return isHarvester()&&this!=STEEL_HARVESTER?184:220;}
    public Part part(int x,int y,int z){
        if(Math.abs(x)>radius()||Math.abs(z)>radius()||y<0||y>=height)throw new IllegalArgumentException("Outside machine blueprint");
        if(realTree()){
            if(Math.abs(x)==2&&Math.abs(z)==2)return Part.SUPPORT;
            if(x==0&&z==-2)return Part.CONTROLLER;
            if(x==2&&z==0)return Part.HATCH;
            if(x==-2&&z==0)return Part.TANK;
            if(x==0&&z==0)return Part.SOIL;
            return Math.abs(x)<2&&Math.abs(z)<2?Part.BED:Part.CASING;
        }
        if(electric()){
            if(x==2&&y==0&&z==2)return Part.POWER;
            if(x==-2&&y==0&&z==0)return Part.TANK;
            if(Math.abs(x)==2&&Math.abs(z)==2)return Part.SUPPORT;
            if(y==0){
                if(x==0&&z==-2)return Part.CONTROLLER;
                if(x==2&&z==1)return Part.HATCH;
                return Math.abs(x)<2&&Math.abs(z)<2?Part.BED:Part.CASING;
            }
            if(y==1)return Math.abs(x)<2&&Math.abs(z)<2?Part.AIR:Part.GLASS;
            if(Math.abs(x)<2&&Math.abs(z)<2)return x==0&&z==0?Part.LIGHT:Part.GLASS;
            return Part.CASING;
        }
        if(Math.abs(x)==1&&Math.abs(z)==1)return Part.SUPPORT;
        if(y==0){if(x==0&&z==-1)return Part.CONTROLLER;if(x==1&&z==0)return Part.HATCH;if(x==0&&z==0)return isHarvester()?Part.GRATE:Part.BED;return Part.CASING;}
        if(isGreenhouse())return y==1?(x==0&&z==0?Part.AIR:Part.GLASS):(x==0&&z==0?Part.GLASS:Part.CASING);
        if(y==1){if(x==0&&z==0)return Part.AIR;if(x==0&&z==-1)return Part.FILTER;return Part.BARS;}
        return Part.CASING;
    }
    public Block block(Part part){
        if(part==Part.POWER)return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("revoindustry:power_converter"));
        if(part==Part.LIGHT)return FarmMaterials.LIGHT.get();
        if(part==Part.SOIL)return Blocks.DIRT;
        if(metal>0&&part!=Part.CONTROLLER&&part!=Part.AIR)return FarmMaterials.block(this,part);
        return switch(part){
        case SUPPORT->RevoCultivation.SUPPORT.get();case CASING->RevoCultivation.CASING.get();case CONTROLLER->controller();case HATCH->RevoCultivation.HATCH.get();
        case BED->RevoCultivation.BED.get();case GRATE->RevoCultivation.GRATE.get();case GLASS->RevoCultivation.GLASS.get();case BARS->RevoCultivation.BARS.get();case FILTER->RevoCultivation.FILTER.get();case AIR,POWER,LIGHT,TANK,SOIL->Blocks.AIR;
    };}
    public boolean acceptsStarter(ItemStack stack){return this==STEEL_HARVESTER?stack.is(Items.NETHERITE_INGOT):isTree()?stack.is(Items.OAK_SAPLING):isGreenhouse()&&(stack.is(Items.WHEAT_SEEDS)||stack.is(Items.CARROT)||stack.is(Items.POTATO)||stack.is(Items.BEETROOT_SEEDS)||stack.is(Items.SUGAR_CANE)||stack.is(Items.KELP)||stack.is(Items.CACTUS)||stack.is(Items.POPPY)||stack.is(RunningShoes.SEEDS.get())||electric()&&stack.is(SmallMachines.SALT_STARTER.get())||electric()&&(stack.is(Items.NETHER_WART)||stack.is(Items.CHORUS_FRUIT)));}
    public int[] harvesterPrograms(){return this==STEEL_HARVESTER?new int[]{20,21,22,23,24,25,26,27,28,29,30}:new int[]{20,21,22,23,25};}
    public int program(ItemStack starter,int filter){
        if(isTree())return starter.is(Items.OAK_SAPLING)?0:-1;
        if(isHarvester()&&filter>=9&&!starter.is(Items.NETHERITE_INGOT))return -1;
        if(isHarvester())return filter>=0&&filter<harvesterPrograms().length?harvesterPrograms()[filter]:-1;
        return starter.is(Items.WHEAT_SEEDS)?10:starter.is(Items.CARROT)?11:starter.is(Items.POTATO)?12:starter.is(Items.BEETROOT_SEEDS)?13:starter.is(Items.SUGAR_CANE)?14:starter.is(Items.KELP)?15:starter.is(Items.CACTUS)?16:electric()&&starter.is(Items.NETHER_WART)?17:electric()&&starter.is(Items.CHORUS_FRUIT)?18:electric()&&starter.is(SmallMachines.SALT_STARTER.get())?19:starter.is(Items.POPPY)?31:starter.is(RunningShoes.SEEDS.get())?32:-1;
    }
    public static String programName(int program){return switch(program){case 0->"oak";case 10->"wheat";case 11->"carrot";case 12->"potato";case 13->"beetroot";case 14->"sugar_cane";case 15->"kelp";case 16->"cactus";case 17->"nether_wart";case 18->"chorus";case 19->"salt_crystal";case 28->"ghast";case 29->"shulker";case 30->"wither_skeleton";case 31->"poppy";case 32->"cotton";case 25->"slime";case 26->"enderman";case 27->"magma_cube";case 20->"zombie";case 21->"skeleton";case 22->"spider";case 23->"cow";case 24->"blaze";default->"none";};}
    public static List<ItemStack> products(int program){return switch(program){
        case 0->List.of(new ItemStack(Items.OAK_LOG,6),new ItemStack(Items.OAK_LEAVES,2),new ItemStack(Items.STICK,2),new ItemStack(Items.OAK_SAPLING));
        case 10->List.of(new ItemStack(Items.WHEAT,3),new ItemStack(Items.WHEAT_SEEDS));case 11->List.of(new ItemStack(Items.CARROT,3));case 12->List.of(new ItemStack(Items.POTATO,3));case 13->List.of(new ItemStack(Items.BEETROOT,3),new ItemStack(Items.BEETROOT_SEEDS));
        case 32->List.of(new ItemStack(RunningShoes.COTTON.get(),3),new ItemStack(RunningShoes.SEEDS.get()));case 17->List.of(new ItemStack(Items.NETHER_WART,3));case 18->List.of(new ItemStack(Items.CHORUS_FRUIT,3));case 19->List.of(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("modern_industrialization:salt_dust"))));case 28->List.of(new ItemStack(Items.GHAST_TEAR));case 29->List.of(new ItemStack(Items.SHULKER_SHELL));case 30->List.of(new ItemStack(Items.BONE,2),new ItemStack(Items.COAL),new ItemStack(Items.WITHER_SKELETON_SKULL));case 31->List.of(new ItemStack(Items.POPPY,3));case 14->List.of(new ItemStack(Items.SUGAR_CANE,3));case 15->List.of(new ItemStack(Items.KELP,3));case 16->List.of(new ItemStack(Items.CACTUS,3));case 25->List.of(new ItemStack(Items.SLIME_BALL));case 26->List.of(new ItemStack(Items.ENDER_PEARL));case 27->List.of(new ItemStack(Items.MAGMA_CREAM));case 20->List.of(new ItemStack(Items.ROTTEN_FLESH,2),new ItemStack(Items.CARROT),new ItemStack(Items.POTATO));
        case 21->List.of(new ItemStack(Items.BONE,2),new ItemStack(Items.ARROW));case 22->List.of(new ItemStack(Items.STRING,2),new ItemStack(Items.SPIDER_EYE));case 23->List.of(new ItemStack(Items.LEATHER),new ItemStack(Items.BEEF,2));case 24->List.of(new ItemStack(Items.BLAZE_ROD));default->List.of();
    };}
    public static List<ItemStack> products(int program,int cycles){return program==30&&Math.floorMod(cycles,64)!=63?List.of(new ItemStack(Items.BONE,2),new ItemStack(Items.COAL)):products(program);}
    public static ItemStack starter(int program){return new ItemStack(switch(program){case 0->Items.OAK_SAPLING;case 10->Items.WHEAT_SEEDS;case 11->Items.CARROT;case 12->Items.POTATO;case 13->Items.BEETROOT_SEEDS;case 14->Items.SUGAR_CANE;case 15->Items.KELP;case 16->Items.CACTUS;case 17->Items.NETHER_WART;case 18->Items.CHORUS_FRUIT;case 19->SmallMachines.SALT_STARTER.get();case 31->Items.POPPY;case 32->RunningShoes.SEEDS.get();default->Items.AIR;});}
}
