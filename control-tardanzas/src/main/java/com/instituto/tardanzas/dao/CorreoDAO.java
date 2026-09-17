package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.model.Correo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CorreoDAO {

    // ── Obtener correos pendientes ────────────────────────────────────────

    public List<Correo> obtenerPendientes()
            throws Exception {
        List<Correo> lista = new ArrayList<>();
        String sql =
            "SELECT id, id_retraso, email_destino, "
          + "       asunto, mensaje "
          + "FROM correo "
          + "WHERE estado = 'PENDIENTE' "
          + "ORDER BY fecha_creacion ASC";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ── Marcar como enviado ───────────────────────────────────────────────

    public void marcarEnviado(int id) throws Exception {
        String sql =
            "UPDATE correo "
          + "SET estado = 'ENVIADO', "
          + "    fecha_envio = NOW(), "
          + "    error_envio = NULL "
          + "WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Marcar como error ─────────────────────────────────────────────────

    public void marcarError(int id, String error)
            throws Exception {
        String sql =
            "UPDATE correo "
          + "SET estado = 'ERROR', "
          + "    error_envio = ? "
          + "WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            ps.setString(1, error);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ── Reintentar todos en error ─────────────────────────────────────────

    public int reintentarTodosEnError()
            throws Exception {
        String sql =
            "UPDATE correo "
          + "SET estado = 'PENDIENTE', "
          + "    error_envio = NULL "
          + "WHERE estado = 'ERROR'";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    // ── Obtener todos (para panel Correos) ────────────────────────────────

    public List<Correo> obtenerTodos()
            throws Exception {
        List<Correo> lista = new ArrayList<>();
        String sql =
            "SELECT c.id, c.id_retraso, "
          + "       c.email_destino, c.asunto, "
          + "       c.mensaje, c.estado, "
          + "       c.fecha_creacion, c.fecha_envio, "
          + "       c.error_envio, "
          + "       CONCAT(a.nombre, ' ', a.apellido1, "
          + "              ' ', a.apellido2) "
          + "           AS nombre_alumno "
          + "FROM correo c "
          + "JOIN retraso r ON c.id_retraso = r.id "
          + "JOIN alumno  a ON r.nia        = a.nia "
          + "ORDER BY c.fecha_creacion DESC";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Correo correo = mapear(rs);
                correo.setEstado(
                        rs.getString("estado"));
                correo.setNombreAlumno(
                        rs.getString("nombre_alumno"));
                Timestamp fc =
                        rs.getTimestamp("fecha_creacion");
                if (fc != null)
                    correo.setFechaCreacion(
                            fc.toLocalDateTime());
                Timestamp fe =
                        rs.getTimestamp("fecha_envio");
                if (fe != null)
                    correo.setFechaEnvio(
                            fe.toLocalDateTime());
                correo.setErrorEnvio(
                        rs.getString("error_envio"));
                lista.add(correo);
            }
        }
        return lista;
    }

    // ── Obtener por estado ────────────────────────────────────────────────

    public List<Correo> obtenerPorEstado(String estado)
            throws Exception {
        if ("TODOS".equals(estado))
            return obtenerTodos();

        List<Correo> lista = new ArrayList<>();
        String sql =
            "SELECT c.id, c.id_retraso, "
          + "       c.email_destino, c.asunto, "
          + "       c.mensaje, c.estado, "
          + "       c.fecha_creacion, c.fecha_envio, "
          + "       c.error_envio, "
          + "       CONCAT(a.nombre, ' ', a.apellido1, "
          + "              ' ', a.apellido2) "
          + "           AS nombre_alumno "
          + "FROM correo c "
          + "JOIN retraso r ON c.id_retraso = r.id "
          + "JOIN alumno  a ON r.nia        = a.nia "
          + "WHERE c.estado = ? "
          + "ORDER BY c.fecha_creacion DESC";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Correo correo = mapear(rs);
                    correo.setEstado(
                            rs.getString("estado"));
                    correo.setNombreAlumno(
                            rs.getString("nombre_alumno"));
                    Timestamp fc =
                            rs.getTimestamp(
                                    "fecha_creacion");
                    if (fc != null)
                        correo.setFechaCreacion(
                                fc.toLocalDateTime());
                    Timestamp fe =
                            rs.getTimestamp("fecha_envio");
                    if (fe != null)
                        correo.setFechaEnvio(
                                fe.toLocalDateTime());
                    correo.setErrorEnvio(
                            rs.getString("error_envio"));
                    lista.add(correo);
                }
            }
        }
        return lista;
    }

    // ── Contar por estado ─────────────────────────────────────────────────

    public int contarPorEstado(String estado)
            throws Exception {
        String sql =
            "SELECT COUNT(*) FROM correo "
          + "WHERE estado = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ── Reintentar un correo concreto ─────────────────────────────────────

    public void reintentar(int id) throws Exception {
        String sql =
            "UPDATE correo "
          + "SET estado = 'PENDIENTE', "
          + "    error_envio = NULL "
          + "WHERE id = ? "
          + "  AND estado = 'ERROR'";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Obtener estado del correo por id de retraso ───────────────────────
    // NUEVO: usado en FicharPanel para mostrar
    // el estado en la tabla de historial de hoy

    public String obtenerEstadoPorRetraso(int idRetraso)
            throws Exception {
        String sql =
            "SELECT estado FROM correo "
          + "WHERE id_retraso = ? "
          + "ORDER BY fecha_creacion DESC "
          + "LIMIT 1";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {
            ps.setInt(1, idRetraso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("estado");
            }
        }
        // Si no hay correo asociado devuelve PENDIENTE
        return "PENDIENTE";
    }

    // ── Helper mapper ─────────────────────────────────────────────────────

    private Correo mapear(ResultSet rs)
            throws SQLException {
        Correo c = new Correo();
        c.setId(rs.getInt("id"));
        c.setIdRetraso(rs.getInt("id_retraso"));
        c.setEmailFamilia(rs.getString("email_destino"));
        c.setAsunto(rs.getString("asunto"));
        c.setMensaje(rs.getString("mensaje"));
        return c;
    }
}