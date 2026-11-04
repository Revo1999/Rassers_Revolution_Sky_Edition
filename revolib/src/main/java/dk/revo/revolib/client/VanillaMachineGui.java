package dk.revo.revolib.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Reference vanilla's own slot/inventory artwork rather than repainting player slots. */
public final class VanillaMachineGui {
    private static final ResourceLocation CHEST=ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private VanillaMachineGui(){}
    public static void panel(GuiGraphics g,int x,int y,int width,int height){
        g.fill(x,y,x+width,y+height,0xFF373737);g.fill(x+1,y+1,x+width-1,y+height-1,0xFFC6C6C6);
        g.fill(x+1,y+1,x+width-2,y+3,0xFFFFFFFF);g.fill(x+1,y+1,x+3,y+height-2,0xFFFFFFFF);
        g.fill(x+width-3,y+3,x+width-1,y+height-1,0xFF555555);g.fill(x+3,y+height-3,x+width-1,y+height-1,0xFF555555);
    }
    public static void playerInventory(GuiGraphics g,int x,int y,int contentHeight){g.blit(CHEST,x,y+contentHeight-96,0,126,176,96);}
    public static void slot(GuiGraphics g,int x,int y){g.blit(CHEST,x-1,y-1,7,17,18,18);}
}
