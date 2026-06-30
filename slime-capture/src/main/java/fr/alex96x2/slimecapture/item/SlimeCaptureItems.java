package fr.alex96x2.slimecapture.item;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class SlimeCaptureItems {

    public static final NamespacedKey VACUUM_GUN_KEY = new NamespacedKey("slimecapture", "vacuum_gun");
    public static final NamespacedKey SLIME_CANISTER_KEY = new NamespacedKey("slimecapture", "slime_canister");
    public static final NamespacedKey GUN_SLIME_COUNT_KEY = new NamespacedKey("slimecapture", "gun_slime_count");
    public static final NamespacedKey CANISTER_AMOUNT_KEY = new NamespacedKey("slimecapture", "canister_amount");
    public static final NamespacedKey EXTRACTOR_ITEM_KEY = new NamespacedKey("slimecapture", "extractor_item");
    public static final NamespacedKey MACHINE_BLOCK_KEY = new NamespacedKey("slimecapture", "machine_block");
    public static final NamespacedKey SLIME_GEL_KEY = new NamespacedKey("slimecapture", "slime_gel");

    private SlimeCaptureItems() {
    }

    public static ItemStack createVacuumGun() {
        return createVacuumGun(0);
    }

    public static ItemStack createVacuumGun(int slimeCount) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        int capacity = SlimeCapturePlugin.getInstance().getPluginConfig().gunCapacity();

        meta.displayName(Component.text("Aspirateur à Slimes", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(buildGunLore(slimeCount, capacity));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(VACUUM_GUN_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(GUN_SLIME_COUNT_KEY, PersistentDataType.INTEGER, Math.max(0, slimeCount));
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isVacuumGun(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(VACUUM_GUN_KEY, PersistentDataType.BYTE);
    }

    public static int getGunSlimeCount(ItemStack item) {
        if (!isVacuumGun(item)) {
            return 0;
        }
        Integer count = item.getItemMeta().getPersistentDataContainer().get(GUN_SLIME_COUNT_KEY, PersistentDataType.INTEGER);
        return count != null ? Math.max(0, count) : 0;
    }

    public static ItemStack setGunSlimeCount(ItemStack item, int count) {
        if (!isVacuumGun(item) || !item.hasItemMeta()) {
            return item;
        }
        int capacity = SlimeCapturePlugin.getInstance().getPluginConfig().gunCapacity();
        count = Math.max(0, Math.min(count, capacity));

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(GUN_SLIME_COUNT_KEY, PersistentDataType.INTEGER, count);
        meta.lore(buildGunLore(count, capacity));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createSlimeCanister(int amount) {
        int maxStack = SlimeCapturePlugin.getInstance().getPluginConfig().canisterMaxStack();
        amount = Math.max(1, Math.min(amount, maxStack));

        ItemStack item = new ItemStack(Material.HONEY_BOTTLE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Capsule de Slimes", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(buildCanisterLore(amount, maxStack));
        meta.setMaxStackSize(1);
        meta.getPersistentDataContainer().set(SLIME_CANISTER_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(CANISTER_AMOUNT_KEY, PersistentDataType.INTEGER, amount);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSlimeCanister(ItemStack item) {
        if (item == null || item.getType() != Material.HONEY_BOTTLE || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(SLIME_CANISTER_KEY, PersistentDataType.BYTE);
    }

    public static int getCanisterAmount(ItemStack item) {
        if (!isSlimeCanister(item)) {
            return 0;
        }
        Integer amount = item.getItemMeta().getPersistentDataContainer().get(CANISTER_AMOUNT_KEY, PersistentDataType.INTEGER);
        return amount != null ? Math.max(0, amount) : 0;
    }

    public static ItemStack consumeFromCanister(ItemStack canister, int amount) {
        if (!isSlimeCanister(canister) || amount <= 0) {
            return canister;
        }
        int remaining = getCanisterAmount(canister) - amount;
        if (remaining <= 0) {
            return null;
        }
        return createSlimeCanister(remaining);
    }

    public static ItemStack createExtractorItem() {
        ItemStack item = new ItemStack(Material.LODESTONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Extracteur de Gelée", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Transforme les slimes en gel", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("▶ Clic droit sur un bloc", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(" : placer la machine", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))
        ));
        meta.getPersistentDataContainer().set(EXTRACTOR_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isExtractorItem(ItemStack item) {
        if (item == null || item.getType() != Material.LODESTONE || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(EXTRACTOR_ITEM_KEY, PersistentDataType.BYTE);
    }

    public static ItemStack createSlimeGel(int amount) {
        amount = Math.max(1, Math.min(amount, 64));
        ItemStack item = new ItemStack(Material.SLIME_BALL, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Gel de Slime", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Produit par l'extracteur", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(SLIME_GEL_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSlimeGel(ItemStack item) {
        if (item == null || item.getType() != Material.SLIME_BALL || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(SLIME_GEL_KEY, PersistentDataType.BYTE);
    }

    private static List<Component> buildGunLore(int slimeCount, int capacity) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Modèle SR-3000", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Aspire les slimes vivants", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("▶ Clic droit", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" : activer l'aspiration", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("▶ Re-clic droit", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" : désactiver", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("▶ Clic gauche", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" : relâcher un slime", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(Component.text("Réservoir : ", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(slimeCount + " / " + capacity, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("Transférez vers une machine avec /sc capsule", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true));
        return lore;
    }

    private static List<Component> buildCanisterLore(int amount, int maxStack) {
        return List.of(
                Component.empty(),
                Component.text("Slimes stabilisés pour transfert", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Contenu : " + amount + " slime(s)", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("◆ À insérer dans l'extracteur", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false),
                Component.text("◆ Max " + maxStack + " slimes par capsule", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
        );
    }
}
