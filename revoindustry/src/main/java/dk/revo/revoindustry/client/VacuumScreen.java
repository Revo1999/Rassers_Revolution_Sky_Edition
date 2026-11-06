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
 public VacuumScreen(VacuumMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=238;inventoryLabelY=144;}
 private net.minecraft.client.gui.components.Button area;
 protected void init(){super.init();area=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(areaLabel(),b->{VacuumAreaOverlay.toggle(menu.position);b.setMessage(areaLabel());}).bounds(leftPos+8,topPos+111,160,20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.revoindustry.vacuum.area_hint"))).build());}
 private Component areaLabel(){return Component.translatable(VacuumAreaOverlay.isShown(menu.position)?"gui.revoindustry.vacuum.hide_area":"gui.revoindustry.vacuum.show_area");}
 @SubscribeEvent public static void register(RegisterMenuScreensEvent e){e.register(Vacuums.MENU.get(),VacuumScreen::new);}
 protected void renderBg(GuiGraphics g,float p,int mx,int my){VanillaMachineGui.panel(g,leftPos,topPos,imageWidth,imageHeight);VanillaMachineGui.playerInventory(g,leftPos,topPos,imageHeight);for(int i=0;i<Vacuums.slots(menu.tier);i++)VanillaMachineGui.slot(g,leftPos+VacuumMenu.x(menu.tier,i),topPos+VacuumMenu.y(menu.tier,i));}
 protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,7,0x404040,false);g.drawString(font,"Area: "+Vacuums.size(menu.tier)+" x "+Vacuums.size(menu.tier)+" x "+Vacuums.size(menu.tier),8,87,0x404040,false);g.drawString(font,"Transfer: "+(20f/Vacuums.interval(menu.tier))+" items/s",8,99,0x404040,false);g.drawString(font,playerInventoryTitle,8,144,0x404040,false);}
 public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}
