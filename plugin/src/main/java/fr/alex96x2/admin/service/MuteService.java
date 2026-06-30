package fr.alex96x2.admin.service;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.storage.DataRepository;
import org.bukkit.Bukkit;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuteService {

    private final AdminPlugin plugin;
    private final DataRepository repository;

    public MuteService(AdminPlugin plugin, DataRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void mute(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source, Runnable onComplete) {
        repository.runAsync(() -> repository.insertMute(target, staffUuid, staffName, reason, expiresAt, source))
                .thenRun(() -> {
                    if (onComplete != null) Bukkit.getScheduler().runTask(plugin, onComplete);
                });
    }

    public void unmute(UUID target, String liftedBy, Runnable onComplete) {
        repository.runAsync(() -> repository.deactivateMutes(target, liftedBy))
                .thenRun(() -> {
                    if (onComplete != null) Bukkit.getScheduler().runTask(plugin, onComplete);
                });
    }

    public void getActiveMute(UUID uuid, java.util.function.Consumer<Optional<SanctionRecord>> callback) {
        repository.supplyAsync(() -> repository.findActiveMute(uuid)).thenAccept(result ->
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result)));
    }

    public Optional<SanctionRecord> findActive(UUID uuid) {
        return repository.findActiveMute(uuid);
    }

    public List<SanctionRecord> listActive(int page, int pageSize) {
        return repository.listActiveMutes(page, pageSize);
    }

    public int countActive() {
        return repository.countActiveMutes();
    }

    public List<SanctionRecord> getHistory(UUID uuid) {
        return repository.getMuteHistory(uuid);
    }

    public void expireOld() {
        repository.runAsync(repository::expireMutes);
    }
}
