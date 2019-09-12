package io.github.indicode.fabric.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

/**
 * @author Indigo Amann
 */
public class VanishCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("vanish");
        {
            LiteralArgumentBuilder<ServerCommandSource> view = CommandManager.literal("view");
            view.executes(context -> toggleSeesVanish(context.getSource().getPlayer().getGameProfile().getId()));
            command.then(view);
        }
        command.executes(context -> toggleVanish(context.getSource().getPlayer().getGameProfile().getId()));
        dispatcher.register(command);
    }
    private static int toggleVanish(UUID player) {
        VanishDB.setVanished(player, !VanishDB.isVanished(player));
        return 0;
    }
    private static int toggleSeesVanish(UUID player) {
        VanishDB.setSeesVanished(player, !VanishDB.canSeeVanished(player));
        return 0;
    }
}
