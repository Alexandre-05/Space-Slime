package fr.alex96x2.admin.service;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.storage.DataRepository;
import fr.alex96x2.admin.util.DurationParser;
import org.bukkit.Bukkit;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WarnService {

    private final AdminPlugin plugin;
    private final DataRepository repository;

    public WarnService(AdminPlugin plugin, DataRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void warn(UUID target, UUID staffUuid, String staffName, String reason, String source, Runnable onComplete) {
        repository.runAsync(() -> repository.insertWarn(target, staffUuid, staffName, reason, source))
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    checkAutoBan(target, staffUuid, staffName);
                    if (onComplete != null) onComplete.run();
                }));
    }

    private void checkAutoBan(UUID target, UUID staffUuid, String staffName) {
        int threshold = plugin.getPluginConfig().autoBanThreshold();
        if (threshold <= 0) return;

        int count = repository.countActiveWarns(target);
        if (count >= threshold) {
            Instant expires = DurationParser.parseExpiry(plugin.getPluginConfig().autoBanDuration());
            plugin.getBanService().ban(target, staffUuid, staffName,
                    "Ban automatique (" + count + " warns)", expires, "INGAME", () ->
                            plugin.getMessageService().send(Bukkit.getConsoleSender(), "warn-auto-ban",
                                    Map.of("player", Bukkit.getOfflinePlayer(target).getName() != null
                                                    ? Bukkit.getOfflinePlayer(target).getName() : target.toString(),
                                            "count", String.valueOf(count))));
        }
    }

    public int countActive(UUID uuid) {
        return repository.countActiveWarns(uuid);
    }

    public List<WarnEntry> getWarns(UUID uuid) {
        return repository.getWarns(uuid);
    }

    public record WarnEntry(String reason, String staffName, Instant createdAt) {}
}
