package com.instituto.tardanzas.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseBootstrapTest {

    @Test
    void elEsquemaIncluidoContieneTodosLosObjetosEsperados() throws IOException {
        List<String> sentencias = DatabaseBootstrap.dividirScript(leerEsquema());

        assertEquals(8, contar(sentencias, "CREATE TABLE IF NOT EXISTS"));
        assertEquals(5, contar(sentencias, "CREATE OR REPLACE VIEW"));
        assertEquals(18, contar(sentencias, "CREATE PROCEDURE"));
        assertEquals(18, contar(sentencias, "DROP PROCEDURE IF EXISTS"));
        assertFalse(sentencias.stream().anyMatch(sql -> sql.startsWith("CREATE DATABASE")));
        assertFalse(sentencias.stream().anyMatch(sql -> sql.startsWith("USE ")));
    }

    @Test
    void elSeparadorNoDivideElCuerpoDeLosProcedimientos() throws IOException {
        List<String> sentencias = DatabaseBootstrap.dividirScript(leerEsquema());
        List<String> procedimientos = sentencias.stream()
                .filter(sql -> sql.startsWith("CREATE PROCEDURE"))
                .toList();

        assertEquals(18, procedimientos.size());
        assertTrue(procedimientos.stream().allMatch(sql ->
                sql.contains("BEGIN") && sql.contains("END")));
        assertTrue(procedimientos.stream().anyMatch(sql ->
                sql.contains("INSERT INTO retraso") && sql.contains("COMMIT;")));
    }

    private static String leerEsquema() throws IOException {
        try (InputStream input = DatabaseBootstrapTest.class
                .getResourceAsStream("/db/sql_completo.sql")) {
            if (input == null) {
                throw new IOException("No se encontró db/sql_completo.sql en el classpath.");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static long contar(List<String> sentencias, String inicio) {
        return sentencias.stream().filter(sql -> sql.startsWith(inicio)).count();
    }
}
