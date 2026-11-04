package dk.revo.revoindustry.client;
import dk.revo.revoindustry.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class PowerConverterScreen extends AbstractContainerScreen<PowerConverterMenu>{
 private Button mode;
 public PowerConverterScreen(PowerConverterMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=184;inventoryLabelY=90;}
 protected void init(){super.init();mode=addRenderableWidget(Button.builder(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+8,topPos+19,160,20).build());}
 protected void containerTick(){super.containerTick();mode.setMessage(Component.literal(menu.feToEu()?"Mode: FE -> EU":"Mode: EU -> FE"));}
 protected void renderBg(GuiGraphics g,float p,int mx,int my){int x=leftPos,y=topPos;VanillaMachineGui.panel(g,x,y,176,184);VanillaMachineGui.playerInventory(g,x,y,184);}
 protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,7,0x404040,false);g.drawString(font,menu.rateEu()+" EU/t = "+menu.rateEu()*menu.ratio()+" FE/t",8,44,0x404040,false);g.drawString(font,"Direct conversion; no storage",8,57,0x404040,false);g.drawString(font,"FE / EU only",8,70,0x555555,false);g.drawString(font,"R",158,82,0,false);g.drawString(font,playerInventoryTitle,8,90,0x404040,false);}
 public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}
