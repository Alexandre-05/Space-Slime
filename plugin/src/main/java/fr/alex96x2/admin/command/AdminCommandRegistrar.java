package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public final class AdminCommandRegistrar {

    private AdminCommandRegistrar() {}

    public static void register(AdminPlugin plugin) {
        register(plugin, "ban", new BanCommand(plugin), new BanTabCompleter());
        register(plugin, "tempban", new BanCommand(plugin, true), new BanTabCompleter());
        register(plugin, "unban", new UnbanCommand(plugin), new PlayerTabCompleter());
        register(plugin, "banlist", new BanListCommand(plugin), null);
        register(plugin, "baninfo", new BanInfoCommand(plugin), new PlayerTabCompleter());
        register(plugin, "mute", new MuteCommand(plugin), new BanTabCompleter());
        register(plugin, "tempmute", new MuteCommand(plugin, true), new BanTabCompleter());
        register(plugin, "unmute", new UnmuteCommand(plugin), new PlayerTabCompleter());
        register(plugin, "mutelist", new MuteListCommand(plugin), null);
        register(plugin, "muteinfo", new MuteInfoCommand(plugin), new PlayerTabCompleter());
        register(plugin, "kick", new KickCommand(plugin), new PlayerTabCompleter());
        register(plugin, "warn", new WarnCommand(plugin), new PlayerTabCompleter());
        register(plugin, "warns", new WarnsCommand(plugin), new PlayerTabCompleter());
        register(plugin, "ignore", new IgnoreCommand(plugin, true), new OnlinePlayerTabCompleter());
        register(plugin, "unignore", new IgnoreCommand(plugin, false), new OnlinePlayerTabCompleter());
        register(plugin, "ignorelist", new IgnoreListCommand(plugin), null);
        register(plugin, "msg", new MsgCommand(plugin), new OnlinePlayerTabCompleter());
        register(plugin, "r", new ReplyCommand(plugin), null);
        register(plugin, "socialspy", new SocialSpyCommand(plugin), null);
        register(plugin, "note", new NoteCommand(plugin), new PlayerTabCompleter());
        register(plugin, "history", new HistoryCommand(plugin), new PlayerTabCompleter());
        register(plugin, "check", new CheckCommand(plugin), new PlayerTabCompleter());
    }

    private static void register(AdminPlugin plugin, String name, CommandExecutor executor, TabCompleter completer) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            plugin.getLogger().warning("Commande non déclarée : " + name);
            return;
        }
        command.setExecutor(executor);
        if (completer != null) {
            command.setTabCompleter(completer);
        }
    }
}
