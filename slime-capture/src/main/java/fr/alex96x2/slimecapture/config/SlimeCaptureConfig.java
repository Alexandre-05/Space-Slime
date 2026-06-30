package fr.alex96x2.slimecapture.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SlimeCaptureConfig {

    private final double vacuumRange;
    private final double captureDistance;
    private final double pullStrength;
    private final int sessionTicks;
    private final int tickInterval;
    private final int gunCapacity;
    private final int canisterMaxStack;
    private final int releaseCost;
    private final int releaseSpawnSize;
    private final double releaseLaunchStrength;
    private final double releaseLaunchUpward;
    private final int actionBarIdleTicks;
    private final Map<Integer, Integer> yieldsBySize;
    private final int machineProcessDurationTicks;
    private final int machineFuelStorageMax;
    private final int machineFoodStorageMax;
    private final int machineGelStorageMax;
    private final int machineFuelCostPerBatch;
    private final int machineFoodCostPerBatch;
    private final int machineGelMin;
    private final int machineGelMax;
    private final double machineSlimeLossChance;
    private final Set<Material> machineFuelMaterials;
    private final Set<Material> machineFoodMaterials;
    private final Map<Material, Integer> machineFuelUnits;
    private final Map<Material, Integer> machineFoodUnits;

    public SlimeCaptureConfig(FileConfiguration config) {
        ConfigurationSection vacuum = config.getConfigurationSection("vacuum");
        this.vacuumRange = vacuum != null ? vacuum.getDouble("range", 8.0) : 8.0;
        this.captureDistance = vacuum != null ? vacuum.getDouble("capture-distance", 1.6) : 1.6;
        this.pullStrength = vacuum != null ? vacuum.getDouble("pull-strength", 0.42) : 0.42;
        this.sessionTicks = vacuum != null ? vacuum.getInt("session-ticks", 100) : 100;
        this.tickInterval = vacuum != null ? vacuum.getInt("tick-interval", 2) : 2;

        ConfigurationSection storage = config.getConfigurationSection("storage");
        this.gunCapacity = storage != null ? storage.getInt("gun-capacity", 100) : 100;
        this.canisterMaxStack = storage != null ? storage.getInt("canister-max-stack", 20) : 20;

        ConfigurationSection release = config.getConfigurationSection("release");
        this.releaseCost = release != null ? release.getInt("cost", 1) : 1;
        this.releaseSpawnSize = release != null ? release.getInt("spawn-size", 1) : 1;
        double launchDistance = release != null ? release.getDouble("launch-distance", 4.5) : 4.5;
        this.releaseLaunchStrength = release != null
                ? release.getDouble("launch-strength", launchDistance * 0.4)
                : launchDistance * 0.4;
        this.releaseLaunchUpward = release != null ? release.getDouble("launch-upward", 0.25) : 0.25;

        ConfigurationSection actionbar = config.getConfigurationSection("actionbar");
        int idleSeconds = actionbar != null ? actionbar.getInt("idle-seconds", 2) : 2;
        this.actionBarIdleTicks = idleSeconds * 20;

        this.yieldsBySize = new HashMap<>();
        ConfigurationSection yields = config.getConfigurationSection("yields");
        if (yields != null) {
            for (String key : yields.getKeys(false)) {
                try {
                    int size = Integer.parseInt(key);
                    yieldsBySize.put(size, yields.getInt(key));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (yieldsBySize.isEmpty()) {
            yieldsBySize.put(1, 1);
            yieldsBySize.put(2, 2);
            yieldsBySize.put(4, 4);
            yieldsBySize.put(8, 8);
        }

        ConfigurationSection machine = config.getConfigurationSection("machine");
        this.machineProcessDurationTicks = machine != null ? machine.getInt("process-duration-ticks", 100) : 100;
        this.machineFuelStorageMax = machine != null ? machine.getInt("fuel-storage-max", 256) : 256;
        this.machineFoodStorageMax = machine != null ? machine.getInt("food-storage-max", 256) : 256;
        this.machineGelStorageMax = machine != null ? machine.getInt("gel-storage-max", 512) : 512;
        this.machineFuelCostPerBatch = machine != null ? machine.getInt("fuel-cost-per-batch", 1) : 1;
        this.machineFoodCostPerBatch = machine != null ? machine.getInt("food-cost-per-batch", 1) : 1;
        this.machineGelMin = machine != null ? machine.getInt("gel-min", 1) : 1;
        this.machineGelMax = machine != null ? machine.getInt("gel-max", 3) : 3;
        this.machineSlimeLossChance = machine != null ? machine.getDouble("slime-loss-chance", 0.35) : 0.35;
        this.machineFuelMaterials = parseMaterials(machine, "fuel-materials",
                Material.COAL, Material.CHARCOAL, Material.COAL_BLOCK);
        this.machineFoodMaterials = parseMaterials(machine, "food-materials",
                Material.SUGAR, Material.WHEAT, Material.HAY_BLOCK);
        this.machineFuelUnits = parseUnitMap(machine, "fuel-units", Map.of(
                Material.COAL, 16,
                Material.CHARCOAL, 16,
                Material.COAL_BLOCK, 128
        ));
        this.machineFoodUnits = parseUnitMap(machine, "food-units", Map.of(
                Material.SUGAR, 4,
                Material.WHEAT, 8,
                Material.HAY_BLOCK, 16
        ));
    }

    private static Map<Material, Integer> parseUnitMap(ConfigurationSection machine, String key, Map<Material, Integer> defaults) {
        Map<Material, Integer> units = new HashMap<>();
        if (machine != null) {
            ConfigurationSection section = machine.getConfigurationSection(key);
            if (section != null) {
                for (String entry : section.getKeys(false)) {
                    Material material = Material.matchMaterial(entry);
                    if (material != null) {
                        units.put(material, section.getInt(entry));
                    }
                }
            }
        }
        if (units.isEmpty()) {
            units.putAll(defaults);
        }
        return units;
    }

    private static Set<Material> parseMaterials(ConfigurationSection machine, String key, Material... defaults) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        if (machine != null) {
            List<String> raw = machine.getStringList(key);
            for (String entry : raw) {
                Material material = Material.matchMaterial(entry);
                if (material != null && material.isItem()) {
                    materials.add(material);
                }
            }
        }
        if (materials.isEmpty()) {
            for (Material material : defaults) {
                materials.add(material);
            }
        }
        return materials;
    }

    public double vacuumRange() {
        return vacuumRange;
    }

    public double captureDistance() {
        return captureDistance;
    }

    public double pullStrength() {
        return pullStrength;
    }

    public int sessionTicks() {
        return sessionTicks;
    }

    public int tickInterval() {
        return tickInterval;
    }

    public int gunCapacity() {
        return gunCapacity;
    }

    public int canisterMaxStack() {
        return canisterMaxStack;
    }

    public int releaseCost() {
        return releaseCost;
    }

    public int releaseSpawnSize() {
        return releaseSpawnSize;
    }

    public double releaseLaunchStrength() {
        return releaseLaunchStrength;
    }

    public double releaseLaunchUpward() {
        return releaseLaunchUpward;
    }

    public int actionBarIdleTicks() {
        return actionBarIdleTicks;
    }

    public int yieldForSize(int slimeSize) {
        if (yieldsBySize.containsKey(slimeSize)) {
            return yieldsBySize.get(slimeSize);
        }
        int best = 1;
        for (Map.Entry<Integer, Integer> entry : yieldsBySize.entrySet()) {
            if (entry.getKey() <= slimeSize) {
                best = Math.max(best, entry.getValue());
            }
        }
        if (best > 1) {
            return Math.min(canisterMaxStack, best);
        }
        return Math.min(canisterMaxStack, Math.max(1, slimeSize));
    }

    public int machineProcessDurationTicks() {
        return machineProcessDurationTicks;
    }

    public int machineFuelStorageMax() {
        return machineFuelStorageMax;
    }

    public int machineFoodStorageMax() {
        return machineFoodStorageMax;
    }

    public int machineGelStorageMax() {
        return machineGelStorageMax;
    }

    public int machineFuelCostPerBatch() {
        return machineFuelCostPerBatch;
    }

    public int machineFoodCostPerBatch() {
        return machineFoodCostPerBatch;
    }

    public int machineGelMin() {
        return machineGelMin;
    }

    public int machineGelMax() {
        return machineGelMax;
    }

    public double machineSlimeLossChance() {
        return machineSlimeLossChance;
    }

    public int fuelUnits(Material material) {
        return machineFuelUnits.getOrDefault(material, 8);
    }

    public int foodUnits(Material material) {
        return machineFoodUnits.getOrDefault(material, 4);
    }

    public boolean isMachineFuel(Material material) {
        return machineFuelMaterials.contains(material);
    }

    public boolean isMachineFood(Material material) {
        return machineFoodMaterials.contains(material);
    }
}
