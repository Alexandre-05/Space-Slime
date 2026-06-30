package fr.alex96x2.admin.api.dto;

import java.time.Instant;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String username, String role) {}

    public record PlayerSummaryDto(
            String uuid,
            String currentName,
            Instant firstSeen,
            Instant lastSeen,
            long totalPlaytime,
            boolean banned,
            boolean muted,
            int warnCount
    ) {}

    public record PlayerDetailDto(
            String uuid,
            String currentName,
            Instant firstSeen,
            Instant lastSeen,
            long totalPlaytime,
            String ip,
            List<NameHistoryDto> nameHistory,
            List<SanctionDto> bans,
            List<SanctionDto> mutes,
            List<KickDto> kicks,
            List<WarnDto> warns,
            List<NoteDto> notes,
            List<SessionDto> sessions
    ) {}

    public record NameHistoryDto(String name, Instant changedAt) {}
    public record SanctionDto(Long id, String reason, String staffName, Instant createdAt, Instant expiresAt, boolean active, String source, Long remainingSeconds, Long expiresAtEpochMs, Long durationSeconds) {}
    public record KickDto(Long id, String reason, String staffName, Instant createdAt, String source) {}
    public record WarnDto(Long id, String reason, String staffName, Instant createdAt, boolean active, String source) {}
    public record NoteDto(Long id, String content, String staffName, Instant createdAt) {}
    public record SessionDto(Long id, Instant joinAt, Instant quitAt, String ipHash) {}

    public record SanctionRequest(String reason, String duration) {}
    public record NoteRequest(String content) {}

    public record DashboardDto(
            long totalPlayers,
            long activeBans,
            long activeMutes,
            long sanctionsLast24h,
            List<SanctionDto> recentSanctions
    ) {}

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}
