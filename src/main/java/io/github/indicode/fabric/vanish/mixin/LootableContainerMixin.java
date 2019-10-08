package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
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
        if (playerEntity.isSpectator() || (VanishDB.INSTANCE.isVanished(playerEntity.getGameProfile().getId()) && !VanishDB.INSTANCE.getOrCreateSettings(playerEntity.getGameProfile().getId()).generates_chests)) {
            playerEntity.addChatMessage((new TranslatableText("container.spectatorCantOpen", new Object[0])).formatted(Formatting.RED), true);
            return true;
        }
        return false;
    }
}
