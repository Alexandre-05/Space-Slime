package fr.alex96x2.admin.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.PlayerProfile;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.service.WarnService;
import fr.alex96x2.admin.storage.json.JsonDataFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class JsonRepository extends AbstractDataRepository {

    private final AdminPlugin plugin;
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Object lock = new Object();
    private JsonDataFile data;

    public JsonRepository(AdminPlugin plugin) {
        super(2);
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), plugin.getPluginConfig().jsonFile());
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        load();
    }

    @Override
    public StorageType type() {
        return StorageType.JSON;
    }

    @Override
    public boolean supportsWebSync() {
        return false;
    }

    @Override
    public void close() {
        synchronized (lock) {
            save();
        }
        super.close();
    }

    @Override
    public void playerJoin(UUID uuid, String name, String ipHash, byte[] ipEncrypted) {
        synchronized (lock) {
            Instant now = Instant.now();
            String key = uuid.toString();
            JsonDataFile.JsonPlayer player = data.players.get(key);
            if (player == null) {
                player = new JsonDataFile.JsonPlayer();
                player.uuid = key;
                player.currentName = name;
                player.firstSeen = now.toEpochMilli();
                player.lastSeen = now.toEpochMilli();
                player.totalPlaytime = 0;
                data.players.put(key, player);
            } else {
                if (!player.currentName.equals(name)) {
                    JsonDataFile.JsonNameHistory nh = new JsonDataFile.JsonNameHistory();
                    nh.id = data.nextNameHistoryId++;
                    nh.uuid = key;
                    nh.name = name;
                    nh.changedAt = now.toEpochMilli();
                    data.nameHistory.add(nh);
                }
                player.currentName = name;
                player.lastSeen = now.toEpochMilli();
            }
            player.lastIpHash = ipHash;
            player.lastIpEncrypted = encodeBytes(ipEncrypted);

            JsonDataFile.JsonSession session = new JsonDataFile.JsonSession();
            session.id = data.nextSessionId++;
            session.uuid = key;
            session.joinAt = now.toEpochMilli();
            session.ipHash = ipHash;
            session.ipEncrypted = encodeBytes(ipEncrypted);
            data.sessions.add(session);
            save();
        }
    }

    @Override
    public void playerQuit(UUID uuid) {
        synchronized (lock) {
            Instant now = Instant.now();
            String key = uuid.toString();
            data.sessions.stream()
                    .filter(s -> key.equals(s.uuid) && s.quitAt == null)
                    .max(Comparator.comparingLong(s -> s.joinAt))
                    .ifPresent(s -> {
                        s.quitAt = now.toEpochMilli();
                        JsonDataFile.JsonPlayer player = data.players.get(key);
                        if (player != null) {
                            player.totalPlaytime += Math.max(0, (s.quitAt - s.joinAt) / 1000);
                        }
                    });
            save();
        }
    }

    @Override
    public Optional<PlayerProfile> getPlayer(UUID uuid) {
        synchronized (lock) {
            JsonDataFile.JsonPlayer p = data.players.get(uuid.toString());
            if (p == null) return Optional.empty();
            return Optional.of(new PlayerProfile(
                    uuid, p.currentName,
                    Instant.ofEpochMilli(p.firstSeen),
                    Instant.ofEpochMilli(p.lastSeen),
                    p.totalPlaytime, p.lastIpHash));
        }
    }

    @Override
    public byte[] getPlayerIpEncrypted(UUID uuid) {
        synchronized (lock) {
            JsonDataFile.JsonPlayer p = data.players.get(uuid.toString());
            return p != null ? decodeBytes(p.lastIpEncrypted) : null;
        }
    }

    @Override
    public void insertBan(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source) {
        insertSanction(data.bans, () -> data.nextBanId++, target, staffUuid, staffName, reason, expiresAt, source);
    }

    @Override
    public void deactivateBans(UUID target, String liftedBy) {
        deactivateSanctions(data.bans, target, liftedBy);
    }

    @Override
    public Optional<SanctionRecord> findActiveBan(UUID uuid) {
        return findActiveSanction(data.bans, uuid);
    }

    @Override
    public List<SanctionRecord> listActiveBans(int page, int pageSize) {
        return listActiveSanctions(data.bans, page, pageSize);
    }

    @Override
    public int countActiveBans() {
        return countActiveSanctions(data.bans);
    }

    @Override
    public List<SanctionRecord> getBanHistory(UUID uuid) {
        return getSanctionHistory(data.bans, uuid);
    }

    @Override
    public void expireBans() {
        expireSanctions(data.bans);
    }

    @Override
    public void insertMute(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source) {
        insertSanction(data.mutes, () -> data.nextMuteId++, target, staffUuid, staffName, reason, expiresAt, source);
    }

    @Override
    public void deactivateMutes(UUID target, String liftedBy) {
        deactivateSanctions(data.mutes, target, liftedBy);
    }

    @Override
    public Optional<SanctionRecord> findActiveMute(UUID uuid) {
        return findActiveSanction(data.mutes, uuid);
    }

    @Override
    public List<SanctionRecord> listActiveMutes(int page, int pageSize) {
        return listActiveSanctions(data.mutes, page, pageSize);
    }

    @Override
    public int countActiveMutes() {
        return countActiveSanctions(data.mutes);
    }

    @Override
    public List<SanctionRecord> getMuteHistory(UUID uuid) {
        return getSanctionHistory(data.mutes, uuid);
    }

    @Override
    public void expireMutes() {
        expireSanctions(data.mutes);
    }

    @Override
    public void insertKick(UUID target, UUID staffUuid, String staffName, String reason, String source) {
        synchronized (lock) {
            JsonDataFile.JsonKick kick = new JsonDataFile.JsonKick();
            kick.id = data.nextKickId++;
            kick.uuid = target.toString();
            kick.staffUuid = staffUuid != null ? staffUuid.toString() : null;
            kick.staffName = staffName;
            kick.reason = reason;
            kick.createdAt = Instant.now().toEpochMilli();
            kick.source = source;
            data.kicks.add(kick);
            save();
        }
    }

    @Override
    public int countKicks(UUID uuid) {
        synchronized (lock) {
            return (int) data.kicks.stream().filter(k -> uuid.toString().equals(k.uuid)).count();
        }
    }

    @Override
    public void insertWarn(UUID target, UUID staffUuid, String staffName, String reason, String source) {
        synchronized (lock) {
            JsonDataFile.JsonWarn warn = new JsonDataFile.JsonWarn();
            warn.id = data.nextWarnId++;
            warn.uuid = target.toString();
            warn.staffUuid = staffUuid != null ? staffUuid.toString() : null;
            warn.staffName = staffName;
            warn.reason = reason;
            warn.createdAt = Instant.now().toEpochMilli();
            warn.active = true;
            warn.source = source;
            data.warns.add(warn);
            save();
        }
    }

    @Override
    public int countActiveWarns(UUID uuid) {
        synchronized (lock) {
            return (int) data.warns.stream().filter(w -> uuid.toString().equals(w.uuid) && w.active).count();
        }
    }

    @Override
    public List<WarnService.WarnEntry> getWarns(UUID uuid) {
        synchronized (lock) {
            return data.warns.stream()
                    .filter(w -> uuid.toString().equals(w.uuid) && w.active)
                    .sorted(Comparator.comparingLong((JsonDataFile.JsonWarn w) -> w.createdAt).reversed())
                    .map(w -> new WarnService.WarnEntry(w.reason, w.staffName, Instant.ofEpochMilli(w.createdAt)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void insertNote(UUID target, String staffName, String content) {
        synchronized (lock) {
            JsonDataFile.JsonNote note = new JsonDataFile.JsonNote();
            note.id = data.nextNoteId++;
            note.uuid = target.toString();
            note.staffName = staffName;
            note.content = content;
            note.createdAt = Instant.now().toEpochMilli();
            data.notes.add(note);
            save();
        }
    }

    @Override
    public List<PendingAction> fetchPendingActions(int limit) {
        return List.of();
    }

    @Override
    public void markPendingProcessed(long id) {
        // no-op en mode JSON
    }

    private void insertSanction(List<JsonDataFile.JsonSanction> list, IdSupplier idSupplier,
                                UUID target, UUID staffUuid, String staffName, String reason,
                                Instant expiresAt, String source) {
        synchronized (lock) {
            deactivateSanctions(list, target, "SYSTEM");
            JsonDataFile.JsonSanction s = new JsonDataFile.JsonSanction();
            s.id = idSupplier.next();
            s.uuid = target.toString();
            s.staffUuid = staffUuid != null ? staffUuid.toString() : null;
            s.staffName = staffName;
            s.reason = reason;
            s.createdAt = Instant.now().toEpochMilli();
            s.expiresAt = expiresAt != null ? expiresAt.toEpochMilli() : null;
            s.active = true;
            s.source = source;
            list.add(s);
            save();
        }
    }

    private void deactivateSanctions(List<JsonDataFile.JsonSanction> list, UUID target, String liftedBy) {
        synchronized (lock) {
            long now = Instant.now().toEpochMilli();
            for (JsonDataFile.JsonSanction s : list) {
                if (target.toString().equals(s.uuid) && s.active) {
                    s.active = false;
                    s.liftedBy = liftedBy;
                    s.liftedAt = now;
                }
            }
            save();
        }
    }

    private Optional<SanctionRecord> findActiveSanction(List<JsonDataFile.JsonSanction> list, UUID uuid) {
        synchronized (lock) {
            return list.stream()
                    .filter(s -> uuid.toString().equals(s.uuid) && s.active && !isExpired(s))
                    .max(Comparator.comparingLong(s -> s.createdAt))
                    .map(this::mapSanction);
        }
    }

    private List<SanctionRecord> listActiveSanctions(List<JsonDataFile.JsonSanction> list, int page, int pageSize) {
        synchronized (lock) {
            List<SanctionRecord> active = list.stream()
                    .filter(s -> s.active && !isExpired(s))
                    .sorted(Comparator.comparingLong((JsonDataFile.JsonSanction s) -> s.createdAt).reversed())
                    .map(this::mapSanction)
                    .collect(Collectors.toList());
            int from = Math.max(0, (page - 1) * pageSize);
            int to = Math.min(active.size(), from + pageSize);
            if (from >= active.size()) return List.of();
            return active.subList(from, to);
        }
    }

    private int countActiveSanctions(List<JsonDataFile.JsonSanction> list) {
        synchronized (lock) {
            return (int) list.stream().filter(s -> s.active && !isExpired(s)).map(s -> s.uuid).distinct().count();
        }
    }

    private List<SanctionRecord> getSanctionHistory(List<JsonDataFile.JsonSanction> list, UUID uuid) {
        synchronized (lock) {
            return list.stream()
                    .filter(s -> uuid.toString().equals(s.uuid))
                    .sorted(Comparator.comparingLong((JsonDataFile.JsonSanction s) -> s.createdAt).reversed())
                    .map(this::mapSanction)
                    .collect(Collectors.toList());
        }
    }

    private void expireSanctions(List<JsonDataFile.JsonSanction> list) {
        synchronized (lock) {
            long now = Instant.now().toEpochMilli();
            boolean changed = false;
            for (JsonDataFile.JsonSanction s : list) {
                if (s.active && s.expiresAt != null && s.expiresAt <= now) {
                    s.active = false;
                    s.liftedBy = "SYSTEM";
                    s.liftedAt = now;
                    changed = true;
                }
            }
            if (changed) save();
        }
    }

    private boolean isExpired(JsonDataFile.JsonSanction s) {
        return s.expiresAt != null && s.expiresAt <= Instant.now().toEpochMilli();
    }

    private SanctionRecord mapSanction(JsonDataFile.JsonSanction s) {
        return new SanctionRecord(
                s.id,
                UUID.fromString(s.uuid),
                s.staffUuid != null ? UUID.fromString(s.staffUuid) : null,
                s.staffName,
                s.reason,
                Instant.ofEpochMilli(s.createdAt),
                s.expiresAt != null ? Instant.ofEpochMilli(s.expiresAt) : null,
                s.active && !isExpired(s),
                s.source);
    }

    private void load() {
        synchronized (lock) {
            if (!dataFile.exists()) {
                data = new JsonDataFile();
                save();
                return;
            }
            try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
                JsonDataFile loaded = gson.fromJson(reader, JsonDataFile.class);
                data = loaded != null ? loaded : new JsonDataFile();
            } catch (Exception e) {
                plugin.getLogger().warning("Impossible de lire " + dataFile.getName() + ", nouveau fichier : " + e.getMessage());
                data = new JsonDataFile();
                save();
            }
        }
    }

    private void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible d'écrire " + dataFile.getName() + " : " + e.getMessage());
        }
    }

    private static String encodeBytes(byte[] bytes) {
        return bytes != null ? Base64.getEncoder().encodeToString(bytes) : null;
    }

    private static byte[] decodeBytes(String encoded) {
        return encoded != null ? Base64.getDecoder().decode(encoded) : null;
    }

    @FunctionalInterface
    private interface IdSupplier {
        long next();
    }
}
