package fr.alex96x2.admin.storage;

import fr.alex96x2.admin.model.PlayerProfile;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.service.WarnService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface DataRepository {

    StorageType type();

    boolean supportsWebSync();

    void close();

    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    CompletableFuture<Void> runAsync(Runnable runnable);

    void playerJoin(UUID uuid, String name, String ipHash, byte[] ipEncrypted);

    void playerQuit(UUID uuid);

    Optional<PlayerProfile> getPlayer(UUID uuid);

    byte[] getPlayerIpEncrypted(UUID uuid);

    void insertBan(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source);

    void deactivateBans(UUID target, String liftedBy);

    Optional<SanctionRecord> findActiveBan(UUID uuid);

    List<SanctionRecord> listActiveBans(int page, int pageSize);

    int countActiveBans();

    List<SanctionRecord> getBanHistory(UUID uuid);

    void expireBans();

    void insertMute(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source);

    void deactivateMutes(UUID target, String liftedBy);

    Optional<SanctionRecord> findActiveMute(UUID uuid);

    List<SanctionRecord> listActiveMutes(int page, int pageSize);

    int countActiveMutes();

    List<SanctionRecord> getMuteHistory(UUID uuid);

    void expireMutes();

    void insertKick(UUID target, UUID staffUuid, String staffName, String reason, String source);

    int countKicks(UUID uuid);

    void insertWarn(UUID target, UUID staffUuid, String staffName, String reason, String source);

    int countActiveWarns(UUID uuid);

    List<WarnService.WarnEntry> getWarns(UUID uuid);

    void insertNote(UUID target, String staffName, String content);

    List<PendingAction> fetchPendingActions(int limit);

    void markPendingProcessed(long id);
}
