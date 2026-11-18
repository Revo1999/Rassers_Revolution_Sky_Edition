package dk.revo.revoextraction;

import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasings;

/** All native hatch cells are above the complete swept volume of the drill. */
public final class ElectricDrillStructure {
 public static BlockPos at(ElectricDrillEntity e,int x,int y,int z){var f=e.facing();var r=f.getClockWise();return e.getBlockPos().offset(r.getStepX()*x+f.getStepX()*(z-1),y-1,r.getStepZ()*x+f.getStepZ()*(z-1));}
 public static String role(int x,int y,int z){if(y!=2)return "";if(x==0&&z==-1)return "sediment";if(x==1&&z==0)return "item_output";if(x==0&&z==1)return "item_input";if(x==-1&&z==0)return "energy_input";if(x==-1&&z==-1)return "fluid_input";return "";}
 public static Block block(int x,int y,int z){String role=role(x,y,z);if(!role.isEmpty())return role.equals("sediment")?RevoExtraction.STEEL_FLUID_HATCH.get():BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:"+(role.equals("energy_input")?"lv_energy_input_hatch":"steel_"+role+"_hatch")));var part=DrillLayout.part(x,y,z);return part==DrillLayout.Part.CONTROLLER?RevoExtraction.ELECTRIC.get():DrillTier.STEEL.block(part==DrillLayout.Part.INPUT||part==DrillLayout.Part.HATCH?DrillLayout.Part.CASING:part);}
 public static HatchBlockEntity hatch(ElectricDrillEntity e,int x,int z){var p=at(e,x,2,z);return e.getLevel()!=null&&e.getLevel().hasChunkAt(p)&&e.getLevel().getBlockEntity(p) instanceof HatchBlockEntity h?h:null;}
 public static boolean validate(ElectricDrillEntity e){var l=e.getLevel();boolean valid=true;for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){var pos=at(e,x,y,z);if(!l.hasChunkAt(pos)){valid=false;continue;}String role=role(x,y,z);if(role.isEmpty()){if(!l.getBlockState(pos).is(block(x,y,z)))valid=false;}else{var h=hatch(e,x,z);if(role.equals("sediment")){if(!(h instanceof DrillSedimentHatch)||h.orientation.outputDirection!=e.facing().getOpposite())valid=false;}else if(h==null||!h.getHatchType().id().getPath().equals(role))valid=false;}}
  setFormation(e,valid);return valid;
 }
 public static void setFormation(ElectricDrillEntity e,boolean formed){var l=e.getLevel();for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){var pos=at(e,x,y,z);if(!l.hasChunkAt(pos))continue;var s=l.getBlockState(pos);if(s.hasProperty(DrillPart.FORMED)&&s.getValue(DrillPart.FORMED)!=formed)l.setBlock(pos,s.setValue(DrillPart.FORMED,formed),3);if(!role(x,y,z).isEmpty()&&l.getBlockEntity(pos) instanceof HatchBlockEntity h){if(formed&&!h.isMatched())h.link(MachineCasings.STEEL);else if(!formed&&h.isMatched())h.unlink();}}}
}
