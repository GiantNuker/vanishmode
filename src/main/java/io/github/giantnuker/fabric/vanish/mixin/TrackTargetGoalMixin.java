package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(TrackTargetGoal.class)
public class TrackTargetGoalMixin {
    @Redirect(method = "shouldContinue", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z"))
    public boolean isMobIgnoreVanished(LivingEntity entity) {
        if (entity instanceof PlayerEntity && VanishDB.INSTANCE.isVanished(((PlayerEntity) entity).getGameProfile().getId()) && VanishDB.INSTANCE.getOrCreateSettings(((PlayerEntity) entity).getGameProfile().getId()).mobs_ignore) {
            return false;
        } else return entity.isAlive();
    }
}
