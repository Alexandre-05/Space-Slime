package fr.alex96x2.slimecapture.listener;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import fr.alex96x2.slimecapture.item.SlimeCaptureItems;
import fr.alex96x2.slimecapture.machine.SlimeExtractorGui;
import fr.alex96x2.slimecapture.machine.SlimeMachineManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class SlimeMachineListener implements Listener {

    private final SlimeCapturePlugin plugin;
    private final SlimeMachineManager machineManager;

    public SlimeMachineListener(SlimeCapturePlugin plugin) {
        this.plugin = plugin;
        this.machineManager = plugin.getMachineManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlaceMachine(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!SlimeCaptureItems.isExtractorItem(item)) {
            return;
        }

        if (!event.getPlayer().hasPermission("slimecapture.machine.place")) {
            return;
        }

        Block target = event.getClickedBlock();
        if (target == null) {
            return;
        }

        if (machineManager.isMachineBlock(target)) {
            event.setCancelled(true);
            machineManager.openGui(event.getPlayer(), target.getLocation());
            return;
        }

        Block placeAt = target.getRelative(event.getBlockFace());
        event.setCancelled(true);
        machineManager.placeMachine(event.getPlayer(), placeAt);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onOpenMachine(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !machineManager.isMachineBlock(block)) {
            return;
        }

        ItemStack item = event.getItem();
        if (item != null && SlimeCaptureItems.isExtractorItem(item)) {
            return;
        }

        if (!event.getPlayer().hasPermission("slimecapture.machine.use")) {
            return;
        }

        event.setCancelled(true);
        machineManager.openGui(event.getPlayer(), block.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreakMachine(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!machineManager.isMachineBlock(block)) {
            return;
        }

        event.setDropItems(false);
        event.setExpToDrop(0);
        machineManager.removeMachine(block, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof SlimeExtractorGui gui)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < top.getSize() && SlimeExtractorGui.isWithdrawSlot(rawSlot)) {
            event.setCancelled(true);
            machineManager.withdrawGel(player, gui.data());
            return;
        }

        if (rawSlot >= top.getSize()) {
            if (event.isShiftClick()) {
                ItemStack current = event.getCurrentItem();
                if (current == null || current.getType().isAir()) {
                    return;
                }
                int targetSlot = findShiftTargetSlot(current);
                int amountBefore = current.getAmount();
                if (targetSlot < 0 || !insertIntoSlot(top, targetSlot, current)) {
                    event.setCancelled(true);
                    return;
                }
                if (current.getAmount() == amountBefore) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                schedulePersist(gui);
            }
            return;
        }

        if (!SlimeExtractorGui.isInteractiveSlot(rawSlot)) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbar = event.getHotbarButton();
            if (hotbar >= 0) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbar);
                if (!canPlaceInSlot(rawSlot, hotbarItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (!canPlaceInSlot(rawSlot, hotbarItem)) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getAction() == InventoryAction.PLACE_ALL
                || event.getAction() == InventoryAction.PLACE_ONE
                || event.getAction() == InventoryAction.PLACE_SOME
                || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
            if (!canPlaceInSlot(rawSlot, event.getCursor())) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.isShiftClick()) {
            ItemStack current = event.getCurrentItem();
            if (current != null && !current.getType().isAir()) {
                event.setCancelled(true);
                player.getInventory().addItem(current.clone());
                top.setItem(rawSlot, null);
                schedulePersist(gui);
            }
            return;
        }

        schedulePersist(gui);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof SlimeExtractorGui gui)) {
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < gui.getInventory().getSize()) {
                if (!SlimeExtractorGui.isInteractiveSlot(rawSlot)
                        || !canPlaceInSlot(rawSlot, event.getOldCursor())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        schedulePersist(gui);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof SlimeExtractorGui gui)) {
            return;
        }
        machineManager.onGuiClose(gui);
    }

    private void schedulePersist(SlimeExtractorGui gui) {
        plugin.getServer().getScheduler().runTask(plugin, () -> machineManager.persistGui(gui));
    }

    private boolean insertIntoSlot(Inventory top, int slot, ItemStack incoming) {
        if (!canPlaceInSlot(slot, incoming)) {
            return false;
        }

        ItemStack inSlot = top.getItem(slot);
        if (inSlot == null || inSlot.getType().isAir()) {
            top.setItem(slot, incoming.clone());
            incoming.setAmount(0);
            return true;
        }

        if (slot == SlimeExtractorGui.SLOT_SLIME) {
            return false;
        }

        if (inSlot.isSimilar(incoming)) {
            int max = inSlot.getMaxStackSize();
            int space = max - inSlot.getAmount();
            if (space <= 0) {
                return false;
            }
            int moved = Math.min(space, incoming.getAmount());
            inSlot.setAmount(inSlot.getAmount() + moved);
            incoming.setAmount(incoming.getAmount() - moved);
            return moved > 0;
        }

        return false;
    }

    private boolean canPlaceInSlot(int slot, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }
        if (slot == SlimeExtractorGui.SLOT_FUEL) {
            return machineManager.acceptsFuel(item);
        }
        if (slot == SlimeExtractorGui.SLOT_SLIME) {
            return machineManager.acceptsSlime(item);
        }
        if (slot == SlimeExtractorGui.SLOT_FOOD) {
            return machineManager.acceptsFood(item);
        }
        return false;
    }

    private int findShiftTargetSlot(ItemStack item) {
        if (machineManager.acceptsSlime(item)) {
            return SlimeExtractorGui.SLOT_SLIME;
        }
        if (machineManager.acceptsFuel(item)) {
            return SlimeExtractorGui.SLOT_FUEL;
        }
        if (machineManager.acceptsFood(item)) {
            return SlimeExtractorGui.SLOT_FOOD;
        }
        return -1;
    }
}
