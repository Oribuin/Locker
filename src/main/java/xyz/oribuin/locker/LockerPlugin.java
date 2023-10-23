package xyz.oribuin.locker;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import xyz.oribuin.locker.command.LockCommand;
import xyz.oribuin.locker.command.ReloadCommand;
import xyz.oribuin.locker.command.TrustCommand;
import xyz.oribuin.locker.command.UnlockCommand;
import xyz.oribuin.locker.command.UntrustCommand;
import xyz.oribuin.locker.listener.PlayerListeners;
import xyz.oribuin.locker.manager.ConfigurationManager;
import xyz.oribuin.locker.manager.LocaleManager;
import xyz.oribuin.locker.manager.LockManager;

import java.util.List;

public class LockerPlugin extends RosePlugin {

    private static LockerPlugin instance;

    public static LockerPlugin getInstance() {
        return instance;
    }

    public LockerPlugin() {
        super(-1, -1, ConfigurationManager.class, null, LocaleManager.class, null);

        instance = this;
    }

    @Override
    public void enable() {
        // Register Plugin Commands
        new LockCommand(this);
        new UnlockCommand(this);
        new TrustCommand(this);
        new UntrustCommand(this);

        new ReloadCommand(this);

        // Register Listener
        this.getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
    }

    @Override
    public void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of(LockManager.class);
    }

}
