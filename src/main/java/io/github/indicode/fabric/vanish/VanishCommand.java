package io.github.indicode.fabric.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.packet.TeamS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Indigo Amann
 */
public class VanishCommand {
    private enum Setting {
        ;
        String id;
        Consumer<Pair<VanishDB.VanishSettings, Boolean>> setter;
        Consumer<Pair<VanishDB.VanishSettings, AtomicBoolean>> getter;
        Setting(String id, Consumer<Pair<VanishDB.VanishSettings, Boolean>> setter, Consumer<Pair<VanishDB.VanishSettings, AtomicBoolean>> getter) {
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }
        public boolean get(UUID uuid) {
            VanishDB.VanishSettings settings = VanishDB.getOrCreateSettings(uuid);
            AtomicBoolean value = new AtomicBoolean();
            getter.accept(new Pair<>(settings, value));
            return value.get();
        }
        public void set(UUID uuid, boolean enabled) {
            VanishDB.VanishSettings settings = VanishDB.getOrCreateSettings(uuid);
            setter.accept(new Pair<>(settings, enabled));
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
        boolean enabled = setting.get(uuid);
        source.sendFeedback(new LiteralText(setting.id + " is " + (enabled ? "enabled" : "disabled")).formatted(Formatting.YELLOW), false);
        return 0;
    }
    private static int writeSetting(ServerCommandSource source, Setting setting, boolean enabled) throws CommandSyntaxException {
        UUID uuid = source.getPlayer().getGameProfile().getId();
        setting.set(uuid, enabled);
        source.sendFeedback(new LiteralText(setting.id + " is now " + (enabled ? "enabled" : "disabled")).formatted(Formatting.GREEN), false);
        return 0;
    }
    private static int toggleVanish(ServerPlayerEntity player) {
        boolean vanished = !VanishDB.isVanished(player.getGameProfile().getId());
        VanishDB.setVanished(player.getGameProfile().getId(), vanished);
        player.sendMessage(new LiteralText("You are " + (vanished ? "now in" : "no longer in") + " vanish.").formatted(Formatting.GREEN));
        updateBossBar(player);
        return 0;
    }
    private static int toggleSeesVanish(ServerPlayerEntity player) {
        boolean seevanished = !VanishDB.canSeeVanished(player.getGameProfile().getId());
        VanishDB.setSeesVanished(player.getGameProfile().getId(), seevanished);
        player.sendMessage(new LiteralText("You can " + (seevanished ? "now see" : "no longer see") + " players in vanish.").formatted(Formatting.GREEN));
        updateBossBar(player);
        return 0;
    }
    private static void updateBossBar(ServerPlayerEntity player) {
        boolean vanished = VanishDB.isVanished(player.getGameProfile().getId());
        if (vanished) {
            VanishDB.vanishBar.addPlayer(player);
        } else {
            VanishDB.vanishBar.removePlayer(player);
        }
        player.sendAbilitiesUpdate();
        boolean seesVanished = VanishDB.canSeeVanished(player.getGameProfile().getId());
        boolean sval = VanishDB.vanishersVisibleTeam.getPlayerList().contains(player.getGameProfile().getName());
        if (sval != seesVanished) {
            if (seesVanished) {
                VanishDB.vanishTeamsScoreboard.addPlayerToTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
            } else {
                VanishDB.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
            }
            System.out.println(VanishDB.vanishersVisibleTeam.getPlayerList());
            player.networkHandler.sendPacket(new TeamS2CPacket(VanishDB.vanishersVisibleTeam, seesVanished ? 0 : 1));
            player.world.getPlayers().forEach(nplayer -> {
                if (VanishDB.canSeeVanished(nplayer.getGameProfile().getId())) {
                    ((ServerPlayerEntity)nplayer).networkHandler.sendPacket(new TeamS2CPacket(VanishDB.vanishersVisibleTeam, Arrays.asList(player.getGameProfile().getName()), seesVanished ? 3 : 4));
                }
            });
        }
    }
        //VanishDB.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
}
