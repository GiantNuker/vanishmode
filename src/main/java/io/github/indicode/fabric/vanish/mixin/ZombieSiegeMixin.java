package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.ZombieSiegeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(ZombieSiegeManager.class)
public class ZombieSiegeMixin {
    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    public boolean isVanished(ServerPlayerEntity playerEntity) {
        return playerEntity.isSpectator() || (VanishDB.isVanished(playerEntity.getGameProfile().getId()) && VanishDB.getOrCreateSettings(playerEntity.getGameProfile().getId()).events_ignore);
    }
}
