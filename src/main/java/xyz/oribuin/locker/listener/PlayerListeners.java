package xyz.oribuin.locker.listener;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.oribuin.locker.LockerPlugin;
import xyz.oribuin.locker.manager.ConfigurationManager.Setting;
import xyz.oribuin.locker.manager.LocaleManager;
import xyz.oribuin.locker.manager.LockManager;

import java.util.UUID;

public class PlayerListeners implements Listener {

    private final LockerPlugin plugin;
    private final LockManager manager;

    public PlayerListeners(LockerPlugin plugin) {
        this.plugin = plugin;
        this.manager = this.plugin.getManager(LockManager.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        if (block == null || !(block.getState() instanceof Container container)) {
            return;
        }

        if (!this.manager.isLocked(container))
            return;

        // TODO: Check if player is bypassing.
        if (!this.manager.canAccess(player, container)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (!Setting.AUTO_LOCK.getBoolean()) {
            return;
        }

        final Block block = event.getBlock();
        final Player who = event.getPlayer();

        if (!(block.getState() instanceof Container container)) {
            return;
        }

        this.manager.lock(who, container);
        who.spawnParticle(
                Particle.REDSTONE,
                container.getLocation().clone().add(0.5, 0.5, 0.5),
                10,
                0.5,
                0.5,
                0.5,
                0,
                new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(255, 0, 0),
                        1
                )
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player who = event.getPlayer();

        if (!(block.getState() instanceof Container container)) {
            return;
        }

        if (!this.manager.isLocked(container)) {
            return;
        }

        // Check if the player can access the container
        if (this.manager.isOwner(who.getUniqueId(), container) || who.hasPermission("locker.bypass")) {

            // Check if the player is sneaking
            if (who.isSneaking()) {
                return;
            }

            // If not sneaking, require the player to sneak to destroy the container.
            event.setCancelled(true);
            this.plugin.getManager(LocaleManager.class).sendMessage(who, "sneak-to-destroy");
            return;
        }

        // Cancel the event and send the player a message
        event.setCancelled(true);
        this.plugin.getManager(LocaleManager.class).sendMessage(who, "container-locked");
    }

    /**
     * Stop locked containers from exploding from blocks (Nether Beds/Respawn Anchors)
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (!(block.getState() instanceof Container container)) {
                return false;
            }

            return this.manager.isLocked(container);
        });
    }

    /**
     * Stop locked containers from exploding through entities (Creepers, TNT, End Crystals, etc.)
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (!(block.getState() instanceof Container container)) {
                return false;
            }

            return this.manager.isLocked(container);
        });
    }

    // TODO: Only allow hoppers to deposit/take items from locked containers if the hopper is also locked by the same owner.

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onHopperTransfer(InventoryMoveItemEvent event) {

        // Make sure the destination is a container
        if (!(event.getDestination().getHolder() instanceof Container destination)) {
            return;
        }

        // Make sure the source is a container
        if (!(event.getInitiator().getHolder() instanceof Container source)) {
            return;
        }

        final UUID destinationOwner = this.manager.getOwner(destination);
        final UUID sourceOwner = this.manager.getOwner(source);

        // If the destination is locked, make sure the source is also locked by the same owner.
        if (destinationOwner != null && !destinationOwner.equals(sourceOwner)) {
            event.setCancelled(true);
            return;
        }

        // If the source is locked, make sure the destination is also locked by the same owner.
        if (sourceOwner != null && !sourceOwner.equals(destinationOwner)) {
            event.setCancelled(true);
        }

    }

}
