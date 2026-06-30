package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class IgnoreListCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public IgnoreListCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande joueur uniquement.");
            return true;
        }
        Set<UUID> ignored = plugin.getIgnoreService().getIgnored(player.getUniqueId());
        if (ignored.isEmpty()) {
            plugin.getMessageService().send(sender, "ignore-list-empty");
            return true;
        }
        plugin.getMessageService().send(sender, "ignore-list-header");
        for (UUID uuid : ignored) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            sender.sendMessage("§7- §f" + (name != null ? name : uuid.toString()));
        }
        return true;
    }
}
