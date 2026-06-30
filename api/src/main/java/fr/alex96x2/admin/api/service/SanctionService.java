package fr.alex96x2.admin.api.service;

import fr.alex96x2.admin.api.dto.Dtos;
import fr.alex96x2.admin.api.util.SanctionMapper;
import fr.alex96x2.admin.api.repository.BanRepository;
import fr.alex96x2.admin.api.repository.MuteRepository;
import fr.alex96x2.admin.api.repository.PlayerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SanctionService {

    private final JdbcTemplate jdbc;
    private final BanRepository banRepository;
    private final MuteRepository muteRepository;
    private final PlayerRepository playerRepository;
    private final SanctionMapper sanctionMapper;

    public SanctionService(JdbcTemplate jdbc, BanRepository banRepository, MuteRepository muteRepository,
                           PlayerRepository playerRepository, SanctionMapper sanctionMapper) {
        this.jdbc = jdbc;
        this.banRepository = banRepository;
        this.muteRepository = muteRepository;
        this.playerRepository = playerRepository;
        this.sanctionMapper = sanctionMapper;
    }

    @Transactional
    public void ban(String uuid, Dtos.SanctionRequest request) {
        ensurePlayer(uuid);
        deactivateBans(uuid);
        insertSanction("bans", uuid, request);
        queueAction("BAN", uuid, request);
        logAction("BAN", uuid, request.reason());
    }

    @Transactional
    public void unban(String uuid) {
        ensurePlayer(uuid);
        jdbc.update("UPDATE bans SET active = 0, lifted_by = ?, lifted_at = ? WHERE uuid = ? AND active = 1",
                staffName(), Instant.now(), uuid);
        queueAction("UNBAN", uuid, new Dtos.SanctionRequest("Déban panel", null));
        logAction("UNBAN", uuid, null);
    }

    @Transactional
    public void mute(String uuid, Dtos.SanctionRequest request) {
        ensurePlayer(uuid);
        deactivateMutes(uuid);
        insertSanction("mutes", uuid, request);
        queueAction("MUTE", uuid, request);
        logAction("MUTE", uuid, request.reason());
    }

    @Transactional
    public void unmute(String uuid) {
        ensurePlayer(uuid);
        jdbc.update("UPDATE mutes SET active = 0, lifted_by = ?, lifted_at = ? WHERE uuid = ? AND active = 1",
                staffName(), Instant.now(), uuid);
        queueAction("UNMUTE", uuid, new Dtos.SanctionRequest("Unmute panel", null));
        logAction("UNMUTE", uuid, null);
    }

    @Transactional
    public void warn(String uuid, Dtos.SanctionRequest request) {
        ensurePlayer(uuid);
        jdbc.update("""
                INSERT INTO warns (uuid, staff_name, reason, created_at, active, source)
                VALUES (?, ?, ?, ?, 1, 'WEB')
                """, uuid, staffName(), request.reason(), Instant.now());
        queueAction("WARN", uuid, request);
        logAction("WARN", uuid, request.reason());
    }

    @Transactional
    public void note(String uuid, Dtos.NoteRequest request) {
        ensurePlayer(uuid);
        jdbc.update("""
                INSERT INTO staff_notes (uuid, staff_name, content, created_at)
                VALUES (?, ?, ?, ?)
                """, uuid, staffName(), request.content(), Instant.now());
        logAction("NOTE", uuid, request.content());
    }

    public Dtos.DashboardDto dashboard() {
        long totalPlayers = playerRepository.count();
        long activeBans = banRepository.countByActiveTrue();
        long activeMutes = muteRepository.countByActiveTrue();
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        Long sanctions24h = jdbc.queryForObject("""
                SELECT (
                    (SELECT COUNT(*) FROM bans WHERE created_at >= ?) +
                    (SELECT COUNT(*) FROM mutes WHERE created_at >= ?) +
                    (SELECT COUNT(*) FROM kicks WHERE created_at >= ?) +
                    (SELECT COUNT(*) FROM warns WHERE created_at >= ?)
                )
                """, Long.class, since, since, since, since);

        List<Dtos.SanctionDto> recent = banRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .limit(10)
                .map(b -> sanctionMapper.toDto(b, "bans"))
                .toList();

        return new Dtos.DashboardDto(totalPlayers, activeBans, activeMutes, sanctions24h != null ? sanctions24h : 0, recent);
    }

    private void ensurePlayer(String uuid) {
        playerRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Joueur introuvable"));
    }

    private void deactivateBans(String uuid) {
        jdbc.update("UPDATE bans SET active = 0, lifted_at = ?, lifted_by = ? WHERE uuid = ? AND active = 1",
                Instant.now(), staffName(), uuid);
    }

    private void deactivateMutes(String uuid) {
        jdbc.update("UPDATE mutes SET active = 0, lifted_at = ?, lifted_by = ? WHERE uuid = ? AND active = 1",
                Instant.now(), staffName(), uuid);
    }

    private void insertSanction(String table, String uuid, Dtos.SanctionRequest request) {
        Long durationSeconds = parseDurationSeconds(request.duration());
        if (durationSeconds == null) {
            jdbc.update("""
                    INSERT INTO %s (uuid, staff_name, reason, created_at, expires_at, active, source)
                    VALUES (?, ?, ?, UTC_TIMESTAMP(), NULL, 1, 'WEB')
                    """.formatted(table),
                    uuid, staffName(), request.reason());
        } else {
            jdbc.update("""
                    INSERT INTO %s (uuid, staff_name, reason, created_at, expires_at, active, source)
                    VALUES (?, ?, ?, UTC_TIMESTAMP(), UTC_TIMESTAMP() + INTERVAL ? SECOND, 1, 'WEB')
                    """.formatted(table),
                    uuid, staffName(), request.reason(), durationSeconds);
        }
    }

    private Long parseDurationSeconds(String duration) {
        if (duration == null || duration.isBlank() || duration.equalsIgnoreCase("perm")) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)([smhdwMy])$").matcher(duration.trim());
        if (!m.matches()) return null;
        long amount = Long.parseLong(m.group(1));
        return switch (m.group(2).toLowerCase()) {
            case "s" -> amount;
            case "m" -> amount * 60;
            case "h" -> amount * 3600;
            case "d" -> amount * 86400;
            case "w" -> amount * 7 * 86400;
            case "y" -> amount * 365 * 86400;
            default -> null;
        };
    }

    private void queueAction(String type, String uuid, Dtos.SanctionRequest request) {
        String payload = "{\"reason\":\"" + escape(request.reason()) + "\",\"staffName\":\"" + escape(staffName()) + "\",\"duration\":\"" + escape(request.duration()) + "\"}";
        jdbc.update("INSERT INTO pending_actions (action_type, target_uuid, payload, created_at, processed) VALUES (?, ?, ?, ?, 0)",
                type, uuid, payload, Instant.now());
    }

    private void logAction(String type, String uuid, String details) {
        jdbc.update("""
                INSERT INTO staff_actions_log (staff_name, action_type, target_uuid, details, created_at, source)
                VALUES (?, ?, ?, ?, ?, 'WEB')
                """, staffName(), type, uuid, details != null ? "{\"reason\":\"" + escape(details) + "\"}" : null, Instant.now());
    }

    private String staffName() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "Panel";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
