package dk.revo.revoindustry.furnace;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.hatches.ItemHatch;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import java.util.*;
/** Revo frame art, with native MI locking, auto-transfer, inventory and matched-casing behavior. */
public final class FurnaceItemHatch extends ItemHatch {
 public final int grade;
 public FurnaceItemHatch(BEP bep,boolean input,int grade){super(bep,new MachineGuiParameters.Builder(Furnaces.id("furnace_item_"+(input?"input":"output")+"_hatch_"+(grade+1)),true).build(),input,grade>0,inventory(input,grade));this.grade=grade;}
 @Override public java.util.List<net.minecraft.network.chat.Component> getTooltips(){var text=new java.util.ArrayList<>(super.getTooltips());text.add(net.minecraft.network.chat.Component.translatable("tooltip.revoindustry.single_port"));return text;}
 public net.minecraft.core.Direction portFace(){return orientation.outputDirection;}
 @Override public aztech.modern_industrialization.machines.models.MachineModelClientData getMachineModelData(){var data=super.getMachineModelData();data.frontDirection=portFace();return data;}
 @Override protected void tickTransfer(){long before=getInventory().getItemStacks().stream().mapToLong(s->s.getAmount()).sum();super.tickTransfer();if(before!=getInventory().getItemStacks().stream().mapToLong(s->s.getAmount()).sum())dk.revo.revolib.MachineFeedback.cue(level,worldPosition,dk.revo.revolib.MachineFeedback.Voice.TRANSFER);}
 private static MIInventory inventory(boolean input,int grade){var slots=new ArrayList<ConfigurableItemStack>();var pos=new SlotPositions.Builder();int count=new int[]{1,2,4}[grade];for(int i=0;i<count;i++){slots.add(input?ConfigurableItemStack.standardInputSlot():ConfigurableItemStack.standardOutputSlot());pos.addSlot(count==1?80:71+(i%2)*18,count<4?40:31+(i/2)*18);}return new MIInventory(slots,List.of(),pos.build(),SlotPositions.empty());}
}
