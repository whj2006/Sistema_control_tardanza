package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.model.ConfiguracionHorarios;

import java.sql.*;

public class ConfiguracionHorariosDAO {

    // ── Obtener ───────────────────────────────────────────────────────────

    public ConfiguracionHorarios obtener() throws Exception {
        String sql = "SELECT hora_entrada "
                   + "FROM configuracion_horarios "
                   + "WHERE id = 1";

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                ConfiguracionHorarios h =
                        new ConfiguracionHorarios();
                h.setId(1);
                h.setHoraEntrada(
                        rs.getString("hora_entrada"));
                return h;
            }

            // Sin fila → valor por defecto
            return new ConfiguracionHorarios("08:00");
        }
    }

    // ── Guardar (upsert) ──────────────────────────────────────────────────

    public void guardar(ConfiguracionHorarios h)
            throws Exception {
        String sql =
            "INSERT INTO configuracion_horarios "
          + "    (id, hora_entrada) "
          + "VALUES (1, ?) "
          + "ON DUPLICATE KEY UPDATE "
          + "    hora_entrada = VALUES(hora_entrada)";

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(sql)) {
            ps.setString(1, h.getHoraEntrada());
            ps.executeUpdate();
        }
    }
}