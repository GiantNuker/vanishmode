package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
    public <T extends ParticleEffect>int removeFallParticles(ServerWorld serverWorld, T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        if ((Object)this instanceof PlayerEntity) {
            UUID id = ((PlayerEntity)(Object)this).getGameProfile().getId();
            if (VanishDB.INSTANCE.isVanished(id)) return 0;
        }
        return serverWorld.spawnParticles(particle, x, y, z, count, deltaX, deltaY, deltaZ, speed);
    }
}
