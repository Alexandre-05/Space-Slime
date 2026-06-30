package fr.alex96x2.admin.service;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.storage.DataRepository;
import org.bukkit.Bukkit;

import java.util.UUID;

public class NoteService {

    private final AdminPlugin plugin;
    private final DataRepository repository;

    public NoteService(DataRepository repository) {
        this.plugin = AdminPlugin.getInstance();
        this.repository = repository;
    }

    public void addNote(UUID target, UUID staffUuid, String staffName, String content, Runnable onComplete) {
        repository.runAsync(() -> repository.insertNote(target, staffName, content))
                .thenRun(() -> {
                    if (onComplete != null) Bukkit.getScheduler().runTask(plugin, onComplete);
                });
    }
}
