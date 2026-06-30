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

public class KickCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public KickCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
            return true;
        }
        Player target = PlayerResolver.online(args[0]);
        if (target == null) {
            plugin.getMessageService().send(sender, "msg-offline", Map.of("player", args[0]));
            return true;
        }

        String reason = args.length >= 2
                ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                : "Aucune raison spécifiée";

        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;
        plugin.getKickService().kick(target.getUniqueId(), staffUuid, sender.getName(), reason, "INGAME");
        target.kick(plugin.getMessageService().get("kick-message", Map.of("reason", reason)));
        plugin.getMessageService().send(sender, "kick-success", Map.of("player", target.getName()));
        return true;
    }
}
