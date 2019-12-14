package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.ZombieSiegeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(ZombieSiegeManager.class)
public class ZombieSiegeMixin {
    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    public boolean isVanished(PlayerEntity playerEntity) {
        return playerEntity.isSpectator() || (VanishDB.INSTANCE.isVanished(playerEntity.getGameProfile().getId()) && VanishDB.INSTANCE.getOrCreateSettings(playerEntity.getGameProfile().getId()).events_ignore);
    }
}
