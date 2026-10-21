package com.maelespecieros.backend.config;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración de alto rendimiento y concurrencia para SQLite.
 * Activa modo WAL (Write-Ahead Logging), synchronous NORMAL y busy_timeout
 * para evitar bloqueos (SQLITE_BUSY) durante lecturas y escrituras simultáneas.
 */
@Component
@Order(-1)
@Slf4j
public class SqlitePerformanceConfig implements CommandLineRunner {

    private final DataSource dataSource;

    public SqlitePerformanceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
            stmt.execute("PRAGMA synchronous = NORMAL;");
            stmt.execute("PRAGMA temp_store = MEMORY;");

            log.info("SQLite optimizado con éxito: WAL mode, busy_timeout=5000ms, synchronous=NORMAL, temp_store=MEMORY.");
        } catch (Exception e) {
            log.warn("No se pudieron aplicar todas las directivas PRAGMA de SQLite: {}", e.getMessage());
        }
    }
}
