package dk.revo.revocultivation.client;
import dk.revo.revocultivation.*;
import dk.revo.revolib.client.VanillaMachineGui;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;
public class FarmControllerScreen extends AbstractContainerScreen<FarmControllerMenu> {
    public FarmControllerScreen(FarmControllerMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=230;imageHeight=menu.harvester?294:190;inventoryLabelX=35;inventoryLabelY=200;}
    @Override protected void init(){super.init();if(ModList.get().isLoaded("emi"))addRenderableWidget(Button.builder(Component.translatable("gui.revocultivation.guide"),b->FarmEmiPlugin.open(menu.kind())).bounds(leftPos+11,topPos+(menu.harvester?187:163),100,18).build());}
    public static Component stateText(int state){return Component.translatable("state.revocultivation."+state);}
    private Component hint(){
        if(menu.value(1)==6&&menu.kind().realTree())return Component.translatable("hint.revocultivation.real_tree");
        if(menu.value(1)!=7)return Component.translatable("hint.revocultivation."+menu.value(1));
        String part=menu.value(6)>0?MachineKind.Part.values()[Math.clamp(menu.value(6)-1,0,MachineKind.Part.values().length-1)].name().toLowerCase():"controller";
        return Component.translatable("hint.revocultivation.facing",Component.translatable("part.revocultivation."+part),menu.value(7),menu.value(8),menu.value(9),Component.translatable("direction.revocultivation."+Direction.from3DDataValue(menu.value(10)).getName()));
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;VanillaMachineGui.panel(g,x,y,imageWidth,imageHeight);
        g.fill(x+10,y+34,x+220,y+81,0xFF55584E);g.fill(x+11,y+35,x+219,y+80,0xFFB6B9AA);
        g.fill(x+11,y+141,x+219,y+151,0xFF55584E);g.fill(x+12,y+142,x+218,y+150,0xFF8C9184);
        int fill=menu.value(2)*204/Math.max(1,menu.value(3));if(fill>0)g.fill(x+13,y+143,x+13+fill,y+149,0xFF6E8D40);
        if(menu.harvester){
            VanillaMachineGui.slot(g,x+116,y+166);
            for(int row=0;row<3;row++)for(int col=0;col<9;col++)VanillaMachineGui.slot(g,x+35+col*18,y+212+row*18);
            for(int col=0;col<9;col++)VanillaMachineGui.slot(g,x+35+col*18,y+270);
        }
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,10,9,0xFF353B2F,false);g.drawString(font,menu.kind().tierText(),10,22,0xFF626957,false);
        g.drawString(font,stateText(menu.value(1)),14,40,menu.value(1)==1?0xFF38592D:0xFF78543B,false);g.drawWordWrap(font,hint(),14,53,198,0xFF41463A);
        g.drawString(font,Component.translatable("gui.revocultivation.program",Component.translatable("program.revocultivation."+MachineKind.programName(menu.value(5)))),11,88,0xFF41463A,false);
        g.drawString(font,Component.translatable("gui.revocultivation.cycle",menu.value(2)*100/Math.max(1,menu.value(3)),MachineKind.seconds(menu.value(3))),11,106,0xFF41463A,false);
        g.drawString(font,menu.kind().needsWater()?Component.translatable("gui.revocultivation.water_capacity",menu.value(4),menu.kind().waterCapacity()):Component.translatable(menu.kind().isHarvester()?"gui.revocultivation.passive":"gui.revocultivation.daylight"),11,124,0xFF626957,false);
        if(menu.kind().electric())g.drawString(font,Component.translatable("gui.revocultivation.energy",menu.value(13),menu.value(14),menu.value(15)),11,153,0xFF3C606A,false);
        g.drawString(font,"R",imageWidth-18,imageHeight-16,0xFF000000,false);
        if(menu.harvester){g.drawString(font,Component.translatable("gui.revocultivation.mob_spawner"),11,170,0xFF41463A,false);g.drawString(font,playerInventoryTitle,35,200,0xFF41463A,false);}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
