package dk.revo.revocultivation.client;
import dk.revo.revocultivation.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid=RevoCultivation.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class SmallMachineScreen extends AbstractContainerScreen<SmallMachineMenu>{
 public SmallMachineScreen(SmallMachineMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=184;inventoryLabelY=90;}
 @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(SmallMachines.MENU.get(),SmallMachineScreen::new);}
 private int percent(){return menu.value(5)==3?100:menu.value(5)==1?menu.value(4)*100/8:menu.value(0)*100/menu.duration();}
 protected void renderBg(GuiGraphics g,float partial,int mx,int my){int x=leftPos,y=topPos;VanillaMachineGui.panel(g,x,y,176,184);VanillaMachineGui.playerInventory(g,x,y,184);VanillaMachineGui.slot(g,x+44,y+32);if(menu.silk)VanillaMachineGui.slot(g,x+17,y+32);VanillaMachineGui.slot(g,x+116,y+32);g.fill(x+69,y+35,x+107,y+45,0xFF555555);int w=percent()*36/100;if(w>0)g.fill(x+70,y+36,x+70+w,y+44,0xFF6C8A42);}
 protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,7,0x404040,false);g.drawString(font,Component.translatable(menu.value(5)==3?"small.revocultivation.ready":"small.revocultivation.status."+menu.value(1)),8,52,0x404040,false);g.drawString(font,Component.translatable("gui.revocultivation.duration",menu.duration()/20),8,67,0x555555,false);g.drawString(font,(menu.value(5)==1?"Fill ":"")+percent()+"%",72,22,0x555555,false);g.drawString(font,"R",158,82,0,false);g.drawString(font,playerInventoryTitle,8,90,0x404040,false);}
 public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);renderTooltip(g,x,y);}
}

