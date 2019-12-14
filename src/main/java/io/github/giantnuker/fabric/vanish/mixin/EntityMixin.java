package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


/**
 * @author Indigo Amann
 */
@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(method = "canSeePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    public boolean isVanished(PlayerEntity playerEntity) {
        return playerEntity.isSpectator() || (VanishDB.INSTANCE.isVanished(playerEntity.getGameProfile().getId()) && VanishDB.INSTANCE.getOrCreateSettings(playerEntity.getGameProfile().getId()).mobs_ignore);
    }
}
