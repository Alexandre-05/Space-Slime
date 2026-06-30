package fr.alex96x2.admin;

import fr.alex96x2.admin.command.AdminCommandRegistrar;
import fr.alex96x2.admin.config.PluginConfig;
import fr.alex96x2.admin.listener.ChatListener;
import fr.alex96x2.admin.listener.ConnectionListener;
import fr.alex96x2.admin.service.*;
import fr.alex96x2.admin.storage.DataRepository;
import fr.alex96x2.admin.storage.StorageFactory;
import fr.alex96x2.admin.util.MessageService;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdminPlugin extends JavaPlugin {

    private static AdminPlugin instance;
    private PluginConfig pluginConfig;
    private DataRepository repository;
    private MessageService messageService;
    private PlayerService playerService;
    private BanService banService;
    private MuteService muteService;
    private KickService kickService;
    private WarnService warnService;
    private NoteService noteService;
    private IgnoreService ignoreService;
    private PrivateMessageService privateMessageService;
    private SyncService syncService;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadLocalConfig();

        repository = StorageFactory.create(this);
        getLogger().info("Stockage actif : " + repository.type()
                + (repository.supportsWebSync() ? " (sync panel web activée)" : " (mode autonome, pas de panel requis)"));

        playerService = new PlayerService(repository);
        banService = new BanService(this, repository);
        muteService = new MuteService(this, repository);
        kickService = new KickService(repository);
        warnService = new WarnService(this, repository);
        noteService = new NoteService(repository);
        ignoreService = new IgnoreService();
        privateMessageService = new PrivateMessageService(ignoreService);
        syncService = new SyncService(this, repository, banService, muteService, kickService);

        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        AdminCommandRegistrar.register(this);
        syncService.start();
    }

    @Override
    public void onDisable() {
        if (syncService != null) {
            syncService.stop();
        }
        if (repository != null) {
            repository.close();
        }
    }

    public void reloadLocalConfig() {
        reloadConfig();
        pluginConfig = PluginConfig.from(getConfig());
        if (messageService == null) {
            messageService = new MessageService(this);
        } else {
            messageService.reload();
        }
    }

    public static AdminPlugin getInstance() {
        return instance;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public DataRepository getRepository() {
        return repository;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public PlayerService getPlayerService() {
        return playerService;
    }

    public BanService getBanService() {
        return banService;
    }

    public MuteService getMuteService() {
        return muteService;
    }

    public KickService getKickService() {
        return kickService;
    }

    public WarnService getWarnService() {
        return warnService;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    public IgnoreService getIgnoreService() {
        return ignoreService;
    }

    public PrivateMessageService getPrivateMessageService() {
        return privateMessageService;
    }
}
