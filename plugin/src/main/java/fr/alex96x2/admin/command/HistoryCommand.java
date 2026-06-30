package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.model.SanctionRecord;
import fr.alex96x2.admin.util.DurationParser;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class HistoryCommand implements CommandExecutor {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private final AdminPlugin plugin;

    public HistoryCommand(AdminPlugin plugin) {
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

        List<SanctionRecord> bans = plugin.getBanService().getHistory(target.getUniqueId());
        List<SanctionRecord> mutes = plugin.getMuteService().getHistory(target.getUniqueId());
        int kicks = plugin.getKickService().countHistory(target.getUniqueId());
        int warns = plugin.getWarnService().countActive(target.getUniqueId());

        plugin.getMessageService().send(sender, "history-header", Map.of("player", targetName));
        plugin.getMessageService().send(sender, "history-bans", Map.of("count", String.valueOf(bans.size())));
        plugin.getMessageService().send(sender, "history-mutes", Map.of("count", String.valueOf(mutes.size())));
        plugin.getMessageService().send(sender, "history-kicks", Map.of("count", String.valueOf(kicks)));
        plugin.getMessageService().send(sender, "history-warns", Map.of("count", String.valueOf(warns)));

        for (SanctionRecord ban : bans.stream().limit(5).toList()) {
            plugin.getMessageService().send(sender, "history-entry", Map.of(
                    "date", FORMAT.format(ban.createdAt()),
                    "reason", "[BAN] " + ban.reason(),
                    "staff", ban.staffName() != null ? ban.staffName() : "Inconnu"
            ));
        }
        for (SanctionRecord mute : mutes.stream().limit(5).toList()) {
            plugin.getMessageService().send(sender, "history-entry", Map.of(
                    "date", FORMAT.format(mute.createdAt()),
                    "reason", "[MUTE] " + mute.reason(),
                    "staff", mute.staffName() != null ? mute.staffName() : "Inconnu"
            ));
        }
        return true;
    }
}
