package fr.alex96x2.admin.service;

import java.util.*;

public class IgnoreService {

    private final Map<UUID, Set<UUID>> ignored = new HashMap<>();

    public void ignore(UUID player, UUID target) {
        ignored.computeIfAbsent(player, k -> new HashSet<>()).add(target);
    }

    public void unignore(UUID player, UUID target) {
        Set<UUID> set = ignored.get(player);
        if (set != null) {
            set.remove(target);
        }
    }

    public boolean isIgnored(UUID player, UUID target) {
        Set<UUID> set = ignored.get(player);
        return set != null && set.contains(target);
    }

    public Set<UUID> getIgnored(UUID player) {
        return ignored.getOrDefault(player, Collections.emptySet());
    }
}
