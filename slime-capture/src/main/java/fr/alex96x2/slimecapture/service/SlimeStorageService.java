package fr.alex96x2.slimecapture.service;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import fr.alex96x2.slimecapture.item.SlimeCaptureItems;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class SlimeStorageService {

    private final SlimeCapturePlugin plugin;

    public SlimeStorageService(SlimeCapturePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Dépose des slimes dans le réservoir de l'aspirateur tenu en main.
     *
     * @return quantité réellement stockée
     */
    public int depositToGun(Player player, int amount) {
        ItemStack gun = player.getInventory().getItemInMainHand();
        if (!SlimeCaptureItems.isVacuumGun(gun)) {
            return 0;
        }

        int capacity = plugin.getPluginConfig().gunCapacity();
        int current = SlimeCaptureItems.getGunSlimeCount(gun);
        int space = capacity - current;
        if (space <= 0) {
            return 0;
        }

        int stored = Math.min(amount, space);
        SlimeCaptureItems.setGunSlimeCount(gun, current + stored);
        player.getInventory().setItemInMainHand(gun);
        return stored;
    }

    /**
     * Retire des slimes du réservoir de l'aspirateur et crée des capsules transférables.
     *
     * @return true si au moins une capsule a été créée
     */
    public boolean withdrawToCanisters(Player player, int requested) {
        ItemStack gun = player.getInventory().getItemInMainHand();
        if (!SlimeCaptureItems.isVacuumGun(gun)) {
            plugin.getActionBarService().show(player, "no-gun-in-hand");
            return false;
        }

        int available = SlimeCaptureItems.getGunSlimeCount(gun);
        if (available <= 0) {
            plugin.getActionBarService().show(player, "gun-empty");
            return false;
        }

        int toWithdraw = requested <= 0 ? available : Math.min(requested, available);
        int maxPerCanister = plugin.getPluginConfig().canisterMaxStack();
        int withdrawn = 0;
        int remaining = toWithdraw;
        PlayerInventory inventory = player.getInventory();

        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxPerCanister);
            ItemStack canister = SlimeCaptureItems.createSlimeCanister(stackAmount);
            Map<Integer, ItemStack> overflow = inventory.addItem(canister);
            if (!overflow.isEmpty()) {
                plugin.getActionBarService().show(player, "inventory-full");
                break;
            }
            withdrawn += stackAmount;
            remaining -= stackAmount;
        }

        if (withdrawn <= 0) {
            return false;
        }

        SlimeCaptureItems.setGunSlimeCount(gun, available - withdrawn);
        player.getInventory().setItemInMainHand(gun);
        int leftInGun = available - withdrawn;
        int capacity = plugin.getPluginConfig().gunCapacity();
        plugin.getActionBarService().show(player, "capsule-created", Map.of(
                "amount", String.valueOf(withdrawn),
                "remaining", String.valueOf(leftInGun),
                "capacity", String.valueOf(capacity)
        ));
        return true;
    }

    public int getGunSlimeCount(Player player) {
        ItemStack gun = player.getInventory().getItemInMainHand();
        return SlimeCaptureItems.getGunSlimeCount(gun);
    }

    /**
     * Relâche un slime depuis le réservoir (bloc visé ou dans le vide).
     */
    public boolean releaseSlime(Player player, Block clickedBlock) {
        if (plugin.getVacuumService().isVacuuming(player)) {
            plugin.getVacuumService().stopVacuum(player, false);
        }

        ItemStack gun = player.getInventory().getItemInMainHand();
        if (!SlimeCaptureItems.isVacuumGun(gun)) {
            plugin.getActionBarService().show(player, "no-gun-in-hand");
            return false;
        }

        int cost = plugin.getPluginConfig().releaseCost();
        int available = SlimeCaptureItems.getGunSlimeCount(gun);
        if (available < cost) {
            plugin.getActionBarService().show(player, "gun-empty");
            return false;
        }

        Location spawnLoc = resolveSpawnLocation(player, clickedBlock);
        if (spawnLoc == null) {
            plugin.getActionBarService().show(player, "release-blocked");
            return false;
        }

        int slimeSize = plugin.getPluginConfig().releaseSpawnSize();
        Vector launch = player.getLocation().getDirection();
        launch.setY(plugin.getPluginConfig().releaseLaunchUpward());
        if (launch.lengthSquared() > 0.0001) {
            launch.normalize();
        } else {
            launch = new Vector(0, 0.25, 0);
        }
        Vector velocity = launch.multiply(plugin.getPluginConfig().releaseLaunchStrength());

        spawnLoc.getWorld().spawn(spawnLoc, Slime.class, entity -> {
            entity.setSize(slimeSize);
            entity.setWander(true);
            entity.setVelocity(velocity);
        });

        SlimeCaptureItems.setGunSlimeCount(gun, available - cost);
        player.getInventory().setItemInMainHand(gun);

        player.spawnParticle(Particle.ITEM_SLIME, spawnLoc, 12, 0.3, 0.3, 0.3, 0.05);
        player.playSound(spawnLoc, Sound.ENTITY_SLIME_SQUISH, 0.7f, 1.0f);

        int remaining = available - cost;
        int capacity = plugin.getPluginConfig().gunCapacity();
        plugin.getActionBarService().recordRelease(player, cost, remaining, capacity);
        return true;
    }

    private Location resolveSpawnLocation(Player player, Block clickedBlock) {
        if (clickedBlock != null) {
            Block spawnBlock = clickedBlock.getRelative(BlockFace.UP);
            if (!spawnBlock.isPassable() && spawnBlock.getType().isSolid()) {
                return null;
            }
            return spawnBlock.getLocation().add(0.5, 0, 0.5);
        }

        Location spawnLoc = player.getLocation().clone();
        Vector horizontal = player.getLocation().getDirection();
        horizontal.setY(0);
        if (horizontal.lengthSquared() > 0.01) {
            horizontal.normalize();
            spawnLoc.add(horizontal.multiply(1.2));
        }
        spawnLoc.setY(Math.floor(spawnLoc.getY()) + 0.1);
        Block feet = spawnLoc.getBlock();
        Block above = feet.getRelative(BlockFace.UP);
        if (!feet.isPassable() && feet.getType().isSolid()) {
            return null;
        }
        if (!above.isPassable() && above.getType().isSolid()) {
            return null;
        }
        spawnLoc.add(0.5, 0, 0.5);
        return spawnLoc;
    }
}
