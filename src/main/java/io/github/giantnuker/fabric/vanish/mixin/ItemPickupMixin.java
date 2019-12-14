package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {
    @Redirect(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    public boolean dontPickup(PlayerInventory playerInventory, ItemStack stack) {
        UUID id = playerInventory.player.getGameProfile().getId();
        return (VanishDB.INSTANCE.isVanished(id) && !VanishDB.INSTANCE.getOrCreateSettings(id).item_pickup) ? false : playerInventory.insertStack(stack);
    }
}
