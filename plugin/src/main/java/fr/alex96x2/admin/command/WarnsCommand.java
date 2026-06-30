package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.service.WarnService;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class WarnsCommand implements CommandExecutor {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private final AdminPlugin plugin;

    public WarnsCommand(AdminPlugin plugin) {
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
        List<WarnService.WarnEntry> warns = plugin.getWarnService().getWarns(target.getUniqueId());

        plugin.getMessageService().send(sender, "warns-header", Map.of("player", targetName));
        if (warns.isEmpty()) {
            plugin.getMessageService().send(sender, "empty-list");
            return true;
        }
        for (WarnService.WarnEntry warn : warns) {
            plugin.getMessageService().send(sender, "warns-entry", Map.of(
                    "date", FORMAT.format(warn.createdAt()),
                    "reason", warn.reason(),
                    "staff", warn.staffName() != null ? warn.staffName() : "Inconnu"
            ));
        }
        return true;
    }
}
