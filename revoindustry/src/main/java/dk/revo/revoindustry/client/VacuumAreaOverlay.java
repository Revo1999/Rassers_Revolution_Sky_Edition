package dk.revo.revoindustry.client;
import com.mojang.blaze3d.vertex.*;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.vacuum.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Local display preference: each machine can be outlined independently until toggled off or logout. */
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,value=Dist.CLIENT)
public final class VacuumAreaOverlay {
 private record Key(ResourceKey<Level> dimension,BlockPos pos){}
 private static final Set<Key> VISIBLE=new HashSet<>();
 public static boolean isShown(BlockPos pos){var l=Minecraft.getInstance().level;return l!=null&&VISIBLE.contains(new Key(l.dimension(),pos));}
 public static void toggle(BlockPos pos){var l=Minecraft.getInstance().level;if(l==null)return;var key=new Key(l.dimension(),pos.immutable());if(!VISIBLE.remove(key))VISIBLE.add(key);}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){VISIBLE.clear();}
 private record Placement(BlockPos pos,int tier,net.minecraft.core.Direction outlet){}
 private static Placement placement(){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||!(mc.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK)return null;
  for(var hand:InteractionHand.values())if(mc.player.getItemInHand(hand).getItem() instanceof BlockItem item&&item.getBlock() instanceof VacuumBlock block){
   var context=new BlockPlaceContext(new UseOnContext(mc.player,hand,hit));var pos=context.getClickedPos();
   if(context.canPlace()&&!mc.level.isOutsideBuildHeight(pos)&&mc.level.getWorldBorder().isWithinBounds(pos))return new Placement(pos.immutable(),block.tier,block.getStateForPlacement(context).getValue(VacuumBlock.OUTLET));
  }return null;
 }
 public static BlockPos placementPosition(){var p=placement();return p==null?null:p.pos;}
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  var preview=placement();if(VISIBLE.isEmpty()&&preview==null)return;
  var camera=e.getCamera().getPosition();var pose=new PoseStack();pose.mulPose(e.getModelViewMatrix());var buffers=mc.renderBuffers().bufferSource();var lines=buffers.getBuffer(RangeLines.TYPE);
  for(var it=VISIBLE.iterator();it.hasNext();){var key=it.next();if(!key.dimension.equals(mc.level.dimension())||!mc.level.hasChunkAt(key.pos))continue;
   if(!(mc.level.getBlockState(key.pos).getBlock() instanceof VacuumBlock vacuum)){it.remove();continue;}
   if(key.pos.distToCenterSqr(camera)>128*128)continue;
   box(pose,lines,key.pos,camera,vacuum.tier,mc.level.getBlockState(key.pos).getValue(VacuumBlock.OUTLET),.2f,.85f,1f);
  }
  if(preview!=null){box(pose,lines,preview.pos,camera,preview.tier,preview.outlet,.35f,1f,.45f);pose.pushPose();pose.translate(preview.pos.getX()-camera.x,preview.pos.getY()-camera.y,preview.pos.getZ()-camera.z);LevelRenderer.renderLineBox(pose,lines,new AABB(0,0,0,1,1,1),.6f,1f,.65f,.9f);pose.popPose();}
  buffers.endBatch(RangeLines.TYPE);
 }
 private static void box(PoseStack pose,VertexConsumer lines,BlockPos pos,Vec3 camera,int tier,net.minecraft.core.Direction outlet,float red,float green,float blue){
  LevelRenderer.renderLineBox(pose,lines,Vacuums.area(pos,tier,outlet).move(-camera.x,-camera.y,-camera.z),red,green,blue,.85f);
 }
 /** Transparent lines remain readable through terrain without modifying the world's depth buffer. */
 private static final class RangeLines extends RenderType {
  private RangeLines(){super("unused",DefaultVertexFormat.POSITION_COLOR_NORMAL,VertexFormat.Mode.LINES,1536,false,false,()->{},()->{});}
  private static final DepthTestStateShard XRAY=new DepthTestStateShard("vacuum_area_always",519){
   private boolean previous;
   public void setupRenderState(){previous=org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();}
   public void clearRenderState(){if(previous)com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();else com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();}
  };
  static final RenderType TYPE=create("revo_vacuum_range",DefaultVertexFormat.POSITION_COLOR_NORMAL,VertexFormat.Mode.LINES,1536,false,false,CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new LineStateShard(OptionalDouble.of(2))).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(XRAY).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
 }
}
