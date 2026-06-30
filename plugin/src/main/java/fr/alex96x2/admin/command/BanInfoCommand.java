package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.util.DurationParser;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Optional;

public class BanInfoCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public BanInfoCommand(AdminPlugin plugin) {
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
        Optional<SanctionRecord> ban = plugin.getBanService().findActive(target.getUniqueId());
        if (ban.isEmpty()) {
            plugin.getMessageService().send(sender, "not-banned", Map.of("player", targetName));
            return true;
        }
        SanctionRecord record = ban.get();
        sender.sendMessage("§6Ban de " + targetName);
        sender.sendMessage("§7Raison: §f" + record.reason());
        sender.sendMessage("§7Staff: §f" + (record.staffName() != null ? record.staffName() : "Inconnu"));
        sender.sendMessage("§7Expire: §f" + (record.expiresAt() == null ? "Permanent" : DurationParser.formatExpiry(record.expiresAt())));
        return true;
    }
}
