package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(LootableContainerBlockEntity.class)
public class LootableContainerMixin {
    @Redirect(method = "checkUnlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    public boolean isVanished(PlayerEntity playerEntity) {
        return playerEntity.isSpectator() || (VanishDB.isVanished(playerEntity.getGameProfile().getId()) && !VanishDB.getOrCreateSettings(playerEntity.getGameProfile().getId()).generates_chests);
    }
}
