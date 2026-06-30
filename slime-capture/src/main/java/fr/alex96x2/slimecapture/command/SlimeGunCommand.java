package fr.alex96x2.slimecapture.command;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import fr.alex96x2.slimecapture.item.SlimeCaptureItems;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SlimeGunCommand implements CommandExecutor, TabCompleter {

    private final SlimeCapturePlugin plugin;

    public SlimeGunCommand(SlimeCapturePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "gun" -> handleGun(sender, args);
            case "capsule" -> handleCapsule(sender, args);
            case "machine", "extracteur" -> handleMachine(sender, args);
            case "status", "balance" -> handleStatus(sender);
            default -> {
                sendUsage(sender, label);
                yield true;
            }
        };
    }

    private boolean handleGun(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("slimecapture.gun.others")) {
                sender.sendMessage("§cPermission insuffisante.");
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable.");
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cConsole : précisez un joueur.");
                return true;
            }
            if (!sender.hasPermission("slimecapture.gun")) {
                sender.sendMessage("§cPermission insuffisante.");
                return true;
            }
            target = player;
        }

        target.getInventory().addItem(SlimeCaptureItems.createVacuumGun());
        if (target.equals(sender)) {
            plugin.getActionBarService().show(target, "gun-received");
        } else {
            if (sender instanceof Player senderPlayer) {
                plugin.getActionBarService().show(senderPlayer, "gun-given", Map.of("player", target.getName()));
            } else {
                sender.sendMessage("§aAspirateur donné à " + target.getName() + ".");
            }
            plugin.getActionBarService().show(target, "gun-received");
        }
        return true;
    }

    private boolean handleCapsule(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }
        if (!sender.hasPermission("slimecapture.use")) {
            sender.sendMessage("§cPermission insuffisante.");
            return true;
        }

        int amount = 0;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cQuantité invalide.");
                return true;
            }
        }

        plugin.getSlimeStorageService().withdrawToCanisters(player, amount);
        return true;
    }

    private boolean handleMachine(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("slimecapture.machine.give.others")) {
                sender.sendMessage("§cPermission insuffisante.");
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable.");
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cConsole : précisez un joueur.");
                return true;
            }
            if (!sender.hasPermission("slimecapture.machine.give")) {
                sender.sendMessage("§cPermission insuffisante.");
                return true;
            }
            target = player;
        }

        target.getInventory().addItem(SlimeCaptureItems.createExtractorItem());
        if (target.equals(sender)) {
            plugin.getMessageService().sendChat(target, "machine-received");
        } else {
            if (sender instanceof Player senderPlayer) {
                plugin.getMessageService().sendChat(senderPlayer, "machine-given", Map.of("player", target.getName()));
            } else {
                sender.sendMessage("§aExtracteur donné à " + target.getName() + ".");
            }
            plugin.getMessageService().sendChat(target, "machine-received");
        }
        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }
        int current = plugin.getSlimeStorageService().getGunSlimeCount(player);
        int capacity = plugin.getPluginConfig().gunCapacity();
        plugin.getActionBarService().show(player, "status", Map.of(
                "current", String.valueOf(current),
                "capacity", String.valueOf(capacity)
        ));
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("§d/" + label + " gun [joueur] §7— aspirateur");
        sender.sendMessage("§d/" + label + " capsule [quantité] §7— créer des capsules pour machine");
        sender.sendMessage("§d/" + label + " machine [joueur] §7— extracteur de gelée");
        sender.sendMessage("§d/" + label + " status §7— voir le réservoir");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : List.of("gun", "capsule", "machine", "status")) {
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("gun") && sender.hasPermission("slimecapture.gun.others")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completions.add(online.getName());
                }
            }
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("machine") && sender.hasPermission("slimecapture.machine.give.others")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completions.add(online.getName());
                }
            }
        }
        return completions;
    }
}
