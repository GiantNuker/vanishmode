package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class TrackingStopperMixin {
    @Shadow @Final private Entity entity;

    @Inject(method = "startTracking", at = @At("HEAD"), cancellable = true)
    public void blockInitialPackets(ServerPlayerEntity player, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity && VanishDB.INSTANCE.isVanished(((ServerPlayerEntity) entity).getGameProfile().getId()) && !VanishDB.INSTANCE.canSeeVanished(player.getGameProfile().getId())) {
            ci.cancel();
        }
    }
}
