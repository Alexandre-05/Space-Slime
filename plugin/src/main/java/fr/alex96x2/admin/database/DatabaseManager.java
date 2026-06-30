package fr.alex96x2.admin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.alex96x2.admin.AdminPlugin;
import fr.alex96x2.admin.config.PluginConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;

public class DatabaseManager {

    private static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";

    private final AdminPlugin plugin;
    private HikariDataSource dataSource;
    private ExecutorService executor;

    public DatabaseManager(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        PluginConfig config = plugin.getPluginConfig();
        try {
            Class.forName(MARIADB_DRIVER);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Driver MariaDB absent du JAR — utilisez le build shadow ou le mode storage JSON.");
            plugin.getLogger().log(Level.FINE, "ClassNotFoundException", e);
            return false;
        }

        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName(MARIADB_DRIVER);
        hikari.setJdbcUrl("jdbc:mariadb://" + config.dbHost() + ":" + config.dbPort() + "/" + config.dbName()
                + "?useUnicode=true&characterEncoding=utf8&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true");
        hikari.setConnectionInitSql("SET time_zone = '+00:00'");
        hikari.setUsername(config.dbUser());
        hikari.setPassword(config.dbPassword());
        hikari.setMaximumPoolSize(config.poolSize());
        hikari.setPoolName("AdminPlugin-Pool");
        hikari.setConnectionTimeout(5000);
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");

        try {
            dataSource = new HikariDataSource(hikari);
            try (Connection conn = dataSource.getConnection()) {
                // test connexion
            }
            executor = Executors.newFixedThreadPool(Math.max(2, config.poolSize() / 2));
            plugin.getLogger().info("Connexion MariaDB établie.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("MariaDB indisponible : " + e.getMessage());
            plugin.getLogger().log(Level.FINE, "Détail connexion MariaDB", e);
            closeQuietly();
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("MariaDB non connectée");
        }
        return dataSource.getConnection();
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    public void close() {
        closeQuietly();
    }

    private void closeQuietly() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
