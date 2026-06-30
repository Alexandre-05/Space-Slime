package fr.alex96x2.slimecapture.machine;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import fr.alex96x2.slimecapture.config.SlimeCaptureConfig;
import fr.alex96x2.slimecapture.item.SlimeCaptureItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class SlimeMachineManager {

    private final SlimeCapturePlugin plugin;
    private final Map<String, MachineData> machines = new HashMap<>();
    private final Map<Location, SlimeExtractorGui> openGuis = new HashMap<>();
    private BukkitTask tickTask;
    private final File dataFile;

    public SlimeMachineManager(SlimeCapturePlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "machines.yml");
        load();
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickMachines, 20L, 1L);
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        for (SlimeExtractorGui gui : openGuis.values()) {
            gui.syncToData();
            absorbInputs(gui.data());
            for (org.bukkit.entity.HumanEntity viewer : gui.getInventory().getViewers()) {
                viewer.closeInventory();
            }
        }
        openGuis.clear();
        save();
    }

    public boolean isMachineBlock(Block block) {
        if (block == null || block.getType() != Material.SMOKER) {
            return false;
        }
        if (!(block.getState() instanceof TileState state)) {
            return false;
        }
        return state.getPersistentDataContainer().has(SlimeCaptureItems.MACHINE_BLOCK_KEY, PersistentDataType.BYTE);
    }

    public MachineData getMachine(Location location) {
        return machines.get(key(location));
    }

    public boolean placeMachine(Player player, Block target) {
        if (target == null || !target.isEmpty() && !target.isReplaceable()) {
            plugin.getMessageService().sendChat(player, "machine-invalid-block");
            return false;
        }

        Location location = target.getLocation();
        if (getMachine(location) != null) {
            openGui(player, location);
            return true;
        }

        target.setType(Material.SMOKER, false);
        if (!(target.getState() instanceof TileState state)) {
            target.setType(Material.AIR, false);
            plugin.getMessageService().sendChat(player, "machine-invalid-block");
            return false;
        }

        state.getPersistentDataContainer().set(SlimeCaptureItems.MACHINE_BLOCK_KEY, PersistentDataType.BYTE, (byte) 1);
        state.update(true, false);

        MachineData data = new MachineData(location);
        machines.put(key(location), data);
        save();

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (SlimeCaptureItems.isExtractorItem(hand) && player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            hand.setAmount(hand.getAmount() - 1);
        }

        plugin.getMessageService().sendChat(player, "machine-placed");
        openGui(player, location);
        return true;
    }

    public void openGui(Player player, Location location) {
        MachineData data = getMachine(location);
        if (data == null && isMachineBlock(location.getBlock())) {
            data = new MachineData(normalize(location));
            machines.put(key(location), data);
            save();
        }
        if (data == null) {
            return;
        }

        SlimeExtractorGui existing = openGuis.get(normalize(location));
        if (existing != null) {
            existing.syncFromData();
            player.openInventory(existing.getInventory());
            return;
        }

        SlimeExtractorGui gui = SlimeExtractorGui.open(data);
        openGuis.put(normalize(location), gui);
        player.openInventory(gui.getInventory());
    }

    public void onGuiClose(SlimeExtractorGui gui) {
        gui.syncToData();
        absorbInputs(gui.data());
        openGuis.remove(normalize(gui.data().location()));
        save();
    }

    public void removeMachine(Block block, Player breaker) {
        Location location = block.getLocation();
        MachineData data = machines.remove(key(location));
        if (data == null) {
            return;
        }

        SlimeExtractorGui open = openGuis.remove(normalize(location));
        if (open != null) {
            open.syncToData();
            absorbInputs(data);
            for (org.bukkit.entity.HumanEntity viewer : open.getInventory().getViewers()) {
                viewer.closeInventory();
            }
        } else {
            absorbInputs(data);
        }

        dropContents(location, data);
        block.setType(Material.AIR, false);
        location.getWorld().dropItemNaturally(location.add(0.5, 0.5, 0.5), SlimeCaptureItems.createExtractorItem());
        save();

        if (breaker != null) {
            plugin.getMessageService().sendChat(breaker, "machine-removed");
        }
    }

    public boolean acceptsFuel(ItemStack item) {
        return item != null && !item.getType().isAir()
                && plugin.getPluginConfig().isMachineFuel(item.getType());
    }

    public boolean acceptsFood(ItemStack item) {
        return item != null && !item.getType().isAir()
                && plugin.getPluginConfig().isMachineFood(item.getType());
    }

    public boolean acceptsSlime(ItemStack item) {
        return SlimeCaptureItems.isSlimeCanister(item)
                && SlimeCaptureItems.getCanisterAmount(item) > 0;
    }

    public void syncOpenGuis() {
        for (SlimeExtractorGui gui : openGuis.values()) {
            gui.syncToData();
            absorbInputs(gui.data());
        }
    }

    public void persistGui(SlimeExtractorGui gui) {
        gui.syncToData();
        absorbInputs(gui.data());
        gui.syncFromData();
        save();
    }

    public void withdrawGel(Player player, MachineData data) {
        if (data.storedGel() <= 0) {
            plugin.getMessageService().sendChat(player, "machine-gel-empty");
            refreshGui(data);
            return;
        }

        int capacity = maxGelFit(player);
        if (capacity <= 0) {
            plugin.getMessageService().sendChat(player, "inventory-full");
            refreshGui(data);
            return;
        }

        int toWithdraw = Math.min(data.storedGel(), capacity);
        int given = 0;
        int remaining = toWithdraw;

        while (remaining > 0) {
            int chunk = Math.min(remaining, 64);
            ItemStack gel = SlimeCaptureItems.createSlimeGel(chunk);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(gel);
            if (!leftover.isEmpty()) {
                int notGiven = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                given += chunk - notGiven;
                break;
            }
            given += chunk;
            remaining -= chunk;
        }

        if (given <= 0) {
            plugin.getMessageService().sendChat(player, "inventory-full");
            refreshGui(data);
            return;
        }

        data.storedGel(data.storedGel() - given);
        plugin.getMessageService().sendChat(player, "machine-gel-withdrawn", Map.of("amount", String.valueOf(given)));
        refreshGui(data);
        save();
    }

    private int maxGelFit(Player player) {
        int total = 0;
        for (ItemStack slot : player.getInventory().getStorageContents()) {
            if (slot == null || slot.getType().isAir()) {
                total += 64;
            } else if (SlimeCaptureItems.isSlimeGel(slot)) {
                total += Math.max(0, 64 - slot.getAmount());
            }
        }
        return total;
    }

    private void tickMachines() {
        syncOpenGuis();

        SlimeCaptureConfig config = plugin.getPluginConfig();
        int duration = config.machineProcessDurationTicks();

        for (MachineData data : machines.values()) {
            Block block = data.location().getBlock();
            if (!isMachineBlock(block)) {
                continue;
            }

            if (!canProcess(data, config)) {
                if (data.progress() > 0) {
                    data.progress(0);
                    refreshGui(data);
                }
                continue;
            }

            data.progress(data.progress() + 1);
            refreshGui(data);

            if (data.progress() >= duration) {
                completeBatch(data, config);
                data.progress(0);
                refreshGui(data);
                save();
            }
        }
    }

    private boolean canProcess(MachineData data, SlimeCaptureConfig config) {
        if (!hasSlime(data.slimeSlot())) {
            return false;
        }
        if (data.virtualFuel() < config.machineFuelCostPerBatch()) {
            return false;
        }
        if (data.virtualFood() < config.machineFoodCostPerBatch()) {
            return false;
        }
        return data.storedGel() < config.machineGelStorageMax();
    }

    private boolean hasSlime(ItemStack canister) {
        return SlimeCaptureItems.isSlimeCanister(canister)
                && SlimeCaptureItems.getCanisterAmount(canister) >= 1;
    }

    private void completeBatch(MachineData data, SlimeCaptureConfig config) {
        data.virtualFuel(data.virtualFuel() - config.machineFuelCostPerBatch());
        data.virtualFood(data.virtualFood() - config.machineFoodCostPerBatch());

        if (ThreadLocalRandom.current().nextDouble() < config.machineSlimeLossChance()) {
            ItemStack updatedCanister = SlimeCaptureItems.consumeFromCanister(data.slimeSlot(), 1);
            data.slimeSlot(updatedCanister);
        }

        int gelMin = Math.min(config.machineGelMin(), config.machineGelMax());
        int gelMax = Math.max(config.machineGelMin(), config.machineGelMax());
        int produced = gelMin + ThreadLocalRandom.current().nextInt(gelMax - gelMin + 1);
        int space = config.machineGelStorageMax() - data.storedGel();
        data.storedGel(data.storedGel() + Math.min(produced, space));
    }

    private void absorbInputs(MachineData data) {
        SlimeCaptureConfig config = plugin.getPluginConfig();
        absorbResource(data.fuelSlot(), true, data, config);
        absorbResource(data.foodSlot(), false, data, config);
    }

    private void absorbResource(ItemStack stack, boolean fuel, MachineData data, SlimeCaptureConfig config) {
        if (stack == null || stack.getType().isAir()) {
            return;
        }

        boolean valid = fuel ? acceptsFuel(stack) : acceptsFood(stack);
        if (!valid) {
            return;
        }

        int unitsPerItem = fuel ? config.fuelUnits(stack.getType()) : config.foodUnits(stack.getType());
        int current = fuel ? data.virtualFuel() : data.virtualFood();
        int max = fuel ? config.machineFuelStorageMax() : config.machineFoodStorageMax();
        int space = max - current;
        if (space <= 0 || unitsPerItem <= 0) {
            return;
        }

        int absorbedItems = 0;
        int absorbedUnits = 0;
        int amount = stack.getAmount();
        for (int i = 0; i < amount; i++) {
            if (absorbedUnits + unitsPerItem > space) {
                break;
            }
            absorbedItems++;
            absorbedUnits += unitsPerItem;
        }

        if (absorbedItems <= 0) {
            return;
        }

        if (fuel) {
            data.virtualFuel(current + absorbedUnits);
            data.fuelSlot(remainingStack(stack, absorbedItems));
        } else {
            data.virtualFood(current + absorbedUnits);
            data.foodSlot(remainingStack(stack, absorbedItems));
        }
    }

    private static ItemStack remainingStack(ItemStack stack, int consumed) {
        int left = stack.getAmount() - consumed;
        if (left <= 0) {
            return null;
        }
        ItemStack copy = stack.clone();
        copy.setAmount(left);
        return copy;
    }

    private void refreshGui(MachineData data) {
        SlimeExtractorGui gui = openGuis.get(normalize(data.location()));
        if (gui != null) {
            gui.syncFromData();
        }
    }

    private void dropContents(Location location, MachineData data) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        Location dropAt = location.clone().add(0.5, 0.5, 0.5);
        dropIfPresent(world, dropAt, data.slimeSlot());
        dropIfPresent(world, dropAt, data.fuelSlot());
        dropIfPresent(world, dropAt, data.foodSlot());
        dropGelStorage(world, dropAt, data.storedGel());
    }

    private void dropGelStorage(World world, Location location, int amount) {
        while (amount > 0) {
            int stack = Math.min(64, amount);
            world.dropItemNaturally(location, SlimeCaptureItems.createSlimeGel(stack));
            amount -= stack;
        }
    }

    private static void dropIfPresent(World world, Location location, ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            world.dropItemNaturally(location, item);
        }
    }

    private void load() {
        machines.clear();
        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = yaml.getConfigurationSection("machines");
        if (section == null) {
            return;
        }

        for (String entryKey : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(entryKey);
            if (entry == null) {
                continue;
            }

            String worldName = entry.getString("world");
            World world = worldName != null ? plugin.getServer().getWorld(worldName) : null;
            if (world == null) {
                continue;
            }

            Location location = new Location(world,
                    entry.getInt("x"),
                    entry.getInt("y"),
                    entry.getInt("z"));

            if (!isMachineBlock(location.getBlock())) {
                continue;
            }

            MachineData data = new MachineData(location);
            data.slimeSlot(entry.getItemStack("slime"));
            data.fuelSlot(entry.getItemStack("fuel"));
            data.foodSlot(entry.getItemStack("food"));
            data.virtualFuel(entry.getInt("virtual-fuel", 0));
            data.virtualFood(entry.getInt("virtual-food", 0));
            data.storedGel(entry.getInt("stored-gel", 0));
            data.progress(entry.getInt("progress", 0));

            ItemStack legacyOutput = entry.getItemStack("output");
            if (legacyOutput != null && SlimeCaptureItems.isSlimeGel(legacyOutput)) {
                data.storedGel(data.storedGel() + legacyOutput.getAmount());
            }

            absorbInputs(data);
            machines.put(key(location), data);
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection section = yaml.createSection("machines");

        for (Map.Entry<String, MachineData> entry : machines.entrySet()) {
            MachineData data = entry.getValue();
            Location location = data.location();
            ConfigurationSection machineSection = section.createSection(entry.getKey());
            machineSection.set("world", Objects.requireNonNull(location.getWorld()).getName());
            machineSection.set("x", location.getBlockX());
            machineSection.set("y", location.getBlockY());
            machineSection.set("z", location.getBlockZ());
            machineSection.set("slime", data.slimeSlot());
            machineSection.set("fuel", data.fuelSlot());
            machineSection.set("food", data.foodSlot());
            machineSection.set("virtual-fuel", data.virtualFuel());
            machineSection.set("virtual-food", data.virtualFood());
            machineSection.set("stored-gel", data.storedGel());
            machineSection.set("progress", data.progress());
        }

        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de sauvegarder machines.yml : " + e.getMessage());
        }
    }

    private static String key(Location location) {
        Location block = normalize(location);
        return block.getWorld().getName() + "," + block.getBlockX() + "," + block.getBlockY() + "," + block.getBlockZ();
    }

    private static Location normalize(Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
