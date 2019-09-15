package io.github.indicode.fabric.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.packet.*;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

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
        MOBS_IGNORE("mobs_ignore", pair -> pair.getLeft().mobs_ignore = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().mobs_ignore)),
        EVENTS_IGNORE("events_ignore", pair -> pair.getLeft().events_ignore = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().events_ignore)),
        SPECTATOR_PREDICATE("spectator_predicate", pair -> pair.getLeft().spectator_predicate = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().spectator_predicate)),
        BOUNDINGBOX("no_hitbox", pair -> pair.getLeft().boundingbox = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().boundingbox));
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
        player.sendMessage(new LiteralText("You are " + (vanished ? "now in" : "no longer in") + " vanish.").formatted(Formatting.GREEN));
        updateBossBar(player, vanished, VanishDB.getOrCreateSettings(player.getGameProfile().getId()).seeVanished);
        return 0;
    }
    private static int toggleSeesVanish(ServerPlayerEntity player) {
        boolean seevanished = !VanishDB.canSeeVanished(player.getGameProfile().getId());
        player.sendMessage(new LiteralText("You can " + (seevanished ? "now see" : "no longer see") + " players in vanish.").formatted(Formatting.GREEN));
        updateBossBar(player, VanishDB.isVanished(player.getGameProfile().getId()), seevanished);
        return 0;
    }
    private static void updateBossBar(ServerPlayerEntity player, boolean vanished, boolean seeVanished) {
        boolean newVanish = VanishDB.isVanished(player.getGameProfile().getId()) != vanished;
        VanishDB.setVanished(player.getGameProfile().getId(), vanished);
        VanishDB.setSeesVanished(player.getGameProfile().getId(), seeVanished);
        boolean seesVanished = vanished || seeVanished;
        if (vanished) {
            VanishDB.vanishBar.addPlayer(player);
        } else {
            VanishDB.vanishBar.removePlayer(player);
        }
        player.sendAbilitiesUpdate();
        boolean sval = VanishDB.vanishersVisibleTeam.getPlayerList().contains(player.getGameProfile().getName());
        if (sval != seesVanished) {
            if (seesVanished) {
                VanishDB.vanishTeamsScoreboard.addPlayerToTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
            } else {
                VanishDB.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
            }
            player.networkHandler.sendPacket(new TeamS2CPacket(VanishDB.vanishersVisibleTeam, seesVanished ? 0 : 1));
            if (!vanished && seesVanished) {
                //player.networkHandler.sendPacket(new TeamS2CPacket(VanishDB.vanishersVisibleTeam, Arrays.asList(player.getGameProfile().getName()), 3));
                VanishDB.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
            }
        } else if (!seesVanished) {
            player.networkHandler.sendPacket(new TeamS2CPacket(VanishDB.vanishersVisibleTeam, 1));
        }
        //Text prevCustomName = player.getCustomName();
            player.world.getPlayers().forEach(nplayer -> {
                ServerPlayerEntity pl = ((ServerPlayerEntity)nplayer);
                if (nplayer != player && VanishDB.canSeeVanished(pl.getGameProfile().getId())) {

                    pl.networkHandler.sendPacket(new TeamS2CPacket(VanishDB.vanishersVisibleTeam, Arrays.asList(player.getGameProfile().getName()), vanished ? 3 : 4));
                } else if (nplayer != player && newVanish) {
                    sendPlayerPacket(pl, player, vanished && !VanishDB.canSeeVanished(pl.getGameProfile().getId()));
                }
                if (sval != seesVanished && pl != player) {
                    sendPlayerPacket(player, pl, !seesVanished && VanishDB.isVanished(pl.getGameProfile().getId()));
                }
                if (newVanish && nplayer != player && !vanished && player.getScoreboardTeam() != null && player.getScoreboardTeam() instanceof Team) {
                    pl.networkHandler.sendPacket(new TeamS2CPacket((Team)player.getScoreboardTeam(), Arrays.asList(player.getGameProfile().getName()), 3));
                }
            });
        //if (vanished) player.setCustomName(prevCustomName);
    }
        //VanishDB.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
    private static void sendPlayerPacket(ServerPlayerEntity to, ServerPlayerEntity packet, boolean hide) {
        if (hide) {
            to.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(packet.getEntityId()));
        } else {
            to.networkHandler.sendPacket(new PlayerSpawnS2CPacket(packet));
            int int_3 = MathHelper.floor(packet.yaw * 256.0F / 360.0F);
            int int_4 = MathHelper.floor(packet.pitch * 256.0F / 360.0F);
            to.networkHandler.sendPacket(new EntityS2CPacket.Rotate(packet.getEntityId(), (byte)int_3, (byte)int_4, packet.onGround));
            to.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(packet.getEntityId(), packet.getDataTracker(), true));
        }
    }
}
