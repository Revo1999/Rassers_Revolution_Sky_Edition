package dk.revo.revoindustry.furnace;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import net.minecraft.network.codec.*;
import net.minecraft.util.Unit;
import java.util.function.Supplier;
public record FurnaceGui(Supplier<String> text) implements GuiComponentServer<Unit,String>{
 public static final Type<Unit,String> TYPE=new Type<>(Furnaces.id("furnace_status"),StreamCodec.unit(Unit.INSTANCE),ByteBufCodecs.STRING_UTF8);
 public Unit getParams(){return Unit.INSTANCE;}public String extractData(){return text.get();}public Type<Unit,String> getType(){return TYPE;}
}
