package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.ai.goal.SitOnOwnerShoulder;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
*/
@Mixin(SitOnOwnerShoulder.class)
public class SitOnShoulderMixin {
    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    public boolean isVanished(ServerPlayerEntity playerEntity) { // No. just no.
        return playerEntity.isSpectator() || VanishDB.INSTANCE.isVanished(playerEntity.getGameProfile().getId());
    }
}
