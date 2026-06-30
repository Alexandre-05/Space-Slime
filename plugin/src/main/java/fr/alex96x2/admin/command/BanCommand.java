package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.util.DurationParser;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class BanCommand implements CommandExecutor {

    private final AdminPlugin plugin;
    private final boolean requireDuration;

    public BanCommand(AdminPlugin plugin) {
        this(plugin, false);
    }

    public BanCommand(AdminPlugin plugin, boolean requireDuration) {
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
        } else if (args.length >= 2 && looksLikeDuration(args[1])) {
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

        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;
        String staffName = sender.getName();

        if (plugin.getBanService().findActive(targetUuid).isPresent()) {
            plugin.getMessageService().send(sender, "already-banned", Map.of("player", targetName));
            return true;
        }

        Instant finalExpires = expires;
        plugin.getBanService().ban(targetUuid, staffUuid, staffName, reason, expires, "INGAME", () -> {
            plugin.getMessageService().send(sender, "ban-success", Map.of("player", targetName));
            if (plugin.getPluginConfig().banBroadcast()) {
                Bukkit.broadcast(plugin.getMessageService().get("ban-broadcast", Map.of(
                        "player", targetName,
                        "staff", staffName,
                        "reason", reason
                )));
            }
            notifyStaff("ban", targetName, staffName, reason, finalExpires);
        });
        return true;
    }

    private boolean looksLikeDuration(String arg) {
        return arg.matches("\\d+[smhdwMy]") || arg.equalsIgnoreCase("perm");
    }

    private void notifyStaff(String type, String target, String staff, String reason, Instant expires) {
        String expiresStr = expires == null ? "Permanent" : DurationParser.formatExpiry(expires);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("admin.notify")) {
                p.sendMessage("§c[Mod] §f" + staff + " a banni " + target + " : " + reason + " (" + expiresStr + ")");
            }
        }
    }
}
