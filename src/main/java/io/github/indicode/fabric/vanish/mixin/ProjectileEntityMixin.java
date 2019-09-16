package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {
    @Redirect(method = "method_18071", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSpectator()Z"))
    private boolean isVanished(Entity playerEntity) {
        return playerEntity.isSpectator() || (playerEntity instanceof PlayerEntity && VanishDB.isVanished(((PlayerEntity) playerEntity).getGameProfile().getId()));
    }
}
