package dk.revo.revoindustry.client;

import dk.revo.revoindustry.hatchery.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class HatcheryScreen extends AbstractContainerScreen<HatcheryMenu> {
    public HatcheryScreen(HatcheryMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=menu.hatch?184:132;inventoryLabelY=90;}
    private String state(){return switch(menu.value(2)){case 0->"Incomplete structure";case 1->"Waiting for an egg";case 2->"Insufficient power";case 3->"Output full";case 4->"Wither spawn blocked";default->switch(menu.value(0)){case 1->"Incubating egg";case 2->"Hatching Wither";default->"Neutralizing Wither";};};}
    protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        VanillaMachineGui.panel(g,leftPos,topPos,imageWidth,imageHeight);
        if(menu.hatch){for(int y=0;y<3;y++)for(int x=0;x<3;x++)VanillaMachineGui.slot(g,leftPos+61+x*18,topPos+26+y*18);VanillaMachineGui.playerInventory(g,leftPos,topPos,184);}
        else{
            g.fill(leftPos+8,topPos+42,leftPos+168,topPos+52,0xFF29272F);
            int ticks=menu.value(5),filled=ticks==0?0:Math.clamp(menu.value(1)*158/ticks,0,158);
            g.fill(leftPos+9,topPos+43,leftPos+9+filled,topPos+51,0xFFB53D48);
            g.fill(leftPos+8,topPos+81,leftPos+168,topPos+91,0xFF29272F);
            g.fill(leftPos+9,topPos+82,leftPos+9+menu.energy()*158/Hatchery.BUFFER_EU,topPos+90,0xFF70A4A0);
        }
    }
    protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,8,7,0x404040,false);
        if(menu.hatch){g.drawString(font,playerInventoryTitle,8,90,0x404040,false);return;}
        g.drawString(font,state(),8,25,0x404040,false);
        g.drawString(font,"Stage: "+menu.value(1)+" / "+menu.value(5)+" ticks",8,57,0x555555,false);
        g.drawString(font,"Power: "+Hatchery.EU_PER_TICK+" EU/t",8,69,0x404040,false);
        g.drawString(font,menu.energy()+" / "+Hatchery.BUFFER_EU+" EU",8,96,0x555555,false);
        if(minecraft.level.getBlockEntity(menu.pos) instanceof HatcheryController c&&menu.value(2)==0)g.drawWordWrap(font,Component.literal(c.problem()),8,109,148,0x7E343A);
        else g.drawString(font,"1 egg -> 1 Nether Star",8,109,0x404040,false);
        g.drawString(font,"R",158,120,0,false);
    }
    public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
