package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(FollowOwnerGoal.class)
public class FollowOwnerGoalMixin {
    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSpectator()Z"))
    public boolean isVanished(LivingEntity playerEntity) {
        return playerEntity.isSpectator() || (playerEntity instanceof PlayerEntity && VanishDB.INSTANCE.isVanished(((PlayerEntity) playerEntity).getGameProfile().getId()));
    }
}
