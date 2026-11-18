package dk.revo.revoextraction;

import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.hatches.FluidHatch;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

/** Native MI output storage, GUI, locking, extraction and automatic transfer. */
public final class DrillSedimentHatch extends FluidHatch {
    public static final int CAPACITY=8000;
    public static ResourceLocation id(){return ResourceLocation.fromNamespaceAndPath(RevoExtraction.MOD_ID,"steel_drill_fluid_output_hatch");}
    public static Fluid sediment(){return BuiltInRegistries.FLUID.get(ResourceLocation.parse("revoindustry:drill_sediment"));}
    public DrillSedimentHatch(BEP bep){
        super(bep,new MachineGuiParameters.Builder(id(),true).build(),false,true,
            new MIInventory(List.of(),List.of(ConfigurableFluidStack.standardOutputSlot(CAPACITY)),SlotPositions.empty(),new SlotPositions.Builder().addSlot(80,40).build()));
        orientation.extractFluids=true;
    }
    public int amount(){return (int)getInventory().getFluidStacks().getFirst().getAmount();}
    public boolean canAccept(int amount){return canAccept(sediment(),amount);}
    public boolean canAccept(Fluid fluid,int amount){var tank=getInventory().getFluidStacks().getFirst();var id=BuiltInRegistries.FLUID.getKey(fluid);boolean sediment=id.getNamespace().equals("revoindustry")&&List.of("drill_sediment","good_drill_sediment","excellent_drill_sediment").contains(id.getPath());return sediment&&amount>=0&&tank.isResourceAllowedByLock(fluid)&&(tank.isEmpty()||tank.getResource().getFluid()==fluid)&&tank.getRemainingSpace()>=amount;}
    public void acceptCoolant(int amount){acceptCoolant(sediment(),amount);}
    public void acceptCoolant(Fluid fluid,int amount){
        if(amount==0)return;if(!canAccept(fluid,amount))throw new IllegalStateException("Reserve sediment capacity before consuming coolant");
        var tank=getInventory().getFluidStacks().getFirst();tank.setKey(FluidVariant.of(fluid));tank.increment(amount);setChanged();
    }
    @Override public aztech.modern_industrialization.machines.models.MachineModelClientData getMachineModelData(){var data=super.getMachineModelData();data.frontDirection=orientation.outputDirection;return data;}
}
