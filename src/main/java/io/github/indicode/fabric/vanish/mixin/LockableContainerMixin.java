package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.container.ContainerLock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(LockableContainerBlockEntity.class)
public class LockableContainerMixin {
    @Redirect(method = "checkUnlocked(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/container/ContainerLock;Lnet/minecraft/text/Text;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    private static boolean isVanished(PlayerEntity playerEntity, PlayerEntity playerEntityB, ContainerLock lock, Text text) {
        if (playerEntity.isSpectator() || (VanishDB.isVanished(playerEntity.getGameProfile().getId()) && VanishDB.getOrCreateSettings(playerEntity.getGameProfile().getId()).ignore_locks)) {
            return true;
        } else if (!playerEntity.isSpectator() && (VanishDB.isVanished(playerEntity.getGameProfile().getId()) && !VanishDB.getOrCreateSettings(playerEntity.getGameProfile().getId()).ignore_locks)) {
            playerEntity.addChatMessage(new TranslatableText("container.isLocked", new Object[]{text}), true);
            playerEntity.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        return false;
    }
}
