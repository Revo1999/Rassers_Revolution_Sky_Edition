package dk.revo.revoindustry.client;

import dk.revo.revoindustry.nexus.*;
import dk.revo.revolib.client.guide.*;
import dk.revo.revolib.client.guide.emi.MultiblockEmiRecipe;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

@EmiEntrypoint
public final class NexusEmiPlugin implements EmiPlugin {
    public static MultiblockGuide guide(String kind){
        var bp=Nexus.blueprint(kind);var origin=Nexus.position(bp.getAsJsonArray("origin"));var parts=new ArrayList<MultiblockGuide.Part>();
        var occupied=new HashSet<BlockPos>();for(var raw:bp.getAsJsonArray("cells"))occupied.add(Nexus.position(raw.getAsJsonObject().getAsJsonArray("position")));
        for(var raw:bp.getAsJsonArray("cells")){
            var cell=raw.getAsJsonObject();var pos=Nexus.position(cell.getAsJsonArray("position")).subtract(origin);
            var state=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(cell.get("block").getAsString())).defaultBlockState();
            var facing=Direction.byName(cell.get("facing").getAsString());
            if(state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING))state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,facing);
            if(state.getBlock() instanceof net.minecraft.world.level.block.FenceBlock){
                var local=Nexus.position(cell.getAsJsonArray("position"));
                for(Direction side:Direction.Plane.HORIZONTAL)state=state.setValue(switch(side){case NORTH->net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH;case EAST->net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST;case SOUTH->net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH;default->net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST;},occupied.contains(local.relative(side)));
            }
            var role=pos.equals(BlockPos.ZERO)?MultiblockGuide.Role.CONTROLLER:switch(cell.get("part").getAsString()){
                case "input"->MultiblockGuide.Role.ITEM_INPUT;case "output","fluid_out"->MultiblockGuide.Role.OUTPUT;case "fluid_in"->MultiblockGuide.Role.FLUID_INPUT;case "energy"->MultiblockGuide.Role.ENERGY_INPUT;default->MultiblockGuide.Role.BLOCK;
            };
            var data=net.neoforged.neoforge.client.model.data.ModelData.EMPTY;
            if(role!=MultiblockGuide.Role.BLOCK){var casing=aztech.modern_industrialization.machines.models.MachineCasings.get(Nexus.id("nexus_machine"));var appearance=new aztech.modern_industrialization.machines.models.MachineModelClientData(casing,facing);data=net.neoforged.neoforge.client.model.data.ModelData.builder().with(aztech.modern_industrialization.machines.models.MachineModelClientData.KEY,appearance).build();}
            parts.add(new MultiblockGuide.Part(pos,state,role,data));
        }
        int min=parts.stream().mapToInt(p->p.offset().getY()).min().orElseThrow(),max=parts.stream().mapToInt(p->p.offset().getY()).max().orElseThrow();
        var steps=new ArrayList<Component>();for(int y=min;y<=max;y++)steps.add(Component.literal("Layer "+(y-min+1)+": place the shown individual blocks; keep reserved spaces empty."));
        return new MultiblockGuide(Component.literal(kind.replace('_',' ')),parts,steps,Component.literal(kind.equals("nexus_builder")?"Retain a Construction Program in the input hatch. Target controller: four blocks behind the builder at the same height.":"Native MI item, fluid and energy hatches; frame blocks remain in the world."));
    }
    @Override public void register(EmiRegistry registry){
        var category=new EmiRecipeCategory(Nexus.id("nexus_assembly"),EmiStack.of(Nexus.block("nexus_core")));registry.addCategory(category);
        for(String kind:Nexus.RECIPES.keySet()){
            var controller=EmiStack.of(Nexus.block(Nexus.blueprint(kind).get("controller").getAsString()));registry.addWorkstation(category,controller);
            registry.addRecipe(new MultiblockEmiRecipe(category,Nexus.id("/assembly/"+kind),guide(kind),controller));
        }
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Nexus.PROGRAM.get()),EmiStack.of(Nexus.block("nexus_builder"))),List.of(Component.literal("Builder: input hatch on the left, output hatch on the right, MV energy hatch directly behind. Insert a Construction Program in its input hatch with the required blocks; the program is retained."),Component.literal("The target portal controller sits four blocks behind the builder at the same height. One real block is placed every ten ticks for 64 EU. Missing materials/power or an obstructed/unloaded position pauses building. Filled machine items are not consumed."),Component.literal("The built portal needs 1,024 EU/t through its own native energy hatch to display its contained animated surface.")),Nexus.id("/info/nexus_builder")));
    }
}
