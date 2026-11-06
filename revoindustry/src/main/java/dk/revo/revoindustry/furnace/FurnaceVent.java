package dk.revo.revoindustry.furnace;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.multiblocks.*;
/** MI links this roof-only hatch to the controller casing, exactly like item hatches. */
public final class FurnaceVent extends HatchBlockEntity {
 public FurnaceVent(BEP bep){super(bep,new MachineGuiParameters.Builder(Furnaces.id("furnace_vent"),false).build(),OrientationComponent.Params.noFacingNoOutput());}
 public HatchType getHatchType(){return Furnaces.VENT;}
 public boolean upgradesToSteel(){return false;}
 public MIInventory getInventory(){return MIInventory.EMPTY;}
}
