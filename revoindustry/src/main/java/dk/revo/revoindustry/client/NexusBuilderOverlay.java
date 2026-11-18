package dk.revo.revoindustry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.nexus.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Local display toggle only. The server remains authoritative for obstruction and building. */
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,value=Dist.CLIENT)
public final class NexusBuilderOverlay {
 private record Key(ResourceKey<Level> dimension,BlockPos pos){}
 private static final Set<Key> visible=new HashSet<>();
 public static boolean shown(BlockPos pos){var l=Minecraft.getInstance().level;return l!=null&&visible.contains(new Key(l.dimension(),pos));}
 public static void toggle(BlockPos pos){var l=Minecraft.getInstance().level;if(l==null)return;var key=new Key(l.dimension(),pos.immutable());if(!visible.remove(key))visible.add(key);}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){visible.clear();}
 @SubscribeEvent public static void render(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||visible.isEmpty())return;var pose=new PoseStack();pose.mulPose(e.getModelViewMatrix());var camera=e.getCamera().getPosition();var buffers=mc.renderBuffers().bufferSource();var lines=buffers.getBuffer(RenderType.lines());for(var it=visible.iterator();it.hasNext();){var key=it.next();if(!key.dimension.equals(mc.level.dimension())||!mc.level.hasChunkAt(key.pos))continue;if(!(mc.level.getBlockEntity(key.pos) instanceof NexusMachine m)||m.construction==null){it.remove();continue;}if(key.pos.distToCenterSqr(camera)>128*128)continue;int status=m.construction.status();boolean blocked=status==NexusConstruction.OBSTRUCTED||status==NexusConstruction.UNLOADED;LevelRenderer.renderLineBox(pose,lines,m.construction.bounds().move(-camera.x,-camera.y,-camera.z),blocked?1:.2f,blocked?.2f:.9f,.35f,.9f);}buffers.endBatch(RenderType.lines());}
}
