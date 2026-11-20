package dk.revo.revoextraction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

/** One tier definition drives formation, production, Create load and the construction guide. */
public enum DrillTier {
    WOODEN(1200,0,4), IRON(600,32,8), STEEL(600,64,16);
    public final int workPerItem, minimumRpm, stress;
    DrillTier(int work,int rpm,int stress){this.workPerItem=work;this.minimumRpm=rpm;this.stress=stress;}
    public Block block(DrillLayout.Part part){
        boolean iron=this==IRON;
        if(this==STEEL)return switch(part){
            case LOG->RevoExtraction.STEEL_SUPPORT.get();case CASING->RevoExtraction.STEEL_CASING.get();
            case CORE->RevoExtraction.STEEL_CORE.get();case CONTROLLER->RevoExtraction.STEEL_CONTROLLER.get();
            case INPUT->RevoExtraction.STEEL_INPUT.get();case HATCH->RevoExtraction.STEEL_HATCH.get();
            case TANK->RevoExtraction.STEEL_TANK.get();case FLUID_HATCH->RevoExtraction.STEEL_FLUID_HATCH.get();case AIR->net.minecraft.world.level.block.Blocks.AIR;
        };
        return switch(part){
            case LOG -> (iron?RevoExtraction.IRON_SUPPORT:RevoExtraction.SUPPORT).get();
            case CASING -> (iron?RevoExtraction.IRON_CASING:RevoExtraction.CASING).get();
            case CORE -> (iron?RevoExtraction.IRON_CORE:RevoExtraction.CORE).get();
            case CONTROLLER -> (iron?RevoExtraction.IRON_CONTROLLER:RevoExtraction.CONTROLLER).get();
            case INPUT -> (iron?RevoExtraction.IRON_INPUT:RevoExtraction.INPUT).get();
            case HATCH -> (iron?RevoExtraction.IRON_HATCH:RevoExtraction.HATCH).get();
            case TANK -> RevoExtraction.STEEL_TANK.get();
            case FLUID_HATCH -> RevoExtraction.STEEL_FLUID_HATCH.get();
            case AIR -> net.minecraft.world.level.block.Blocks.AIR;
        };
    }
    private static Item optional(String id){
        Item item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        return item==Items.AIR?Items.RAW_IRON:item;
    }
    public static final int FLUX_INTERVAL=128;
    private static final int GEM_SLUDGE_INTERVAL=32;
    private volatile Item[] cachedOutputs;
    public Item output(int cycle){
        if(this!=WOODEN&&Math.floorMod(cycle,FLUX_INTERVAL)==FLUX_INTERVAL-1){var flux=BuiltInRegistries.ITEM.get(ResourceLocation.parse("fluxnetworks:flux_dust"));if(flux!=Items.AIR)return flux;}
        if(this==STEEL&&Math.floorMod(cycle,GEM_SLUDGE_INTERVAL)==8)return RevoExtraction.GEM_SLUDGE.get();
        // Fixed sequence is saved by the controller: blocked outputs cannot be rerolled.
        Item[] pool=cachedOutputs;
        if(pool==null)cachedOutputs=pool=this==STEEL?new Item[]{Items.RAW_IRON,Items.RAW_COPPER,optional("modern_industrialization:raw_tin"),Items.COAL,
            Items.REDSTONE,Items.RAW_IRON,optional("create:raw_zinc"),Items.RAW_GOLD,Items.GRAVEL,Items.RAW_IRON,Items.GRAVEL,Items.COAL,
            optional("modern_industrialization:raw_lead"),optional("modern_industrialization:raw_antimony"),RevoExtraction.MINERAL_SLUDGE.get(),optional("modern_industrialization:raw_lead")}:this==WOODEN
            ?new Item[]{Items.COBBLESTONE,Items.COBBLESTONE,Items.GRAVEL,Items.SAND,Items.RAW_IRON}
            :new Item[]{Items.RAW_IRON,Items.RAW_COPPER,optional("modern_industrialization:raw_tin"),Items.COAL,
                Items.RAW_IRON,Items.RAW_COPPER,Items.COAL,optional("modern_industrialization:raw_tin"),
                Items.RAW_IRON,optional("create:raw_zinc"),Items.RAW_GOLD,Items.REDSTONE,RevoExtraction.MINERAL_SLUDGE.get()};
        return pool[Math.floorMod(cycle,pool.length)];
    }
    public DrillLayout.Part part(int x,int y,int z){return this==STEEL&&x==0&&y==2&&z==-1?DrillLayout.Part.FLUID_HATCH:this==STEEL&&x==-1&&y==1&&z==0?DrillLayout.Part.TANK:DrillLayout.part(x,y,z);}
    public String displayName(){return this==STEEL?"Steel":this==IRON?"Iron":"Wooden";}
    public String prefix(){return name().toLowerCase(java.util.Locale.ROOT)+"_drill";}
}
