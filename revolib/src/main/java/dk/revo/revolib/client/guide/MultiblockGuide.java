package dk.revo.revolib.client.guide;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Client guide data. Offsets are relative to the controller, using the real formation blueprint. */
public record MultiblockGuide(Component title, List<Part> parts, List<Component> layerSteps, Component power) {
    public enum Role {
        BLOCK("Structure"), CONTROLLER("Controller"), OUTPUT("Item output hatch"), POWER("Create shaft input"), INPUT_OUTPUT("Item input/output hatch"), FLUID_INPUT("Water tank / fluid input"), ELECTRIC_INPUT("Electrical power converter");
        public final String label;
        Role(String label) { this.label=label; }
    }
    public record Part(BlockPos offset, BlockState state, Role role) {}
    public MultiblockGuide {
        parts=List.copyOf(parts);layerSteps=List.copyOf(layerSteps);
        if(parts.isEmpty()||layerSteps.isEmpty())throw new IllegalArgumentException("Guide needs blocks and layer instructions");
        if(maxY(parts)-minY(parts)+1!=layerSteps.size())throw new IllegalArgumentException("One instruction required per vertical layer");
        if(parts.stream().map(Part::offset).distinct().count()!=parts.size())throw new IllegalArgumentException("Duplicate guide position");
    }
    private static int minY(List<Part> parts){return parts.stream().mapToInt(p->p.offset().getY()).min().orElseThrow();}
    private static int maxY(List<Part> parts){return parts.stream().mapToInt(p->p.offset().getY()).max().orElseThrow();}
    public int minY(){return minY(parts);}
    public int layers(){return layerSteps.size();}
    public int sizeX(){return parts.stream().mapToInt(p->p.offset().getX()).max().orElseThrow()-parts.stream().mapToInt(p->p.offset().getX()).min().orElseThrow()+1;}
    public int sizeZ(){return parts.stream().mapToInt(p->p.offset().getZ()).max().orElseThrow()-parts.stream().mapToInt(p->p.offset().getZ()).min().orElseThrow()+1;}
    public List<ItemStack> materials(){
        Map<net.minecraft.world.item.Item,Integer> counts=new LinkedHashMap<>();
        for(var part:parts)if(!part.state().isAir())counts.merge(part.state().getBlock().asItem(),1,Integer::sum);
        return counts.entrySet().stream().map(e->new ItemStack(e.getKey(),e.getValue())).toList();
    }
    public int blocks(){return parts.stream().mapToInt(p->p.state().isAir()?0:1).sum();}
}
