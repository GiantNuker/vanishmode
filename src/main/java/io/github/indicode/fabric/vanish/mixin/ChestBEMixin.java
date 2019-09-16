package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(ChestBlockEntity.class)
public class ChestBEMixin {
    @Redirect(method = {"onInvOpen", "onInvClose"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    private boolean isVanished(PlayerEntity playerEntity) {
        return playerEntity.isSpectator() || (VanishDB.isVanished(playerEntity.getGameProfile().getId()) && VanishDB.getOrCreateSettings(playerEntity.getGameProfile().getId()).silent_chests);
    }
}
