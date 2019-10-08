package io.github.indicode.fabric.vanish;

import io.github.indicode.fabric.permissions.Permission;
import io.github.indicode.fabric.permissions.Thimble;
import io.github.indicode.fabric.worlddata.WorldDataLib;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Indigo Amann
 */
public class Vanish implements ModInitializer {
    @Override
    public void onInitialize() {
        Thimble.permissionWriters.add(pair -> {
            pair.getLeft().addGroup(getPerm("vanish", "vanish"));
            pair.getLeft().addGroup(getPerm(null, "vanish"));
            pair.getLeft().addGroup(getPerm("vanish", "view"));
            pair.getLeft().addGroup(getPerm("vanish", "setting"));
            for (VanishCommand.Setting setting: VanishCommand.Setting.values()) {
                pair.getLeft().addGroup(getPerm("vanish.setting", setting.id));
            }
            Thimble.PERMISSIONS.mapPermissions(Thimble.PERMISSIONS.getRegisteredPermissions()).forEach((a, b) -> System.out.println(a + " - " + b));
        });
        WorldDataLib.addIOCallback(new VanishDB.DataStorageHandler());
    }
    private Permission getPerm(String parent, String perm) {
        //System.out.println(Thimble.PERMISSIONS.getPermission(parent).getFullIdentifier());
        return new Permission(perm, parent == null ? null : Thimble.PERMISSIONS.getPermission(parent)) {
            @Override
            public boolean shouldSave() {
                return false;
            }
            @Override
            public void onStateChanged(ServerPlayerEntity player, boolean hasPerm) {
                player.server.getCommandManager().sendCommandTree(player);
            }
        };
    }
}
