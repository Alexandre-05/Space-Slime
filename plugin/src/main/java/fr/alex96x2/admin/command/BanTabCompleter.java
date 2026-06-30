package fr.alex96x2.admin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class BanTabCompleter implements TabCompleter {

    private final PlayerTabCompleter playerCompleter = new PlayerTabCompleter();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return playerCompleter.onTabComplete(sender, command, alias, args);
        }
        if (args.length == 2) {
            List<String> durations = List.of("30m", "1h", "6h", "1d", "7d", "30d", "perm");
            String prefix = args[1].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String d : durations) {
                if (d.startsWith(prefix)) result.add(d);
            }
            return result;
        }
        return List.of();
    }
}
