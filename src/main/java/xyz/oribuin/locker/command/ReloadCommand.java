package xyz.oribuin.locker.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.locker.LockerPlugin;
import xyz.oribuin.locker.manager.LocaleManager;

public class ReloadCommand implements CommandExecutor {

    private final LockerPlugin plugin;

    public ReloadCommand(LockerPlugin plugin) {
        this.plugin = plugin;

        final PluginCommand command = this.plugin.getCommand("reloadlocker");
        if (command != null) {
            command.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final LocaleManager locale = this.plugin.getManager(LocaleManager.class);

        // Check if the player has permission to lock containers
        if (!sender.hasPermission("locker.reload")) {
            locale.sendMessage(sender, "no-permission");
            return true;
        }

        this.plugin.reload();
        locale.sendMessage(sender, "command-reload-reloaded");
        return true;
    }


}
