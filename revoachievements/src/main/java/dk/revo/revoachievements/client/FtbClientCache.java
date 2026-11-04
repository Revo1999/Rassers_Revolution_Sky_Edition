package dk.revo.revoachievements.client;

/** Loaded only when FTB Quests is present; catalogue updates invalidate its cached reward labels. */
public final class FtbClientCache {
    public static void refresh(){var file=dev.ftb.mods.ftbquests.client.ClientQuestFile.INSTANCE;if(file!=null)file.clearCachedData();}
}
