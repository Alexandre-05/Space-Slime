package fr.alex96x2.admin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class OnlinePlayerTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase();
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.getName().toLowerCase().startsWith(prefix)) {
                names.add(p.getName());
            }
        });
        return names;
    }
}
