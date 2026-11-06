package dk.revo.revoindustry.client;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.furnace.*;
import aztech.modern_industrialization.client.machines.*;
import aztech.modern_industrialization.client.machines.gui.*;
import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class FurnaceClient {
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->GuiComponentsClient.register(FurnaceGui.TYPE,Status::new));}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
  e.registerBlockEntityRenderer(Furnaces.CONTROLLER_ENTITY.get(),ctx->new Dispatch<>(ctx,true));
  e.registerBlockEntityRenderer(Furnaces.ITEM_ENTITY.get(),ctx->new Dispatch<>(ctx,false));
  e.registerBlockEntityRenderer(Furnaces.FUEL_ENTITY.get(),ctx->new Dispatch<>(ctx,false));
  e.registerBlockEntityRenderer(Furnaces.VENT_ENTITY.get(),ctx->new Dispatch<>(ctx,false));
 }
 /** MI caches a block model per BER; these addon BE types intentionally serve several blocks. */
 private static final class Dispatch<T extends aztech.modern_industrialization.machines.MachineBlockEntity> implements net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T>{
  private final net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context;private final boolean multiblock;
  private final java.util.Map<net.minecraft.world.level.block.Block,net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T>> renderers=new java.util.HashMap<>();
  Dispatch(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context,boolean multiblock){this.context=context;this.multiblock=multiblock;}
  @SuppressWarnings("unchecked") private net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T> renderer(T machine){return renderers.computeIfAbsent(machine.getBlockState().getBlock(),b->(net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T>)(multiblock?new MultiblockMachineBER(context):new MachineBlockEntityRenderer(context)));}
  public void render(T machine,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,int overlay){renderer(machine).render(machine,partial,pose,buffers,light,overlay);}
  public net.minecraft.world.phys.AABB getRenderBoundingBox(T machine){return new net.minecraft.world.phys.AABB(machine.getBlockPos()).inflate(multiblock?4:0);}
  public boolean shouldRenderOffScreen(T machine){return multiblock;}
 }
 private static final class Status extends GuiComponentClient<Unit,String>{
  Status(Unit params,String data){super(params,data);}
  public ClientComponentRenderer createRenderer(MachineScreen screen){return new ClientComponentRenderer(){public void renderBackground(GuiGraphics g,int x,int y){g.fill(x+5,y+16,x+171,y+144,0xFF272D2B);int lineY=y+21;var font=Minecraft.getInstance().font;for(String line:data.split("\n")){for(var part:font.split(Component.literal(line),154)){g.drawString(font,part,x+10,lineY,0xE3E8DE,false);lineY+=10;}}}};}
 }
}
