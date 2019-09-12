package io.github.indicode.fabric.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;

/**
 * @author Indigo Amann
 */
public class VanishCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("vanish");
        {
            LiteralArgumentBuilder<ServerCommandSource> view = CommandManager.literal("view");
            view.executes(context -> toggleSeesVanish(context.getSource().getPlayer()));
            command.then(view);
        }
        command.executes(context -> toggleVanish(context.getSource().getPlayer()));
        dispatcher.register(command);
    }
    private static int toggleVanish(ServerPlayerEntity player) {
        VanishDB.setVanished(player.getGameProfile().getId(), !VanishDB.isVanished(player.getGameProfile().getId()));
        updateBossBar(player);
        return 0;
    }
    private static int toggleSeesVanish(ServerPlayerEntity player) {
        VanishDB.setSeesVanished(player.getGameProfile().getId(), !VanishDB.canSeeVanished(player.getGameProfile().getId()));
        updateBossBar(player);
        return 0;
    }
    private static void updateBossBar(ServerPlayerEntity player) {
        boolean vanished = VanishDB.isVanished(player.getGameProfile().getId());
        player.sendMessage(new LiteralText("You are " + (vanished ? "now in" : "no longer in") + " vanish.").formatted(Formatting.GREEN));
        if (vanished) {
            VanishDB.vanishBar.addPlayer(player);
        } else {
            VanishDB.vanishBar.removePlayer(player);
        }
    }
}
