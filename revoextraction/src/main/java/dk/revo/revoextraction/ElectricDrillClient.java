package dk.revo.revoextraction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid="revoextraction",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class ElectricDrillClient {
 @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(RevoExtraction.ELECTRIC_MENU.get(),Screen::new);}
 private static net.minecraft.client.resources.model.ModelResourceLocation model(String name){return net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.parse("revoextraction:block/"+name));}
 @SubscribeEvent public static void models(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional e){for(String n:new String[]{"electric_drill_body","electric_drill_fan","digital_input","digital_output","digital_energy"})e.register(model(n));}
 @SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(RevoExtraction.ELECTRIC_ENTITY.get(),c->new net.minecraft.client.renderer.blockentity.BlockEntityRenderer<ElectricDrillEntity>(){
  private void draw(String name,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,int overlay){var mc=net.minecraft.client.Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(net.minecraft.client.renderer.RenderType.cutout()),null,mc.getModelManager().getModel(model(name)),1,1,1,light,overlay);}
  public net.minecraft.world.phys.AABB getRenderBoundingBox(ElectricDrillEntity m){return new net.minecraft.world.phys.AABB(m.getBlockPos()).inflate(m.isDigital()?0:3);}
  public boolean shouldRenderOffScreen(ElectricDrillEntity m){return !m.isDigital();}
  public void render(ElectricDrillEntity m,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,int overlay){
   if(m.isDigital()){for(var side:net.minecraft.core.Direction.values()){int mode=m.sideMode(side);if(mode==0)continue;pose.pushPose();pose.translate(.5,.5,.5);switch(side){case SOUTH->pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));case EAST->pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));case WEST->pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));case UP->pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));case DOWN->pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));default->{}}pose.translate(-.5,-.5,-.5);draw("digital_"+new String[]{"off","input","output","energy"}[mode],pose,buffers,light,overlay);pose.popPose();}return;}
   if(!m.formed())return;var center=ElectricDrillStructure.at(m,0,0,0);var origin=m.getBlockPos();pose.pushPose();pose.translate(center.getX()-origin.getX()+.5,center.getY()-origin.getY(),center.getZ()-origin.getZ()+.5);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180-m.facing().toYRot()));pose.translate(-1.5,0,-1.5);pose.scale(3,3,3);draw("electric_drill_body",pose,buffers,light,overlay);
   float angle=net.minecraft.util.Mth.lerp(partial,m.previousAngle,m.angle);pose.pushPose();pose.translate(.5,.375,.5);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));pose.translate(-.5,-.375,-.5);draw("steel_drill_drill_bit",pose,buffers,light,overlay);pose.popPose();pose.pushPose();pose.translate(46.0/48,24.0/48,.5);pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-angle));pose.translate(-46.0/48,-24.0/48,-.5);draw("steel_drill_drive_gear",pose,buffers,light,overlay);pose.popPose();pose.popPose();
  }
 });}
 public static class Screen extends AbstractContainerScreen<ElectricDrillMenu>{
  public Screen(ElectricDrillMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=276;imageHeight=210;inventoryLabelY=116;}
  private boolean sides;
  private Button sideToggle;private final java.util.List<Button> faceButtons=new java.util.ArrayList<>();
  private static final int[] COLORS={0xff777780,0xff4696f5,0xfff0852d,0xfff5d237};
  protected void init(){super.init();sideToggle=addRenderableWidget(Button.builder(Component.literal("Sides"),b->{sides=!sides;}).bounds(leftPos+178,topPos+8,50,20).build());for(var d:net.minecraft.core.Direction.values()){int[] xs={1,1,1,3,0,2},ys={2,0,1,1,1,1};faceButtons.add(addRenderableWidget(Button.builder(Component.literal(d.getName().substring(0,1).toUpperCase()),b->{if(menu.data.get(8)==1&&sides)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,100+d.ordinal());}).bounds(leftPos+180+xs[d.ordinal()]*23,topPos+45+ys[d.ordinal()]*23,22,22).build()));}addRenderableWidget(Button.builder(Component.literal("Next geological program"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,(menu.data.get(3)+1)%ElectricDrillEntity.PROGRAMS.length)).bounds(leftPos+8,topPos+27,160,20).build());}
  protected void renderBg(GuiGraphics g,float f,int mx,int my){if(sides&&menu.data.get(8)==1){g.fill(leftPos+177,topPos+32,leftPos+276,topPos+155,0xff34343f);int[] xs={1,1,1,3,0,2},ys={2,0,1,1,1,1};for(var d:net.minecraft.core.Direction.values()){int x=leftPos+180+xs[d.ordinal()]*23,y=topPos+45+ys[d.ordinal()]*23;g.fill(x-1,y-1,x+23,y+23,COLORS[menu.data.get(9+d.ordinal())]);}g.drawString(font,"Blue: input",leftPos+181,topPos+119,COLORS[1],false);g.drawString(font,"Orange: output",leftPos+181,topPos+130,COLORS[2],false);g.drawString(font,"Yellow: EU",leftPos+181,topPos+141,COLORS[3],false);}g.fill(leftPos,topPos,leftPos+176,topPos+210,0xffbcbcc6);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff555560);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xff888895);}g.drawString(font,ElectricDrillEntity.PROGRAMS[menu.data.get(3)],leftPos+8,topPos+18,0xff34343f,false);g.drawString(font,menu.data.get(0)+" EU / "+menu.data.get(4)+" EU/t",leftPos+49,topPos+57,0xff34343f,false);g.drawString(font,"Work "+menu.data.get(1)+" / "+menu.data.get(2),leftPos+49,topPos+70,0xff34343f,false);}
  protected void renderLabels(GuiGraphics g,int mx,int my){super.renderLabels(g,mx,my);String cooling=menu.data.get(8)==1?"Dry sediment | no coolant":switch(menu.data.get(7)){case 1->"Fit rear MI fluid hatch";case 2->"Drain sediment hatch";case 3->"Needs cooling water";case 4->"Incomplete 3x3 structure";default->menu.data.get(3)==5?"Glacial ice: bucket input":"Water "+menu.data.get(5)+" | "+(menu.data.get(4)>=128?"Excellent":"Good");};g.drawString(font,cooling,8,106,0xff34343f,false);}
  public void render(GuiGraphics g,int x,int y,float f){sideToggle.visible=menu.data.get(8)==1;for(var b:faceButtons)b.visible=sideToggle.visible&&sides;super.render(g,x,y,f);renderTooltip(g,x,y);}
 }
}
