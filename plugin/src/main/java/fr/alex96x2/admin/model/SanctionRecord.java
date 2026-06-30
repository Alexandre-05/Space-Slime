package fr.alex96x2.admin.model;

import java.time.Instant;
import java.util.UUID;

public record SanctionRecord(
        long id,
        UUID uuid,
        UUID staffUuid,
        String staffName,
        String reason,
        Instant createdAt,
        Instant expiresAt,
        boolean active,
        String source
) {}
