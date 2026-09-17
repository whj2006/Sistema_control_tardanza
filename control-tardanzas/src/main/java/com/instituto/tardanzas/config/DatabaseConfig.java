package com.instituto.tardanzas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {

    private static HikariDataSource dataSource;

    // Ya NO hay static {} ni credenciales hardcodeadas

    /**
     * Inicializa el pool con los datos que le pasamos.
     * Se llama desde Main después de validar la conexión.
     */
    public static void inicializar(String url, String user, String password) {
        // Si ya había un pool abierto, cerrarlo primero
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Devuelve una conexión del pool.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("El pool de conexiones no está inicializado.");
        }
        return dataSource.getConnection();
    }

    /**
     * Cierra el pool al cerrar la aplicación.
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}