package io.github.indicode.fabric.vanish;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Indigo Amann
 */
public class VanishDB {
    public static final Map<UUID, VanishSettings> data = new HashMap<>();
    public static class VanishSettings {
        public boolean vanished = false;
        public boolean seeVanished = false;
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
        return getOrCreateSettings(player).seeVanished;
    }
    public static void setVanished(UUID player, boolean vanished) {
        getOrCreateSettings(player).vanished = vanished;
    }
    public static void setSeesVanished(UUID player, boolean canSeeVanished) {
        getOrCreateSettings(player).seeVanished = canSeeVanished;
    }
}