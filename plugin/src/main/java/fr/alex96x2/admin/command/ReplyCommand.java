package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class ReplyCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public ReplyCommand(AdminPlugin plugin) {
        this.plugin = plugin;
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
        String message = String.join(" ", args);
        plugin.getPrivateMessageService().reply(player, message);
        return true;
    }
}
