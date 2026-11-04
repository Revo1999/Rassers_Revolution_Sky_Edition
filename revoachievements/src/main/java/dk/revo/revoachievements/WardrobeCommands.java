package dk.revo.revoachievements;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.*;
import net.minecraft.network.chat.Component;
import static net.minecraft.commands.Commands.*;

public final class WardrobeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(literal("revoachievements")
            .then(literal("wardrobe").executes(c->{WardrobeService.sendOwn(c.getSource().getPlayerOrException(),true,"");return 1;}))
            .then(literal("equip").then(argument("cosmetic",ResourceLocationArgument.id()).suggests((c,b)->SharedSuggestionProvider.suggestResource(CosmeticCatalog.all().stream().map(Cosmetic::id),b)).executes(c->{
                if(!WardrobeService.equip(c.getSource().getPlayerOrException(),ResourceLocationArgument.getId(c,"cosmetic").toString())){c.getSource().sendFailure(Component.literal("Cosmetic is locked or unavailable."));return 0;}return 1;
            })))
            .then(literal("clear").then(literal("title").executes(c->{WardrobeService.clear(c.getSource().getPlayerOrException(),Cosmetic.Kind.TITLE);return 1;})).then(literal("hat").executes(c->{WardrobeService.clear(c.getSource().getPlayerOrException(),Cosmetic.Kind.HAT);return 1;})))
            .then(literal("list").executes(c->{CosmeticCatalog.all().forEach(e->c.getSource().sendSuccess(()->Component.literal(e.id()+" — "+e.name()),false));return CosmeticCatalog.all().size();}))
            .then(literal("unlock").requires(s->s.hasPermission(2)).then(argument("players",EntityArgument.players()).then(argument("cosmetic",ResourceLocationArgument.id()).suggests((c,b)->SharedSuggestionProvider.suggestResource(CosmeticCatalog.all().stream().map(Cosmetic::id),b)).executes(c->{
                String id=ResourceLocationArgument.getId(c,"cosmetic").toString();if(CosmeticCatalog.get(id)==null){c.getSource().sendFailure(Component.literal("Unknown Revo cosmetic: "+id));return 0;}
                int changed=0;for(var player:EntityArgument.getPlayers(c,"players"))if(WardrobeService.grant(c.getSource().getServer(),player.getUUID(),id,true)==1)changed++;
                int count=changed;c.getSource().sendSuccess(()->Component.literal("Unlocked cosmetic for "+count+" player(s)."),true);return changed;
            }))))
            .then(literal("revoke").requires(s->s.hasPermission(2)).then(argument("players",EntityArgument.players()).then(argument("cosmetic",ResourceLocationArgument.id()).executes(c->{
                String id=ResourceLocationArgument.getId(c,"cosmetic").toString();int changed=0;for(var player:EntityArgument.getPlayers(c,"players")){if(WardrobeData.get(c.getSource().getServer()).revoke(player.getUUID(),id))changed++;WardrobeService.changed(c.getSource().getServer(),player.getUUID());}return changed;
            })))));
    }
}
