package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
