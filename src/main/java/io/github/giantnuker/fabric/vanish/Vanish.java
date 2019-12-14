package io.github.giantnuker.fabric.vanish;

import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.fabricmc.api.ModInitializer;

/**
 * @author Indigo Amann
 */
public class Vanish implements ModInitializer {
    @Override
    public void onInitialize() {
        Thimble.permissionWriters.add((map, server) -> {
            map.registerPermission("vanish.vanish", PermChangeBehavior.UPDATE_COMMAND_TREE);
            map.registerPermission("vanish.view", PermChangeBehavior.UPDATE_COMMAND_TREE);
            map.registerPermission("vanish.setting", PermChangeBehavior.UPDATE_COMMAND_TREE);
            for (VanishCommand.Setting setting : VanishCommand.Setting.values()) {
                map.registerPermission("vanish.setting." + setting.id, PermChangeBehavior.UPDATE_COMMAND_TREE);
            }
        });
        WorldDataLib.addIOCallback(new VanishDB.DataStorageHandler());
    }
}
