package dk.revo.revoindustry.client;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.vacuum.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class VacuumScreen extends AbstractContainerScreen<VacuumMenu> {
 public VacuumScreen(VacuumMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=216;inventoryLabelY=122;}
 @SubscribeEvent public static void register(RegisterMenuScreensEvent e){e.register(Vacuums.MENU.get(),VacuumScreen::new);}
 protected void renderBg(GuiGraphics g,float p,int mx,int my){VanillaMachineGui.panel(g,leftPos,topPos,imageWidth,imageHeight);VanillaMachineGui.playerInventory(g,leftPos,topPos,imageHeight);for(int i=0;i<Vacuums.slots(menu.tier);i++)VanillaMachineGui.slot(g,leftPos+VacuumMenu.x(menu.tier,i),topPos+VacuumMenu.y(menu.tier,i));}
 protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,7,0x404040,false);g.drawString(font,"Radius: "+Vacuums.radius(menu.tier)+" blocks",8,87,0x404040,false);g.drawString(font,"Transfer: "+(20f/Vacuums.interval(menu.tier))+" items/s",8,99,0x404040,false);g.drawString(font,"Outlet feeds attached inventory",8,111,0x555555,false);g.drawString(font,playerInventoryTitle,8,122,0x404040,false);}
 public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}
