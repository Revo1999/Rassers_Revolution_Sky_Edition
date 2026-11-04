package dk.revo.revocultivation;
import java.util.*;
import dk.revo.revolib.client.guide.MultiblockGuide;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
/** Shared blueprint-derived guide data; has no dependency on EMI or client renderers. */
public final class FarmGuideDefinition {
    public static MultiblockGuide create(MachineKind kind){
        List<MultiblockGuide.Part> parts=new ArrayList<>();
        for(int y=0;y<kind.height;y++)for(int z=-kind.radius();z<=kind.radius();z++)for(int x=-kind.radius();x<=kind.radius();x++){
            var part=kind.part(x,y,z);var state=kind.block(part).defaultBlockState();
            if(part==MachineKind.Part.CONTROLLER)state=state.setValue(FarmController.FACING,Direction.NORTH);
            if(part==MachineKind.Part.HATCH)state=state.setValue(FarmHatch.FACING,Direction.EAST);
            if(part==MachineKind.Part.FILTER)state=state.setValue(FarmFilter.FACING,Direction.NORTH);
            if(part==MachineKind.Part.POWER)state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,Direction.SOUTH);
            var role=part==MachineKind.Part.CONTROLLER?MultiblockGuide.Role.CONTROLLER:part==MachineKind.Part.HATCH?MultiblockGuide.Role.INPUT_OUTPUT:part==MachineKind.Part.TANK?MultiblockGuide.Role.FLUID_INPUT:part==MachineKind.Part.POWER?MultiblockGuide.Role.ELECTRIC_INPUT:MultiblockGuide.Role.BLOCK;
            parts.add(new MultiblockGuide.Part(new BlockPos(x,y,z+kind.radius()),state,role));
        }
        List<Component> steps=kind.isTree()?List.of(Component.translatable("guide.revocultivation.tree")):List.of(Component.translatable("guide.revocultivation.base"),Component.translatable("guide.revocultivation."+(kind.isGreenhouse()?"glass":"bars")),Component.translatable("guide.revocultivation.top"));
        if(kind.realTree())steps=List.of(Component.translatable("guide.revocultivation.steel_tree"));
        if(kind.electric())steps=List.of(Component.translatable("guide.revocultivation.iron_base"),Component.translatable("guide.revocultivation.iron_glass"),Component.translatable("guide.revocultivation.iron_lights"));
        return new MultiblockGuide(kind.title(),parts,steps,kind.electric()?Component.literal("8 EU/t ("+8*dk.revo.revolib.energy.EnergyInterop.fePerEu()+" FE/t via converter) | 250 mB/cycle | day/night"):Component.translatable(kind.isHarvester()?"gui.revocultivation.passive":"gui.revocultivation.daylight"));
    }
}
