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
 public static final DeferredHolder<MenuType<?>,MenuType<VacuumMenu>> MENU=RevoIndustry.MENUS.register("vacuum",()->IMenuTypeExtension.create((id,inv,buf)->new VacuumMenu(id,inv,buf.readVarInt())));
 public static int slots(int tier){return new int[]{1,3,9}[tier];}
 public static int radius(int tier){return 3+tier*2;}
 public static int interval(int tier){return new int[]{8,4,1}[tier];}
 public static void bootstrap(){}
 public static void capabilities(RegisterCapabilitiesEvent e){e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,ENTITY.get(),(be,side)->be.inventory);}
}
