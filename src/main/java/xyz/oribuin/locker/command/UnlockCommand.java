package xyz.oribuin.locker.command;

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

public class UnlockCommand implements CommandExecutor {

    private final LockerPlugin plugin;

    public UnlockCommand(LockerPlugin plugin) {
        this.plugin = plugin;

        final PluginCommand command = this.plugin.getCommand("unlock");
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
        if (!player.hasPermission("locker.unlock")) {
            locale.sendMessage(player, "no-permission");
            return true;
        }

        // Check if the player is looking at a container
        final Block target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof Container container)) {
            locale.sendMessage(player, "not-container");
            return true;
        }

        // Check if the container is locked
        if (!manager.isLocked(container)) {
            locale.sendMessage(player, "command-unlock-not-locked");
            return true;
        }

        // Check if the player is the owner of the container or has bypass permission
        if (!manager.isOwner(player.getUniqueId(), container) || !player.hasPermission("locker.bypass")) {
            locale.sendMessage(player, "command-unlock-not-owner");
            return true;
        }

        manager.unlock(container);
        locale.sendMessage(player, "command-unlock-success");

        player.spawnParticle(
                Particle.REDSTONE,
                container.getLocation().clone().add(0.5, 0.5, 0.5),
                10,
                0.5,
                0.5,
                0.5,
                0,
                new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(0, 255, 0),
                        1
                )
        );
        return true;
    }

}
