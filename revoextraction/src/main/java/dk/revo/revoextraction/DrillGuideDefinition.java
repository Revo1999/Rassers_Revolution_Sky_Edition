package dk.revo.revoextraction;
import java.util.*;
import dk.revo.revolib.client.guide.MultiblockGuide;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
/** Shared guide data is deliberately independent of the optional EMI/client renderer classes. */
final class DrillGuideDefinition {
    /** Uses exactly the server's formation blueprint. Canonical front is south (+Z). */
    static MultiblockGuide create(){return create(DrillTier.WOODEN);}
    static MultiblockGuide create(DrillTier tier){
        List<MultiblockGuide.Part> parts=new ArrayList<>();
        for(int y=0;y<3;y++)for(int z=-1;z<=1;z++)for(int x=-1;x<=1;x++){
            var part=tier.part(x,y,z);
            var state=switch(part){
                case LOG->tier.block(DrillLayout.Part.LOG).defaultBlockState();case CASING->tier.block(DrillLayout.Part.CASING).defaultBlockState();
                case CORE->tier.block(DrillLayout.Part.CORE).defaultBlockState();case AIR->Blocks.AIR.defaultBlockState();
                case CONTROLLER->tier.block(DrillLayout.Part.CONTROLLER).defaultBlockState().setValue(DrillController.FACING,Direction.SOUTH);
                case INPUT->tier.block(DrillLayout.Part.INPUT).defaultBlockState().setValue(DrillInput.FACING,Direction.WEST);
                case TANK->RevoExtraction.STEEL_TANK.get().defaultBlockState();
                case FLUID_HATCH->RevoExtraction.STEEL_FLUID_HATCH.get().defaultBlockState();
                case HATCH->tier.block(DrillLayout.Part.HATCH).defaultBlockState().setValue(DrillHatch.FACING,Direction.EAST);
            };
            var role=switch(part){case CONTROLLER->MultiblockGuide.Role.CONTROLLER;case HATCH,FLUID_HATCH->MultiblockGuide.Role.OUTPUT;case INPUT->MultiblockGuide.Role.POWER;case TANK->MultiblockGuide.Role.FLUID_INPUT;default->MultiblockGuide.Role.BLOCK;};
            parts.add(new MultiblockGuide.Part(new BlockPos(-x,y-1,z-1),state,role));
        }
        return new MultiblockGuide(Component.literal(tier.displayName()+" Drill Construction"),parts,List.of(
            Component.literal(tier==DrillTier.STEEL?"Keep the bottom centre empty. Rear-top centre: MI sediment output hatch, output face pointing out of the drill. Other bottom parts follow the guide.":"Start at the bottom. Keep the centre empty; face the output hatch outward."),
            Component.literal("Add the core in the centre, controller facing front and shaft input facing outward."),
            Component.literal(tier==DrillTier.STEEL?"Cap the frame. Tank: left middle, above the output hatch. Fill with water before starting at 64+ RPM.":tier==DrillTier.IRON?"Cap the frame. Supply at least 32 RPM through the side shaft input.":"Cap with casings and four corner supports. Connect a Create crank or shaft.")
        ),Component.literal("Create | "+tier.stress+" SU/RPM | "+tier.workPerItem+" work/item"+(tier.minimumRpm>0?" | "+tier.minimumRpm+"+ RPM":"")+(tier==DrillTier.STEEL?" | 100 mB water/item":"")));
    }
    static MultiblockGuide electric(){
        List<MultiblockGuide.Part> parts=new ArrayList<>();
        for(int y=0;y<3;y++)for(int z=-1;z<=1;z++)for(int x=-1;x<=1;x++){
            var state=ElectricDrillStructure.block(x,y,z).defaultBlockState();if(state.is(RevoExtraction.ELECTRIC.get()))state=state.setValue(ElectricDrill.FACING,Direction.SOUTH);
            String role=ElectricDrillStructure.role(x,y,z);var kind=switch(role){case "sediment","item_output"->MultiblockGuide.Role.OUTPUT;case "fluid_input"->MultiblockGuide.Role.FLUID_INPUT;case "energy_input"->MultiblockGuide.Role.ENERGY_INPUT;case "item_input"->MultiblockGuide.Role.ITEM_INPUT;default->state.is(RevoExtraction.ELECTRIC.get())?MultiblockGuide.Role.CONTROLLER:MultiblockGuide.Role.BLOCK;};
            parts.add(new MultiblockGuide.Part(new BlockPos(-x,y-1,z-1),state,kind));
        }
        return new MultiblockGuide(Component.literal("Electric Drill Construction"),parts,List.of(Component.literal("Steel supports and casings. Keep bottom centre empty."),Component.literal("Steel core in centre; controller faces front."),Component.literal("Top: water back-left, sediment back-centre facing out; EU left, items out right, items in front. LV or better energy hatch.")),Component.literal("16 / 128 / 512 EU/t | 100 mB cooling water per geological cycle"));
    }
}
