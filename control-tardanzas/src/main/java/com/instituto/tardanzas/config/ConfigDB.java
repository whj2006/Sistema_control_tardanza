package com.instituto.tardanzas.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

/**
 * Carga y guarda la conexión a la base de datos en el perfil del usuario.
 * No se escriben credenciales en la carpeta de instalación de la aplicación.
 */
public final class ConfigDB {

    private static final String NOMBRE_ARCHIVO = "config.properties";
    private static final String CARPETA_APP = "ControlTardanzas";

    private ConfigDB() {
    }

    /** Devuelve un Properties vacío si aún no existe una configuración. */
    public static Properties cargar() {
        Properties props = new Properties();
        Path archivo = rutaConfiguracion();

        if (Files.isRegularFile(archivo)) {
            try (InputStream input = Files.newInputStream(archivo)) {
                props.load(input);
                return props;
            } catch (IOException e) {
                System.err.println("No se pudo leer " + archivo + ": " + e.getMessage());
            }
        }

        // Migra configuraciones creadas por versiones anteriores, que las
        // guardaban en el directorio de trabajo o junto al ejecutable.
        for (Path antigua : rutasAntiguas()) {
            if (!antigua.equals(archivo) && Files.isRegularFile(antigua)) {
                try (InputStream input = Files.newInputStream(antigua)) {
                    props.load(input);
                    guardar(props);
                    System.out.println("Configuración migrada a " + archivo);
                    return props;
                } catch (IOException e) {
                    System.err.println("No se pudo migrar " + antigua + ": " + e.getMessage());
                    return new Properties();
                }
            }
        }

        return props;
    }

    /** Guarda las credenciales en el perfil del usuario actual. */
    public static void guardar(Properties props) throws IOException {
        Path archivo = rutaConfiguracion();
        Files.createDirectories(archivo.getParent());
        try (OutputStream output = Files.newOutputStream(archivo)) {
            props.store(output, "Control de Tardanzas - configuración local de base de datos");
        }

        // En sistemas POSIX limita el archivo a lectura/escritura del usuario.
        // En Windows se heredan los permisos privados del perfil de usuario.
        try {
            Files.setPosixFilePermissions(archivo, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
            // El proveedor del sistema de archivos no ofrece permisos POSIX.
        }
    }

    /** Ruta de configuración por usuario, escribible sin privilegios de administrador. */
    public static Path rutaConfiguracion() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Path.of(appData, CARPETA_APP, NOMBRE_ARCHIVO);
            }
        } else if (os.contains("mac")) {
            return Path.of(System.getProperty("user.home"), "Library",
                    "Application Support", CARPETA_APP, NOMBRE_ARCHIVO);
        }
        return Path.of(System.getProperty("user.home"), ".config",
                "control-tardanzas", NOMBRE_ARCHIVO);
    }

    private static List<Path> rutasAntiguas() {
        Path directorioActual = Path.of(System.getProperty("user.dir", "."));
        Path directorioAplicacion = directorioAplicacion();
        List<Path> candidatas = new ArrayList<>();
        candidatas.add(directorioActual.resolve(NOMBRE_ARCHIVO));
        candidatas.add(directorioActual.resolve("..").resolve(NOMBRE_ARCHIVO).normalize());
        candidatas.add(directorioAplicacion.resolve(NOMBRE_ARCHIVO));

        // En una aplicación empaquetada, las clases suelen estar bajo app/ y
        // el EXE antiguo guardaba config.properties en el directorio padre.
        Path padreAplicacion = directorioAplicacion.getParent();
        if (padreAplicacion != null) {
            candidatas.add(padreAplicacion.resolve(NOMBRE_ARCHIVO));
        }
        return candidatas;
    }

    private static Path directorioAplicacion() {
        try {
            Path ubicacion = Path.of(ConfigDB.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return Files.isDirectory(ubicacion) ? ubicacion : ubicacion.getParent();
        } catch (Exception ignored) {
            return Path.of(System.getProperty("user.dir", "."));
        }
    }

    /** Prueba la conexión configurada y lanza la excepción JDBC si falla. */
    public static void probarConexion(Properties props) throws Exception {
        String url = props.getProperty("db.url");
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Falta db.url en la configuración.");
        }

        try (java.sql.Connection ignored = java.sql.DriverManager.getConnection(
                url,
                props.getProperty("db.user", ""),
                props.getProperty("db.password", ""))) {
            // La apertura de la conexión es la comprobación.
        }
    }
}
