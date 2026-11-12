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
 private boolean updating;
 public StorageMenu(int id,Inventory inv){this(id,inv,null);}
 public StorageMenu(int id,Inventory inv,StorageEntity entity){
  super(RevoStorage.MENU.get(),id);this.entity=entity;player=inv.player;
  data=entity==null?new SimpleContainerData(5):new ContainerData(){public int get(int i){return switch(i){case 0->entity.terminal()?1:0;case 1->entity.charge;case 2->Math.min(32767,entity.networkItems().getSlots());case 3->page;case 4->entries().size();default->0;};}public void set(int i,int v){}public int getCount(){return 5;}};addDataSlots(data);
  craft=new TransientCraftingContainer(this,3,3,entity==null?NonNullList.withSize(9,ItemStack.EMPTY):entity.craftingItems);
  addSlot(new ResultSlot(player,craft,result,0,178,164){public boolean mayPickup(Player p){return terminal()&&super.mayPickup(p)&&(StorageMenu.this.entity==null||StorageMenu.this.entity.charge>0);}public void onTake(Player p,ItemStack s){var keys=craft.getItems().stream().map(ItemStack::copy).toList();updating=true;super.onTake(p,s);if(StorageMenu.this.entity!=null){StorageMenu.this.entity.charge--;for(int i=0;i<9;i++)if(craft.getItem(i).isEmpty()&&!keys.get(i).isEmpty())craft.setItem(i,StorageMenu.this.entity.take(keys.get(i),1,false));StorageMenu.this.entity.setChanged();}updating=false;slotsChanged(craft);}});
  for(int y=0;y<3;y++)for(int x=0;x<3;x++)addSlot(new Slot(craft,x+y*3,70+x*18,146+y*18){public boolean mayPlace(ItemStack s){return terminal();}});
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,9+x+y*9,25+x*18,216+y*18));
  for(int x=0;x<9;x++)addSlot(new Slot(inv,x,25+x*18,274));
  for(int y=0;y<6;y++)for(int x=0;x<9;x++)addSlot(new Slot(view,x+y*9,25+x*18,28+y*18){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}public boolean isActive(){return terminal();}});
  var chests=entity==null?new ItemStackHandler(9):entity.chests;
  for(int y=0;y<3;y++)for(int x=0;x<3;x++){final int bay=x+y*3;addSlot(new SlotItemHandler(chests,bay,70+x*18,55+y*18){public boolean isActive(){return !terminal();}public boolean mayPickup(Player p){return StorageMenu.this.entity==null||StorageMenu.this.entity.emptyBay(bay);}});}
  slotsChanged(craft);
 }
 public boolean terminal(){return data.get(0)==1;}
 public List<ItemStack> entries(){if(entity==null)return List.of();var list=new ArrayList<ItemStack>();var h=entity.networkItems();for(int i=0;i<h.getSlots();i++){var s=h.getStackInSlot(i);if(s.isEmpty())continue;var key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem());if(!search.isBlank()&&!(search.startsWith("@")?key.getNamespace().contains(search.substring(1)):s.getHoverName().getString().toLowerCase(Locale.ROOT).contains(search)||key.toString().contains(search)))continue;var found=list.stream().filter(k->ItemStack.isSameItemSameComponents(k,s)).findFirst();if(found.isPresent())found.get().grow(s.getCount());else list.add(s.copy());}list.sort(sortCount?Comparator.comparingInt(ItemStack::getCount).reversed().thenComparing(s->s.getHoverName().getString()):Comparator.comparing(s->s.getHoverName().getString()));return list;}
 private void refresh(){if(entity==null)return;var entries=entries();page=Math.clamp(page,0,Math.max(0,(entries.size()-1)/54));for(int i=0;i<54;i++)view.setItem(i,page*54+i<entries.size()?entries.get(page*54+i):ItemStack.EMPTY);}
 public void broadcastChanges(){if(entity!=null){refresh();slotsChanged(craft);}super.broadcastChanges();}
 public void slotsChanged(Container c){if(updating||entity==null||!entity.terminal())return;var level=entity.getLevel();if(level==null||level.isClientSide)return;var recipe=level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,craft.asCraftInput(),level);ItemStack out=ItemStack.EMPTY;if(recipe.isPresent()&&result.setRecipeUsed(level,(net.minecraft.server.level.ServerPlayer)player,recipe.get()))out=recipe.get().value().assemble(craft.asCraftInput(),level.registryAccess());result.setItem(0,out);entity.setChanged();}
 public void clicked(int index,int button,ClickType type,Player p){
  if(index>=46&&index<100){if(entity==null||!entity.terminal())return;refresh();var key=view.getItem(index-46);var carried=getCarried();if(!carried.isEmpty()){if(type==ClickType.PICKUP){int n=button==1?1:carried.getCount();var rest=entity.insert(carried.copyWithCount(n),false);carried.shrink(n-rest.getCount());setCarried(carried);}}else if(!key.isEmpty()&&(type==ClickType.PICKUP||type==ClickType.QUICK_MOVE)){int n=button==1?(Math.min(key.getCount(),key.getMaxStackSize())+1)/2:key.getMaxStackSize();if(type==ClickType.QUICK_MOVE){var extracted=entity.take(key,n,false);if(!p.getInventory().add(extracted)){var rest=entity.insert(extracted,false);if(!rest.isEmpty())p.drop(rest,false);}}else setCarried(entity.take(key,n,false));}broadcastChanges();return;}
  if(index==0&&entity!=null){slotsChanged(craft);if(!entity.terminal()||entity.charge<=0)return;}
  super.clicked(index,button,type,p);
 }
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();if(i==0){if(!moveItemStackTo(stack,10,46,true))return ItemStack.EMPTY;slot.onQuickCraft(stack,original);slot.onTake(p,stack);return original;}if(i>=10&&i<46){if(entity==null)return ItemStack.EMPTY;var rest=entity.terminal()?entity.insert(stack.copy(),false):ItemHandlerHelper.insertItemStacked(entity.chests,stack.copy(),false);if(rest.getCount()==stack.getCount())return ItemStack.EMPTY;slot.setByPlayer(rest);}else{if(!moveItemStackTo(stack,10,46,true))return ItemStack.EMPTY;slot.setChanged();}return original;}
 public boolean clickMenuButton(Player p,int id){if(entity==null)return false;switch(id){case 0->page=Math.max(0,page-1);case 1->page++;case 2->{sortCount=!sortCount;page=0;}case 3->entity.wind();case 4->{for(int i=0;i<9;i++){var s=craft.getItem(i);var rest=entity.insert(s,false);craft.setItem(i,rest);}}default->{return false;}}broadcastChanges();return true;}
 public boolean stillValid(Player p){return entity==null||!entity.isRemoved()&&p.distanceToSqr(entity.getBlockPos().getCenter())<=64;}
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
