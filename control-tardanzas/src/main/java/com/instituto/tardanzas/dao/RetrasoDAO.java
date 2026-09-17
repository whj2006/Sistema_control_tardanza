package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.model.RankingEntry;
import com.instituto.tardanzas.model.Retraso;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RetrasoDAO {

    // ── Fichar retraso ────────────────────────────────────────────────────

    public void ficharRetrasoAutomatico(String nia)
            throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_fichar_retraso_automatico(?)}")) {
            cs.setString(1, nia);
            cs.execute();
        }
    }

    // ── Eliminar retraso por error de fichaje ─────────────────────────────

    public void eliminarRetraso(int idRetraso)
            throws Exception {
        Connection con = DatabaseConfig.getConnection();
        try {
            con.setAutoCommit(false);

            // 1. Eliminar correos pendientes/error
            //    La tabla es "correo" (singular)
            //    La FK tiene ON DELETE CASCADE pero
            //    solo borramos los no enviados por
            //    si hay alguno ENVIADO que queremos
            //    conservar en el historial
            String sqlCorreo =
                "DELETE FROM correo "
              + "WHERE id_retraso = ? "
              + "  AND estado IN ('PENDIENTE', 'ERROR')";

            try (PreparedStatement ps =
                    con.prepareStatement(sqlCorreo)) {
                ps.setInt(1, idRetraso);
                ps.executeUpdate();
            }

            // 2. Eliminar el retraso
            //    La tabla es "retraso" (singular)
            String sqlRetraso =
                "DELETE FROM retraso WHERE id = ?";

            try (PreparedStatement ps =
                    con.prepareStatement(sqlRetraso)) {
                ps.setInt(1, idRetraso);
                int filas = ps.executeUpdate();
                if (filas == 0)
                    throw new Exception(
                        "No se encontro el retraso "
                        + "con id=" + idRetraso);
            }

            con.commit();

        } catch (Exception e) {
            try { con.rollback(); }
            catch (Exception ignored) {}
            throw e;
        } finally {
            try { con.setAutoCommit(true); }
            catch (Exception ignored) {}
            try { con.close(); }
            catch (Exception ignored) {}
        }
    }

    // ── Historial completo ────────────────────────────────────────────────

    public List<Retraso> historialCompleto()
            throws SQLException {
        return ejecutarVistaHistorial(
                "SELECT * FROM v_historial_completo "
              + "ORDER BY fecha_hora DESC");
    }

    // ── Historial de hoy ──────────────────────────────────────────────────

    public List<Retraso> historialHoy()
            throws SQLException {
        return ejecutarVistaHistorial(
                "SELECT * FROM v_historial_hoy "
              + "ORDER BY fecha_hora DESC");
    }

    // ── Historial por alumno ──────────────────────────────────────────────

    public List<Retraso> historialPorAlumno(String nia)
            throws SQLException {
        List<Retraso> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_historial_por_alumno(?)}")) {
            cs.setString(1, nia);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearHistorial(rs));
                }
            }
        }
        return lista;
    }

    // ── Historial por rango de fechas ─────────────────────────────────────

    public List<Retraso> historialPorFechas(
            LocalDate desde, LocalDate hasta)
            throws SQLException {
        List<Retraso> lista = new ArrayList<>();
        String sql =
            "SELECT r.id AS id_retraso, "
          + "       a.nia, "
          + "       r.fecha_hora, "
          + "       CONCAT(a.nombre, ' ', a.apellido1, "
          + "              ' ', a.apellido2) "
          + "           AS nombre_completo, "
          + "       c.nombre AS curso, "
          + "       r.minutos_tarde "
          + "FROM retraso r "
          + "JOIN alumno a ON r.nia      = a.nia "
          + "JOIN curso  c ON a.id_curso = c.id "
          + "WHERE DATE(r.fecha_hora) BETWEEN ? AND ? "
          + "ORDER BY r.fecha_hora DESC";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearHistorial(rs));
                }
            }
        }
        return lista;
    }

    // ── Contar retrasos de hoy ────────────────────────────────────────────

    public int contarRetrasosHoy() throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM v_historial_hoy";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Ranking ───────────────────────────────────────────────────────────

    public List<RankingEntry> rankingTardanzas()
            throws SQLException {
        List<RankingEntry> lista = new ArrayList<>();
        String sql =
            "SELECT nia, alumno, curso, "
          + "       total_retrasos, "
          + "       total_minutos_perdidos "
          + "FROM v_ranking_tardanzas";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RankingEntry e = new RankingEntry();
                e.setNia(rs.getString("nia"));
                e.setAlumno(rs.getString("alumno"));
                e.setCurso(rs.getString("curso"));
                e.setTotalRetrasos(
                        rs.getInt("total_retrasos"));
                e.setTotalMinutosPerdidos(
                        rs.getInt(
                            "total_minutos_perdidos"));
                lista.add(e);
            }
        }
        return lista;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private List<Retraso> ejecutarVistaHistorial(
            String sql) throws SQLException {
        List<Retraso> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearHistorial(rs));
            }
        }
        return lista;
    }

    private Retraso mapearHistorial(ResultSet rs)
            throws SQLException {
        Retraso r = new Retraso();
        r.setId(rs.getInt("id_retraso"));
        r.setNia(rs.getString("nia"));
        r.setFechaHora(
                rs.getTimestamp("fecha_hora")
                  .toLocalDateTime());
        r.setNombreCompleto(
                rs.getString("nombre_completo"));
        r.setCurso(rs.getString("curso"));
        r.setMinutosTarde(
                rs.getInt("minutos_tarde"));
        return r;
    }
}