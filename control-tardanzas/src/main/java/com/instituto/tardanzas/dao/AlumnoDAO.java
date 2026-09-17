package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.model.Alumno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO {

    // ── Importar / Actualizar alumno ─────────────────────────────────────

    public void importarActualizar(Alumno a) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_importar_actualizar_alumno(?,?,?,?,?,?,?)}")) {

            cs.setString(1, a.getNia());
            cs.setString(2, a.getNombre());
            cs.setString(3, a.getApellido1());
            cs.setString(4, a.getApellido2());
            setStringOrNull(cs, 5, a.getEmailFamilia1());
            setStringOrNull(cs, 6, a.getEmailFamilia2());
            cs.setInt   (7, a.getIdCurso());
            cs.execute();
        }
    }

    // ── Editar alumno ─────────────────────────────────────────────────────

    public void editar(Alumno a) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_editar_alumno(?,?,?,?,?,?,?)}")) {

            cs.setString(1, a.getNia());
            cs.setString(2, a.getNombre());
            cs.setString(3, a.getApellido1());
            cs.setString(4, a.getApellido2());
            setStringOrNull(cs, 5, a.getEmailFamilia1());
            setStringOrNull(cs, 6, a.getEmailFamilia2());
            cs.setInt   (7, a.getIdCurso());
            cs.execute();
        }
    }

    // ── Listar alumnos de un curso ────────────────────────────────────────

    public List<Alumno> listarPorCurso(int idCurso) throws SQLException {
        List<Alumno> lista = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_listar_alumnos_curso(?)}")) {

            cs.setInt(1, idCurso);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Alumno a = new Alumno();
                    a.setNia          (rs.getString("nia"));
                    a.setNombre       (rs.getString("nombre"));
                    a.setApellido1    (rs.getString("apellido1"));
                    a.setApellido2    (rs.getString("apellido2"));
                    a.setEmailFamilia1(rs.getString("email_familia1"));
                    a.setEmailFamilia2(rs.getString("email_familia2"));
                    a.setIdCurso      (idCurso);
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    // ── Buscar alumno por NIA ─────────────────────────────────────────────

    public Alumno buscarPorNia(String nia) throws SQLException {
        String sql = """
                SELECT a.nia, a.nombre, a.apellido1, a.apellido2,
                       a.email_familia1, a.email_familia2,
                       a.id_curso, c.nombre AS nombre_curso
                FROM alumno a
                JOIN curso c ON a.id_curso = c.id
                WHERE a.nia = ?
                """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Alumno a = new Alumno();
                    a.setNia          (rs.getString("nia"));
                    a.setNombre       (rs.getString("nombre"));
                    a.setApellido1    (rs.getString("apellido1"));
                    a.setApellido2    (rs.getString("apellido2"));
                    a.setEmailFamilia1(rs.getString("email_familia1"));
                    a.setEmailFamilia2(rs.getString("email_familia2"));
                    a.setIdCurso      (rs.getInt("id_curso"));
                    a.setNombreCurso  (rs.getString("nombre_curso"));
                    return a;
                }
            }
        }
        return null;
    }

    // ── Listar todos los alumnos ──────────────────────────────────────────

    public List<Alumno> listarTodos() throws SQLException {
        List<Alumno> lista = new ArrayList<>();
        String sql = """
                SELECT a.nia, a.nombre, a.apellido1, a.apellido2,
                       a.email_familia1, a.email_familia2,
                       a.id_curso, c.nombre AS nombre_curso
                FROM alumno a
                JOIN curso c ON a.id_curso = c.id
                ORDER BY a.apellido1, a.apellido2, a.nombre
                """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Alumno a = new Alumno();
                a.setNia          (rs.getString("nia"));
                a.setNombre       (rs.getString("nombre"));
                a.setApellido1    (rs.getString("apellido1"));
                a.setApellido2    (rs.getString("apellido2"));
                a.setEmailFamilia1(rs.getString("email_familia1"));
                a.setEmailFamilia2(rs.getString("email_familia2"));
                a.setIdCurso      (rs.getInt("id_curso"));
                a.setNombreCurso  (rs.getString("nombre_curso"));
                lista.add(a);
            }
        }
        return lista;
    }

    // ── Eliminar alumno ───────────────────────────────────────────────────

    public void eliminar(String nia) throws SQLException {
        String sql = "DELETE FROM alumno WHERE nia = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nia);
            ps.executeUpdate();
        }
    }

    // ── Helper: setString o setNull si está vacío ────────────────────────

    private void setStringOrNull(CallableStatement cs, int index, String value)
            throws SQLException {
        if (value == null || value.isBlank()) {
            cs.setNull(index, Types.VARCHAR);
        } else {
            cs.setString(index, value.trim());
        }
    }
}