package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.thrown.ThrownEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(ThrownEntity.class)
public class ThrownEntityMixin {
    @Redirect(method = "method_18081", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSpectator()Z"))
    private static boolean isVanished(Entity playerEntity) {
        return playerEntity.isSpectator() || (playerEntity instanceof PlayerEntity && VanishDB.INSTANCE.isVanished(((PlayerEntity) playerEntity).getGameProfile().getId()));
    }
    @Redirect(method = "method_18080", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSpectator()Z"))
    private boolean isVanished2(Entity playerEntity) {
        return playerEntity.isSpectator() || (playerEntity instanceof PlayerEntity && VanishDB.INSTANCE.isVanished(((PlayerEntity) playerEntity).getGameProfile().getId()));
    }
}
