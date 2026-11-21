package dk.revo.revostorage;
import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.items.*;
public class StorageMenu extends AbstractContainerMenu {
 public final StorageEntity entity;
 public final Player player;
 public final CraftingContainer craft;
 private final ResultContainer result=new ResultContainer();
 private final SimpleContainer view=new SimpleContainer(54);
 public final ContainerData data;
 public String search="";
 public int page;public boolean sortCount;
 private int gridRevision;
 private boolean updating;
 private final NonNullList<ItemStack> savedCraft=NonNullList.withSize(9,ItemStack.EMPTY);
 public StorageMenu(int id,Inventory inv){this(id,inv,null);}
 public StorageMenu(int id,Inventory inv,StorageEntity entity){
  super(RevoStorage.MENU.get(),id);this.entity=entity;player=inv.player;
  data=entity==null?new SimpleContainerData(6):new ContainerData(){public int get(int i){return switch(i){case 0->entity.terminal()?1:0;case 1->entity.springCharge();case 2->Math.min(32767,entity.networkItems().getSlots());case 3->page;case 4->entries().size();case 5->gridRevision;default->0;};}public void set(int i,int v){}public int getCount(){return 6;}};addDataSlots(data);
  craft=new TransientCraftingContainer(this,3,3,entity==null?NonNullList.withSize(9,ItemStack.EMPTY):entity.craftingItems);
  addSlot(new ResultSlot(player,craft,result,0,178,164){public boolean mayPickup(Player p){return terminal()&&super.mayPickup(p)&&(StorageMenu.this.entity==null||StorageMenu.this.entity.springCharge()>0);}public void onTake(Player p,ItemStack s){if(StorageMenu.this.entity!=null&&!StorageMenu.this.entity.spendSpringCharge())return;var keys=craft.getItems().stream().map(ItemStack::copy).toList();updating=true;super.onTake(p,s);if(StorageMenu.this.entity!=null){dk.revo.revolib.MachineFeedback.cue(StorageMenu.this.entity.getLevel(),StorageMenu.this.entity.getBlockPos(),dk.revo.revolib.MachineFeedback.Voice.CRAFT);for(int i=0;i<9;i++)if(craft.getItem(i).isEmpty()&&!keys.get(i).isEmpty())craft.setItem(i,StorageMenu.this.entity.take(keys.get(i),1,false));StorageMenu.this.entity.setChanged();}updating=false;slotsChanged(craft);}});
  for(int y=0;y<3;y++)for(int x=0;x<3;x++)addSlot(new Slot(craft,x+y*3,70+x*18,146+y*18){public boolean mayPlace(ItemStack s){return terminal();}});
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,9+x+y*9,25+x*18,216+y*18));
  for(int x=0;x<9;x++)addSlot(new Slot(inv,x,25+x*18,274));
  for(int y=0;y<6;y++)for(int x=0;x<9;x++)addSlot(new Slot(view,x+y*9,25+x*18,28+y*18){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}public boolean isActive(){return terminal();}});
  var chests=entity==null?new ItemStackHandler(9):entity.chests;
  for(int y=0;y<3;y++)for(int x=0;x<3;x++){final int bay=x+y*3;addSlot(new SlotItemHandler(chests,bay,70+x*18,55+y*18){public boolean isActive(){return !terminal();}public boolean mayPickup(Player p){return StorageMenu.this.entity==null||StorageMenu.this.entity.emptyBay(bay);}});}
  slotsChanged(craft);
 }
 public boolean terminal(){return data.get(0)==1;}
 private record StackKey(ItemStack stack){
  @Override public int hashCode(){return ItemStack.hashItemAndComponents(stack);}
  @Override public boolean equals(Object other){return other instanceof StackKey key&&ItemStack.isSameItemSameComponents(stack,key.stack);}
 }
 private List<ItemStack> cachedEntries=List.of();
 private long entriesTick=Long.MIN_VALUE,entriesVersion=-1,entriesTopology=-1;
 private String entriesSearch;private boolean entriesSort;
 public List<ItemStack> entries(){
  if(entity==null)return List.of();long tick=entity.getLevel()==null?0:entity.getLevel().getGameTime();
  long version=StorageEntity.contentVersion(),topology=StorageEntity.topologyVersion();
  if(entriesTick==tick&&entriesVersion==version&&entriesTopology==topology&&Objects.equals(entriesSearch,search)&&entriesSort==sortCount)return cachedEntries;
  var merged=new HashMap<StackKey,ItemStack>();var h=entity.networkItems();int slots=h.getSlots();
  for(int i=0;i<slots;i++){
   var stack=h.getStackInSlot(i);if(stack.isEmpty())continue;
   if(!search.isBlank()){var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());if(!(search.startsWith("@")?id.getNamespace().contains(search.substring(1)):stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(search)||id.toString().contains(search)))continue;}
   var key=new StackKey(stack);var existing=merged.get(key);if(existing==null)merged.put(key,stack.copy());else existing.grow(stack.getCount());
  }
  var list=new ArrayList<>(merged.values());list.sort(sortCount?Comparator.comparingInt(ItemStack::getCount).reversed().thenComparing(stack->stack.getHoverName().getString()):Comparator.comparing(stack->stack.getHoverName().getString()));
  entriesTick=tick;entriesVersion=version;entriesTopology=topology;entriesSearch=search;entriesSort=sortCount;return cachedEntries=list;
 }
 private void refresh(){
  if(entity==null)return;
  var entries=entries();
  page=Math.clamp(page,0,Math.max(0,(entries.size()-1)/54));
  boolean changed=false;
  for(int i=0;i<54;i++){
   var stack=page*54+i<entries.size()?entries.get(page*54+i):ItemStack.EMPTY;
   int shown=Math.min(stack.getCount(),view.getMaxStackSize(stack));
   if(shown<stack.getCount())stack=stack.copyWithCount(shown);
   if(!ItemStack.matches(view.getItem(i),stack)){
    view.setItem(i,stack);
    changed=true;
   }
  }
  // Container data uses a signed short on the wire.
  if(changed)gridRevision=(gridRevision+1)&0x7fff;
 }
 public int gridRevision(){return data.get(5);}
 public void broadcastChanges(){if(entity!=null){refresh();slotsChanged(craft);}super.broadcastChanges();}
 public void slotsChanged(Container c){if(updating||entity==null||!entity.terminal())return;var level=entity.getLevel();if(level==null||level.isClientSide)return;var recipe=level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,craft.asCraftInput(),level);ItemStack out=ItemStack.EMPTY;if(recipe.isPresent()&&result.setRecipeUsed(level,(net.minecraft.server.level.ServerPlayer)player,recipe.get()))out=recipe.get().value().assemble(craft.asCraftInput(),level.registryAccess());result.setItem(0,out);boolean changed=false;for(int i=0;i<9;i++)if(!ItemStack.matches(savedCraft.get(i),craft.getItem(i))){savedCraft.set(i,craft.getItem(i).copy());changed=true;}if(changed)entity.setChanged();}
 public static boolean isGridSlot(int index){return index>=46&&index<100;}
 @Override public boolean canTakeItemForPickAll(ItemStack stack,Slot slot){
  return !isGridSlot(slot.index)&&super.canTakeItemForPickAll(stack,slot);
 }
 public void clicked(int index,int button,ClickType type,Player p){
  if(isGridSlot(index))return;
  if(index==0&&entity!=null){slotsChanged(craft);if(!entity.terminal()||entity.springCharge()<=0)return;}
  super.clicked(index,button,type,p);
 }
 void clickGrid(Player player,int revision,int index,int button,boolean quickMove,boolean deposit){
  if(entity==null||!entity.terminal()||!stillValid(player)||!isGridSlot(index)||button<0||button>1)return;
  // Other viewers can change the sorted grid while this click is in flight.
  boolean carrying=!getCarried().isEmpty();
  if(revision!=gridRevision()||deposit!=carrying){
   broadcastFullState();
   return;
  }
  var key=view.getItem(index-46);
  var carried=getCarried();
  if(!carried.isEmpty()){
   if(quickMove)return;
   int amount=button==1?1:carried.getCount();
   var rest=entity.insert(carried.copyWithCount(amount),false);
   carried.shrink(amount-rest.getCount());
   setCarried(carried);
  }else if(!key.isEmpty()){
   int amount=button==1?(Math.min(key.getCount(),key.getMaxStackSize())+1)/2:key.getMaxStackSize();
   var extracted=entity.take(key,amount,false);
   if(quickMove){
    if(!player.getInventory().add(extracted)){
     var rest=entity.insert(extracted,false);
     if(!rest.isEmpty())player.drop(rest,false);
    }
   }else setCarried(extracted);
  }
  broadcastChanges();
 }
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();if(i==0){if(!moveItemStackTo(stack,10,46,true))return ItemStack.EMPTY;slot.onQuickCraft(stack,original);slot.onTake(p,stack);return original;}if(i>=10&&i<46){if(entity==null)return ItemStack.EMPTY;var rest=entity.terminal()?entity.insert(stack.copy(),false):ItemHandlerHelper.insertItemStacked(entity.chests,stack.copy(),false);if(rest.getCount()==stack.getCount())return ItemStack.EMPTY;slot.setByPlayer(rest);}else{if(!moveItemStackTo(stack,10,46,true))return ItemStack.EMPTY;slot.setChanged();}return original;}
 public boolean clickMenuButton(Player p,int id){if(entity==null||!entity.terminal()||!stillValid(p))return false;switch(id){case 0->page=Math.max(0,page-1);case 1->page++;case 2->{sortCount=!sortCount;page=0;}case 4->{for(int i=0;i<9;i++){var s=craft.getItem(i);var rest=entity.insert(s,false);craft.setItem(i,rest);}}default->{return false;}}broadcastChanges();return true;}
 public boolean stillValid(Player p){return entity==null||dk.revo.revolib.MachineMenuAccess.isUsable(p,entity);}
 /** Server resolves the actual recipe; client never supplies output stacks. Transfer consumes actual network/player items. */
 public void transfer(ResourceLocation id,boolean maximum){
  if(entity==null||!entity.terminal())return;var optional=player.level().getRecipeManager().byKey(id);if(optional.isEmpty()||!(optional.get().value() instanceof CraftingRecipe recipe)||!recipe.canCraftInDimensions(3,3))return;
  // Return the current grid first, preserving any overflow in player inventory.
  for(int i=0;i<9;i++){var s=craft.getItem(i);var rest=entity.insert(s,false);if(!rest.isEmpty()&&!player.getInventory().add(rest)){craft.setItem(i,rest);slotsChanged(craft);return;}craft.setItem(i,ItemStack.EMPTY);}
  int width=recipe instanceof ShapedRecipe shaped?shaped.getWidth():3;
  var ingredients=recipe.getIngredients();int repeats=maximum?64:1;updating=true;
  for(int round=0;round<repeats;round++){boolean complete=true;for(int k=0;k<ingredients.size();k++){var ingredient=ingredients.get(k);if(ingredient.isEmpty())continue;int slot=recipe instanceof ShapedRecipe?(k%width)+(k/width)*3:k;var existing=craft.getItem(slot);if(existing.getCount()>=existing.getMaxStackSize()&&!existing.isEmpty()){complete=false;continue;}ItemStack chosen=ItemStack.EMPTY;var h=entity.networkItems();for(int j=0;j<h.getSlots();j++){var candidate=h.getStackInSlot(j);if(ingredient.test(candidate)&&(existing.isEmpty()||ItemStack.isSameItemSameComponents(existing,candidate))){chosen=h.extractItem(j,1,false);break;}}if(chosen.isEmpty())for(int j=0;j<player.getInventory().items.size();j++){var candidate=player.getInventory().getItem(j);if(ingredient.test(candidate)&&(existing.isEmpty()||ItemStack.isSameItemSameComponents(existing,candidate))){chosen=player.getInventory().removeItem(j,1);break;}}if(chosen.isEmpty()){complete=false;continue;}if(existing.isEmpty())craft.setItem(slot,chosen);else existing.grow(1);}if(!complete)break;}
  updating=false;slotsChanged(craft);broadcastChanges();
 }
}
