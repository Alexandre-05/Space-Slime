package fr.alex96x2.admin.service;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.storage.DataRepository;
import fr.alex96x2.admin.storage.PendingAction;
import fr.alex96x2.admin.util.DurationParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SyncService {

    private final AdminPlugin plugin;
    private final DataRepository repository;
    private final BanService banService;
    private final MuteService muteService;
    private final KickService kickService;
    private BukkitTask pollTask;
    private BukkitTask expireTask;

    public SyncService(AdminPlugin plugin, DataRepository repository, BanService banService,
                       MuteService muteService, KickService kickService) {
        this.plugin = plugin;
        this.repository = repository;
        this.banService = banService;
        this.muteService = muteService;
        this.kickService = kickService;
    }

    public void start() {
        long expireTicks = plugin.getPluginConfig().expirationCheckSeconds() * 20L;
        expireTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::expireSanctions, expireTicks, expireTicks);

        if (!repository.supportsWebSync()) {
            plugin.getLogger().info("Sync panel web désactivée (mode " + repository.type() + ").");
            return;
        }

        long pollTicks = plugin.getPluginConfig().syncPollSeconds() * 20L;
        pollTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::processPendingActions, pollTicks, pollTicks);
    }

    public void stop() {
        if (pollTask != null) pollTask.cancel();
        if (expireTask != null) expireTask.cancel();
    }

    private void expireSanctions() {
        try {
            banService.expireOld();
            muteService.expireOld();
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur expiration sanctions : " + e.getMessage());
        }
    }

    private void processPendingActions() {
        try {
            List<PendingAction> actions = repository.fetchPendingActions(20);
            for (PendingAction action : actions) {
                try {
                    handleAction(action.actionType(), UUID.fromString(action.targetUuid()), action.payload());
                    repository.markPendingProcessed(action.id());
                } catch (Exception e) {
                    plugin.getLogger().warning("Action panel ignorée (id=" + action.id() + ") : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Sync panel web indisponible : " + e.getMessage());
        }
    }

    private void handleAction(String action, UUID target, String payload) {
        String reason = extractJson(payload, "reason", "Sanction panel web");
        String staffName = extractJson(payload, "staffName", "Panel Web");
        String duration = extractJson(payload, "duration", null);

        switch (action.toUpperCase()) {
            case "BAN" -> {
                Instant expires = duration != null && !duration.isBlank() ? DurationParser.parseExpiry(duration) : null;
                banService.ban(target, null, staffName, reason, expires, "WEB", null);
            }
            case "UNBAN" -> banService.unban(target, staffName, null);
            case "MUTE" -> {
                Instant expires = duration != null && !duration.isBlank() ? DurationParser.parseExpiry(duration) : null;
                muteService.mute(target, null, staffName, reason, expires, "WEB", null);
            }
            case "UNMUTE" -> muteService.unmute(target, staffName, null);
            case "KICK" -> Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(target);
                if (player != null) {
                    kickService.kick(target, null, staffName, reason, "WEB");
                    player.kick(plugin.getMessageService().get("kick-message", Map.of("reason", reason)));
                }
            });
            case "WARN" -> warnFromWeb(target, staffName, reason);
            default -> plugin.getLogger().fine("Action pending inconnue : " + action);
        }
    }

    private void warnFromWeb(UUID target, String staffName, String reason) {
        plugin.getWarnService().warn(target, null, staffName, reason, "WEB", () -> {
            Player player = Bukkit.getPlayer(target);
            if (player != null) {
                plugin.getMessageService().send(player, "warn-player", Map.of("reason", reason));
            }
        });
    }

    private String extractJson(String json, String key, String defaultValue) {
        if (json == null) return defaultValue;
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return defaultValue;
        start += search.length();
        int end = json.indexOf('"', start);
        if (end == -1) return defaultValue;
        return json.substring(start, end);
    }
}
