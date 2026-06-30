package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class UnmuteCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public UnmuteCommand(AdminPlugin plugin) {
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

        if (plugin.getMuteService().findActive(target.getUniqueId()).isEmpty()) {
            plugin.getMessageService().send(sender, "not-muted", Map.of("player", targetName));
            return true;
        }

        plugin.getMuteService().unmute(target.getUniqueId(), sender.getName(), () ->
                plugin.getMessageService().send(sender, "unmute-success", Map.of("player", targetName)));
        return true;
    }
}
