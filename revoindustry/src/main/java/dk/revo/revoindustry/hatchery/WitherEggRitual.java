package dk.revo.revoindustry.hatchery;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;

/** Independent of players: vanilla placement, dispensers and block placers all use the same ritual. */
public final class WitherEggRitual {
    private static final Set<ServerLevel> BUSY=Collections.newSetFromMap(new WeakHashMap<>());
    private static List<BlockPos> shape(ServerLevel level,BlockPos foot,Direction axis){
        var blocks=new ArrayList<BlockPos>();
        for(int y=0;y<3;y++)for(int x=-1;x<=1;x++){
            var p=foot.relative(axis,x).above(y);if(!level.hasChunkAt(p))return List.of();
            var s=level.getBlockState(p);
            if(y==0&&x!=0){if(!s.isAir())return List.of();continue;}
            if(y==2){if(!s.is(Blocks.WITHER_SKELETON_SKULL)&&!s.is(Blocks.WITHER_SKELETON_WALL_SKULL))return List.of();}
            else if(!s.is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))return List.of();
            blocks.add(p);
        }
        return blocks;
    }
    public static boolean convert(ServerLevel level,BlockPos placed){
        if(BUSY.contains(level)||!level.hasChunkAt(placed))return false;
        // The placed block may be any of the seven ingredients, including a base placed last.
        for(var axis:List.of(Direction.EAST,Direction.SOUTH))for(int y=0;y<3;y++)for(int x=-1;x<=1;x++){
            if(y==0&&x!=0)continue;
            var foot=placed.relative(axis,-x).below(y);var blocks=shape(level,foot,axis);
            if(!blocks.isEmpty())return commit(level,foot,blocks);
        }
        return false;
    }
    private static boolean commit(ServerLevel level,BlockPos foot,List<BlockPos> blocks){
        var egg=new ItemEntity(level,foot.getX()+.5,foot.getY()+.25,foot.getZ()+.5,new ItemStack(Hatchery.EGG.get()));
        egg.setDeltaMovement(0,0,0);egg.setPickUpDelay(0);
        BUSY.add(level);
        try {
            // Admit the collectible result first. A cancelled spawn leaves all seven inputs intact.
            if(!level.addFreshEntity(egg))return false;
            for(var p:blocks)level.setBlock(p,Blocks.AIR.defaultBlockState(),2);
            for(var p:blocks)level.updateNeighborsAt(p,Blocks.AIR);
            level.sendParticles(ParticleTypes.SOUL,foot.getX()+.5,foot.getY()+1,foot.getZ()+.5,30,.8,.8,.8,.02);
            level.playSound(null,foot,SoundEvents.WITHER_SPAWN,SoundSource.BLOCKS,.45f,1.5f);
            return true;
        } finally {BUSY.remove(level);}
    }
}
