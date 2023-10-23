package xyz.oribuin.locker.manager;

import com.jeff_media.morepersistentdatatypes.DataType;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.locker.util.LockerKeys;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockManager extends Manager {

    public LockManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    /**
     * Lock a container of any type
     *
     * @param player    The player locking the container
     * @param container The container being locked
     */
    public void lock(@NotNull Player player, @NotNull Container container) {
        final PersistentDataContainer dataContainer = container.getPersistentDataContainer();

        // Set the container data
        dataContainer.set(LockerKeys.OWNER.getKey(), DataType.UUID, player.getUniqueId());
        dataContainer.set(LockerKeys.OWNER_NAME.getKey(), PersistentDataType.STRING, player.getName());
        dataContainer.set(LockerKeys.TRUSTED.getKey(), DataType.asList(DataType.UUID), new ArrayList<>());
        dataContainer.set(LockerKeys.LOCKED_TIME.getKey(), DataType.LONG, System.currentTimeMillis());

        container.update(); // Update the container
    }

    /**
     * Unlock a container of any type
     *
     * @param container The container being unlocked
     */
    public void unlock(@NotNull Container container) {
        final PersistentDataContainer dataContainer = container.getPersistentDataContainer();

        Arrays.stream(LockerKeys.values())
                .map(LockerKeys::getKey)
                .forEach(dataContainer::remove);

        container.update(); // Update the container
    }

    /**
     * Check if a container is locked
     *
     * @param container The container being checked
     * @return true if the container is locked
     */
    public boolean isLocked(@NotNull Container container) {
        return container.getPersistentDataContainer().has(LockerKeys.OWNER.getKey(), DataType.UUID);
    }

    /**
     * Get the owner of the container
     *
     * @param container The container being checked
     * @return The owner of the container
     */
    @Nullable
    public UUID getOwner(@NotNull Container container) {
        return container.getPersistentDataContainer().get(LockerKeys.OWNER.getKey(), DataType.UUID);
    }

    /**
     * Check if the owner of the container is the player
     *
     * @param player    The player being checked
     * @param container The container being checked
     * @return true if the player is the owner
     */
    public boolean isOwner(@NotNull UUID player, @NotNull Container container) {
        final UUID owner = container.getPersistentDataContainer().get(LockerKeys.OWNER.getKey(), DataType.UUID);
        return owner != null && owner.equals(player);
    }

    /**
     * Add a player to the trusted list
     *
     * @param player    The player being trusted
     * @param container The container being trusted
     */
    public boolean trust(@NotNull Player player, @NotNull Container container) {
        final PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        final List<UUID> trusted = dataContainer.getOrDefault(
                LockerKeys.TRUSTED.getKey(),
                DataType.asList(DataType.UUID),
                new ArrayList<>()
        );

        if (trusted.contains(player.getUniqueId())) {
            return false;
        }

        trusted.add(player.getUniqueId());
        dataContainer.set(LockerKeys.TRUSTED.getKey(), DataType.asList(DataType.UUID), trusted); // Set the trusted list
        container.update(); // Update the container
        return true;
    }

    /**
     * Remove a player from the trusted list
     *
     * @param player    The player being untrusted
     * @param container The container being untrusted
     */
    public boolean untrust(@NotNull Player player, @NotNull Container container) {
        final PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        final List<UUID> trusted = dataContainer.getOrDefault(
                LockerKeys.TRUSTED.getKey(),
                DataType.asList(DataType.UUID),
                new ArrayList<>()
        );

        if (!trusted.contains(player.getUniqueId())) {
            return false;
        }

        trusted.remove(player.getUniqueId());
        dataContainer.set(LockerKeys.TRUSTED.getKey(), DataType.asList(DataType.UUID), trusted); // Set the trusted list
        container.update(); // Update the container

        return true;
    }

    /**
     * Check if a player is trusted
     *
     * @param player    The player being checked
     * @param container The container being checked
     * @return true if the player is trusted
     */
    public boolean isTrusted(Player player, Container container) {
        final PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        final List<UUID> trusted = dataContainer.getOrDefault(
                LockerKeys.TRUSTED.getKey(),
                DataType.asList(DataType.UUID),
                new ArrayList<>()
        );

        System.out.println("trusted: " + trusted);

        return trusted.contains(player.getUniqueId());
    }

    /**
     * Get the trusted list
     *
     * @param container The container being checked
     * @return The trusted list
     */
    @NotNull
    public List<UUID> getTrusted(@NotNull Container container) {
        return container.getPersistentDataContainer().getOrDefault(
                LockerKeys.TRUSTED.getKey(),
                DataType.asList(DataType.UUID),
                new ArrayList<>()
        );
    }

    /**
     * Get the time the container was locked
     *
     * @param container The container being checked
     * @return The time the container was locked
     */
    public long getLockedTime(@NotNull Container container) {
        return container.getPersistentDataContainer().getOrDefault(
                LockerKeys.LOCKED_TIME.getKey(),
                DataType.LONG,
                0L
        );
    }

    /**
     * Check if a player can access a container
     *
     * @param player    The player being checked
     * @param container The container being checked
     * @return true if the player can access the container
     */
    public boolean canAccess(@NotNull Player player, Container container) {
        if (this.isOwner(player.getUniqueId(), container)) {
            return true;
        }

        return this.isTrusted(player, container);
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

}
