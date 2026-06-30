package fr.alex96x2.admin.storage;

public record PendingAction(long id, String actionType, String targetUuid, String payload) {}
