package dk.revo.revoindustry.client;
import dk.revo.revoindustry.PrimitiveMachines;
import dk.revo.revolib.energy.EnergyInterop;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.*;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
@EmiEntrypoint
public final class PowerConverterEmi implements EmiPlugin{
 public void register(EmiRegistry r){var blocks=List.of(PrimitiveMachines.CONVERTER.get(),PrimitiveMachines.ADVANCED_CONVERTER.get(),PrimitiveMachines.ELITE_CONVERTER.get());for(var b:blocks){int eu=new int[]{32,128,512}[b.tier];int fe=eu*EnergyInterop.fePerEu();r.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(b)),List.of(
  Component.literal("Teal face: Forge Energy. Opposite copper face: MI EU."),
  Component.literal("Flux Receiver -> teal face -> copper face -> MI machine or storage."),
  Component.literal("Reverse: MI generator/storage -> copper face -> teal face -> Flux Transmitter."),
  Component.literal("Connect the EU face directly to an MI machine/storage. Use that storage to connect an MI cable network."),
  Component.literal("FE -> EU: up to "+fe+" FE/t converts to "+eu+" EU/t."),
  Component.literal("EU -> FE: up to "+eu+" EU/t converts to "+fe+" FE/t."),
  Component.literal("Direct conversion: no internal power storage. Unaccepted power remains at the source."),
  Component.literal("1 EU = "+EnergyInterop.fePerEu()+" FE (MI config). Rotate with a wrench. No kinetic/RPM conversion.")),ResourceLocation.fromNamespaceAndPath("revoindustry","info/"+net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(b).getPath())));}}
}
