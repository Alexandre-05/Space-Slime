package fr.alex96x2.admin.service;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PrivateMessageService {

    private final IgnoreService ignoreService;
    private final Map<UUID, UUID> lastContact = new java.util.concurrent.ConcurrentHashMap<>();
    private final Set<UUID> socialSpy = new HashSet<>();

    public PrivateMessageService(IgnoreService ignoreService) {
        this.ignoreService = ignoreService;
    }

    public boolean send(Player sender, Player target, String message) {
        if (ignoreService.isIgnored(target.getUniqueId(), sender.getUniqueId())) {
            AdminPlugin.getInstance().getMessageService().send(sender, "msg-ignored");
            return false;
        }

        lastContact.put(sender.getUniqueId(), target.getUniqueId());
        lastContact.put(target.getUniqueId(), sender.getUniqueId());

        var msgService = AdminPlugin.getInstance().getMessageService();
        msgService.send(sender, "msg-format-sent", Map.of("target", target.getName(), "message", message));
        msgService.send(target, "msg-format-received", Map.of("sender", sender.getName(), "message", message));
        playMsgSound(target);

        broadcastSocialSpy(sender, target, message);
        return true;
    }

    public boolean reply(Player sender, String message) {
        UUID targetUuid = lastContact.get(sender.getUniqueId());
        if (targetUuid == null) {
            AdminPlugin.getInstance().getMessageService().send(sender, "no-reply-target");
            return false;
        }
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            AdminPlugin.getInstance().getMessageService().send(sender, "msg-offline",
                    Map.of("player", Bukkit.getOfflinePlayer(targetUuid).getName() != null
                            ? Bukkit.getOfflinePlayer(targetUuid).getName() : targetUuid.toString()));
            return false;
        }
        return send(sender, target, message);
    }

    public void toggleSocialSpy(UUID staff) {
        if (socialSpy.contains(staff)) {
            socialSpy.remove(staff);
        } else {
            socialSpy.add(staff);
        }
    }

    public boolean hasSocialSpy(UUID staff) {
        return socialSpy.contains(staff);
    }

    private void playMsgSound(Player target) {
        PluginConfig config = AdminPlugin.getInstance().getPluginConfig();
        if (!config.msgSoundEnabled()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(config.msgSound());
            target.playSound(target.getLocation(), sound, config.msgSoundVolume(), config.msgSoundPitch());
        } catch (IllegalArgumentException e) {
            AdminPlugin.getInstance().getLogger().warning("Son MP invalide : " + config.msgSound());
        }
    }

    private void broadcastSocialSpy(Player sender, Player target, String message) {
        var msgService = AdminPlugin.getInstance().getMessageService();
        for (UUID spyUuid : socialSpy) {
            if (spyUuid.equals(sender.getUniqueId()) || spyUuid.equals(target.getUniqueId())) continue;
            Player spy = Bukkit.getPlayer(spyUuid);
            if (spy != null && spy.hasPermission("admin.socialspy")) {
                msgService.send(spy, "socialspy-format", Map.of(
                        "sender", sender.getName(),
                        "target", target.getName(),
                        "message", message
                ));
            }
        }
    }
}
