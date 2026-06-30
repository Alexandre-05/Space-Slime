package fr.alex96x2.admin.config;

import fr.alex96x2.admin.storage.StorageType;
import org.bukkit.configuration.file.FileConfiguration;

public record PluginConfig(
        StorageType storageType,
        String jsonFile,
        String dbHost,
        int dbPort,
        String dbName,
        String dbUser,
        String dbPassword,
        int poolSize,
        boolean storeIp,
        String ipHashSalt,
        String ipEncryptionKey,
        int syncPollSeconds,
        int expirationCheckSeconds,
        String messagePrefix,
        boolean banBroadcast,
        boolean msgSoundEnabled,
        String msgSound,
        float msgSoundVolume,
        float msgSoundPitch,
        int autoBanThreshold,
        String autoBanDuration
) {
    public static PluginConfig from(FileConfiguration config) {
        return new PluginConfig(
                StorageType.from(config.getString("storage.type", "AUTO")),
                config.getString("storage.json-file", "data.json"),
                config.getString("database.host", "localhost"),
                config.getInt("database.port", 3306),
                config.getString("database.database", "minecraft_admin"),
                config.getString("database.username", "admin"),
                config.getString("database.password", ""),
                config.getInt("database.pool-size", 10),
                config.getBoolean("ip.store", true),
                config.getString("ip.hash-salt", "change-me"),
                config.getString("ip.encryption-key", "change-me-32-chars-minimum-key!!"),
                config.getInt("sync.poll-interval-seconds", 5),
                config.getInt("sync.expiration-check-seconds", 60),
                config.getString("messages.prefix", "<#C62828>[Admin] "),
                config.getBoolean("messages.ban-broadcast", true),
                config.getBoolean("messages.msg-sound.enabled", true),
                config.getString("messages.msg-sound.sound", "ENTITY_EXPERIENCE_ORB_PICKUP"),
                (float) config.getDouble("messages.msg-sound.volume", 0.4),
                (float) config.getDouble("messages.msg-sound.pitch", 1.5),
                config.getInt("warns.auto-ban-threshold", 0),
                config.getString("warns.auto-ban-duration", "1d")
        );
    }
}
