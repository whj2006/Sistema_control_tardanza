package com.instituto.tardanzas.config;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConfigDB {

    private static final String ARCHIVO = "config.properties";

    /**
     * Busca config.properties en varios sitios (para que funcione tanto en JAR como en EXE):
     * 1. Directorio de trabajo actual (donde se lanza el EXE/JAR)
     * 2. Directorio donde está el JAR/EXE (para cuando se hace doble clic)
     * 3. Carpeta del usuario (fallback)
     */
    private static File buscarArchivoExistente() {
        // 1. Working dir
        File f1 = new File(ARCHIVO);
        if (f1.exists()) return f1;

        // 2. Directorio del JAR/EXE
        try {
            String jarPath = ConfigDB.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            File jarDir = jarFile.isFile() ? jarFile.getParentFile() : jarFile;
            if (jarDir != null) {
                File f2 = new File(jarDir, ARCHIVO);
                if (f2.exists()) return f2;
            }
        } catch (Exception ignored) {}

        // 3. No existe en ningún sitio
        return null;
    }

    private static File obtenerArchivoParaGuardar() {
        // Intentar guardar junto al JAR/EXE primero (mejor para EXE)
        try {
            String jarPath = ConfigDB.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            File jarDir = jarFile.isFile() ? jarFile.getParentFile() : jarFile;
            if (jarDir != null && jarDir.canWrite()) {
                return new File(jarDir, ARCHIVO);
            }
        } catch (Exception ignored) {}

        // Fallback: working dir
        return new File(ARCHIVO);
    }

    /**
     * Carga el archivo config.properties si existe.
     * Devuelve un Properties vacío si no existe.
     */
    public static Properties cargar() {
        Properties props = new Properties();
        File archivo = buscarArchivoExistente();

        if (archivo == null) {
            // También probar working dir por si acaso
            archivo = new File(ARCHIVO);
        }

        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                props.load(fis);
                System.out.println("[ConfigDB] Cargado desde: " + archivo.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error leyendo config.properties: " + e.getMessage());
            }
        } else {
            System.out.println("[ConfigDB] No se encontró config.properties, se pedirá configuración");
        }

        return props;
    }

    /**
     * Guarda las credenciales en config.properties.
     * Guarda tanto en el directorio del EXE/JAR como en working dir para asegurar que el EXE lo encuentre.
     */
    public static void guardar(Properties props) {
        File destinoPrincipal = obtenerArchivoParaGuardar();
        try (FileOutputStream fos = new FileOutputStream(destinoPrincipal)) {
            props.store(fos, "Configuracion de base de datos - Control Tardanzas");
            System.out.println("[ConfigDB] Guardado en: " + destinoPrincipal.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error guardando config.properties: " + e.getMessage());
        }

        // También guardar en working dir si es diferente (para compatibilidad)
        File workingDirFile = new File(ARCHIVO);
        if (!destinoPrincipal.getAbsolutePath().equals(workingDirFile.getAbsolutePath())) {
            try (FileOutputStream fos = new FileOutputStream(workingDirFile)) {
                props.store(fos, "Configuracion de base de datos - Control Tardanzas");
            } catch (IOException ignored) {}
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
