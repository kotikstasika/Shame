package org.dimasik.shame.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final HikariDataSource dataSource;

    public DatabaseManager(String host, String database, String user, String password, int port) {
        this.dataSource = createDataSource(host, database, user, password, port);
        initializeDatabase();
    }

    public DatabaseManager(String host, String database, String user, String password) {
        this(host, database, user, password, 3306);
    }

    private HikariDataSource createDataSource(String host, String database, String user, String password, int port) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(true);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        return new HikariDataSource(config);
    }

    private void initializeDatabase() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String createDBSQL = "CREATE DATABASE IF NOT EXISTS " +
                    connection.getCatalog() + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            statement.execute(createDBSQL);

            statement.execute("USE " + connection.getCatalog());

            String createTableSQL = "CREATE TABLE IF NOT EXISTS nicknames (" +
                    "nickname VARCHAR(16) PRIMARY KEY NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public CompletableFuture<Boolean> addNickname(String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            if (nickname == null || nickname.length() > 16 || nickname.trim().isEmpty()) {
                return false;
            }

            String sql = "INSERT INTO nicknames (nickname) VALUES (?)";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, nickname.trim());
                statement.executeUpdate();
                return true;

            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    return false;
                }
                throw new RuntimeException("Failed to add nickname: " + nickname, e);
            }
        });
    }

    public CompletableFuture<Boolean> nicknameExists(String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            if (nickname == null || nickname.trim().isEmpty()) {
                return false;
            }

            String sql = "SELECT 1 FROM nicknames WHERE nickname = ?";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, nickname.trim());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to check nickname existence: " + nickname, e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteNickname(String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            if (nickname == null || nickname.trim().isEmpty()) {
                return false;
            }

            String sql = "DELETE FROM nicknames WHERE nickname = ?";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, nickname.trim());
                int affectedRows = statement.executeUpdate();
                return affectedRows > 0;

            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete nickname: " + nickname, e);
            }
        });
    }

    public CompletableFuture<Integer> getNicknameCount() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) as count FROM nicknames";

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                if (resultSet.next()) {
                    return resultSet.getInt("count");
                }
                return 0;

            } catch (SQLException e) {
                throw new RuntimeException("Failed to get nickname count", e);
            }
        });
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool closed");
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            shutdown();
        } finally {
            super.finalize();
        }
    }
}