package fr.alex96x2.slimecapture.service;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import fr.alex96x2.slimecapture.config.SlimeCaptureConfig;
import fr.alex96x2.slimecapture.item.SlimeCaptureItems;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VacuumService {

    private final SlimeCapturePlugin plugin;
    private final Map<UUID, VacuumSession> activeSessions = new HashMap<>();

    public VacuumService(SlimeCapturePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isVacuuming(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public void toggleVacuum(Player player) {
        if (isVacuuming(player)) {
            stopVacuum(player, true);
            return;
        }
        startVacuum(player);
    }

    public void stopVacuum(Player player, boolean notify) {
        VacuumSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.task.cancel();
            if (notify) {
                plugin.getMessageService().sendChat(player, "vacuum-end");
            }
        }
    }

    public void shutdown() {
        for (VacuumSession session : activeSessions.values()) {
            session.task.cancel();
        }
        activeSessions.clear();
    }

    private void startVacuum(Player player) {
        SlimeCaptureConfig config = plugin.getPluginConfig();
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> tick(player), 0L, config.tickInterval());
        activeSessions.put(player.getUniqueId(), new VacuumSession(task, config.sessionTicks() / config.tickInterval()));
        plugin.getMessageService().sendChat(player, "vacuum-start");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.4f, 1.8f);
    }

    private void tick(Player player) {
        if (!player.isOnline()) {
            activeSessions.remove(player.getUniqueId());
            return;
        }

        VacuumSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        session.remainingTicks--;
        if (session.remainingTicks <= 0) {
            stopVacuum(player, true);
            return;
        }

        Slime target = findTargetSlime(player);
        if (target == null) {
            session.targetSlimeId = null;
            spawnIdleParticles(player);
            return;
        }

        session.targetSlimeId = target.getUniqueId();

        pullSlime(player, target);
        if (player.getLocation().distance(target.getLocation()) <= plugin.getPluginConfig().captureDistance()) {
            captureSlime(player, target);
        }
    }

    public UUID getVacuumTarget(Player player) {
        VacuumSession session = activeSessions.get(player.getUniqueId());
        return session != null ? session.targetSlimeId : null;
    }

    private Slime findTargetSlime(Player player) {
        double range = plugin.getPluginConfig().vacuumRange();
        RayTraceResult trace = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.6,
                entity -> entity instanceof Slime slime && !slime.isDead() && slime.isValid()
        );
        if (trace == null) {
            return null;
        }
        Entity hit = trace.getHitEntity();
        return hit instanceof Slime slime ? slime : null;
    }

    private void pullSlime(Player player, Slime slime) {
        // Évite les dégâts de contact pendant l'aspiration
        slime.setTarget(null);
        slime.setAggressive(false);

        Location playerLoc = player.getEyeLocation();
        Location slimeLoc = slime.getLocation().add(0, slime.getHeight() * 0.5, 0);
        Vector direction = playerLoc.toVector().subtract(slimeLoc.toVector());
        double distance = direction.length();
        if (distance < 0.1) {
            return;
        }
        direction.normalize();
        double strength = plugin.getPluginConfig().pullStrength();
        double boost = Math.min(1.5, 3.0 / distance);
        slime.setVelocity(direction.multiply(strength * boost));

        Location mid = slimeLoc.clone().add(direction.clone().multiply(0.5));
        player.spawnParticle(Particle.ITEM_SLIME, mid, 6, 0.2, 0.2, 0.2, 0.02);
        if (player.getTicksLived() % 10 == 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH_SMALL, 0.3f, 1.4f);
        }
    }

    private void spawnIdleParticles(Player player) {
        Location tip = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.5));
        player.spawnParticle(Particle.END_ROD, tip, 2, 0.05, 0.05, 0.05, 0.01);
    }

    private void captureSlime(Player player, Slime slime) {
        int yield = plugin.getPluginConfig().yieldForSize(slime.getSize());
        ItemStack gun = player.getInventory().getItemInMainHand();
        if (!SlimeCaptureItems.isVacuumGun(gun)) {
            return;
        }
        int current = SlimeCaptureItems.getGunSlimeCount(gun);
        int capacity = plugin.getPluginConfig().gunCapacity();
        if (current + yield > capacity) {
            plugin.getActionBarService().show(player, "gun-full");
            return;
        }

        player.spawnParticle(Particle.EXPLOSION, slime.getLocation(), 1);
        player.playSound(slime.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1.0f, 0.8f);
        slime.remove();

        int stored = plugin.getSlimeStorageService().depositToGun(player, yield);
        int total = plugin.getSlimeStorageService().getGunSlimeCount(player);
        plugin.getActionBarService().recordCapture(player, stored, total, capacity);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
    }

    private static final class VacuumSession {
        private final BukkitTask task;
        private int remainingTicks;
        private UUID targetSlimeId;

        private VacuumSession(BukkitTask task, int remainingTicks) {
            this.task = task;
            this.remainingTicks = remainingTicks;
        }
    }
}
