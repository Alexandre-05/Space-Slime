package fr.alex96x2.slimecapture.listener;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import fr.alex96x2.slimecapture.item.SlimeCaptureItems;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Event;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class VacuumListener implements Listener {

    private final SlimeCapturePlugin plugin;

    public VacuumListener(SlimeCapturePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!SlimeCaptureItems.isVacuumGun(event.getItem())) {
            return;
        }
        if (!event.getPlayer().hasPermission("slimecapture.use")) {
            return;
        }

        Action action = event.getAction();
        switch (action) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
                plugin.getVacuumService().toggleVacuum(event.getPlayer());
            }
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                event.setCancelled(true);
                plugin.getSlimeStorageService().releaseSlime(event.getPlayer(), event.getClickedBlock());
            }
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (SlimeCaptureItems.isVacuumGun(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (plugin.getVacuumService().isVacuuming(event.getPlayer())) {
            plugin.getVacuumService().stopVacuum(event.getPlayer(), true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getVacuumService().stopVacuum(event.getPlayer(), false);
        plugin.getActionBarService().clear(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSlimeDamageDuringVacuum(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!(event.getDamager() instanceof Slime)) {
            return;
        }
        if (plugin.getVacuumService().isVacuuming(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGunDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof org.bukkit.entity.Player player)) {
            return;
        }
        if (SlimeCaptureItems.isVacuumGun(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (SlimeCaptureItems.isVacuumGun(event.getItemDrop().getItemStack())) {
            plugin.getVacuumService().stopVacuum(event.getPlayer(), true);
        }
    }
}
