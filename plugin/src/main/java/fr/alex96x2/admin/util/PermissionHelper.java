package fr.alex96x2.admin.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public final class PermissionHelper {

    private PermissionHelper() {}

    public static boolean hasBypassBan(UUID uuid) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        if (offline.isOp()) {
            return true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                var provider = net.luckperms.api.LuckPermsProvider.get();
                var user = provider.getUserManager().loadUser(uuid).join();
                if (user != null) {
                    return user.getCachedData().getPermissionData()
                            .checkPermission("admin.bypass.ban").asBoolean();
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
