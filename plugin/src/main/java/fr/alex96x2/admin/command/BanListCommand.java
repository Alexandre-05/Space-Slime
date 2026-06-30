package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.util.DurationParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class BanListCommand implements CommandExecutor {

    private static final int PAGE_SIZE = 10;
    private final AdminPlugin plugin;

    public BanListCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int page = 1;
        if (args.length >= 1) {
            try {
                page = Math.max(1, Integer.parseInt(args[0]));
            } catch (NumberFormatException ignored) {}
        }
        int total = plugin.getBanService().countActive();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        List<SanctionRecord> records = plugin.getBanService().listActive(page, PAGE_SIZE);

        plugin.getMessageService().send(sender, "banlist-header", Map.of(
                "page", String.valueOf(page),
                "total", String.valueOf(totalPages)
        ));
        if (records.isEmpty()) {
            plugin.getMessageService().send(sender, "empty-list");
            return true;
        }
        for (SanctionRecord record : records) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(record.uuid());
            String name = player.getName() != null ? player.getName() : record.uuid().toString();
            plugin.getMessageService().send(sender, "banlist-entry", Map.of(
                    "player", name,
                    "reason", record.reason(),
                    "expires", record.expiresAt() == null ? "Permanent" : DurationParser.formatExpiry(record.expiresAt())
            ));
        }
        return true;
    }
}
