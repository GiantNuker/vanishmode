package io.github.indicode.fabric.vanish;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
