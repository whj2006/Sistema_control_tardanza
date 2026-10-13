package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.config.PasswordUtil;
import com.instituto.tardanzas.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /**
     * Autentica un usuario. Las contraseñas se guardan exclusivamente
     * con hash PBKDF2 (formato "pbkdf2:..."). No se aceptan contraseñas
     * en texto plano ni en formatos antiguos (SHA-256): si el valor
     * almacenado no es un hash PBKDF2 válido, el login falla y el
     * administrador debe resetear la contraseña del usuario.
     *
     * Se usa una consulta SQL directa (no un procedimiento almacenado)
     * para que el login funcione también en bases de datos antiguas.
     */
    public Usuario autenticar(String username, String password)
            throws SQLException {
        int     id     = -1;
        String  rol    = null;
        boolean activo = false;
        String  hashAlmacenado = null;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, username, password, rol, activo "
                     + "FROM usuario WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id     = rs.getInt("id");
                    rol    = rs.getString("rol");
                    activo = rs.getBoolean("activo");
                    hashAlmacenado = rs.getString("password");
                }
            }
        }

        if (id == -1 || !activo || hashAlmacenado == null)
            return null;

        // Solo se acepta el formato seguro PBKDF2.
        if (!PasswordUtil.esHash(hashAlmacenado)
                || !PasswordUtil.verificar(password, hashAlmacenado))
            return null;

        return new Usuario(id, username, rol);
    }

    public int contarUsuarios() throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_contar_usuarios()}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_listar_usuarios()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setRol(rs.getString("rol"));
                u.setActivo(rs.getBoolean("activo"));
                u.setFechaCreacion(
                        rs.getTimestamp("fecha_creacion").toLocalDateTime());
                lista.add(u);
            }
        }
        return lista;
    }

    public void crear(String username, String password,
                      String rol) throws SQLException {
        String hash;
        try {
            hash = PasswordUtil.hash(password);
        } catch (Exception e) {
            throw new SQLException(
                "No se pudo cifrar la contrasena: "
                + e.getMessage(), e);
        }
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_crear_usuario(?, ?, ?)}")) {
            cs.setString(1, username);
            cs.setString(2, hash);
            cs.setString(3, rol);
            cs.execute();
        }
    }

    public void editar(int id, String username,
                       String rol, boolean activo) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_editar_usuario(?, ?, ?, ?)}")) {
            cs.setInt(1, id);
            cs.setString(2, username);
            cs.setString(3, rol);
            cs.setInt(4, activo ? 1 : 0);
            cs.execute();
        }
    }

    /**
     * Cambia la contraseña comprobando antes la actual (PBKDF2).
     */
    public void cambiarPassword(int id, String actual,
                                String nueva) throws SQLException {
        String almacenado = obtenerPassword(id);
        if (almacenado == null)
            throw new SQLException("El usuario no existe.");

        if (!PasswordUtil.esHash(almacenado)
                || !PasswordUtil.verificar(actual, almacenado))
            throw new SQLException("La contrasena actual no es correcta.");

        try {
            actualizarHash(id, PasswordUtil.hash(nueva));
        } catch (Exception e) {
            throw new SQLException(
                "No se pudo cifrar la contrasena: "
                + e.getMessage(), e);
        }
    }

    public void resetearPassword(int id, String nueva)
            throws SQLException {
        try {
            actualizarHash(id, PasswordUtil.hash(nueva));
        } catch (Exception e) {
            throw new SQLException(
                "No se pudo cifrar la contrasena: "
                + e.getMessage(), e);
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_eliminar_usuario(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String obtenerPassword(int id) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT password FROM usuario WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("password");
            }
        }
        return null;
    }

    private void actualizarHash(int id, String hash) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE usuario SET password = ? WHERE id = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
