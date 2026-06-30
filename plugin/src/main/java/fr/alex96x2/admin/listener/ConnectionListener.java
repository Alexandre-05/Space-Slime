package fr.alex96x2.admin.listener;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.util.PermissionHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Optional;

public class ConnectionListener implements Listener {

    private final AdminPlugin plugin;

    public ConnectionListener(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (PermissionHelper.hasBypassBan(event.getUniqueId())) {
            return;
        }
        Optional<SanctionRecord> ban = plugin.getBanService().findActive(event.getUniqueId());
        if (ban.isPresent()) {
            SanctionRecord record = ban.get();
            String expires = record.expiresAt() == null ? "Permanent"
                    : fr.alex96x2.admin.util.DurationParser.formatExpiry(record.expiresAt());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    plugin.getMessageService().get("join-banned", Map.of(
                            "reason", record.reason(),
                            "expires", expires
                    )));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : null;
        plugin.getPlayerService().onJoin(player.getUniqueId(), player.getName(), ip);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerService().onQuit(event.getPlayer().getUniqueId());
    }
}
