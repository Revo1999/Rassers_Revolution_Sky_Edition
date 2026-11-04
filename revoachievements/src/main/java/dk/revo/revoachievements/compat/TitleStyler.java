package dk.revo.revoachievements.compat;

import dk.revo.revoachievements.WardrobeService;
import net.minecraft.server.level.ServerPlayer;
import sierra.thing.playernamestyler.api.PlaceholderRegistry;
import sierra.thing.playernamestyler.events.NicknameEvents;

/** Cooperates with the pack's nickname formatter instead of overwriting names or scoreboard teams. */
public final class TitleStyler {
    public static void register(){PlaceholderRegistry.register("%revo_title%",context->{
        var title=WardrobeService.title(context.getPlayer());
        return title==null?"":"&#"+String.format("%06X",title.color())+"["+title.name()+"] &r";
    });}
    public static void refresh(ServerPlayer player){NicknameEvents.updateNicknameFor(player);player.refreshTabListName();}
    public static net.minecraft.network.chat.Component tab(ServerPlayer player){return sierra.thing.playernamestyler.util.ChatFormatUtil.formatTablist(player,sierra.thing.playernamestyler.PlayerNameStyler.nicknameManager);}
}
