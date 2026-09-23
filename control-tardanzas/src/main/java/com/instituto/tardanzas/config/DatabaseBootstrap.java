package com.instituto.tardanzas.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Consumer;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Comprueba, crea y verifica el esquema de Control de Tardanzas. */
public final class DatabaseBootstrap {

    private static final String SCRIPT_INTERNO = "/db/sql_completo.sql";
    private static final String[] TABLAS = {
            "configuracion_horarios", "usuario", "destinatario_correo",
            "curso", "alumno", "retraso", "correo", "configuracion"
    };
    private static final String[] VISTAS = {
            "v_historial_completo", "v_historial_hoy", "v_ranking_tardanzas",
            "v_resumen_cursos", "v_estado_correos"
    };
    private static final String[] PROCEDIMIENTOS = {
            "sp_fichar_retraso_automatico", "sp_historial_por_alumno",
            "sp_importar_actualizar_alumno", "sp_crear_curso_si_no_existe",
            "sp_editar_alumno", "sp_editar_curso", "sp_listar_alumnos_curso",
            "sp_ver_estado_correos", "sp_guardar_configuracion",
            "sp_obtener_configuracion", "sp_autenticar_usuario",
            "sp_contar_usuarios", "sp_listar_usuarios", "sp_crear_usuario",
            "sp_editar_usuario", "sp_cambiar_password", "sp_resetear_password",
            "sp_eliminar_usuario"
    };

    private DatabaseBootstrap() {
    }

