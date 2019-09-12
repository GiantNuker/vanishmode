package io.github.indicode.fabric.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Indigo Amann
 */
public class VanishCommand {
    private enum Setting {
        COLLIDEABLE("colideable",
                pair -> VanishDB.getOrCreateSettings(pair.getLeft()).collideable = pair.getRight(),
                pair -> pair.getRight().set(VanishDB.getOrCreateSettings(pair.getLeft()).collideable));
        String id;
        Consumer<Pair<UUID, Boolean>> setter;
        Consumer<Pair<UUID, AtomicBoolean>> getter;
        Setting(String id, Consumer<Pair<UUID, Boolean>> setter, Consumer<Pair<UUID, AtomicBoolean>> getter) {
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }
    }
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("vanish");
        {
            LiteralArgumentBuilder<ServerCommandSource> view = CommandManager.literal("view");
            view.executes(context -> toggleSeesVanish(context.getSource().getPlayer()));
            command.then(view);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> settings = CommandManager.literal("settings");
            for (Setting setting : Setting.values()) {
                LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal(setting.id);
                literal.executes(context -> readSetting(context.getSource(), setting));
                {
                    RequiredArgumentBuilder<ServerCommandSource, Boolean> set = CommandManager.argument("enabled", BoolArgumentType.bool());
                    set.executes(context -> writeSetting(context.getSource(), setting, BoolArgumentType.getBool(context, "enabled")));
                    literal.then(set);
                }
                settings.then(literal);
            }
            command.then(settings);
        }
        command.executes(context -> toggleVanish(context.getSource().getPlayer()));
        dispatcher.register(command);
    }
    private static int readSetting(ServerCommandSource source, Setting setting) throws CommandSyntaxException {
        UUID uuid = source.getPlayer().getGameProfile().getId();
        AtomicBoolean enabled = new AtomicBoolean();
        setting.getter.accept(new Pair<>(uuid, enabled));
        source.sendFeedback(new LiteralText(setting.id + " is " + (enabled.get() ? "enabled" : "disabled")).formatted(Formatting.YELLOW), false);
        return 0;
    }
    private static int writeSetting(ServerCommandSource source, Setting setting, boolean enabled) throws CommandSyntaxException {
        UUID uuid = source.getPlayer().getGameProfile().getId();
        setting.setter.accept(new Pair<>(uuid, enabled));
        source.sendFeedback(new LiteralText(setting.id + " is now " + (enabled ? "enabled" : "disabled")).formatted(Formatting.GREEN), false);
        return 0;
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
