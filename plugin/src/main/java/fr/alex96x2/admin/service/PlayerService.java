package fr.alex96x2.admin.service;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.config.PluginConfig;
import fr.alex96x2.admin.model.PlayerProfile;
import fr.alex96x2.admin.storage.DataRepository;
import fr.alex96x2.admin.util.IpUtil;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerService {

    private final DataRepository repository;
    private final AdminPlugin plugin;

    public PlayerService(DataRepository repository) {
        this.plugin = AdminPlugin.getInstance();
        this.repository = repository;
    }

    public CompletableFuture<Void> onJoin(UUID uuid, String name, String ip) {
        return repository.runAsync(() -> {
            PluginConfig config = plugin.getPluginConfig();
            String ipHash = config.storeIp() && ip != null ? IpUtil.hashIp(ip, config.ipHashSalt()) : null;
            byte[] ipEncrypted = config.storeIp() && ip != null ? IpUtil.encryptIp(ip, config.ipEncryptionKey()) : null;
            repository.playerJoin(uuid, name, ipHash, ipEncrypted);
        });
    }

    public CompletableFuture<Void> onQuit(UUID uuid) {
        return repository.runAsync(() -> repository.playerQuit(uuid));
    }

    public CompletableFuture<Optional<PlayerProfile>> getProfile(UUID uuid) {
        return repository.supplyAsync(() -> repository.getPlayer(uuid));
    }

    public CompletableFuture<String> getDecryptedIp(UUID uuid) {
        return repository.supplyAsync(() -> {
            byte[] encrypted = repository.getPlayerIpEncrypted(uuid);
            if (encrypted != null) {
                return IpUtil.decryptIp(encrypted, plugin.getPluginConfig().ipEncryptionKey());
            }
            return null;
        });
    }
}
