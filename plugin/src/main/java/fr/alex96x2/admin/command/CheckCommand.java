package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.PlayerProfile;
import fr.alex96x2.admin.util.DurationParser;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class CheckCommand implements CommandExecutor {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private final AdminPlugin plugin;

    public CheckCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
            return true;
        }
        OfflinePlayer target = PlayerResolver.resolve(args[0]);
        String targetName = target.getName() != null ? target.getName() : args[0];

        plugin.getPlayerService().getProfile(target.getUniqueId()).thenAccept(opt -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getMessageService().send(sender, "check-header", Map.of("player", targetName));
                if (opt.isEmpty()) {
                    sender.sendMessage("§7Aucune donnée en base pour ce joueur.");
                    return;
                }
                PlayerProfile profile = opt.get();
                plugin.getMessageService().send(sender, "check-uuid", Map.of("uuid", profile.uuid().toString()));
                plugin.getMessageService().send(sender, "check-first-seen", Map.of("first_seen", FORMAT.format(profile.firstSeen())));
                plugin.getMessageService().send(sender, "check-last-seen", Map.of("last_seen", FORMAT.format(profile.lastSeen())));
                plugin.getMessageService().send(sender, "check-playtime", Map.of("playtime", DurationParser.formatPlaytime(profile.totalPlaytime())));

                if (plugin.getBanService().findActive(profile.uuid()).isPresent()) {
                    plugin.getMessageService().send(sender, "check-status-banned");
                } else if (plugin.getMuteService().findActive(profile.uuid()).isPresent()) {
                    plugin.getMessageService().send(sender, "check-status-muted");
                } else {
                    plugin.getMessageService().send(sender, "check-status-clean");
                }

                if (sender.hasPermission("admin.view.ip")) {
                    plugin.getPlayerService().getDecryptedIp(profile.uuid()).thenAccept(ip -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (ip != null) {
                                plugin.getMessageService().send(sender, "check-ip", Map.of("ip", ip));
                            }
                        });
                    });
                }
            });
        });
        return true;
    }
}
