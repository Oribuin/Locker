package xyz.oribuin.locker.command;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.locker.LockerPlugin;
import xyz.oribuin.locker.manager.LocaleManager;
import xyz.oribuin.locker.manager.LockManager;

public class TrustCommand implements CommandExecutor {

    private final LockerPlugin plugin;

    public TrustCommand(LockerPlugin plugin) {
        this.plugin = plugin;

        final PluginCommand command = this.plugin.getCommand("trust");
        if (command != null) {
            command.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final LockManager manager = this.plugin.getManager(LockManager.class);
        final LocaleManager locale = this.plugin.getManager(LocaleManager.class);

        // Check if the sender is a player
        if (!(sender instanceof Player player)) {
            locale.sendMessage(sender, "only-player");
            return true;
        }

        // Check if the player has permission to lock containers
        if (!player.hasPermission("locker.trust")) {
            locale.sendMessage(player, "no-permission");
            return true;
        }

        // Check if the player is looking at a container
        final Block target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof Container container)) {
            locale.sendMessage(player, "not-container");
            return true;
        }

        // Make sure the player to trust is provided
        if (args.length == 0) {
            locale.sendMessage(player, "command-trust-usage");
            return true;
        }

        // Check if the container is locked
        if (!manager.isOwner(player.getUniqueId(), container)) {
            locale.sendMessage(player, "command-trust-not-owner");
            return true;
        }

        // Make sure the player to trust is online
        final Player trusted = this.plugin.getServer().getPlayer(args[0]);
        if (trusted == null) {
            locale.sendMessage(player, "command-trust-not-player");
            return true;
        }

        // Check if the player is already trusted
        if (!manager.trust(trusted, container)) {
            locale.sendMessage(player, "command-trust-already-trusted");
            return true;
        }

        // Trust the player
        locale.sendMessage(player, "command-trust-success", StringPlaceholders.of("target", trusted.getName()));

        // Add particles to symbolize the container that has been modified
        player.spawnParticle(
                Particle.REDSTONE,
                container.getLocation().clone().add(0.5, 0.5, 0.5),
                10,
                0.5,
                0.5,
                0.5,
                0,
                new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(0, 0, 255),
                        1
                )
        );
        return true;
    }


}
