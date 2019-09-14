package io.github.indicode.fabric.vanish.mixin;

import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;


/**
 * @author Indigo Amann
 */
@Mixin(EntityPredicates.class)
public class EntityPredicatesMixin {
    @Redirect(method = "method_5907", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSpectator()Z"))
    private static boolean checkSpectator(Entity entity) {
        if (entity.isSpectator()) return true;
        if (entity instanceof ServerPlayerEntity) {
            UUID uuid = ((ServerPlayerEntity) entity).getGameProfile().getId();
            return VanishDB.isVanished(uuid) && VanishDB.getOrCreateSettings(uuid).spectator_predicate;
        }
        return false;
    }
    @Redirect(method = "method_5910", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSpectator()Z"))
    private static boolean checkMobsIgnore(Entity entity) {
        if (entity.isSpectator()) return true;
        if (entity instanceof ServerPlayerEntity) {
            UUID uuid = ((ServerPlayerEntity) entity).getGameProfile().getId();
            return VanishDB.isVanished(uuid) && VanishDB.getOrCreateSettings(uuid).mobs_ignore;
        }
        return false;
    }
}
