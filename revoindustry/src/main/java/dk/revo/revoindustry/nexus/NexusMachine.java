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
    public final NexusConstruction construction;
    public NexusMachine(BEP bep,String kind){
        super(bep,Nexus.id(kind),Nexus.shape(kind),Nexus.RECIPES.get(kind));this.kind=kind;
        construction=kind.equals("nexus_builder")?new NexusConstruction(this):null;if(construction!=null)registerComponents(construction);
        registerComponents(new aztech.modern_industrialization.machines.MachineComponent.ClientOnly(){
            public void writeClientNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r){tag.putBoolean("NexusPowered",portalPowered);if(construction!=null){tag.putInt("BuildStatus",construction.status());tag.putString("BuildProblem",construction.problem());}}
            public void readClientNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r){portalPowered=tag.getBoolean("NexusPowered");if(construction!=null)construction.readClient(tag.getInt("BuildStatus"),tag.getString("BuildProblem"));}
        });
        registerComponents(new aztech.modern_industrialization.machines.MachineComponent.ServerOnly(){
            public void writeNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r){tag.putFloat("NexusPreviousProgress",previousProgress);tag.putInt("NexusCompletedReactions",completedReactions);}
            public void readNbt(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider r,boolean upgrade){previousProgress=Math.clamp(tag.getFloat("NexusPreviousProgress"),0,1);completedReactions=Math.max(0,tag.getInt("NexusCompletedReactions"));}
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
        if(kind.equals("nexus_builder")){construction.tick();}
        else if(kind.equals("nexus_portal")){
            boolean active=isShapeValid()&&isEnabled()&&consumeEu(1024,Simulation.SIMULATE)==1024;
            if(active)consumeEu(1024,Simulation.ACT);if(active!=portalPowered){portalPowered=active;sync();}if(active&&level.getGameTime()%20==0){var advancement=level.getServer().getAdvancements().get(Nexus.id("nexus_stabilized"));if(advancement!=null)for(var player:level.getServer().getPlayerList().getPlayers())if(player.level()==level&&player.distanceToSqr(worldPosition.getCenter())<=64*64)player.getAdvancements().award(advancement,"stabilize");}
        }else if(kind.equals("particle_collider")){
            float progress=crafter.getProgress();
            if(isShapeValid()&&previousProgress>0&&progress<previousProgress){completedReactions++;setChanged();level.blockEvent(worldPosition,getBlockState().getBlock(),201,0);}
            previousProgress=progress;
        }
    }
    public int buildCursor(){return construction==null?0:construction.cursor();}
    public String status(){if(construction!=null)return construction.problem();if(!isShapeValid())return "Incomplete structure";if(kind.equals("nexus_portal"))return portalPowered?"Nexus stabilized | 1,024 EU/t":"Needs 1,024 EU/t";return getMachineModelData().isActive?"Processing":"Idle";}
    @Override public java.util.List<net.minecraft.world.item.ItemStack> dropExtra(){var result=super.dropExtra();if(construction!=null)result.addAll(construction.drainDrops());return result;}
    @Override public boolean useWrench(net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){if(construction!=null&&construction.started()){if(!getLevel().isClientSide)player.displayClientMessage(net.minecraft.network.chat.Component.literal("Construction orientation is locked after starting"),true);return true;}return super.useWrench(player,hand,hit);}
    @Override public boolean triggerEvent(int id,int value){if(id==201){if(level!=null&&level.isClientSide)flashAt=level.getGameTime();return true;}return super.triggerEvent(id,value);}
}
