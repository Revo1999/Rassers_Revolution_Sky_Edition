package dk.revo.revoextraction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import dk.revo.revolib.PlayerInventoryLayout;
import dk.revo.revolib.client.VanillaMachineGui;

/** MI-inspired layout: hatch slot grid above the player inventory. */
public final class DrillHatchScreen extends AbstractContainerScreen<DrillHatchMenu> {
    public DrillHatchScreen(DrillHatchMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title);
        imageWidth=PlayerInventoryLayout.WIDTH;imageHeight=DrillHatchMenu.HEIGHT;
        titleLabelX=8;titleLabelY=6;inventoryLabelX=8;inventoryLabelY=DrillHatchMenu.CONTENT_HEIGHT-94;
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        int x=leftPos,y=topPos;
        VanillaMachineGui.panel(g,x,y,imageWidth,imageHeight);
        VanillaMachineGui.playerInventory(g,x,y,DrillHatchMenu.CONTENT_HEIGHT);
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)VanillaMachineGui.slot(g,x+DrillHatchMenu.OUTPUT_X+col*18,y+DrillHatchMenu.OUTPUT_Y+row*18);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial) {
        super.render(g,mouseX,mouseY,partial);renderTooltip(g,mouseX,mouseY);
    }
}
