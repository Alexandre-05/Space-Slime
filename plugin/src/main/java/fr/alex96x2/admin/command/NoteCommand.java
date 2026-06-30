package fr.alex96x2.admin.command;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.util.PlayerResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class NoteCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public NoteCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            plugin.getMessageService().send(sender, "usage", Map.of("usage", command.getUsage()));
            return true;
        }
        OfflinePlayer target = PlayerResolver.resolve(args[0]);
        String targetName = target.getName() != null ? target.getName() : args[0];
        String content = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;

        plugin.getNoteService().addNote(target.getUniqueId(), staffUuid, sender.getName(), content, () ->
                plugin.getMessageService().send(sender, "note-success", Map.of("player", targetName)));
        return true;
    }
}
