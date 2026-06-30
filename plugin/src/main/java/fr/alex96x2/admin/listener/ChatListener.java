package fr.alex96x2.admin.listener;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.util.DurationParser;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;
import java.util.Optional;

public class ChatListener implements Listener {

    private final AdminPlugin plugin;

    public ChatListener(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        handleMutedPlayer(event.getPlayer(), () -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();
        if (!msg.startsWith("/msg ") && !msg.startsWith("/m ") && !msg.startsWith("/tell ")
                && !msg.startsWith("/r ") && !msg.startsWith("/reply ")) {
            return;
        }
        handleMutedPlayer(event.getPlayer(), () -> event.setCancelled(true));
    }

    private void handleMutedPlayer(Player player, Runnable onMuted) {
        if (player.hasPermission("admin.bypass.mute")) {
            return;
        }
        Optional<SanctionRecord> mute = plugin.getMuteService().findActive(player.getUniqueId());
        if (mute.isEmpty()) {
            return;
        }
        onMuted.run();
        SanctionRecord record = mute.get();
        plugin.getMessageService().send(player, "muted", Map.of(
                "reason", record.reason(),
                "remaining", DurationParser.formatRemaining(record.expiresAt())
        ));
    }
}
