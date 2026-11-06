package dk.revo.revoindustry.furnace;
import aztech.modern_industrialization.machines.models.*;
import ironfurnaces.Config;
import java.util.function.IntSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** All enabled, non-ATM Iron Furnaces tiers. Speeds follow the installed mod's configuration. */
public enum FurnaceTier {
 COPPER("copper",0,()->Config.copperFurnaceSpeed.get(),"modern_industrialization:bronze_machine_casing","modern_industrialization:bronze"),
 IRON("iron",0,()->Config.ironFurnaceSpeed.get(),"modern_industrialization:steel_machine_casing","modern_industrialization:steel"),
 SILVER("silver",1,()->Config.silverFurnaceSpeed.get(),null,null),
 GOLD("gold",1,()->Config.goldFurnaceSpeed.get(),null,null),
 DIAMOND("diamond",2,()->Config.diamondFurnaceSpeed.get(),null,null),
 EMERALD("emerald",2,()->Config.emeraldFurnaceSpeed.get(),null,null),
 OBSIDIAN("obsidian",2,()->Config.obsidianFurnaceSpeed.get(),null,null),
 CRYSTAL("crystal",2,()->Config.crystalFurnaceSpeed.get(),null,null),
 NETHERITE("netherite",2,()->Config.netheriteFurnaceSpeed.get(),"modern_industrialization:blastproof_casing","modern_industrialization:revo_netherite"),
 RAINBOW("rainbow",2,()->Config.millionFurnaceSpeed.get(),null,null);
 public final String id,casingId,casingKey;public final int factoryTier;private final IntSupplier speed;
 FurnaceTier(String id,int factoryTier,IntSupplier speed,String casingId,String casingKey){this.id=id;this.factoryTier=factoryTier;this.speed=speed;this.casingId=casingId==null?"revoindustry:"+id+"_furnace_casing":casingId;this.casingKey="revoindustry:"+id+"_furnace";}
 public String furnaceId(){return id.equals("rainbow")?"million_furnace":id+"_furnace";}
 public int lanes(){return 2+factoryTier*2;}
 public int batch(){return this==RAINBOW?64:1;}
 public int duration(int vanillaTicks){return Math.max(1,(int)Math.ceil(speed.getAsInt()*vanillaTicks/200d));}
 public Block casingBlock(){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(casingId));}
 public MachineCasing casing(){return MachineCasings.get(ResourceLocation.parse(casingKey));}
}
