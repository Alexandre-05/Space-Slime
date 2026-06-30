package fr.alex96x2.admin.storage;

import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.database.DatabaseManager;

public final class StorageFactory {

    private StorageFactory() {}

    public static DataRepository create(AdminPlugin plugin) {
        StorageType configured = plugin.getPluginConfig().storageType();

        if (configured == StorageType.JSON) {
            plugin.getLogger().info("Mode stockage : JSON local (" + plugin.getPluginConfig().jsonFile() + ")");
            return new JsonRepository(plugin);
        }

        if (configured == StorageType.MARIADB) {
            return connectMariaDb(plugin);
        }

        // AUTO
        DataRepository maria = tryMariaDb(plugin);
        if (maria != null) {
            return maria;
        }
        plugin.getLogger().warning("MariaDB indisponible — bascule vers le stockage JSON local.");
        return new JsonRepository(plugin);
    }

    private static DataRepository connectMariaDb(AdminPlugin plugin) {
        DataRepository maria = tryMariaDb(plugin);
        if (maria != null) {
            return maria;
        }
        plugin.getLogger().warning("MariaDB indisponible — bascule vers le stockage JSON local.");
        return new JsonRepository(plugin);
    }

    private static DataRepository tryMariaDb(AdminPlugin plugin) {
        try {
            DatabaseManager db = new DatabaseManager(plugin);
            if (db.connect()) {
                plugin.getLogger().info("Mode stockage : MariaDB");
                return new MariaDbRepository(plugin, db);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur initialisation MariaDB : " + e.getMessage());
        }
        return null;
    }
}
