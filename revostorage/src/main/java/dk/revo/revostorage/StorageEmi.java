package dk.revo.revostorage;
import java.util.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.recipe.handler.*;
import dev.emi.emi.api.stack.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
@EmiEntrypoint
public class StorageEmi implements EmiPlugin {
 public void register(EmiRegistry r){r.addRecipeHandler(RevoStorage.MENU.get(),new Handler());r.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(RevoStorage.DRIVE.get()),EmiStack.of(RevoStorage.TERMINAL.get()),EmiStack.of(RevoStorage.SPRING.get())),List.of(Component.literal("Place terminals and chest drives face-adjacent. Install nine empty vanilla or Iron Chests per drive. Search, sort, shift-click and use EMI recipe + to craft from stored materials. Place a Spring Module against the terminal to power crafting. Attach a Create Hand Crank or a powered 16+ RPM shaft to the front socket.")),ResourceLocation.fromNamespaceAndPath("revostorage","guide")));}
 public static class Handler implements EmiRecipeHandler<StorageMenu>{
  public EmiPlayerInventory getInventory(AbstractContainerScreen<StorageMenu> screen){var list=new java.util.ArrayList<EmiStack>();for(int i=1;i<100;i++){var stack=screen.getMenu().slots.get(i).getItem();if(!stack.isEmpty())list.add(EmiStack.of(stack));}return new EmiPlayerInventory(list);}
  public List<net.minecraft.world.inventory.Slot> getInputSources(StorageMenu m){return m.slots.subList(1,46);}
  public List<net.minecraft.world.inventory.Slot> getCraftingSlots(StorageMenu m){return m.slots.subList(1,10);}
  public boolean supportsRecipe(EmiRecipe recipe){return recipe.getCategory()==VanillaEmiRecipeCategories.CRAFTING&&recipe.getId()!=null;}
  public boolean canCraft(EmiRecipe recipe,EmiCraftContext<StorageMenu> context){return context.getScreenHandler().terminal();}
  public boolean craft(EmiRecipe recipe,EmiCraftContext<StorageMenu> context){if(!context.getScreenHandler().terminal()||recipe.getId()==null)return false;PacketDistributor.sendToServer(new StorageAction(context.getScreenHandler().containerId,"recipe",recipe.getId().toString(),context.getAmount()>1));return true;}
 }
}
