package fr.alex96x2.admin.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PlayerResolver {

    private PlayerResolver() {}

    public static OfflinePlayer resolve(String nameOrUuid) {
        try {
            UUID uuid = UUID.fromString(nameOrUuid);
            return Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException ignored) {
            return Bukkit.getOfflinePlayer(nameOrUuid);
        }
    }

    public static Player online(String name) {
        return Bukkit.getPlayerExact(name);
    }

    public static CompletableFuture<UUID> resolveUuidAsync(String nameOrUuid) {
        try {
            UUID uuid = UUID.fromString(nameOrUuid);
            return CompletableFuture.completedFuture(uuid);
        } catch (IllegalArgumentException ignored) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(nameOrUuid);
            if (offline.hasPlayedBefore() || offline.isOnline()) {
                return CompletableFuture.completedFuture(offline.getUniqueId());
            }
            return CompletableFuture.supplyAsync(() -> {
                OfflinePlayer fetched = Bukkit.getOfflinePlayer(nameOrUuid);
                return fetched.getUniqueId();
            });
        }
    }
}
