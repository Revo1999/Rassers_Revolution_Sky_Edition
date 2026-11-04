package dk.revo.revoindustry.client;
import dk.revo.revoindustry.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class PrimitiveVesselScreen extends AbstractContainerScreen<PrimitiveVesselMenu>{
 public PrimitiveVesselScreen(PrimitiveVesselMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=176;imageHeight=184;inventoryLabelY=90;}
 protected void renderBg(GuiGraphics g,float p,int mx,int my){int x=leftPos,y=topPos;VanillaMachineGui.panel(g,x,y,176,184);VanillaMachineGui.playerInventory(g,x,y,184);VanillaMachineGui.slot(g,x+44,y+32);if(menu.rain)VanillaMachineGui.slot(g,x+116,y+32);g.fill(x+69,y+34,x+107,y+45,0xFF444444);int n=menu.rain?menu.value(0)*36/1000:menu.value(1)*36/200;g.fill(x+70,y+35,x+70+n,y+44,menu.rain?0xFF397BC1:0xFFE79D38);g.fill(x+148,y+24,x+159,y+67,0xFF444444);int h=menu.value(0)*40/1000;g.fill(x+149,y+65-h,x+158,y+65,menu.rain?0xFF397BC1:0xFFE79D38);}
 protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,8,7,0x404040,false);g.drawString(font,Component.translatable("state.revoindustry."+menu.value(2)),8,52,0x404040,false);g.drawString(font,menu.value(0)+"/1000 mB",8,67,0x555555,false);g.drawString(font,"R",158,82,0,false);g.drawString(font,playerInventoryTitle,8,90,0x404040,false);}
 public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);renderTooltip(g,x,y);}
}

