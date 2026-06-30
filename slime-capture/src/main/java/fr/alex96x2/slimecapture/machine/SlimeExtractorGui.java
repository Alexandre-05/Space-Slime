package fr.alex96x2.slimecapture.machine;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import fr.alex96x2.slimecapture.config.SlimeCaptureConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SlimeExtractorGui implements InventoryHolder {

    public static final int SLOT_FUEL_TANK = 10;
    public static final int SLOT_FUEL = 11;
    public static final int SLOT_FOOD_TANK = 12;
    public static final int SLOT_SLIME = 13;
    public static final int SLOT_FOOD = 14;
    public static final int SLOT_PROGRESS = 22;
    public static final int SLOT_WITHDRAW = 24;

    private static final Set<Integer> INPUT_SLOTS = Set.of(SLOT_FUEL, SLOT_SLIME, SLOT_FOOD);
    private static final Set<Integer> INTERACTIVE_SLOTS = Set.of(SLOT_FUEL, SLOT_SLIME, SLOT_FOOD);

    private final MachineData data;
    private final Inventory inventory;

    private SlimeExtractorGui(MachineData data) {
        this.data = data;
        this.inventory = Bukkit.createInventory(this, 27, Component.text("Extracteur de Gelée", NamedTextColor.LIGHT_PURPLE));
        fillDecorations();
        syncFromData();
    }

    public static SlimeExtractorGui open(MachineData data) {
        return new SlimeExtractorGui(data);
    }

    public MachineData data() {
        return data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void syncFromData() {
        inventory.setItem(SLOT_FUEL, cloneOrNull(data.fuelSlot()));
        inventory.setItem(SLOT_SLIME, cloneOrNull(data.slimeSlot()));
        inventory.setItem(SLOT_FOOD, cloneOrNull(data.foodSlot()));
        updateTankDisplays();
        updateProgressPane();
        updateWithdrawButton();
    }

    public void syncToData() {
        data.fuelSlot(cloneOrNull(inventory.getItem(SLOT_FUEL)));
        data.slimeSlot(cloneOrNull(inventory.getItem(SLOT_SLIME)));
        data.foodSlot(cloneOrNull(inventory.getItem(SLOT_FOOD)));
    }

    public void updateTankDisplays() {
        SlimeCaptureConfig config = SlimeCapturePlugin.getInstance().getPluginConfig();
        inventory.setItem(SLOT_FUEL_TANK, createFuelTankDisplay(data.virtualFuel(), config.machineFuelStorageMax(), config.machineFuelCostPerBatch()));
        inventory.setItem(SLOT_FOOD_TANK, createFoodTankDisplay(data.virtualFood(), config.machineFoodStorageMax(), config.machineFoodCostPerBatch()));
    }

    public void updateProgressPane() {
        SlimeCaptureConfig config = SlimeCapturePlugin.getInstance().getPluginConfig();
        int max = config.machineProcessDurationTicks();
        int progress = data.progress();
        int percent = max <= 0 ? 0 : Math.min(100, (progress * 100) / max);

        ItemStack pane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.text("Extraction", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(percent + " %", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Gel aléatoire : " + config.machineGelMin() + "–" + config.machineGelMax(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Risque perte slime : " + Math.round(config.machineSlimeLossChance() * 100) + " %", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        pane.setItemMeta(meta);
        inventory.setItem(SLOT_PROGRESS, pane);
    }

    public void updateWithdrawButton() {
        SlimeCaptureConfig config = SlimeCapturePlugin.getInstance().getPluginConfig();
        int stored = data.storedGel();
        int max = config.machineGelStorageMax();

        ItemStack button = new ItemStack(stored > 0 ? Material.SLIME_BALL : Material.GRAY_DYE);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Stockage gel", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text(stored + " / " + max, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        if (stored > 0) {
            lore.add(Component.text("▶ Clic gauche : retirer", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Vide", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(SLOT_WITHDRAW, button);
    }

    public static boolean isInputSlot(int slot) {
        return INPUT_SLOTS.contains(slot);
    }

    public static boolean isInteractiveSlot(int slot) {
        return INTERACTIVE_SLOTS.contains(slot);
    }

    public static boolean isWithdrawSlot(int slot) {
        return slot == SLOT_WITHDRAW;
    }

    private void fillDecorations() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.text(" "));
        filler.setItemMeta(meta);

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (!INTERACTIVE_SLOTS.contains(slot)
                    && slot != SLOT_PROGRESS
                    && slot != SLOT_WITHDRAW
                    && slot != SLOT_FUEL_TANK
                    && slot != SLOT_FOOD_TANK) {
                inventory.setItem(slot, filler.clone());
            }
        }

        inventory.setItem(4, labeledHint(Material.HONEY_BOTTLE, "Capsule de slimes", NamedTextColor.GREEN));
        inventory.setItem(3, labeledHint(Material.COAL, "Déposer carburant →", NamedTextColor.DARK_GRAY));
        inventory.setItem(5, labeledHint(Material.WHEAT, "Déposer nourriture →", NamedTextColor.YELLOW));
    }

    private static ItemStack createFuelTankDisplay(int current, int max, int cost) {
        ItemStack item = new ItemStack(Material.COAL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Réservoir carburant", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(buildTankLore(current, max, cost, "unités"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createFoodTankDisplay(int current, int max, int cost) {
        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Réservoir nourriture", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(buildTankLore(current, max, cost, "unités"));
        item.setItemMeta(meta);
        return item;
    }

    private static List<Component> buildTankLore(int current, int max, int cost, String unitLabel) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text(current + " / " + max + " " + unitLabel, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Coût : " + cost + " / extraction", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Les items déposés alimentent ce stockage", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true));
        return lore;
    }

    private static ItemStack labeledHint(Material material, String label, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, color).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack cloneOrNull(ItemStack item) {
        return item == null || item.getType().isAir() ? null : item.clone();
    }
}
