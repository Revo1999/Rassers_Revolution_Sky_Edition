package dk.revo.revoindustry.nexus;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.core.BlockPos;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;

/** MI owns linking, hatches, recipe reservations, power accounting, GUI and persistence. */
public final class NexusMachine extends ElectricCraftingMultiblockBlockEntity {
    public final String kind;
    private float previousProgress;
    public long flashAt=Long.MIN_VALUE;
    private long emittedFlash=Long.MIN_VALUE;
    public boolean portalPowered;
    public int completedReactions;
    private int buildCursor;
    private final java.util.List<aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity> linkedHatches=new java.util.ArrayList<>();
    public NexusMachine(BEP bep,String kind){
        super(bep,Nexus.id(kind),Nexus.shape(kind),Nexus.RECIPES.get(kind));this.kind=kind;
        registerComponents(new aztech.modern_industrialization.machines.MachineComponent.ClientOnly(){
            public void writeClientNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r){tag.putBoolean("NexusPowered",portalPowered);}
            public void readClientNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r){portalPowered=tag.getBoolean("NexusPowered");}
        });
        registerComponents(new aztech.modern_industrialization.machines.MachineComponent.ServerOnly(){
            public void writeNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r){tag.putInt("NexusBuildCursor",buildCursor);tag.putFloat("NexusPreviousProgress",previousProgress);tag.putInt("NexusCompletedReactions",completedReactions);}
            public void readNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r,boolean upgrade){buildCursor=Math.clamp(tag.getInt("NexusBuildCursor"),0,Nexus.blueprint("nexus_portal").getAsJsonArray("cells").size());previousProgress=Math.clamp(tag.getFloat("NexusPreviousProgress"),0,1);completedReactions=Math.max(0,tag.getInt("NexusCompletedReactions"));}
        });
    }
    @Override public long getBaseRecipeEu(){return switch(kind){case "pressure_casting_plant"->32768;case "electroplating_line"->65536;case "particle_collider"->262144;case "nexus_builder"->64;default->1024;};}
    @Override public long getMaxRecipeEu(){return getBaseRecipeEu();}
    public BlockPos blueprintWorld(int x,int y,int z){var origin=Nexus.position(Nexus.blueprint(kind).getAsJsonArray("origin"));return ShapeMatcher.toWorldPos(worldPosition,orientation.facingDirection,new BlockPos(x,y,z).subtract(origin));}
    @Override public void tickExtra(){
        if(level.isClientSide){
            if(kind.equals("particle_collider")&&isShapeValid()){
                var center=blueprintWorld(10,4,10);double age=level.getGameTime();
                if(getMachineModelData().isActive)for(int sign:new int[]{-1,1}){
                    double angle=sign*age*Math.PI/120;
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,center.getX()+.5+8*Math.cos(angle),center.getY()+.98,center.getZ()+.5+8*Math.sin(angle),0,0,0);
                }
                if(flashAt!=Long.MIN_VALUE&&flashAt!=emittedFlash&&age-flashAt<24){
                    emittedFlash=flashAt;
                    for(int i=0;i<32;i++){
                        double angle=i*Math.PI/16;
                        level.addParticle(net.minecraft.core.particles.ParticleTypes.FLASH,center.getX()+.5+8*Math.cos(angle),center.getY()+1,center.getZ()+.5+8*Math.sin(angle),0,0,0);
                    }
                }
            }return;
        }
        if(kind.equals("nexus_builder")){buildNext();}
        else if(kind.equals("nexus_portal")){
            boolean active=isShapeValid()&&isEnabled()&&consumeEu(1024,Simulation.SIMULATE)==1024;
            if(active)consumeEu(1024,Simulation.ACT);if(active!=portalPowered){portalPowered=active;sync();}
        }else if(kind.equals("particle_collider")){
            float progress=crafter.getProgress();
            if(isShapeValid()&&previousProgress>0&&progress<previousProgress){completedReactions++;setChanged();level.blockEvent(worldPosition,getBlockState().getBlock(),201,0);}
            previousProgress=progress;
        }
    }
    @Override protected void onRematch(ShapeMatcher matcher){super.onRematch(matcher);linkedHatches.clear();if(matcher.isMatchSuccessful())linkedHatches.addAll(matcher.getMatchedHatches());}
    public int buildCursor(){return buildCursor;}
    public String status(){
        if(!isShapeValid())return "Incomplete structure";
        if(kind.equals("nexus_portal"))return portalPowered?"Nexus stabilized | 1,024 EU/t":"Needs 1,024 EU/t";
        if(!kind.equals("nexus_builder"))return getMachineModelData().isActive?"Processing":"Idle";
        var cells=Nexus.blueprint("nexus_portal").getAsJsonArray("cells");
        String progress=buildCursor+" / "+cells.size()+" blocks";
        if(buildCursor>=cells.size())return "Construction complete | "+progress;
        if(inventory.getItemInputs().stream().noneMatch(slot->slot.getAmount()>0&&slot.toStack().is(Nexus.PROGRAM.get())))return "Insert Construction Program | "+progress;
        var cell=cells.get(buildCursor).getAsJsonObject();
        return progress+" | Next: "+cell.get("block").getAsString();
    }
    private void buildNext(){
        if(!isShapeValid()||!isEnabled()||level.getGameTime()%10!=0)return;
        var inputs=inventory.getItemInputs();
        if(inputs.stream().noneMatch(slot->slot.getAmount()>0&&slot.toStack().is(Nexus.PROGRAM.get())))return;
        var cells=Nexus.blueprint("nexus_portal").getAsJsonArray("cells");
        if(buildCursor>=cells.size())return;
        var cell=cells.get(buildCursor).getAsJsonObject();var local=Nexus.position(cell.getAsJsonArray("position")).subtract(Nexus.position(Nexus.blueprint("nexus_portal").getAsJsonArray("origin"))).offset(0,0,4);
        var pos=ShapeMatcher.toWorldPos(worldPosition,orientation.facingDirection,local);if(!level.hasChunkAt(pos))return;
        var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse(cell.get("block").getAsString()));
        var face=net.minecraft.core.Direction.byName(cell.get("facing").getAsString());
        int turns=Math.floorMod(orientation.facingDirection.get2DDataValue()-net.minecraft.core.Direction.NORTH.get2DDataValue(),4);for(int i=0;i<turns;i++)face=face.getClockWise();
        var state=block.defaultBlockState();
        if(state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING))state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,face);
        if(level.getBlockState(pos).is(block)){buildCursor++;setChanged();return;}
        // Never erase existing terrain, fluids, items or player builds, and never force chunk loads.
        if(!level.getBlockState(pos).isAir()||consumeEu(64,Simulation.SIMULATE)!=64)return;
        for(var slot:inputs){
            var stack=slot.toStack();
            if(slot.getAmount()<1||!stack.is(block.asItem())||stack.has(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA)||stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))continue;
            if(!level.setBlock(pos,state,3))return;
            if(level.getBlockEntity(pos) instanceof aztech.modern_industrialization.machines.MachineBlockEntity machine){machine.orientation.facingDirection=face;machine.orientation.outputDirection=face;machine.setChanged();machine.sync();}
            consumeEu(64,Simulation.ACT);slot.setAmount(slot.getAmount()-1);linkedHatches.forEach(net.minecraft.world.level.block.entity.BlockEntity::setChanged);
            buildCursor++;setChanged();return;
        }
    }
    @Override public boolean triggerEvent(int id,int value){if(id==201){if(level!=null&&level.isClientSide)flashAt=level.getGameTime();return true;}return super.triggerEvent(id,value);}
}
