package io.github.indicode.fabric.vanish.mixin;

import com.mojang.authlib.GameProfile;
import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * @author Indigo Amann
 */
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerEntityMixin extends PlayerEntity {
    public PlayerEntityMixin(World world_1, GameProfile gameProfile_1) {
        super(world_1, gameProfile_1);
    }

    @Override
    public Box getBoundingBox() {
        if (VanishDB.isVanished(getGameProfile().getId())) {
            return new Box(0, 0, 0, 0, 0, 0);
        } else {
            return super.getBoundingBox();
        }
    }
    @Override
    public boolean isPushable() {
        return VanishDB.isVanished(getGameProfile().getId()) ? false : super.isPushable();
    }
    @Override
    public void pushAwayFrom(Entity entity_1) {
        if (VanishDB.isVanished(getGameProfile().getId())) return;
        else super.pushAwayFrom(entity_1);
    }
    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void isVanished(CallbackInfoReturnable ci) {
        if (VanishDB.isVanished(getGameProfile().getId())) ci.setReturnValue(true);
    }
}
