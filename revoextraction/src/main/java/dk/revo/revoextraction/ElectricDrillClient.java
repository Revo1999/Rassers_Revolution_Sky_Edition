package dk.revo.revoextraction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid="revoextraction",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class ElectricDrillClient {
 @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(RevoExtraction.ELECTRIC_MENU.get(),Screen::new);}
 public static class Screen extends AbstractContainerScreen<ElectricDrillMenu>{
  public Screen(ElectricDrillMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=176;imageHeight=210;inventoryLabelY=116;}
  protected void init(){super.init();addRenderableWidget(Button.builder(Component.literal("Next geological program"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,(menu.data.get(3)+1)%7)).bounds(leftPos+8,topPos+27,160,20).build());}
  protected void renderBg(GuiGraphics g,float f,int mx,int my){g.fill(leftPos,topPos,leftPos+176,topPos+210,0xffbcbcc6);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff555560);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xff888895);}g.drawString(font,ElectricDrillEntity.PROGRAMS[menu.data.get(3)],leftPos+8,topPos+18,0xff34343f,false);g.drawString(font,menu.data.get(0)+" EU / "+menu.data.get(4)+" EU/t",leftPos+49,topPos+57,0xff34343f,false);g.drawString(font,"Work "+menu.data.get(1)+" / "+menu.data.get(2),leftPos+49,topPos+70,0xff34343f,false);}
  public void render(GuiGraphics g,int x,int y,float f){super.render(g,x,y,f);renderTooltip(g,x,y);}
 }
}
