package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SocialSpyCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public SocialSpyCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande joueur uniquement.");
            return true;
        }
        plugin.getPrivateMessageService().toggleSocialSpy(player.getUniqueId());
        if (plugin.getPrivateMessageService().hasSocialSpy(player.getUniqueId())) {
            plugin.getMessageService().send(sender, "socialspy-on");
        } else {
            plugin.getMessageService().send(sender, "socialspy-off");
        }
        return true;
    }
}
