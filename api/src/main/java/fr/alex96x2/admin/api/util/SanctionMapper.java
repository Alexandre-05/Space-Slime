package fr.alex96x2.admin.api.util;

import fr.alex96x2.admin.api.dto.Dtos;
import fr.alex96x2.admin.api.entity.SanctionEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SanctionMapper {

    private final JdbcTemplate jdbc;

    public SanctionMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Dtos.SanctionDto toDto(SanctionEntity entity, String table) {
        Timing timing = queryTiming(table, entity.getId());

        Long remainingSeconds = null;
        if (!entity.isActive()) {
            remainingSeconds = 0L;
        } else if (entity.getExpiresAt() != null) {
            remainingSeconds = timing.remainingSeconds();
        }

        return new Dtos.SanctionDto(
                entity.getId(),
                entity.getReason(),
                entity.getStaffName(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.isActive(),
                entity.getSource().name(),
                remainingSeconds,
                timing.epochMs(),
                timing.durationSeconds()
        );
    }

    private Timing queryTiming(String table, long id) {
        Map<String, Object> row = jdbc.queryForMap("""
                SELECT CASE WHEN expires_at IS NULL THEN NULL
                            ELSE GREATEST(0, UNIX_TIMESTAMP(expires_at) - UNIX_TIMESTAMP(UTC_TIMESTAMP()))
                       END AS remaining,
                       CASE WHEN expires_at IS NULL THEN NULL
                            ELSE UNIX_TIMESTAMP(expires_at)
                       END AS epoch_sec,
                       CASE WHEN expires_at IS NULL THEN NULL
                            ELSE TIMESTAMPDIFF(SECOND, created_at, expires_at)
                       END AS duration_sec
                FROM %s
                WHERE id = ?
                """.formatted(table), id);

        Number remaining = (Number) row.get("remaining");
        Number epochSec = (Number) row.get("epoch_sec");
        Number durationSec = (Number) row.get("duration_sec");

        return new Timing(
                remaining != null ? remaining.longValue() : null,
                epochSec != null ? epochSec.longValue() * 1000 : null,
                durationSec != null ? durationSec.longValue() : null
        );
    }

    private record Timing(Long remainingSeconds, Long epochMs, Long durationSeconds) {}
}
