package com.instituto.tardanzas.dao;

import com.instituto.tardanzas.config.CryptoUtil;
import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.model.Configuracion;

import java.nio.charset.StandardCharsets;
import java.sql.*;

public class ConfiguracionDAO {

    public void guardar(Configuracion cfg) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_guardar_configuracion(?,?,?,?,?,?)}")) {

            cs.setString(1, cfg.getSmtpHost());
            cs.setInt   (2, cfg.getSmtpPuerto());
            cs.setString(3, cfg.getSmtpUsuario());

            String cifrado;
            try {
                cifrado = CryptoUtil.cifrar(cfg.getSmtpPassword());
            } catch (Exception e) {
                throw new SQLException(
                    "No se pudo cifrar la contrasena SMTP: "
                    + e.getMessage(), e);
            }
            cs.setBytes(4, cifrado.getBytes(StandardCharsets.UTF_8));

            cs.setString(5, cfg.getEmailRemitente());
            cs.setString(6, cfg.getNombreRemitente());
            cs.execute();
        }
    }

    public Configuracion obtener() throws SQLException {
        try (Connection con = DatabaseConfig.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL sp_obtener_configuracion()}");
             ResultSet rs = cs.executeQuery()) {

            if (rs.next()) {
                Configuracion cfg = new Configuracion();
                cfg.setSmtpHost       (rs.getString("smtp_host"));
                cfg.setSmtpPuerto     (rs.getInt("smtp_puerto"));
                cfg.setSmtpUsuario    (rs.getString("smtp_usuario"));

                byte[] passBytes = rs.getBytes("smtp_password");
                String pass = "";
                if (passBytes != null && passBytes.length > 0) {
                    String crudo = new String(
                            passBytes, StandardCharsets.UTF_8);
                    try {
                        pass = CryptoUtil.descifrar(crudo);
                    } catch (Exception e) {
                        // Dato antiguo guardado sin cifrar:
                        // se mantiene tal cual hasta que el usuario
                        // vuelva a guardar la configuración.
                        pass = crudo;
                    }
                }
                cfg.setSmtpPassword(pass);

                cfg.setEmailRemitente (rs.getString("email_remitente"));
                cfg.setNombreRemitente(rs.getString("nombre_remitente"));
                return cfg;
            }
        }
        return null;
    }
}
