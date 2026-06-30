package fr.alex96x2.slimecapture;

import fr.alex96x2.slimecapture.command.SlimeGunCommand;
import fr.alex96x2.slimecapture.config.SlimeCaptureConfig;
import fr.alex96x2.slimecapture.listener.SlimeMachineListener;
import fr.alex96x2.slimecapture.listener.VacuumListener;
import fr.alex96x2.slimecapture.machine.SlimeMachineManager;
import fr.alex96x2.slimecapture.service.ActionBarService;
import fr.alex96x2.slimecapture.service.MessageService;
import fr.alex96x2.slimecapture.service.SlimeStorageService;
import fr.alex96x2.slimecapture.service.VacuumService;
import org.bukkit.plugin.java.JavaPlugin;

public final class SlimeCapturePlugin extends JavaPlugin {

    private static SlimeCapturePlugin instance;

    private SlimeCaptureConfig pluginConfig;
    private MessageService messageService;
    private ActionBarService actionBarService;
    private VacuumService vacuumService;
    private SlimeStorageService slimeStorageService;
    private SlimeMachineManager machineManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        pluginConfig = new SlimeCaptureConfig(getConfig());
        messageService = new MessageService(this);
        actionBarService = new ActionBarService(this, messageService);
        vacuumService = new VacuumService(this);
        slimeStorageService = new SlimeStorageService(this);
        machineManager = new SlimeMachineManager(this);

        getServer().getPluginManager().registerEvents(new VacuumListener(this), this);
        getServer().getPluginManager().registerEvents(new SlimeMachineListener(this), this);
        var command = getCommand("slimecapture");
        if (command != null) {
            SlimeGunCommand executor = new SlimeGunCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("SlimeCapture activé — bonne chasse aux slimes !");
    }

    @Override
    public void onDisable() {
        vacuumService.shutdown();
        actionBarService.shutdown();
        if (machineManager != null) {
            machineManager.shutdown();
        }
        instance = null;
    }

    public void reloadPlugin() {
        reloadConfig();
        pluginConfig = new SlimeCaptureConfig(getConfig());
        messageService.reload();
    }

    public static SlimeCapturePlugin getInstance() {
        return instance;
    }

    public SlimeCaptureConfig getPluginConfig() {
        return pluginConfig;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public ActionBarService getActionBarService() {
        return actionBarService;
    }

    public VacuumService getVacuumService() {
        return vacuumService;
    }

    public SlimeStorageService getSlimeStorageService() {
        return slimeStorageService;
    }

    public SlimeMachineManager getMachineManager() {
        return machineManager;
    }
}
