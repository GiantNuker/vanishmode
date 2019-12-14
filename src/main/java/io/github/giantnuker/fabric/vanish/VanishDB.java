package io.github.giantnuker.fabric.vanish;

import io.github.indicode.fabric.worlddata.NBTWorldData;
import net.minecraft.client.network.packet.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.util.*;

/**
 * @author Indigo Amann
 */
public class VanishDB {
    public static VanishDB INSTANCE = null;
    public ServerBossBar vanishBar = null;
    public Team vanishersVisibleTeam = null;
    public Scoreboard vanishTeamsScoreboard;
    public final Map<UUID, VanishSettings> data = new HashMap<>();
    public class VanishSettings {
        public boolean vanished = false,
        seeVanished = false,
        partial_spectator = false,
        mobs_ignore = true,
        events_ignore = true,
        spectator_predicate = false, // dont ask
        boundingbox = false,
        silent_chests = true,
        generates_chests = false,
        ignore_locks = true,
        invincible = true,
        item_pickup = false;
        public void updateSettings(ServerPlayerEntity player) {
            if(vanished && invincible) player.abilities.invulnerable = true;
            else if (!vanished) {
                player.interactionManager.getGameMode().setAbilitites(player.abilities);
            }
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("vanished", vanished);
            tag.putBoolean("see_vanished", seeVanished);
            tag.putBoolean("spectator", partial_spectator);
            tag.putBoolean("mobs_ignore", mobs_ignore);
            tag.putBoolean("events_ignore", events_ignore);
            tag.putBoolean("boundingbox", boundingbox);
            tag.putBoolean("silent_chests", silent_chests);
            tag.putBoolean("gen_chests", generates_chests);
            tag.putBoolean("ignore_locks", ignore_locks);
            tag.putBoolean("invincible", invincible);
            tag.putBoolean("item_pickup", item_pickup);
            return tag;
        }
        public void fromNBT(CompoundTag tag) {
            vanished = tag.getBoolean("vanished");
            seeVanished = tag.getBoolean("see_vanished");
            partial_spectator = tag.getBoolean("spectator");
            mobs_ignore = tag.getBoolean("mobs_ignore");
            events_ignore = tag.getBoolean("events_ignore");
            boundingbox = tag.getBoolean("boundingbox");
            silent_chests = tag.getBoolean("silent_chests");
            generates_chests = tag.getBoolean("gen_chests");
            ignore_locks = tag.getBoolean("ignore_locks");
            item_pickup = tag.getBoolean("item_pickup");
        }
    }
    public VanishSettings getOrCreateSettings(UUID id) {
        if (!data.containsKey(id)) {
            VanishSettings settings = new VanishSettings();
            data.put(id, settings);
            return settings;
        } else {
            return data.get(id);
        }
    }
    public boolean isVanished(UUID player) {
        return data.containsKey(player) && getOrCreateSettings(player).vanished;
    }
    public boolean canSeeVanished(UUID player) {
        return data.containsKey(player) && getOrCreateSettings(player).seeVanished || isVanished(player);
    }
    public void setVanished(UUID player, boolean vanished) {
        getOrCreateSettings(player).vanished = vanished;
    }
    public void setSeesVanished(UUID player, boolean canSeeVanished) {
        getOrCreateSettings(player).seeVanished = canSeeVanished;
    }
    public void updateNewlyJoinedClient(ServerPlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();
        resetVanish(player);
        updateClient(player, isVanished(uuid), canSeeVanished(uuid), isVanished(uuid));
    }
    public void resetVanish(ServerPlayerEntity player) {
        if (this.vanishersVisibleTeam.getPlayerList().contains(player.getGameProfile().getName()))this.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), this.vanishersVisibleTeam);
    }
    public void updateClient(ServerPlayerEntity player, boolean vanished, boolean seeVanished) {
        updateClient(player, vanished, seeVanished, this.isVanished(player.getGameProfile().getId()) != vanished);
    }
    public void updateClient(ServerPlayerEntity player, boolean vanished, boolean seeVanished, boolean newVanish) {
        this.setVanished(player.getGameProfile().getId(), vanished);
        this.setSeesVanished(player.getGameProfile().getId(), seeVanished);
        this.getOrCreateSettings(player.getGameProfile().getId()).updateSettings(player);
        boolean seesVanished = vanished || seeVanished;
        if (vanished) {
            this.vanishBar.addPlayer(player);
        } else {
            this.vanishBar.removePlayer(player);
        }
        boolean sval = this.vanishersVisibleTeam.getPlayerList().contains(player.getGameProfile().getName());
        if (sval != seesVanished) {
            if (seesVanished) {
                this.vanishTeamsScoreboard.addPlayerToTeam(player.getGameProfile().getName(), this.vanishersVisibleTeam);
            } else {
                this.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), this.vanishersVisibleTeam);
            }
            player.networkHandler.sendPacket(new TeamS2CPacket(this.vanishersVisibleTeam, seesVanished ? 0 : 1));
            if (!seesVanished && player.getScoreboardTeam() instanceof Team) {
                player.networkHandler.sendPacket(new TeamS2CPacket((Team)player.getScoreboardTeam(), Collections.singletonList(player.getGameProfile().getName()), 3));
            }
            if (!vanished && seesVanished) {
                //player.networkHandler.sendPacket(new TeamS2CPacket(this.vanishersVisibleTeam, Arrays.asList(player.getGameProfile().getName()), 3));
                this.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), this.vanishersVisibleTeam);
            }
        } else if (!seesVanished) {
            player.networkHandler.sendPacket(new TeamS2CPacket(this.vanishersVisibleTeam, 1));
        }
        //Text prevCustomName = player.getCustomName();
        player.world.getPlayers().forEach(nplayer -> {
            ServerPlayerEntity pl = ((ServerPlayerEntity)nplayer);
            if (nplayer != player && this.canSeeVanished(pl.getGameProfile().getId())) {
                pl.networkHandler.sendPacket(new TeamS2CPacket(this.vanishersVisibleTeam, Collections.singletonList(player.getGameProfile().getName()), vanished ? 3 : 4));
            } else if (nplayer != player && newVanish) {
                sendPlayerPacket(pl, player, vanished && !this.canSeeVanished(pl.getGameProfile().getId()));
            }
            if (sval != seesVanished && pl != player) {
                sendPlayerPacket(player, pl, !seesVanished && this.isVanished(pl.getGameProfile().getId()));
            }
            if (newVanish && nplayer != player && !vanished && player.getScoreboardTeam() != null && player.getScoreboardTeam() instanceof Team) {
                pl.networkHandler.sendPacket(new TeamS2CPacket((Team)player.getScoreboardTeam(), Collections.singletonList(player.getGameProfile().getName()), 3));
            }
        });
        player.sendAbilitiesUpdate();
        //if (vanished) player.setCustomName(prevCustomName);
    }
    //this.vanishTeamsScoreboard.removePlayerFromTeam(player.getGameProfile().getName(), this.vanishersVisibleTeam);
    private void sendPlayerPacket(ServerPlayerEntity to, ServerPlayerEntity packet, boolean hide) {
        if (hide) {
            to.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(packet.getEntityId()));
        } else {
            to.networkHandler.sendPacket(new PlayerSpawnS2CPacket(packet));
            int int_3 = MathHelper.floor(packet.yaw * 256.0F / 360.0F);
            int int_4 = MathHelper.floor(packet.pitch * 256.0F / 360.0F);
            byte int_5 = (byte)MathHelper.floor(packet.getHeadYaw() * 256.0F / 360.0F);
            to.networkHandler.sendPacket(new EntityS2CPacket.Rotate(packet.getEntityId(), (byte)int_3, (byte)int_4, packet.onGround));
            to.networkHandler.sendPacket(new EntitySetHeadYawS2CPacket(packet, int_5));
            to.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(packet.getEntityId(), packet.getDataTracker(), true));
            {
                EquipmentSlot[] var9 = EquipmentSlot.values();
                int var12 = var9.length;

                for(int var6 = 0; var6 < var12; ++var6) {
                    EquipmentSlot equipmentSlot = var9[var6];
                    ItemStack itemStack = packet.getEquippedStack(equipmentSlot);
                    if (!itemStack.isEmpty()) {
                        to.networkHandler.sendPacket(new EntityEquipmentUpdateS2CPacket(packet.getEntityId(), equipmentSlot, itemStack));
                    }
                }
            }
            for (StatusEffectInstance statusEffectInstance : packet.getStatusEffects()) {
                to.networkHandler.sendPacket(new EntityPotionEffectS2CPacket(packet.getEntityId(), statusEffectInstance));
            }
        }
    }

    private VanishDB(MinecraftServer server) {
        this.data.clear();
        this.vanishBar = new ServerBossBar(new LiteralText("You Are In Vanish").formatted(Formatting.WHITE), BossBar.Color.WHITE, BossBar.Style.PROGRESS);

        this.vanishTeamsScoreboard = new Scoreboard();

        this.vanishersVisibleTeam = new Team(this.vanishTeamsScoreboard, "vanish_seers");
        this.vanishersVisibleTeam.setShowFriendlyInvisibles(true);
        this.vanishersVisibleTeam.setFriendlyFireAllowed(true);
        this.vanishersVisibleTeam.setPrefix(new LiteralText("[").formatted(Formatting.GRAY).append(new LiteralText("V").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("] ").formatted(Formatting.GRAY))));
        this.vanishersVisibleTeam.setSuffix(new LiteralText(" [").formatted(Formatting.GRAY).append(new LiteralText("V").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("]").formatted(Formatting.GRAY))));
        this.vanishersVisibleTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
    }
    public static void init(MinecraftServer server) {
        INSTANCE = new VanishDB(server);
    }
    static class DataStorageHandler extends NBTWorldData {

        @Override
        public File getSaveFile(File worldDir, File rootDir, boolean backup) {
            return new File(worldDir, "vanished." + (backup ? "dat_old" : "dat"));
        }

        @Override
        public CompoundTag toNBT(CompoundTag tag) {
            VanishDB.INSTANCE.data.forEach((uuid, settings) -> tag.put(uuid.toString(), settings.toNBT()));
            return tag;
        }

        @Override
        public void fromNBT(CompoundTag tag) {
            System.out.println(VanishDB.INSTANCE);
            VanishDB.INSTANCE.data.clear();
            tag.getKeys().forEach(uuid -> {
                CompoundTag stag = tag.getCompound(uuid);
                VanishDB.INSTANCE.getOrCreateSettings(UUID.fromString(uuid)).fromNBT(stag);
            });
        }
    }
}
