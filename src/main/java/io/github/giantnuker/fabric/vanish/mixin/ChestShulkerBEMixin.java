package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin({ChestBlockEntity.class, ShulkerBoxBlockEntity.class})
public class ChestShulkerBEMixin {
    @Redirect(method = {"onInvOpen", "onInvClose"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    private boolean isVanished(PlayerEntity playerEntity) {
        return playerEntity.isSpectator() || (VanishDB.INSTANCE.isVanished(playerEntity.getGameProfile().getId()) && VanishDB.INSTANCE.getOrCreateSettings(playerEntity.getGameProfile().getId()).silent_chests);
    }
}
