package dk.revo.revocultivation;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Early textile/cobbler branch. Shoe rubber is deliberately not industrial rubber sheet. */
public final class RunningShoes {
    public static final List<String> COLORS=List.of("white","black","brown","red","orange","yellow","green","cyan","blue","purple","magenta","pink");
    public static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,RevoCultivation.MOD_ID);
    public static final DeferredItem<Item> COTTON=RevoCultivation.ITEMS.registerSimpleItem("cotton");
    public static final DeferredItem<Item> LATEX=RevoCultivation.ITEMS.registerSimpleItem("shoe_latex");
    public static final DeferredItem<Item> RUBBER=RevoCultivation.ITEMS.registerSimpleItem("natural_shoe_rubber");
    public static final DeferredItem<Item> GLUE=RevoCultivation.ITEMS.registerSimpleItem("shoe_glue");
    public static final DeferredBlock<CottonCrop> CROP=RevoCultivation.BLOCKS.register("cotton_crop",()->new CottonCrop(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredItem<ItemNameBlockItem> SEEDS=RevoCultivation.ITEMS.register("cotton_seeds",()->new ItemNameBlockItem(CROP.get(),new Item.Properties()));
    public static final Map<String,DeferredItem<RunningShoesItem>> SHOES=new LinkedHashMap<>();
    static {
        for(String color:COLORS){
            var material=MATERIALS.register("running_shoes_"+color,()->new ArmorMaterial(
                Map.of(ArmorItem.Type.BOOTS,0),10,SoundEvents.ARMOR_EQUIP_LEATHER,
                ()->Ingredient.of(RUBBER.get()),List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(RevoCultivation.MOD_ID,"running_shoes/"+color))),0,0));
            SHOES.put(color,RevoCultivation.ITEMS.register(color+"_running_shoes",()->new RunningShoesItem(material)));
        }
    }
    public static void bootstrap(IEventBus bus){MATERIALS.register(bus);}
    private RunningShoes(){}
}
