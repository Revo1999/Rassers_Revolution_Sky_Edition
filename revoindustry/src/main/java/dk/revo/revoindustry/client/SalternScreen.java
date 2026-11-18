package dk.revo.revoindustry.client;
import dk.revo.revoindustry.saltern.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class SalternScreen extends AbstractContainerScreen<SalternMenu>{
    public SalternScreen(SalternMenu m,Inventory i,Component title){super(m,i,title);imageWidth=176;imageHeight=144;}
    protected void renderBg(GuiGraphics g,float partial,int mx,int my){VanillaMachineGui.panel(g,leftPos,topPos,176,144);g.fill(leftPos+8,topPos+42,leftPos+168,topPos+52,0xFF28313B);g.fill(leftPos+9,topPos+43,leftPos+9+menu.value(1)*158/400,topPos+51,0xFF93C3DB);}
    protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,7,0x404040,false);String state=switch(menu.value(0)){case 0->"Check structure";case 1->"Needs seawater";case 2->"Needs electricity";case 3->"Salt-water output full";default->menu.value(1)<330?"Growing submerged crystals":"Harvesting into brine";};g.drawString(font,state,8,25,0x404040,false);g.drawString(font,"Seawater: "+menu.value(2)+" / 8000 mB",8,62,0x404040,false);g.drawString(font,"Brine: "+menu.value(3)+" / 8000 mB",8,76,0x404040,false);g.drawString(font,menu.value(4)+" EU | 16 EU/t",8,90,0x404040,false);g.drawString(font,"Seawater -> salt water",8,107,0x555555,false);g.drawString(font,"Next: MI Centrifuge",8,120,0x555555,false);g.drawString(font,"R",158,132,0,false);}
    public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}
