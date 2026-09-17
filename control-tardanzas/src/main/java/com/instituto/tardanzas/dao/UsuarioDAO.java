package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String username, String password) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_autenticar_usuario(?, ?)}")) {
            cs.setString(1, username);
            cs.setString(2, password);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("rol")
                    );
                }
            }
        }
        return null;
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
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_crear_usuario(?, ?, ?)}")) {
            cs.setString(1, username);
            cs.setString(2, password);
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

    public void cambiarPassword(int id, String actual,
                                String nueva) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_cambiar_password(?, ?, ?)}")) {
            cs.setInt(1, id);
            cs.setString(2, actual);
            cs.setString(3, nueva);
            cs.execute();
        }
    }

    public void resetearPassword(int id, String nueva) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_resetear_password(?, ?)}")) {
            cs.setInt(1, id);
            cs.setString(2, nueva);
            cs.execute();
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
}