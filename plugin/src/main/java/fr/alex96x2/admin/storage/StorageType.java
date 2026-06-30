package fr.alex96x2.admin.storage;

public enum StorageType {
    AUTO,
    MARIADB,
    JSON;

    public static StorageType from(String value) {
        if (value == null) return AUTO;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AUTO;
        }
    }
}
