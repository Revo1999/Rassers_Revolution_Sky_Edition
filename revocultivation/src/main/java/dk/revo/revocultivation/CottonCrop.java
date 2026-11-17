package dk.revo.revocultivation;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

public final class CottonCrop extends CropBlock {
    public static final MapCodec<CottonCrop> CODEC=simpleCodec(CottonCrop::new);
    public CottonCrop(Properties properties){super(properties);}
    @Override public MapCodec<CottonCrop> codec(){return CODEC;}
    @Override protected ItemLike getBaseSeedId(){return RunningShoes.SEEDS.get();}
}
