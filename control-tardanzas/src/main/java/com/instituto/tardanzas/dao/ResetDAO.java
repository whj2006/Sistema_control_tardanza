package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ResetDAO {

    /**
     * Borra todos los correos, retrasos, alumnos, cursos y destinatarios.
     * Usa TRUNCATE para resetear también los AUTO_INCREMENT.
     */
    public void resetearTodo() throws Exception {
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement()) {

            // Desactivar checks de foreign keys
            st.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Truncar todas las tablas (resetea también los AUTO_INCREMENT)
            st.executeUpdate("TRUNCATE TABLE correo");
            st.executeUpdate("TRUNCATE TABLE retraso");
            st.executeUpdate("TRUNCATE TABLE alumno");
            st.executeUpdate("TRUNCATE TABLE curso");
            st.executeUpdate("TRUNCATE TABLE destinatario_correo");

            // Reactivar checks
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    /**
     * Cuenta los registros para mostrar antes de borrar.
     * Devuelve un array: [cursos, alumnos, retrasos, correos, destinatarios]
     */
    public int[] contarRegistros() throws Exception {
        int[] conteos = new int[5];
        String[] tablas = {
            "curso", "alumno", "retraso",
            "correo", "destinatario_correo"
        };

        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement()) {

            for (int i = 0; i < tablas.length; i++) {
                try (ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM " + tablas[i])) {
                    if (rs.next()) conteos[i] = rs.getInt(1);
                }
            }
        }
        return conteos;
    }
}