package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.util.DurationParser;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class MuteCommand implements CommandExecutor {

    private final AdminPlugin plugin;
    private final boolean requireDuration;

    public MuteCommand(AdminPlugin plugin) {
        this(plugin, false);
    }

    public MuteCommand(AdminPlugin plugin, boolean requireDuration) {
        this.plugin = plugin;
        this.requireDuration = requireDuration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (requireDuration && args.length < 2) {
            plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
            return true;
        }
        if (!requireDuration && args.length < 1) {
            plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
            return true;
        }

        OfflinePlayer target = PlayerResolver.resolve(args[0]);
        String targetName = target.getName() != null ? target.getName() : args[0];
        UUID targetUuid = target.getUniqueId();

        Instant expires = null;
        int reasonStart = 1;
        if (requireDuration) {
            try {
                expires = DurationParser.parseExpiry(args[1]);
                reasonStart = 2;
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
                return true;
            }
        } else if (args.length >= 2 && args[1].matches("\\d+[smhdwMy]")) {
            try {
                expires = DurationParser.parseExpiry(args[1]);
                reasonStart = 2;
            } catch (IllegalArgumentException ignored) {
                reasonStart = 1;
            }
        }

        String reason = reasonStart < args.length
                ? String.join(" ", java.util.Arrays.copyOfRange(args, reasonStart, args.length))
                : "Aucune raison spécifiée";

        if (plugin.getMuteService().findActive(targetUuid).isPresent()) {
            plugin.getMessageService().send(sender, "already-muted", Map.of("player", targetName));
            return true;
        }

        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;
        plugin.getMuteService().mute(targetUuid, staffUuid, sender.getName(), reason, expires, "INGAME", () ->
                plugin.getMessageService().send(sender, "mute-success", Map.of("player", targetName)));
        return true;
    }
}
