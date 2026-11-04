package dk.revo.revoextraction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
public class HatchEntity extends BlockEntity {
    public HatchEntity(BlockPos p,BlockState s) { super(RevoExtraction.OUTPUT.get(),p,s); }
    private boolean exporting;
    /** Push through the hatch face. Rejected items remain in the persistent machine buffer. */
    public void ejectOutput() {
        if(level==null||level.isClientSide||exporting)return;
        var m=machine();if(m==null)return;
        var direction=getBlockState().getValue(DrillHatch.FACING);
        var targetPos=worldPosition.relative(direction);
        if(!level.hasChunkAt(targetPos))return;
        var target=level.getCapability(Capabilities.ItemHandler.BLOCK,targetPos,direction.getOpposite());
        if(target==null)return;
        exporting=true;
        try {
            for(int slot=0;slot<m.inventory.getSlots();slot++){
                var offered=m.inventory.extractItem(slot,64,true);
                if(offered.isEmpty())continue;
                int accepted=offered.getCount()-ItemHandlerHelper.insertItemStacked(target,offered,true).getCount();
                if(accepted<=0)continue;
                var extracted=m.inventory.extractItem(slot,accepted,false);
                var remainder=ItemHandlerHelper.insertItemStacked(target,extracted,false);
                if(!remainder.isEmpty())m.inventory.insertItem(slot,remainder,false);
            }
        } finally {exporting=false;}
    }
    public DrillMachine machine() {
        if(level==null || !getBlockState().getValue(DrillPart.FORMED)) return null;
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) {
            BlockPos candidate=worldPosition.offset(x,1,z);
            if(level.hasChunkAt(candidate) && level.getBlockEntity(candidate) instanceof DrillMachine m && m.formed() && m.at(-1,0,0).equals(worldPosition)) return m;
        }
        return null;
    }
    // Resolve the controller on every operation: cached capabilities cannot retain a dismantled inventory.
    public IItemHandler output() { return new IItemHandler() {
        public int getSlots(){return 9;}
        public ItemStack getStackInSlot(int slot){var m=machine(); return m==null?ItemStack.EMPTY:m.inventory.getStackInSlot(slot).copy();}
        public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}
        public ItemStack extractItem(int slot,int amount,boolean simulate){var m=machine(); return m==null?ItemStack.EMPTY:m.inventory.extractItem(slot,amount,simulate);}
        public int getSlotLimit(int slot){return 64;}
        public boolean isItemValid(int slot,ItemStack stack){return false;}
    }; }
}
