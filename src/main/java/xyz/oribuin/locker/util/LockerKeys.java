package xyz.oribuin.locker.util;

import org.bukkit.NamespacedKey;
import xyz.oribuin.locker.LockerPlugin;

public enum LockerKeys {

    OWNER, // UUID
    OWNER_NAME, // String
    TRUSTED, // List<UUID>
    LOCKED_TIME, // Long

    ;

    private final NamespacedKey key;

    LockerKeys() {
        this.key = new NamespacedKey(LockerPlugin.getInstance(), this.name().toLowerCase());
    }

    public NamespacedKey getKey() {
        return key;
    }


}