    /** Comprueba por TCP si el servidor indicado responde, sin validar credenciales. */
    public static boolean puertoDisponible(Properties props) {
        String host = props.getProperty("db.host", "localhost").trim();
        int puerto;
        try {
            puerto = Integer.parseInt(props.getProperty("db.puerto", "3306").trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (host.isBlank() || puerto < 1 || puerto > 65_535) {
            return false;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), 1_500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Devuelve true si están presentes las 8 tablas, 5 vistas y 18 procedimientos. */
    public static boolean esquemaCompleto(Properties props) {
        try (Connection conexion = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user", ""),
                props.getProperty("db.password", ""))) {
            String base = nombreBaseDatos(props);
            for (String tabla : TABLAS) {
                if (!existeObjeto(conexion, "BASE TABLE", tabla, base)) {
                    return false;
                }
            }
            for (String vista : VISTAS) {
                if (!existeObjeto(conexion, "VIEW", vista, base)) {
                    return false;
                }
            }
            for (String procedimiento : PROCEDIMIENTOS) {
                if (!existeProcedimiento(conexion, procedimiento, base)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Crea lo que falte y verifica el resultado. No borra los datos existentes. */
    public static void instalarEsquema(Properties props, Consumer<String> progreso)
            throws Exception {
        Consumer<String> log = progreso == null ? mensaje -> { } : progreso;
        String base = nombreBaseDatos(props);
        String usuario = props.getProperty("db.user", "");
        String clave = props.getProperty("db.password", "");

        log.accept("Conectando con MariaDB/MySQL…");
        try (Connection servidor = DriverManager.getConnection(
                urlServidor(props), usuario, clave);
             Statement sentencia = servidor.createStatement()) {
            log.accept("Creando la base de datos «" + base + "» si no existe…");
            sentencia.execute("CREATE DATABASE IF NOT EXISTS " + identificador(base)
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci");
        }

        List<String> sentencias = leerScriptInterno();
        log.accept("Preparando tablas, vistas y procedimientos…");
        int aplicadas = 0;
        int yaExistian = 0;
        try (Connection conexion = DriverManager.getConnection(
                props.getProperty("db.url"), usuario, clave);
             Statement sentencia = conexion.createStatement()) {
            for (String sql : sentencias) {
                try {
                    sentencia.execute(sql);
                    aplicadas++;
                } catch (SQLException e) {
                    if (esConflictoDeObjetoExistente(e)) {
                        yaExistian++;
                    } else {
                        throw new SQLException("Error al ejecutar «" + resumir(sql)
                                + "»: " + e.getMessage(), e.getSQLState(),
                                e.getErrorCode(), e);
                    }
                }
            }
        }

        log.accept("Esquema aplicado: " + aplicadas + " instrucciones; "
                + yaExistian + " objetos ya estaban creados.");
        if (!esquemaCompleto(props)) {
            throw new SQLException("No se pudo verificar todo el esquema. "
                    + "La cuenta de base de datos necesita permisos para crear "
                    + "tablas, vistas y procedimientos.");
        }
        log.accept("Base de datos verificada y lista para usar.");
    }

    /**
     * Convierte la URL de la aplicación en una URL del servidor, sin catálogo.
     * Se conservan los parámetros JDBC (codificación, zona horaria, etc.).
     */
    static String urlServidor(Properties props) {
        String url = props.getProperty("db.url", "");
        int inicioAutoridad = url.indexOf("://");
        if (inicioAutoridad < 0) {
            throw new IllegalArgumentException("La URL de MariaDB no es válida.");
        }
        int primeraBarra = url.indexOf('/', inicioAutoridad + 3);
        if (primeraBarra < 0) {
            return url.endsWith("/") ? url : url + "/";
        }
        int parametros = url.indexOf('?', primeraBarra);
        String sufijo = parametros >= 0 ? url.substring(parametros) : "";
        return url.substring(0, primeraBarra + 1) + sufijo;
    }

    private static String nombreBaseDatos(Properties props) {
        String nombre = props.getProperty("db.nombre", "control_tardanzas").trim();
        if (!nombre.matches("[A-Za-z0-9_$-]{1,64}")) {
            throw new IllegalArgumentException("El nombre de la base de datos no es válido.");
        }
        return nombre;
    }

    private static String identificador(String nombre) {
        return "`" + nombre.replace("`", "``") + "`";
    }

    private static boolean existeObjeto(Connection conexion, String tipo,
                                        String nombre, String base) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND TABLE_TYPE = ? LIMIT 1";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, base);
            consulta.setString(2, nombre);
            consulta.setString(3, tipo);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    private static boolean existeProcedimiento(Connection conexion, String nombre,
                                               String base) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.ROUTINES "
                + "WHERE ROUTINE_SCHEMA = ? AND ROUTINE_NAME = ? "
                + "AND ROUTINE_TYPE = 'PROCEDURE' LIMIT 1";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, base);
            consulta.setString(2, nombre);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    private static boolean esConflictoDeObjetoExistente(SQLException error) {
        for (SQLException actual = error; actual != null; actual = actual.getNextException()) {
            // 1050: tabla existente; 1061: índice existente; 1304: rutina existente.
            if (actual.getErrorCode() == 1050
                    || actual.getErrorCode() == 1061
                    || actual.getErrorCode() == 1304) {
                return true;
            }
        }
        return false;
    }

    private static List<String> leerScriptInterno() throws IOException {
        try (InputStream entrada = DatabaseBootstrap.class
                .getResourceAsStream(SCRIPT_INTERNO)) {
            if (entrada == null) {
                throw new IOException("No se encontró el esquema incluido en la aplicación: "
                        + SCRIPT_INTERNO);
            }
            return dividirScript(new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /** Separa sentencias SQL respetando las directivas DELIMITER de MariaDB. */
    static List<String> dividirScript(String contenido) throws IOException {
        List<String> sentencias = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        String delimitador = ";";
        boolean omitirCreateDatabase = false;

        try (BufferedReader lector = new BufferedReader(new StringReader(contenido))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String recortada = linea.trim();
                if (recortada.isEmpty() || recortada.startsWith("--")) {
                    continue;
                }

                if (recortada.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                    delimitador = recortada.substring("DELIMITER".length()).trim();
                    if (delimitador.isEmpty()) {
                        throw new IOException("Directiva DELIMITER vacía en el esquema SQL.");
                    }
                    continue;
                }

                if (delimitador.equals(";")) {
                    String mayusculas = recortada.toUpperCase(Locale.ROOT);
                    if (omitirCreateDatabase) {
                        if (recortada.endsWith(";")) {
                            omitirCreateDatabase = false;
                        }
                        continue;
                    }
                    if (mayusculas.startsWith("CREATE DATABASE ")) {
                        omitirCreateDatabase = !recortada.endsWith(";");
                        continue;
                    }
                    if (mayusculas.equals("USE") || mayusculas.startsWith("USE ")) {
                        continue;
                    }
                }

                actual.append(linea).append('\n');
                if (recortada.endsWith(delimitador)) {
                    String texto = actual.toString().trim();
                    int fin = texto.lastIndexOf(delimitador);
                    String sentencia = texto.substring(0, fin).trim();
                    if (!sentencia.isEmpty()) {
                        sentencias.add(sentencia);
                    }
                    actual.setLength(0);
                }
            }
        }

        if (!actual.toString().isBlank()) {
            throw new IOException("El esquema SQL termina con una sentencia incompleta.");
        }
        if (sentencias.isEmpty()) {
            throw new IOException("El esquema SQL incluido está vacío.");
        }
        return sentencias;
    }

    private static String resumir(String sql) {
        String texto = sql.replaceAll("\\s+", " ").trim();
        return texto.length() > 110 ? texto.substring(0, 110) + "…" : texto;
    }
}
