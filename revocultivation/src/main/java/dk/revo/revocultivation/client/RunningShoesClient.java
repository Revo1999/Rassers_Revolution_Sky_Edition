package dk.revo.revocultivation.client;

import dk.revo.revocultivation.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid=RevoCultivation.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class RunningShoesClient {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"running_shoes"),"main");
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(LAYER,RunningShoesClient::layer);}
    @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->ItemBlockRenderTypes.setRenderLayer(RunningShoes.CROP.get(),RenderType.cutout()));}
    @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent event){
        var extension=new IClientItemExtensions(){
            private HumanoidModel<?> model;
            @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){
                if(model==null)model=new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
                return model;
            }
        };
        for(var item:RunningShoes.SHOES.values())event.registerItem(extension,item.get());
    }
    private static LayerDefinition layer(){return RunningShoesGeometry.createBodyLayer();}
}
