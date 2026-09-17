package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.model.Curso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    public void crearSiNoExiste(String nombre) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_crear_curso_si_no_existe(?)}")) {
            cs.setString(1, nombre);
            cs.execute();
        }
    }

    public void editar(int id, String nuevoNombre) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_editar_curso(?,?)}")) {
            cs.setInt   (1, id);
            cs.setString(2, nuevoNombre);
            cs.execute();
        }
    }

    // ── Obtener ID por nombre ─────────────────────────────────────────────

    public int obtenerIdPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id FROM curso WHERE nombre = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        throw new SQLException("Curso no encontrado: " + nombre);
    }

    // ── Listar todos ──────────────────────────────────────────────────────

    public List<Curso> listarTodos() throws SQLException {
        List<Curso> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, total_alumnos FROM v_resumen_cursos";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Curso c = new Curso();
                c.setId          (rs.getInt("id"));
                c.setNombre      (rs.getString("nombre"));
                c.setTotalAlumnos(rs.getInt("total_alumnos"));
                lista.add(c);
            }
        }
        return lista;
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM curso WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}