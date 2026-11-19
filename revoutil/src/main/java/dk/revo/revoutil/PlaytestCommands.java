package dk.revo.revoutil;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

final class PlaytestCommands {
    private final PlaytestStatus status;

    PlaytestCommands(PlaytestStatus status) {
        this.status = status;
    }

    void register(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("revohelp").executes(context -> help(context.getSource())));
        dispatcher.register(Commands.literal("log")
                .executes(context -> {
                    context.getSource().sendFailure(Component.literal("Usage: /log <message>"));
                    return 0;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> log(context.getSource(), StringArgumentType.getString(context, "message")))));
        dispatcher.register(Commands.literal("revonotes")
                .executes(context -> notes(context.getSource(), 5))
                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                        .executes(context -> notes(context.getSource(), IntegerArgumentType.getInteger(context, "count")))));
        dispatcher.register(Commands.literal("revostatus")
                .executes(context -> status.toggle(context.getSource())));
    }

    private static int help(CommandSourceStack source) {
        reply(source, "RevoUtil | Pack " + RevoUtil.PACK_VERSION);
        reply(source, "/log \"Check the drill output hatch\" - save a playtest note");
        reply(source, "/revonotes [1-20] - show recent notes (default 5)");
        reply(source, "/revostatus - toggle the top-right status HUD");
        reply(source, "/revohelp - show this command list");
        return 1;
    }

    private static int log(CommandSourceStack source, String rawMessage) {
        String message = rawMessage.strip();
        if (message.length() >= 2 && message.startsWith("\"") && message.endsWith("\"")) {
            message = message.substring(1, message.length() - 1).strip();
        }
        message = message.replaceAll("[\\p{Cc}\\p{Zl}\\p{Zp}]", " ").strip();
        if (message.isBlank() || message.length() > 1000) {
            source.sendFailure(Component.literal("Enter a note between 1 and 1,000 characters."));
            return 0;
        }

        BlockPos pos = BlockPos.containing(source.getPosition());
        try {
            long number = file(source).append(source.getTextName(), source.getLevel().dimension().location().toString(),
                    "%d %d %d".formatted(pos.getX(), pos.getY(), pos.getZ()), message);
            reply(source, "Saved #" + number + " to " + PlaytestNotes.LOCATION);
            return 1;
        } catch (IOException exception) {
            RevoUtil.LOGGER.error("Could not save the playtest note", exception);
            source.sendFailure(Component.literal("Could not save the playtest note. See the server log."));
            return 0;
        }
    }

    private static int notes(CommandSourceStack source, int count) {
        try {
            var recent = file(source).recent(count);
            if (recent.isEmpty()) {
                reply(source, "No playtest notes yet. Use /log <message> to add one.");
                return 1;
            }
            reply(source, "Recent playtest notes | " + PlaytestNotes.LOCATION);
            for (var note : recent) {
                var line = Component.literal("#" + note.number() + " " + note.message())
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, Component.literal(note.details()))));
                source.sendSuccess(() -> line, false);
            }
            return recent.size();
        } catch (IOException exception) {
            RevoUtil.LOGGER.error("Could not read playtest notes", exception);
            source.sendFailure(Component.literal("Could not read playtest notes. See the server log."));
            return 0;
        }
    }

    private static PlaytestNotes file(CommandSourceStack source) {
        return new PlaytestNotes(source.getServer().getWorldPath(LevelResource.ROOT));
    }

    private static void reply(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }
}
