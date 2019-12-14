package io.github.giantnuker.fabric.vanish.mixin;

import io.github.giantnuker.fabric.vanish.VanishDB;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Redirect(method = "setBlockBreakingInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void dontSendPacket(ServerPlayNetworkHandler networkHandler, Packet<?> packet, int entityId, BlockPos pos, int progress) {
        for (ServerPlayerEntity player : getPlayers()) {
            if (player.getEntityId() == entityId && VanishDB.INSTANCE.isVanished(player.getGameProfile().getId())) {
                return;
            }
        }
        networkHandler.sendPacket(packet);
    }

    @Inject(method = "playLevelEvent", at = @At("HEAD"), cancellable = true)
    private void dontPlayBreakEvent(PlayerEntity player, int eventId, BlockPos blockPos, int data, CallbackInfo ci) {
        if (eventId == 2001 && player != null && VanishDB.INSTANCE.isVanished(player.getGameProfile().getId())) {
            ci.cancel();
        }
    }
}
