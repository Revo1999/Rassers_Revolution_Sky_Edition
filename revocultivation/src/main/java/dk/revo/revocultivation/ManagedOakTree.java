package dk.revo.revocultivation;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

/** Real vanilla tree blocks with explicit ownership. Never harvest unrelated world blocks. */
public final class ManagedOakTree {
    private final Map<BlockPos,Boolean> owned=new LinkedHashMap<>();
    public boolean exists(){return !owned.isEmpty();}
    public static Map<BlockPos,Boolean> layout(BlockPos base){
        var result=new LinkedHashMap<BlockPos,Boolean>();
        for(int y=1;y<=6;y++)result.put(base.above(y),true);
        for(int y=4;y<=7;y++){
            int radius=y<=5?2:1;
            for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
                if(Math.abs(x)==radius&&Math.abs(z)==radius)continue;
                var p=base.offset(x,y,z);result.putIfAbsent(p,false);
            }
        }
        return result;
    }
    private boolean matches(Level level,BlockPos p,boolean log){return level.getBlockState(p).is(log?Blocks.OAK_LOG:Blocks.OAK_LEAVES);}
    public boolean intact(Level level){for(var e:owned.entrySet())if(!level.hasChunkAt(e.getKey())||!matches(level,e.getKey(),e.getValue()))return false;return true;}
    public boolean areaClear(Level level,BlockPos base){
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=1;y<=7;y++){
            var p=base.offset(x,y,z);if(!level.hasChunkAt(p)||level.isOutsideBuildHeight(p))return false;
            if(owned.containsKey(p)){if(!matches(level,p,owned.get(p)))return false;}
            else if(!level.getBlockState(p).isAir())return false;
        }
        return true;
    }
    public boolean grow(Level level,BlockPos base){
        if(exists()||!areaClear(level,base))return false;
        var layout=layout(base);
        for(var e:layout.entrySet()){
            var state=e.getValue()?Blocks.OAK_LOG.defaultBlockState():Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
            if(level.setBlock(e.getKey(),state,3))owned.put(e.getKey().immutable(),e.getValue());
            else{clear(level);return false;}
        }
        return true;
    }
    public void clear(Level level){
        for(var e:owned.entrySet())if(level.hasChunkAt(e.getKey())&&matches(level,e.getKey(),e.getValue()))level.removeBlock(e.getKey(),false);
        owned.clear();
    }
    public void save(CompoundTag tag){
        tag.putLongArray("OwnedOakLogs",owned.entrySet().stream().filter(Map.Entry::getValue).mapToLong(e->e.getKey().asLong()).toArray());
        tag.putLongArray("OwnedOakLeaves",owned.entrySet().stream().filter(e->!e.getValue()).mapToLong(e->e.getKey().asLong()).toArray());
    }
    public void load(CompoundTag tag,BlockPos base){
        owned.clear();var allowed=layout(base);
        for(long n:tag.getLongArray("OwnedOakLogs")){var p=BlockPos.of(n);if(Boolean.TRUE.equals(allowed.get(p)))owned.put(p,true);}
        for(long n:tag.getLongArray("OwnedOakLeaves")){var p=BlockPos.of(n);if(Boolean.FALSE.equals(allowed.get(p)))owned.put(p,false);}
    }
}
