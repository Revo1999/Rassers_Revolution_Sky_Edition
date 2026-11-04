package dk.revo.revocultivation.client;
import dk.revo.revocultivation.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class FarmHatchScreen extends AbstractContainerScreen<FarmHatchMenu> {
    public FarmHatchScreen(FarmHatchMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=menu.kind.hatchHeight();titleLabelX=8;titleLabelY=6;inventoryLabelX=8;inventoryLabelY=imageHeight-94;}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;VanillaMachineGui.panel(g,x,y,imageWidth,imageHeight);VanillaMachineGui.playerInventory(g,x,y,imageHeight);
        for(int i=0;i<menu.kind.inputSlots();i++)VanillaMachineGui.slot(g,x+(menu.kind.inputSlots()==1?80:62+i*36),y+25);
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)VanillaMachineGui.slot(g,x+62+col*18,y+menu.outputY()+row*18);
        if(menu.kind.needsWater()){g.fill(x+128,y+24,x+136,y+43,0xFF4B4F52);int fill=menu.water()*17/menu.kind.waterCapacity();g.fill(x+129,y+42-fill,x+135,y+42,0xFF397CB8);}
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        super.renderLabels(g,mx,my);if(menu.kind.inputSlots()>0){g.drawString(font,Component.translatable("gui.revocultivation.inputs"),8,21,0xFF404040,false);g.drawString(font,Component.translatable("gui.revocultivation.outputs"),8,51,0xFF404040,false);}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        super.render(g,mx,my,partial);renderTooltip(g,mx,my);
        if(menu.kind.needsWater()&&mx>=leftPos+127&&mx<leftPos+137&&my>=topPos+23&&my<topPos+44)g.renderTooltip(font,Component.translatable("gui.revocultivation.water_capacity",menu.water(),menu.kind.waterCapacity()),mx,my);
    }
}
