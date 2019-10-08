package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.raid.RaidManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Indigo Amann
 */
@Mixin(RaidManager.class)
public class RaidManagerMixin {
    @Inject(method = "startRaid", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"), cancellable = true)
    public void disableVanishedRaid(ServerPlayerEntity entity, CallbackInfoReturnable ci) {
        if (VanishDB.INSTANCE.isVanished(entity.getGameProfile().getId()) && VanishDB.INSTANCE.getOrCreateSettings(entity.getGameProfile().getId()).events_ignore) {
            ci.setReturnValue(null);
        }
    }
}
