package fr.alex96x2.slimecapture.service;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class MessageService {

    private final SlimeCapturePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageService(SlimeCapturePlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        // messages in config.yml
    }

    public Component get(String key, Map<String, String> placeholders) {
        FileConfiguration config = plugin.getConfig();
        String raw = config.getConfigurationSection("messages") != null
                ? config.getConfigurationSection("messages").getString(key, key)
                : key;
        String prefix = config.getString("messages.prefix", "");
        raw = raw.replace("<prefix>", prefix);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                raw = raw.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return miniMessage.deserialize(raw);
    }

    public void sendChat(org.bukkit.entity.Player player, String key) {
        sendChat(player, key, null);
    }

    public void sendChat(org.bukkit.entity.Player player, String key, Map<String, String> placeholders) {
        player.sendMessage(get(key, placeholders));
    }
}
