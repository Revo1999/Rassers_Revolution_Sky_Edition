package dk.revo.revoextraction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class DrillScreen extends AbstractContainerScreen<DrillMenu> {
    public DrillScreen(DrillMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title);
        imageWidth=230;imageHeight=154;
        titleLabelX=11;titleLabelY=11;
        inventoryLabelX=35;inventoryLabelY=136;
    }

    private void panel(GuiGraphics g,int x,int y,int width,int height,int fill) {
        g.fill(x,y,x+width,y+height,0xFF373735);
        g.fill(x+1,y+1,x+width-1,y+height-1,0xFFE5E2D8);
        g.fill(x+2,y+2,x+width-2,y+height-2,fill);
        g.fill(x+width-2,y+2,x+width-1,y+height-1,0xFF77776F);
        g.fill(x+2,y+height-2,x+width-1,y+height-1,0xFF77776F);
    }

    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        int x=leftPos,y=topPos;
        panel(g,x,y,imageWidth,imageHeight,0xFFC6C5BB);
        panel(g,x+10,y+37,210,35,0xFFB8B7AA);
        g.fill(x+11,y+114,x+imageWidth-11,y+123,0xFF55564F);
        g.fill(x+12,y+115,x+imageWidth-12,y+122,0xFF85867D);
        int progress=menu.progress()*(imageWidth-26)/1200;
        if(progress>0)g.fill(x+13,y+116,x+13+progress,y+121,0xFFA3874B);

    }

    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY) {
        g.drawString(font,title,titleLabelX,titleLabelY,0xFF404238,false);
        g.drawString(font,Component.translatable("gui.revoextraction.kinetic"),11,24,0xFF626257,false);
        RevoScreenBrand.draw(g,font,imageWidth-18,imageHeight-16);
        if(menu.waterCooled()){
            g.drawString(font,Component.translatable("gui.revoextraction.water",menu.water(),DrillWaterTankEntity.CAPACITY),103,83,0xFF28596C,false);
            if(menu.dryTicks()>0&&menu.state()!=8)g.drawString(font,Component.translatable("gui.revoextraction.overheat",menu.dryTicks()),12,138,0xFF903E2C,false);
        }
        var diagnostics=menu.snapshot();
        int color=menu.state()==1?0xFF38552C:menu.state()==0||menu.state()==5?0xFF844D38:0xFF6F5932;
        g.drawString(font,diagnostics.stateText(),14,42,color,false);
        g.drawWordWrap(font,menu.state()==6?Component.translatable("gui.revoextraction.hint.minimum_rpm",menu.waterCooled()?64:32):diagnostics.hint(),14,54,198,0xFF42443B);
        g.drawString(font,Component.translatable("gui.revoextraction.rpm",menu.rpm()),12,83,0xFF404238,false);
        g.drawString(font,Component.translatable("gui.revoextraction.cycle",menu.progress()*100/1200),12,101,0xFF404238,false);
        g.drawString(font,Component.translatable("gui.revoextraction.emi"),12,125,0xFF626257,false);
    }

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial) {
        super.render(graphics,mouseX,mouseY,partial);
        renderTooltip(graphics,mouseX,mouseY);
        if(mouseX>=leftPos+imageWidth-20&&mouseX<leftPos+imageWidth-7&&mouseY>=topPos+imageHeight-18&&mouseY<topPos+imageHeight-5)
            graphics.renderTooltip(font,Component.translatable("gui.revoextraction.brand"),mouseX,mouseY);
    }
}
