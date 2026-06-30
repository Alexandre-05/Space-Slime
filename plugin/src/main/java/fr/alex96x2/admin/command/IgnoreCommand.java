package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class IgnoreCommand implements CommandExecutor {

    private final AdminPlugin plugin;
    private final boolean ignore;

    public IgnoreCommand(AdminPlugin plugin, boolean ignore) {
        this.plugin = plugin;
        this.ignore = ignore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande joueur uniquement.");
            return true;
        }
        if (args.length < 1) {
            plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
            return true;
        }
        Player target = PlayerResolver.online(args[0]);
        if (target == null) {
            plugin.getMessageService().send(sender, "msg-offline", Map.of("player", args[0]));
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.getMessageService().send(sender, "ignore-self");
            return true;
        }
        if (ignore) {
            plugin.getIgnoreService().ignore(player.getUniqueId(), target.getUniqueId());
            plugin.getMessageService().send(sender, "ignore-success", Map.of("player", target.getName()));
        } else {
            plugin.getIgnoreService().unignore(player.getUniqueId(), target.getUniqueId());
            plugin.getMessageService().send(sender, "unignore-success", Map.of("player", target.getName()));
        }
        return true;
    }
}
