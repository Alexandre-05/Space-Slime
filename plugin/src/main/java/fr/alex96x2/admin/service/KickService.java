package fr.alex96x2.admin.service;

import fr.alex96x2.admin.storage.DataRepository;

import java.util.UUID;

public class KickService {

    private final DataRepository repository;

    public KickService(DataRepository repository) {
        this.repository = repository;
    }

    public void kick(UUID target, UUID staffUuid, String staffName, String reason, String source) {
        repository.runAsync(() -> repository.insertKick(target, staffUuid, staffName, reason, source));
    }

    public int countHistory(UUID uuid) {
        return repository.countKicks(uuid);
    }
}
