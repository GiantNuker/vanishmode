package io.github.indicode.fabric.vanish;

import io.github.indicode.fabric.permissions.Permission;
import io.github.indicode.fabric.permissions.Thimble;
import io.github.indicode.fabric.permissions.command.CommandPermission;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Indigo Amann
 */
public class Vanish implements ModInitializer {
    @Override
    public void onInitialize() {
        Thimble.permissionWriters.add(pair -> {
            try {
                pair.getLeft().getPermission("vanish", CommandPermission.class);
                pair.getLeft().getPermission("vanish.vanish", CommandPermission.class);
                pair.getLeft().getPermission("vanish.view", CommandPermission.class);
                pair.getLeft().getPermission("vanish.setting", CommandPermission.class);
                for (VanishCommand.Setting setting: VanishCommand.Setting.values()) {
                    pair.getLeft().getPermission("vanish.setting." + setting.id, CommandPermission.class);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        });
        WorldDataLib.addIOCallback(new VanishDB.DataStorageHandler());
    }
}
