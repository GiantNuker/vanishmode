package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.client.network.packet.EntityVelocityUpdateS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.function.Consumer;


/**
 * @author Indigo Amann
 */
@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow
    private Entity entity;
    @Shadow
    private Consumer field_18259;
    private Consumer nearbyConsumer;
    private Consumer vanishViewConsumer;
    @Inject(method = "<init>", at = @At("RETURN"))
    public void invoke(ServerWorld serverWorld_1, Entity entity_1, int int_1, boolean boolean_1, Consumer<Packet<?>> consumer_1, CallbackInfo ci) {
        nearbyConsumer = consumer_1;
        vanishViewConsumer = packet -> {
            Iterator var2 = serverWorld_1.getPlayers().iterator();

            while(var2.hasNext()) {
                ServerPlayerEntity serverPlayerEntity_1 = (ServerPlayerEntity)var2.next();
                if (serverPlayerEntity_1 != entity_1 && VanishDB.canSeeVanished(serverPlayerEntity_1.getGameProfile().getId()))serverPlayerEntity_1.networkHandler.sendPacket((Packet<?>) packet);
            }
        };
    }
    //@Redirect(method = "method_18756", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    @Inject(method = "method_18756", at = @At("HEAD"))
    public void checkForVanishedPlayer(CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity && VanishDB.isVanished(((ServerPlayerEntity) entity).getGameProfile().getId())) {
            field_18259 = vanishViewConsumer;
        } else {
            field_18259 = nearbyConsumer;
        }
    }
}
