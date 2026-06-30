package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class MsgCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public MsgCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande joueur uniquement.");
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
            return true;
        }
        Player target = PlayerResolver.online(args[0]);
        if (target == null) {
            plugin.getMessageService().send(sender, "msg-offline", Map.of("player", args[0]));
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            plugin.getMessageService().send(sender, "msg-self");
            return true;
        }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        plugin.getPrivateMessageService().send(player, target, message);
        return true;
    }
}
