package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
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
    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    public boolean isVanished(PlayerEntity playerEntity) {
        return playerEntity.isSpectator() || VanishDB.isVanished(playerEntity.getGameProfile().getId());
    }
}
