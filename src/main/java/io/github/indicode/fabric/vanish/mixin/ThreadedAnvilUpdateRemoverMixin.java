package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.Vanish;
import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/world/ThreadedAnvilChunkStorage$EntityTracker")
public class ThreadedAnvilUpdateRemoverMixin {
    @Shadow @Final private Entity entity;

    @Redirect(method = "sendToOtherNearbyPlayers", at= @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void dontSendVanishPackets(ServerPlayNetworkHandler serverPlayNetworkHandler, Packet<?> packet) {
        if (!(entity instanceof ServerPlayerEntity && VanishDB.INSTANCE.isVanished(((ServerPlayerEntity) entity).getGameProfile().getId()) && !VanishDB.INSTANCE.canSeeVanished(serverPlayNetworkHandler.player.getGameProfile().getId()))) {
            serverPlayNetworkHandler.sendPacket(packet);
        }
    }
}
