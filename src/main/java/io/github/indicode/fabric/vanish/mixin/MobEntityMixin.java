package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Indigo Amann
 */
@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    public void cantTarget(LivingEntity entity, CallbackInfoReturnable ci) {
        if (entity instanceof PlayerEntity && VanishDB.INSTANCE.isVanished(((PlayerEntity) entity).getGameProfile().getId()) && VanishDB.INSTANCE.getOrCreateSettings(((PlayerEntity) entity).getGameProfile().getId()).mobs_ignore) {
            ci.setReturnValue(false);
        }
    }
}
