package dk.revo.revoindustry.client;

import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.nexus.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class NexusBuilderScreen extends AbstractContainerScreen<NexusBuilderMenu> {
 private Button build,preview;
 public NexusBuilderScreen(NexusBuilderMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=272;imageHeight=222;inventoryLabelX=55;inventoryLabelY=133;}
 @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(Nexus.BUILDER_MENU.get(),NexusBuilderScreen::new);}
 @Override protected void init(){super.init();build=addRenderableWidget(Button.builder(Component.literal("Build"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1)).bounds(leftPos+194,topPos+113,70,16).build());preview=addRenderableWidget(Button.builder(Component.literal("Box"),b->NexusBuilderOverlay.toggle(menu.pos)).bounds(leftPos+234,topPos+3,30,14).tooltip(Tooltip.create(Component.literal("Toggle the portal bounding box in the world"))).build());}
 private int cell(int x,int y){int col=(x-leftPos-8)/32,row=(y-topPos-20)/20;if(x<leftPos+8||x>=leftPos+264||y<topPos+20||y>=topPos+100||col<0||row<0)return -1;int i=row*8+col;return i<NexusConstruction.requirements().size()?i:-1;}
 @Override public boolean mouseClicked(double x,double y,int button){int i=cell((int)x,(int)y);if(i>=0&&(button==0||button==1)){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,200+i);return true;}return super.mouseClicked(x,y,button);}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
  g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff232731);g.fill(leftPos+2,topPos+2,leftPos+270,topPos+220,0xffc3c5cc);
  var needs=NexusConstruction.requirements();for(int i=0;i<needs.size();i++){
   int x=leftPos+8+i%8*32,y=topPos+20+i/8*20;var need=needs.get(i);boolean filled=menu.stored(i)+menu.used(i)>0;g.fill(x,y,x+30,y+19,filled?0xff59656a:0xff777b85);
   g.setColor(filled?1:.5f,filled?1:.5f,filled?1:.5f,filled?1:.3f);g.renderItem(new ItemStack(need.item()),x+7,y);g.flush();g.setColor(1,1,1,1);
   if(!filled){g.pose().pushPose();g.pose().translate(0,0,250);g.fill(x+6,y,x+24,y+15,0xa0777b85);g.pose().popPose();}
   String count=(menu.stored(i)+menu.used(i))+"/"+need.amount();g.pose().pushPose();g.pose().translate(x+15,y+14,300);g.pose().scale(.65f,.65f,1);g.drawString(font,count,-font.width(count)/2,0,filled?0xfff2f2ed:0xffb5b7bf,false);g.pose().popPose();
  }
  int total=menu.required(),supplied=menu.supplied(),width=256*supplied/Math.max(1,total);g.fill(leftPos+8,topPos+102,leftPos+264,topPos+112,0xff444851);g.fill(leftPos+8,topPos+102,leftPos+8+width,topPos+112,0xff4f8b69);String progress=supplied+" / "+total+"  ("+(100*supplied/Math.max(1,total))+"%)";g.drawCenteredString(font,progress,leftPos+136,topPos+103,0xffffffff);
  String status=switch(menu.data.get(2)){case NexusConstruction.OBSTRUCTED->"Obstructed - cannot build";case NexusConstruction.UNLOADED->"Build area not loaded";case NexusConstruction.STRUCTURE->"Missing MI hatches";case NexusConstruction.PROGRAM->"Needs Construction Program";case NexusConstruction.MATERIALS->"Supply blueprint materials";case NexusConstruction.POWER->"Charge before building";case NexusConstruction.BUILDING->"Building "+(100*menu.data.get(0)/NexusConstruction.cells().size())+"%";case NexusConstruction.COMPLETE->"Construction complete";default->"Ready to build";};g.drawString(font,status,leftPos+8,topPos+115,menu.data.get(2)==NexusConstruction.OBSTRUCTED?0xffa12f39:0xff303641,false);
  String power=String.format(java.util.Locale.ROOT,"Power %.1f / 100.0 M EU",menu.energy()/1_000_000d);g.pose().pushPose();g.pose().translate(leftPos+8,topPos+125,0);g.pose().scale(.8f,.8f,1);g.drawString(font,power,0,0,0xff303641,false);g.pose().popPose();
  for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff4d515d);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xff858993);}
 }
 @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,8,6,0xff252c38,false);g.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xff303641,false);}
 @Override public void render(GuiGraphics g,int x,int y,float partial){build.visible=menu.supplied()==menu.required()&&menu.data.get(1)==0;build.active=menu.data.get(2)==NexusConstruction.READY;preview.setMessage(Component.literal(NexusBuilderOverlay.shown(menu.pos)?"Box*":"Box"));super.render(g,x,y,partial);renderTooltip(g,x,y);int i=cell(x,y);if(i>=0){var r=NexusConstruction.requirements().get(i);g.renderComponentTooltip(font,java.util.List.of(new ItemStack(r.item()).getHoverName(),Component.literal("Stored "+menu.stored(i)+" / "+r.amount()+" | Placed "+menu.used(i)),Component.literal("Deposit carried items; empty hand withdraws up to 64")),x,y);}}
}
