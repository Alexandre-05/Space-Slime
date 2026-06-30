package fr.alex96x2.admin.model;

import java.time.Instant;
import java.util.UUID;

public record PlayerProfile(
        UUID uuid,
        String currentName,
        Instant firstSeen,
        Instant lastSeen,
        long totalPlaytime,
        String lastIpHash
) {}
