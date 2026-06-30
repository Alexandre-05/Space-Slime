package fr.alex96x2.admin.util;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {

    private static final Pattern PATTERN = Pattern.compile("^(\\d+)([smhdwMy])$", Pattern.CASE_INSENSITIVE);

    private DurationParser() {}

    public static Instant parseExpiry(String input) {
        if (input == null || input.isBlank() || input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Durée invalide : " + input);
        }
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();
        Duration duration = switch (unit) {
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            case "w" -> Duration.ofDays(amount * 7);
            case "y" -> Duration.ofDays(amount * 365);
            default -> throw new IllegalArgumentException("Unité invalide : " + unit);
        };
        return Instant.now().plus(duration);
    }

    public static String formatExpiry(Instant expiresAt) {
        return formatRemaining(expiresAt);
    }

    /** Temps restant détaillé (utilisé pour les mutes, bans, etc.) */
    public static String formatRemaining(Instant expiresAt) {
        if (expiresAt == null) {
            return "Permanent";
        }
        if (expiresAt.isBefore(Instant.now())) {
            return "Expiré";
        }
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        long days = remaining.toDays();
        long hours = remaining.toHoursPart();
        long minutes = remaining.toMinutesPart();
        long seconds = remaining.toSecondsPart();
        if (days > 0) {
            return days + "j " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public static String formatPlaytime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
