package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class WarnCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public WarnCommand(AdminPlugin plugin) {
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
        String reason = args.length >= 2
                ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                : "Aucune raison spécifiée";

        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;
        plugin.getWarnService().warn(target.getUniqueId(), staffUuid, sender.getName(), reason, "INGAME", () -> {
            plugin.getMessageService().send(sender, "warn-success", Map.of("player", targetName));
            Player online = Bukkit.getPlayer(target.getUniqueId());
            if (online != null) {
                plugin.getMessageService().send(online, "warn-player", Map.of("reason", reason));
            }
        });
        return true;
    }
}
