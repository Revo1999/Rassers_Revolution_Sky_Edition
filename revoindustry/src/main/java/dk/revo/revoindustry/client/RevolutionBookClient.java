package dk.revo.revoindustry.client;
import net.neoforged.fml.ModList;
public final class RevolutionBookClient{
 public static void open(){if(ModList.get().isLoaded("ftbquests")&&dev.ftb.mods.ftbquests.client.ClientQuestFile.exists())dev.ftb.mods.ftbquests.client.ClientQuestFile.openGui();}
}

