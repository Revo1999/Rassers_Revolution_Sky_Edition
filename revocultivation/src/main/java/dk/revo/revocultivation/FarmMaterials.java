package dk.revo.revocultivation;

import java.util.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Material sets are shared by their metal-tier farms; wooden registrations remain intact. */
public final class FarmMaterials {
    public static final Map<MachineKind.Part,DeferredBlock<? extends Block>> STEEL=register("steel",true),IRON=register("iron",false);
    public static final DeferredBlock<FarmGrowLight> LIGHT=RevoCultivation.BLOCKS.register("iron_grow_light",FarmGrowLight::new);
    private static Map<MachineKind.Part,DeferredBlock<? extends Block>> register(String tier,boolean steel){
        var parts=new EnumMap<MachineKind.Part,DeferredBlock<? extends Block>>(MachineKind.Part.class);
        for(var p:new MachineKind.Part[]{MachineKind.Part.SUPPORT,MachineKind.Part.CASING,MachineKind.Part.BED,steel?MachineKind.Part.GRATE:MachineKind.Part.GLASS}){
            String suffix=switch(p){case SUPPORT->"support";case CASING->"casing";case BED->"soil_bed";case GRATE->"collection_grate";default->"glass_panel";};
            parts.put(p,RevoCultivation.BLOCKS.register(tier+"_"+suffix,()->p==MachineKind.Part.GLASS?new FarmGlass():new FarmPart(Block.Properties.of().strength(steel?5:4).noOcclusion().sound(SoundType.METAL))));
        }
        parts.put(MachineKind.Part.HATCH,RevoCultivation.BLOCKS.register(tier+"_item_hatch",FarmHatch::new));
        parts.put(MachineKind.Part.TANK,RevoCultivation.BLOCKS.register(tier+"_water_tank",FarmWaterTank::new));
        if(steel){parts.put(MachineKind.Part.BARS,RevoCultivation.BLOCKS.register("steel_barred_panel",()->new FarmPart(Block.Properties.of().strength(5).noOcclusion().sound(SoundType.METAL))));parts.put(MachineKind.Part.FILTER,RevoCultivation.BLOCKS.register("steel_mob_filter",FarmFilter::new));}
        return parts;
    }
    public static Block block(MachineKind kind,MachineKind.Part part){return (kind.metal==2?STEEL:IRON).get(part).get();}
    public static Block[] hatches(){return new Block[]{RevoCultivation.HATCH.get(),STEEL.get(MachineKind.Part.HATCH).get(),IRON.get(MachineKind.Part.HATCH).get()};}
    public static void bootstrap(){}
    private FarmMaterials(){}
}
