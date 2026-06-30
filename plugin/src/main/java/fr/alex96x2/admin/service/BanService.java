package fr.alex96x2.admin.service;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.storage.DataRepository;
import fr.alex96x2.admin.util.DurationParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BanService {

    private final AdminPlugin plugin;
    private final DataRepository repository;

    public BanService(AdminPlugin plugin, DataRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void ban(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source, Runnable onComplete) {
        repository.runAsync(() -> repository.insertBan(target, staffUuid, staffName, reason, expiresAt, source))
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    kickIfOnline(target, reason, expiresAt);
                    if (onComplete != null) onComplete.run();
                }));
    }

    public void unban(UUID target, String liftedBy, Runnable onComplete) {
        repository.runAsync(() -> repository.deactivateBans(target, liftedBy))
                .thenRun(() -> {
                    if (onComplete != null) Bukkit.getScheduler().runTask(plugin, onComplete);
                });
    }

    public void kickIfOnline(UUID target, String reason, Instant expiresAt) {
        Player player = Bukkit.getPlayer(target);
        if (player != null && !player.hasPermission("admin.bypass.ban")) {
            String expires = expiresAt == null ? "Permanent" : DurationParser.formatExpiry(expiresAt);
            player.kick(plugin.getMessageService().get("join-banned", Map.of(
                    "reason", reason,
                    "expires", expires
            )));
        }
    }

    public void getActiveBan(UUID uuid, java.util.function.Consumer<Optional<SanctionRecord>> callback) {
        repository.supplyAsync(() -> repository.findActiveBan(uuid)).thenAccept(result ->
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result)));
    }

    public Optional<SanctionRecord> findActive(UUID uuid) {
        return repository.findActiveBan(uuid);
    }

    public List<SanctionRecord> listActive(int page, int pageSize) {
        return repository.listActiveBans(page, pageSize);
    }

    public int countActive() {
        return repository.countActiveBans();
    }

    public List<SanctionRecord> getHistory(UUID uuid) {
        return repository.getBanHistory(uuid);
    }

    public void expireOld() {
        repository.runAsync(repository::expireBans);
    }
}
