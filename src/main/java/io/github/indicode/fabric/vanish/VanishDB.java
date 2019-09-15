package io.github.indicode.fabric.vanish;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;

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
        mobs_ignore = true,
        events_ignore = true,
        spectator_predicate = true,
        boundingbox = false,
        generates_chests = false;
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
}
