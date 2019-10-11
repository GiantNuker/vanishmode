package io.github.indicode.fabric.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Indigo Amann
 */
public class VanishCommand {
    public static final String PERM_HEAD = "vanish";
    public enum Setting {
        MOBS_IGNORE("mobs_ignore", pair -> pair.getLeft().mobs_ignore = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().mobs_ignore)),
        EVENTS_IGNORE("events_ignore", pair -> pair.getLeft().events_ignore = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().events_ignore)),
        //SPECTATOR_PREDICATE("spectator_predicate", pair -> pair.getLeft().spectator_predicate = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().spectator_predicate)),
        BOUNDINGBOX("no_hitbox", pair -> pair.getLeft().boundingbox = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().boundingbox)),
        SILENT_CHESTS("silent_chests", pair -> pair.getLeft().silent_chests = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().silent_chests)),
        GENERATE_LOOT("generate_loot", pair -> pair.getLeft().generates_chests = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().generates_chests)),
        IGNORE_LOCKS("ignore_locks", pair -> pair.getLeft().ignore_locks = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().ignore_locks)),
        INVINCIBLE("invincible", pair -> pair.getLeft().invincible = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().invincible)),
        SPECTATOR("partial-spectator", pair -> pair.getLeft().partial_spectator = pair.getRight(), pair -> pair.getRight().set(pair.getLeft().partial_spectator));
        String id;
        Consumer<Pair<VanishDB.VanishSettings, Boolean>> setter;
        Consumer<Pair<VanishDB.VanishSettings, AtomicBoolean>> getter;
        Setting(String id, Consumer<Pair<VanishDB.VanishSettings, Boolean>> setter, Consumer<Pair<VanishDB.VanishSettings, AtomicBoolean>> getter) {
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }
        public boolean get(UUID uuid) {
            VanishDB.VanishSettings settings = VanishDB.INSTANCE.getOrCreateSettings(uuid);
            AtomicBoolean value = new AtomicBoolean();
            getter.accept(new Pair<>(settings, value));
            return value.get();
        }
        public void set(ServerPlayerEntity player, boolean enabled) {
            VanishDB.VanishSettings settings = VanishDB.INSTANCE.getOrCreateSettings(player.getGameProfile().getId());
            setter.accept(new Pair<>(settings, enabled));
            settings.updateSettings(player);
        }
    }
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("vanish");
        command.requires(source -> Thimble.hasPermissionOrOp(source, PERM_HEAD, 2) || Thimble.hasPermissionOrOp(source, PERM_HEAD + ".vanish", 2) || Thimble.hasPermissionOrOp(source, PERM_HEAD + ".view", 2));
        {
            LiteralArgumentBuilder<ServerCommandSource> view = CommandManager.literal("view");
            view.requires(source -> Thimble.hasPermissionOrOp(source, PERM_HEAD + ".view", 2));
            view.executes(context -> toggleSeesVanish(context.getSource().getPlayer()));
            command.then(view);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> settings = CommandManager.literal("setting");
            settings.requires(source -> Thimble.hasPermissionOrOp(source, PERM_HEAD + ".vanish", 2));
            for (Setting setting : Setting.values()) {
                LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal(setting.id);
                literal.requires(source -> Thimble.hasPermissionOrOp(source, PERM_HEAD + ".setting." + setting.id, 2));
                literal.executes(context -> readSetting(context.getSource(), setting));
                {
                    RequiredArgumentBuilder<ServerCommandSource, Boolean> set = CommandManager.argument("enabled", BoolArgumentType.bool());
                    set.requires(source -> Thimble.hasPermissionOrOp(source, PERM_HEAD + ".setting." + setting.id, 2));
                    set.executes(context -> writeSetting(context.getSource(), setting, BoolArgumentType.getBool(context, "enabled")));
                    literal.then(set);
                }
                settings.then(literal);
            }
            command.then(settings);
        }
        command.executes(context -> {
            if (Thimble.hasPermissionOrOp(context.getSource(), PERM_HEAD + ".vanish", 2)) return toggleVanish(context.getSource().getPlayer());
            else {
                context.getSource().sendFeedback(new LiteralText("You do not have permission to vanish.").formatted(Formatting.RED), false);
                return 0;
            }
        });
        dispatcher.register(command);
    }
    private static int readSetting(ServerCommandSource source, Setting setting) throws CommandSyntaxException {
        UUID uuid = source.getPlayer().getGameProfile().getId();
        boolean enabled = setting.get(uuid);
        source.sendFeedback(new LiteralText(setting.id + " is " + (enabled ? "enabled" : "disabled")).formatted(Formatting.YELLOW), false);
        return 0;
    }
    private static int writeSetting(ServerCommandSource source, Setting setting, boolean enabled) throws CommandSyntaxException {
        setting.set(source.getPlayer(), enabled);
        source.sendFeedback(new LiteralText(setting.id + " is now " + (enabled ? "enabled" : "disabled")).formatted(Formatting.GREEN), false);
        return 0;
    }
    private static int toggleVanish(ServerPlayerEntity player) {
        boolean vanished = !VanishDB.INSTANCE.isVanished(player.getGameProfile().getId());
        player.sendMessage(new LiteralText("You are " + (vanished ? "now in" : "no longer in") + " vanish.").formatted(Formatting.GREEN));
        VanishDB.INSTANCE.updateClient(player, vanished, VanishDB.INSTANCE.getOrCreateSettings(player.getGameProfile().getId()).seeVanished);
        return 0;
    }
    private static int toggleSeesVanish(ServerPlayerEntity player) {
        boolean seevanished = !VanishDB.INSTANCE.canSeeVanished(player.getGameProfile().getId());
        player.sendMessage(new LiteralText("You can " + (seevanished ? "now see" : "no longer see") + " players in vanish.").formatted(Formatting.GREEN));
        VanishDB.INSTANCE.updateClient(player, VanishDB.INSTANCE.isVanished(player.getGameProfile().getId()), seevanished);
        return 0;
    }

}
