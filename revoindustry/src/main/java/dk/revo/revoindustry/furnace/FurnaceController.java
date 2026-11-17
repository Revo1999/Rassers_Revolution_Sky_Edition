package dk.revo.revoindustry.furnace;

import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.*;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.*;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Tickable;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Native MI shape matching, exclusive hatch links, menus and casing adaptation; vanilla smelting recipes. */
public final class FurnaceController extends MultiblockMachineBlockEntity implements Tickable {
 public final FurnaceTier tier;
 private final MultiblockInventoryComponent inventory=new MultiblockInventoryComponent();
 private final List<FurnaceFuelHatch> fuels=new ArrayList<>();
 private final Job[] jobs=new Job[6];
 private final IsActiveComponent active=new IsActiveComponent();
 private final RecipeManager.CachedCheck<SingleRecipeInput,SmeltingRecipe> smelting=RecipeManager.createCheck(RecipeType.SMELTING);
 private ShapeTemplate shape;private float experience;private String status="Incomplete structure";private long consumedHeat;
 public FurnaceController(BEP bep,FurnaceTier tier){
  super(bep,new MachineGuiParameters.Builder(Furnaces.id(tier.id+"_multiblock_furnace"),false).backgroundHeight(240).build(),new OrientationComponent.Params(false,false,false));this.tier=tier;
  registerComponents(active,new MachineComponent(){
   public void writeNbt(CompoundTag n,HolderLookup.Provider r){var list=new ListTag();for(int i=0;i<jobs.length;i++)if(jobs[i]!=null){var entry=jobs[i].save(r);entry.putInt("Lane",i);list.add(entry);}n.put("FurnaceJobs",list);n.putFloat("FurnaceExperience",experience);}
   public void readNbt(CompoundTag n,HolderLookup.Provider r,boolean upgrade){Arrays.fill(jobs,null);var list=n.getList("FurnaceJobs",10);for(int i=0;i<list.size();i++){var entry=list.getCompound(i);int lane=entry.getInt("Lane");if(lane>=0&&lane<tier.lanes())jobs[lane]=Job.load(entry,r);}experience=Math.max(0,n.getFloat("FurnaceExperience"));}
  });
  registerGuiComponent(new FurnaceGui(this::guiText));
 }
 @Override public MIInventory getInventory(){return MIInventory.EMPTY;}
 @Override public MachineModelClientData getMachineModelData(){return new MachineModelClientData(tier.casing(),orientation.facingDirection).active(active.isActive);}
 @Override public ShapeTemplate getActiveShape(){
  if(shape==null){
   var casing=SimpleMember.forBlock(tier::casingBlock);var ports=new HatchFlags.Builder().with(HatchTypes.ITEM_INPUT,HatchTypes.ITEM_OUTPUT,Furnaces.FUEL).build();
   var builder=new ShapeTemplate.Builder(tier.casing()).add3by3(0,casing,false,ports).add3by3(1,casing,false,ports).add3by3(2,casing,false,ports);
   builder.remove(0,1,1).add(0,1,1,SimpleMember.forBlock(()->Blocks.AIR));
   builder.add(0,2,1,new SimpleMember(){public boolean matchesState(BlockState s,BlockEntity be){return false;}public BlockState getPreviewState(){return Furnaces.VENT_BLOCK.get().defaultBlockState();}},new HatchFlags.Builder().with(Furnaces.VENT).build());
   shape=builder.build();
  }return shape;
 }
 @Override public ShapeMatcher createShapeMatcher(){return new ShapeMatcher(level,worldPosition,orientation.facingDirection,getActiveShape(),shapeValid){
  @Override protected boolean checkRematch(net.minecraft.world.level.Level world){
   boolean input=false,output=false,fuel=false,vent=false;
   for(var hatch:getMatchedHatches()){
    if(hatch instanceof FurnaceFuelHatch){fuel=true;continue;}if(hatch instanceof FurnaceVent){vent=true;continue;}
    if(hatch instanceof FurnaceItemHatch own){if(own.grade<tier.factoryTier)return false;input|=own.getHatchType()==HatchTypes.ITEM_INPUT;output|=own.getHatchType()==HatchTypes.ITEM_OUTPUT;continue;}
    var id=BuiltInRegistries.BLOCK.getKey(hatch.getBlockState().getBlock());
    boolean admitted=false;for(int grade=tier.factoryTier;grade<3;grade++){String p=new String[]{"bronze","steel","advanced"}[grade];if(id.equals(ResourceLocation.parse("modern_industrialization:"+p+"_item_input_hatch"))||id.equals(ResourceLocation.parse("modern_industrialization:"+p+"_item_output_hatch")))admitted=true;}
    if(!admitted)return false;input|=hatch.getHatchType()==HatchTypes.ITEM_INPUT;output|=hatch.getHatchType()==HatchTypes.ITEM_OUTPUT;
   }return input&&output&&fuel&&vent;
  }
 };}
 @Override protected void onRematch(ShapeMatcher matcher){inventory.rebuild(matcher);fuels.clear();if(matcher.isMatchSuccessful())for(var hatch:matcher.getMatchedHatches())if(hatch instanceof FurnaceFuelHatch f)fuels.add(f);fuels.sort(Comparator.comparingLong(f->f.getBlockPos().asLong()));}
 public String status(){return status;}public long heatConsumedLastTick(){return consumedHeat;}public int runningJobs(){int n=0;for(var j:jobs)if(j!=null)n++;return n;}
 private String guiText(){StringBuilder text=new StringBuilder(isShapeValid()?"Structure formed":"Incomplete / incorrect hatches");text.append("\n").append(status).append("\nFactory: ").append(tier.lanes()).append(" lanes; batch ").append(tier.batch()).append("\nHeat: ").append(consumedHeat).append(" / tick");for(int i=0;i<tier.lanes();i++){var j=jobs[i];text.append("\n").append(i+1).append(": ").append(j==null?"Idle":(100*j.progress/j.duration)+"% x"+j.input.getCount());}return text.toString();}
 @Override public void tick(){
  if(level.isClientSide){
   if(active.isActive){
    var vent=ShapeMatcher.toWorldPos(worldPosition,orientation.facingDirection,new BlockPos(0,2,1));
    if(level.random.nextInt(5)==0)level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,vent.getX()+.5,vent.getY()+1.05,vent.getZ()+.5,0,.04,0);
   }return;
  }link();consumedHeat=0;
  if(!isShapeValid()){status="Check casing, centre air, roof vent and hatches";active.updateActive(false,this);return;}
  boolean working=false,blocked=false,needsFuel=false,invalid=false;
  int firstLane=(int)Math.floorMod(level.getGameTime(),(long)tier.lanes());
  for(int turn=0;turn<tier.lanes();turn++){
   int lane=(firstLane+turn)%tier.lanes();
   if(jobs[lane]==null)jobs[lane]=startJob();var j=jobs[lane];if(j==null)continue;
   if(!valid(j)){invalid=true;continue;}
   if(!canFit(j.output)){blocked=true;continue;}
   long target=(j.totalHeat*(j.progress+1)+j.duration-1)/j.duration;
   long need=Math.max(0,target-j.paid);
   for(var fuel:fuels){long got=fuel.takeHeat(need);j.paid+=got;need-=got;consumedHeat+=got;if(need<=0)break;}
   if(j.paid<target){needsFuel=true;continue;}
   working=true;j.progress++;
   if(j.progress>=j.duration){insert(j.output);experience+=j.experience*j.input.getCount();jobs[lane]=null;}
  }
  status=invalid?"Recipe changed: stored ingredients are safe":blocked?"Output blocked":needsFuel?"More fuel / fuel throughput needed":working?"Smelting":"Waiting for smelting input";
  if(working)dk.revo.revolib.MachineFeedback.work(level,worldPosition,switch(tier){case COPPER,IRON->dk.revo.revolib.MachineFeedback.Voice.FURNACE;case SILVER,GOLD,DIAMOND,EMERALD->dk.revo.revolib.MachineFeedback.Voice.FURNACE_TURBINE;case OBSIDIAN,NETHERITE->dk.revo.revolib.MachineFeedback.Voice.FURNACE_HEAVY;case CRYSTAL->dk.revo.revolib.MachineFeedback.Voice.FURNACE_CRYSTAL;case RAINBOW->dk.revo.revolib.MachineFeedback.Voice.FURNACE_RAINBOW;});
  active.updateActive(working,this);setChanged();
 }
 private Job startJob(){
  for(var slot:inventory.getItemInputs()){
   var stack=slot.toStack();if(stack.isEmpty())continue;
   var recipe=smelting.getRecipeFor(new SingleRecipeInput(stack),level);if(recipe.isEmpty())continue;
   var value=recipe.get().value();var result=value.assemble(new SingleRecipeInput(stack),level.registryAccess());if(result.isEmpty())continue;
   int amount=(int)Math.min(Math.min(tier.batch(),slot.getAmount()),result.getMaxStackSize()/result.getCount());
   while(amount>0&&!canFit(result.copyWithCount(result.getCount()*amount)))amount--;
   if(amount==0)continue;
   var job=new Job();job.input=stack.copyWithCount(amount);job.output=result.copyWithCount(result.getCount()*amount);job.recipe=recipe.get().id();job.duration=tier.duration(value.getCookingTime());job.totalHeat=(long)value.getCookingTime()*amount;job.experience=value.getExperience();slot.decrement(amount);return job;
  }return null;
 }
 private boolean valid(Job j){var holder=level.getRecipeManager().byKey(j.recipe);if(holder.isEmpty())return false;if(holder.get()==j.validatedRecipe)return true;if(!(holder.get().value() instanceof SmeltingRecipe r)||!r.matches(new SingleRecipeInput(j.input),level))return false;var output=r.assemble(new SingleRecipeInput(j.input),level.registryAccess());boolean valid=!output.isEmpty()&&ItemStack.isSameItemSameComponents(output,j.output)&&output.getCount()*j.input.getCount()==j.output.getCount();if(valid)j.validatedRecipe=holder.get();return valid;}
 private boolean canFit(ItemStack stack){var key=ItemVariant.of(stack);long left=stack.getCount();for(var slot:inventory.getItemOutputs())if(slot.isResourceAllowedByLock(key)&&(slot.isEmpty()||slot.getResource().equals(key))){left-=Math.max(0,slot.getRemainingCapacityFor(key));if(left<=0)return true;}return false;}
 private void insert(ItemStack stack){var key=ItemVariant.of(stack);long left=stack.getCount();for(var slot:inventory.getItemOutputs())if(slot.isResourceAllowedByLock(key)&&(slot.isEmpty()||slot.getResource().equals(key))){long amount=Math.min(left,Math.max(0,slot.getRemainingCapacityFor(key)));if(amount>0){slot.setKey(key);slot.increment(amount);left-=amount;}if(left==0)return;}throw new IllegalStateException("Output reservation invalidated during atomic furnace completion");}
 private void claimExperience(){if(level instanceof ServerLevel server&&experience>0){int amount=(int)experience;if(level.random.nextFloat()<experience-amount)amount++;experience=0;ExperienceOrb.award(server,worldPosition.getCenter(),amount);setChanged();}}
 @Override public void openMenu(ServerPlayer player){claimExperience();super.openMenu(player);}
 @Override public List<ItemStack> dropExtra(){var drops=new ArrayList<>(super.dropExtra());for(int i=0;i<jobs.length;i++)if(jobs[i]!=null){drops.add(jobs[i].input.copy());jobs[i]=null;}claimExperience();return drops;}
 private static final class Job{
  RecipeHolder<?> validatedRecipe;
  ItemStack input=ItemStack.EMPTY,output=ItemStack.EMPTY;ResourceLocation recipe;int progress,duration;long totalHeat,paid;float experience;
  CompoundTag save(HolderLookup.Provider r){var n=new CompoundTag();n.put("Input",input.save(r));n.put("Output",output.save(r));n.putString("Recipe",recipe.toString());n.putInt("Progress",progress);n.putInt("Duration",duration);n.putLong("TotalHeat",totalHeat);n.putLong("PaidHeat",paid);n.putFloat("Experience",experience);return n;}
  static Job load(CompoundTag n,HolderLookup.Provider r){var j=new Job();j.input=ItemStack.parseOptional(r,n.getCompound("Input"));j.output=ItemStack.parseOptional(r,n.getCompound("Output"));j.recipe=ResourceLocation.tryParse(n.getString("Recipe"));j.duration=Math.max(1,n.getInt("Duration"));j.progress=Math.clamp(n.getInt("Progress"),0,j.duration-1);j.totalHeat=Math.max(1,n.getLong("TotalHeat"));j.paid=Math.clamp(n.getLong("PaidHeat"),0,j.totalHeat);j.experience=Math.max(0,n.getFloat("Experience"));return j.recipe==null||j.input.isEmpty()||j.output.isEmpty()?null:j;}
 }
}
