package io.github.indicode.fabric.vanish.mixin;

import com.google.gson.JsonElement;
import io.github.indicode.fabric.vanish.VanishDB;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Indigo Amann
 */
@Mixin({MinecraftServer.class, IntegratedServer.class})
public class MinecraftServerMixin {
    @Inject(method = "loadWorld", at = @At("HEAD"))
    protected void load(String string_1, String string_2, long long_1, LevelGeneratorType levelGeneratorType_1, JsonElement jsonElement_1, CallbackInfo ci) {
        VanishDB.data.clear();
        VanishDB.vanishBar = new ServerBossBar(new LiteralText("You Are In Vanish").formatted(Formatting.WHITE), BossBar.Color.WHITE, BossBar.Style.PROGRESS);

        MinecraftServer server = (MinecraftServer)(Object)this;
        VanishDB.vanishTeamsScoreboard = new Scoreboard();

        VanishDB.vanishersVisibleTeam = new Team(VanishDB.vanishTeamsScoreboard, "vanish_seers");
        VanishDB.vanishersVisibleTeam.setShowFriendlyInvisibles(true);
        VanishDB.vanishersVisibleTeam.setFriendlyFireAllowed(true);
        VanishDB.vanishersVisibleTeam.setPrefix(new LiteralText("[").formatted(Formatting.GRAY).append(new LiteralText("V").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("] ").formatted(Formatting.GRAY))));
        VanishDB.vanishersVisibleTeam.setSuffix(new LiteralText(" [").formatted(Formatting.GRAY).append(new LiteralText("V").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("]").formatted(Formatting.GRAY))));
        VanishDB.vanishersVisibleTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
    }
}
