package io.github.indicode.fabric.vanish;

import net.minecraft.client.network.packet.*;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.*;

/**
 * @author Indigo Amann
 */
public class VanishDB {
    public static ServerBossBar vanishBar = null;
    public static Team vanishersVisibleTeam = null;
    public static Scoreboard vanishTeamsScoreboard;
    public static final Map<UUID, VanishSettings> data = new HashMap<>();
    public static class VanishSettings {
        public boolean vanished = false,
        seeVanished = false,
        partial_spectator = false,
        mobs_ignore = true,
        events_ignore = true,
        spectator_predicate = false, // dont ask
        boundingbox = false,
        silent_chests = true,
        generates_chests = false,
        ignore_locks = true,
        invincible = true;
        public void updateSettings(ServerPlayerEntity player) {
            if(vanished && invincible) player.abilities.invulnerable = true;
            else if (!vanished) {
                player.interactionManager.getGameMode().setAbilitites(player.abilities);
            }
        }
    }
    public static VanishSettings getOrCreateSettings(UUID id) {
        if (!data.containsKey(id)) {
            VanishSettings settings = new VanishSettings();
            data.put(id, settings);
            return settings;
        } else {
            return data.get(id);
        }
    }
    public static boolean isVanished(UUID player) {
        return getOrCreateSettings(player).vanished;
    }
    public static boolean canSeeVanished(UUID player) {
        return getOrCreateSettings(player).seeVanished || isVanished(player);
    }
    public static void setVanished(UUID player, boolean vanished) {
        getOrCreateSettings(player).vanished = vanished;
    }
    public static void setSeesVanished(UUID player, boolean canSeeVanished) {
        getOrCreateSettings(player).seeVanished = canSeeVanished;
    }
    public static void updateNewlyJoinedClient(ServerPlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();
        resetVanish(player);
        updateClient(player, isVanished(uuid), canSeeVanished(uuid), isVanished(uuid));
    }
    public static void resetVanish(ServerPlayerEntity player) {
        if (VanishDB.vanishersVisibleTeam.getPlayerList().contains(player.getGameProfile().getName()))VanishDB.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), VanishDB.vanishersVisibleTeam);
    }
    public static void updateClient(ServerPlayerEntity player, boolean vanished, boolean seeVanished) {
        updateClient(player, vanished, seeVanished, VanishDB.isVanished(player.getGameProfile().getId()) != vanished);
    }
    public static void updateClient(ServerPlayerEntity player, boolean vanished, boolean seeVanished, boolean newVanish) {
        VanishDB.setVanished(player.getGameProfile().getId(), vanished);
        VanishDB.setSeesVanished(player.getGameProfile().getId(), seeVanished);
        VanishDB.getOrCreateSettings(player.getGameProfile().getId()).updateSettings(player);
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

                pl.networkHandler.sendPacket(new TeamS2CPacket(VanishDB.vanishersVisibleTeam, Collections.singletonList(player.getGameProfile().getName()), vanished ? 3 : 4));
            } else if (nplayer != player && newVanish) {
                sendPlayerPacket(pl, player, vanished && !VanishDB.canSeeVanished(pl.getGameProfile().getId()));
            }
            if (sval != seesVanished && pl != player) {
                sendPlayerPacket(player, pl, !seesVanished && VanishDB.isVanished(pl.getGameProfile().getId()));
            }
            if (newVanish && nplayer != player && !vanished && player.getScoreboardTeam() != null && player.getScoreboardTeam() instanceof Team) {
                pl.networkHandler.sendPacket(new TeamS2CPacket((Team)player.getScoreboardTeam(), Collections.singletonList(player.getGameProfile().getName()), 3));
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

    public static void init(MinecraftServer server) {
        VanishDB.data.clear();
        VanishDB.vanishBar = new ServerBossBar(new LiteralText("You Are In Vanish").formatted(Formatting.WHITE), BossBar.Color.WHITE, BossBar.Style.PROGRESS);

        VanishDB.vanishTeamsScoreboard = new Scoreboard();

        VanishDB.vanishersVisibleTeam = new Team(VanishDB.vanishTeamsScoreboard, "vanish_seers");
        VanishDB.vanishersVisibleTeam.setShowFriendlyInvisibles(true);
        VanishDB.vanishersVisibleTeam.setFriendlyFireAllowed(true);
        VanishDB.vanishersVisibleTeam.setPrefix(new LiteralText("[").formatted(Formatting.GRAY).append(new LiteralText("V").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("] ").formatted(Formatting.GRAY))));
        VanishDB.vanishersVisibleTeam.setSuffix(new LiteralText(" [").formatted(Formatting.GRAY).append(new LiteralText("V").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("]").formatted(Formatting.GRAY))));
        VanishDB.vanishersVisibleTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
    }
}
