package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinatarioDAO {

    // ── Obtener todos los correos ─────────────────────────────────────────
    public List<String[]> obtenerTodos() throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, correo FROM destinatario_correo ORDER BY id";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("correo")
                });
            }
        }
        return lista;
    }

    // ── Agregar correo ────────────────────────────────────────────────────
    public void agregar(String correo) throws SQLException {
        String sql = "INSERT INTO destinatario_correo (correo) VALUES (?)";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo.trim().toLowerCase());
            ps.executeUpdate();
        }
    }

    // ── Eliminar por id ───────────────────────────────────────────────────
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM destinatario_correo WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Obtener solo los correos (para enviar) ────────────────────────────
    public List<String> obtenerCorreos() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT correo FROM destinatario_correo ORDER BY id";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("correo"));
            }
        }
        return lista;
    }
}