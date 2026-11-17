package dk.revo.revoindustry.vacuum;
import dk.revo.revoindustry.RevoIndustry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import java.util.*;
public final class Vacuums {
 public static final List<DeferredBlock<VacuumBlock>> BLOCKS=new ArrayList<>();
 static { for(int i=0;i<3;i++){final int tier=i;BLOCKS.add(RevoIndustry.BLOCKS.register("vacuum_"+(i+1),()->new VacuumBlock(tier)));} }
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<VacuumEntity>> ENTITY=RevoIndustry.ENTITIES.register("vacuum",()->BlockEntityType.Builder.of(VacuumEntity::new,BLOCKS.stream().map(DeferredBlock::get).toArray(VacuumBlock[]::new)).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<VacuumMenu>> MENU=RevoIndustry.MENUS.register("vacuum",()->IMenuTypeExtension.create((id,inv,buf)->new VacuumMenu(id,inv,buf.readVarInt(),buf.readBlockPos())));
 private static final int[] SLOTS={1,3,9},SIZES={3,5,9},INTERVALS={8,4,1};
 public static int slots(int tier){return SLOTS[tier];}
 public static int size(int tier){return SIZES[tier];}
 /** The authored nozzle mouths are flush with the front face of their block. */
 public static net.minecraft.world.phys.Vec3 mouth(net.minecraft.core.BlockPos pos,net.minecraft.core.Direction outlet){return net.minecraft.world.phys.Vec3.atCenterOf(pos).relative(outlet.getOpposite(),.5);}
 public static net.minecraft.world.phys.AABB area(net.minecraft.core.BlockPos pos,int tier,net.minecraft.core.Direction outlet){var centre=mouth(pos,outlet).relative(outlet.getOpposite(),size(tier)/2.0);return net.minecraft.world.phys.AABB.ofSize(centre,size(tier),size(tier),size(tier));}

 public static int interval(int tier){return INTERVALS[tier];}
 public static void bootstrap(){}
 public static void capabilities(RegisterCapabilitiesEvent e){e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,ENTITY.get(),(be,side)->be.inventory);}
}
