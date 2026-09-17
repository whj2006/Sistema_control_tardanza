package com.instituto.tardanzas.config;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConfigDB {

    private static final String ARCHIVO = "config.properties";

    /**
     * Carga el archivo config.properties si existe.
     * Devuelve un Properties vacío si no existe.
     */
    public static Properties cargar() {
        Properties props = new Properties();
        File archivo = new File(ARCHIVO);

        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error leyendo config.properties: " + e.getMessage());
            }
        }

        return props;
    }

    /**
     * Guarda las credenciales en config.properties.
     */
    public static void guardar(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(ARCHIVO)) {
            props.store(fos, "Configuracion de base de datos - Control Tardanzas");
        } catch (IOException e) {
            System.err.println("Error guardando config.properties: " + e.getMessage());
        }
    }

    /**
     * Intenta conectar con los datos del Properties.
     * Lanza excepción si falla.
     */
    public static void probarConexion(Properties props) throws Exception {
        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        // Prueba rápida con JDBC puro (sin HikariCP todavía)
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Si llega aquí, la conexión es correcta
        }
    }
}
