package fr.alex96x2.admin.storage;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.database.DatabaseManager;
import fr.alex96x2.admin.model.PlayerProfile;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.service.WarnService;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class MariaDbRepository extends AbstractDataRepository {

    private final AdminPlugin plugin;
    private final DatabaseManager db;

    public MariaDbRepository(AdminPlugin plugin, DatabaseManager db) {
        super(plugin.getPluginConfig().poolSize());
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public StorageType type() {
        return StorageType.MARIADB;
    }

    @Override
    public boolean supportsWebSync() {
        return true;
    }

    @Override
    public void close() {
        super.close();
        db.close();
    }

    @Override
    public void playerJoin(UUID uuid, String name, String ipHash, byte[] ipEncrypted) {
        Instant now = Instant.now();
        try (Connection conn = db.getConnection()) {
            Optional<PlayerProfile> existing = getPlayer(conn, uuid);
            if (existing.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO players (uuid, current_name, first_seen, last_seen, total_playtime, last_ip_hash, last_ip_encrypted)
                        VALUES (?, ?, ?, ?, 0, ?, ?)
                        """)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, name);
                    ps.setTimestamp(3, Timestamp.from(now));
                    ps.setTimestamp(4, Timestamp.from(now));
                    ps.setString(5, ipHash);
                    ps.setBytes(6, ipEncrypted);
                    ps.executeUpdate();
                }
            } else {
                if (!existing.get().currentName().equals(name)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO player_names_history (uuid, name, changed_at) VALUES (?, ?, ?)")) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, name);
                        ps.setTimestamp(3, Timestamp.from(now));
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE players SET current_name = ?, last_seen = ?, last_ip_hash = ?, last_ip_encrypted = ? WHERE uuid = ?
                        """)) {
                    ps.setString(1, name);
                    ps.setTimestamp(2, Timestamp.from(now));
                    ps.setString(3, ipHash);
                    ps.setBytes(4, ipEncrypted);
                    ps.setString(5, uuid.toString());
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sessions (uuid, join_at, ip_hash, ip_encrypted) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setTimestamp(2, Timestamp.from(now));
                ps.setString(3, ipHash);
                ps.setBytes(4, ipEncrypted);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logError("playerJoin", e);
        }
    }

    @Override
    public void playerQuit(UUID uuid) {
        Instant now = Instant.now();
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE sessions SET quit_at = ? WHERE uuid = ? AND quit_at IS NULL ORDER BY join_at DESC LIMIT 1
                    """)) {
                ps.setTimestamp(1, Timestamp.from(now));
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE players p SET total_playtime = total_playtime + COALESCE(
                        (SELECT TIMESTAMPDIFF(SECOND, s.join_at, s.quit_at) FROM sessions s
                         WHERE s.uuid = p.uuid AND s.quit_at IS NOT NULL ORDER BY s.quit_at DESC LIMIT 1), 0)
                    WHERE p.uuid = ?
                    """)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logError("playerQuit", e);
        }
    }

    @Override
    public Optional<PlayerProfile> getPlayer(UUID uuid) {
        try (Connection conn = db.getConnection()) {
            return getPlayer(conn, uuid);
        } catch (SQLException e) {
            logError("getPlayer", e);
            return Optional.empty();
        }
    }

    @Override
    public byte[] getPlayerIpEncrypted(UUID uuid) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT last_ip_encrypted FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBytes("last_ip_encrypted");
            }
        } catch (SQLException e) {
            logError("getPlayerIpEncrypted", e);
        }
        return null;
    }

    @Override
    public void insertBan(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source) {
        try (Connection conn = db.getConnection()) {
            deactivate(conn, "bans", target, "SYSTEM");
            try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO bans (uuid, staff_uuid, staff_name, reason, created_at, expires_at, active, source)
                    VALUES (?, ?, ?, ?, ?, ?, 1, ?)
                    """)) {
                ps.setString(1, target.toString());
                ps.setString(2, staffUuid != null ? staffUuid.toString() : null);
                ps.setString(3, staffName);
                ps.setString(4, reason);
                ps.setTimestamp(5, Timestamp.from(Instant.now()));
                ps.setTimestamp(6, expiresAt != null ? Timestamp.from(expiresAt) : null);
                ps.setString(7, source);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logError("insertBan", e);
        }
    }

    @Override
    public void deactivateBans(UUID target, String liftedBy) {
        deactivate(connSafe -> {
            try (PreparedStatement ps = connSafe.prepareStatement(
                    "UPDATE bans SET active = 0, lifted_by = ?, lifted_at = ? WHERE uuid = ? AND active = 1")) {
                ps.setString(1, liftedBy);
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setString(3, target.toString());
                ps.executeUpdate();
            }
        }, "deactivateBans");
    }

    @Override
    public Optional<SanctionRecord> findActiveBan(UUID uuid) {
        return findActiveSanction("bans", uuid);
    }

    @Override
    public List<SanctionRecord> listActiveBans(int page, int pageSize) {
        return listActiveSanctions("bans", page, pageSize);
    }

    @Override
    public int countActiveBans() {
        return countActiveSanctions("bans");
    }

    @Override
    public List<SanctionRecord> getBanHistory(UUID uuid) {
        return getSanctionHistory("bans", uuid);
    }

    @Override
    public void expireBans() {
        expireSanctions("bans");
    }

    @Override
    public void insertMute(UUID target, UUID staffUuid, String staffName, String reason, Instant expiresAt, String source) {
        try (Connection conn = db.getConnection()) {
            deactivate(conn, "mutes", target, "SYSTEM");
            try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO mutes (uuid, staff_uuid, staff_name, reason, created_at, expires_at, active, source)
                    VALUES (?, ?, ?, ?, ?, ?, 1, ?)
                    """)) {
                ps.setString(1, target.toString());
                ps.setString(2, staffUuid != null ? staffUuid.toString() : null);
                ps.setString(3, staffName);
                ps.setString(4, reason);
                ps.setTimestamp(5, Timestamp.from(Instant.now()));
                ps.setTimestamp(6, expiresAt != null ? Timestamp.from(expiresAt) : null);
                ps.setString(7, source);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logError("insertMute", e);
        }
    }

    @Override
    public void deactivateMutes(UUID target, String liftedBy) {
        deactivate(connSafe -> {
            try (PreparedStatement ps = connSafe.prepareStatement(
                    "UPDATE mutes SET active = 0, lifted_by = ?, lifted_at = ? WHERE uuid = ? AND active = 1")) {
                ps.setString(1, liftedBy);
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setString(3, target.toString());
                ps.executeUpdate();
            }
        }, "deactivateMutes");
    }

    @Override
    public Optional<SanctionRecord> findActiveMute(UUID uuid) {
        return findActiveSanction("mutes", uuid);
    }

    @Override
    public List<SanctionRecord> listActiveMutes(int page, int pageSize) {
        return listActiveSanctions("mutes", page, pageSize);
    }

    @Override
    public int countActiveMutes() {
        return countActiveSanctions("mutes");
    }

    @Override
    public List<SanctionRecord> getMuteHistory(UUID uuid) {
        return getSanctionHistory("mutes", uuid);
    }

    @Override
    public void expireMutes() {
        expireSanctions("mutes");
    }

    @Override
    public void insertKick(UUID target, UUID staffUuid, String staffName, String reason, String source) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     INSERT INTO kicks (uuid, staff_uuid, staff_name, reason, created_at, source) VALUES (?, ?, ?, ?, ?, ?)
                     """)) {
            ps.setString(1, target.toString());
            ps.setString(2, staffUuid != null ? staffUuid.toString() : null);
            ps.setString(3, staffName);
            ps.setString(4, reason);
            ps.setTimestamp(5, Timestamp.from(Instant.now()));
            ps.setString(6, source);
            ps.executeUpdate();
        } catch (SQLException e) {
            logError("insertKick", e);
        }
    }

    @Override
    public int countKicks(UUID uuid) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM kicks WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logError("countKicks", e);
        }
        return 0;
    }

    @Override
    public void insertWarn(UUID target, UUID staffUuid, String staffName, String reason, String source) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     INSERT INTO warns (uuid, staff_uuid, staff_name, reason, created_at, active, source) VALUES (?, ?, ?, ?, ?, 1, ?)
                     """)) {
            ps.setString(1, target.toString());
            ps.setString(2, staffUuid != null ? staffUuid.toString() : null);
            ps.setString(3, staffName);
            ps.setString(4, reason);
            ps.setTimestamp(5, Timestamp.from(Instant.now()));
            ps.setString(6, source);
            ps.executeUpdate();
        } catch (SQLException e) {
            logError("insertWarn", e);
        }
    }

    @Override
    public int countActiveWarns(UUID uuid) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM warns WHERE uuid = ? AND active = 1")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logError("countActiveWarns", e);
        }
        return 0;
    }

    @Override
    public List<WarnService.WarnEntry> getWarns(UUID uuid) {
        List<WarnService.WarnEntry> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT reason, staff_name, created_at FROM warns WHERE uuid = ? AND active = 1 ORDER BY created_at DESC")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WarnService.WarnEntry(
                            rs.getString("reason"),
                            rs.getString("staff_name"),
                            rs.getTimestamp("created_at").toInstant()));
                }
            }
        } catch (SQLException e) {
            logError("getWarns", e);
        }
        return list;
    }

    @Override
    public void insertNote(UUID target, String staffName, String content) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO staff_notes (uuid, staff_name, content, created_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, target.toString());
            ps.setString(2, staffName);
            ps.setString(3, content);
            ps.setTimestamp(4, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            logError("insertNote", e);
        }
    }

    @Override
    public List<PendingAction> fetchPendingActions(int limit) {
        List<PendingAction> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT id, action_type, target_uuid, payload FROM pending_actions
                     WHERE processed = 0 ORDER BY created_at ASC LIMIT ?
                     """)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PendingAction(
                            rs.getLong("id"),
                            rs.getString("action_type"),
                            rs.getString("target_uuid"),
                            rs.getString("payload")));
                }
            }
        } catch (SQLException e) {
            logError("fetchPendingActions", e);
        }
        return list;
    }

    @Override
    public void markPendingProcessed(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE pending_actions SET processed = 1 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logError("markPendingProcessed", e);
        }
    }

    private Optional<PlayerProfile> getPlayer(Connection conn, UUID uuid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT uuid, current_name, first_seen, last_seen, total_playtime, last_ip_hash FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new PlayerProfile(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("current_name"),
                        rs.getTimestamp("first_seen").toInstant(),
                        rs.getTimestamp("last_seen").toInstant(),
                        rs.getLong("total_playtime"),
                        rs.getString("last_ip_hash")));
            }
        }
    }

    private void deactivate(Connection conn, String table, UUID target, String liftedBy) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE " + table + " SET active = 0, lifted_at = ?, lifted_by = ? WHERE uuid = ? AND active = 1")) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setString(2, liftedBy);
            ps.setString(3, target.toString());
            ps.executeUpdate();
        }
    }

    private void deactivate(SqlConsumer consumer, String label) {
        try (Connection conn = db.getConnection()) {
            consumer.accept(conn);
        } catch (SQLException e) {
            logError(label, e);
        }
    }

    private Optional<SanctionRecord> findActiveSanction(String table, UUID uuid) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + table + " WHERE uuid = ? AND active = 1 ORDER BY created_at DESC LIMIT 1")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapSanction(rs));
            }
        } catch (SQLException e) {
            logError("findActiveSanction", e);
        }
        return Optional.empty();
    }

    private List<SanctionRecord> listActiveSanctions(String table, int page, int pageSize) {
        List<SanctionRecord> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT t.* FROM %s t INNER JOIN (
                         SELECT uuid, MAX(created_at) AS max_created FROM %s WHERE active = 1 GROUP BY uuid
                     ) latest ON t.uuid = latest.uuid AND t.created_at = latest.max_created
                     WHERE t.active = 1 ORDER BY t.created_at DESC LIMIT ? OFFSET ?
                     """.formatted(table, table))) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSanction(rs));
            }
        } catch (SQLException e) {
            logError("listActiveSanctions", e);
        }
        return list;
    }

    private int countActiveSanctions(String table) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT uuid) FROM " + table + " WHERE active = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logError("countActiveSanctions", e);
        }
        return 0;
    }

    private List<SanctionRecord> getSanctionHistory(String table, UUID uuid) {
        List<SanctionRecord> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + table + " WHERE uuid = ? ORDER BY created_at DESC")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSanction(rs));
            }
        } catch (SQLException e) {
            logError("getSanctionHistory", e);
        }
        return list;
    }

    private void expireSanctions(String table) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     UPDATE %s SET active = 0, lifted_at = ?, lifted_by = 'SYSTEM'
                     WHERE active = 1 AND expires_at IS NOT NULL AND expires_at <= ?
                     """.formatted(table))) {
            Instant now = Instant.now();
            ps.setTimestamp(1, Timestamp.from(now));
            ps.setTimestamp(2, Timestamp.from(now));
            ps.executeUpdate();
        } catch (SQLException e) {
            logError("expireSanctions", e);
        }
    }

    private SanctionRecord mapSanction(ResultSet rs) throws SQLException {
        String staffUuidStr = rs.getString("staff_uuid");
        Timestamp expires = rs.getTimestamp("expires_at");
        return new SanctionRecord(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                staffUuidStr != null ? UUID.fromString(staffUuidStr) : null,
                rs.getString("staff_name"),
                rs.getString("reason"),
                rs.getTimestamp("created_at").toInstant(),
                expires != null ? expires.toInstant() : null,
                rs.getBoolean("active"),
                rs.getString("source"));
    }

    private void logError(String op, SQLException e) {
        plugin.getLogger().warning("MariaDB [" + op + "] : " + e.getMessage());
    }

    @FunctionalInterface
    private interface SqlConsumer {
        void accept(Connection conn) throws SQLException;
    }
}
