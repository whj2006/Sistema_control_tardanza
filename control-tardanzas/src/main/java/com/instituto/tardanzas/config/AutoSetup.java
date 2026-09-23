package com.instituto.tardanzas.config;

import com.instituto.tardanzas.ui.VentanaConfigDB;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Asistente de primer arranque: prepara la conexión y el esquema de la BD. */
public final class AutoSetup {

    private static final String NOMBRE_BD = "control_tardanzas";
    private static final int PUERTO_MARIADB = 3306;
    private static final SecureRandom RANDOM = new SecureRandom();

    private AutoSetup() {
    }

    /**
     * Devuelve una configuración conectable y deja creados los objetos del
     * esquema. Si se cancela el asistente, devuelve {@code null}.
     */
    public static Properties resolver(Properties guardadas) throws Exception {
        Properties props = normalizar(guardadas);

        if (DatabaseBootstrap.puertoDisponible(props)) {
            try {
                prepararBaseDeDatos(props);
                return props;
            } catch (Exception e) {
                return pedirConfiguracion(props,
                        "No se pudo preparar la base de datos: " + mensajeCorto(e));
            }
        }

        if (esWindows() && esServidorLocal(props)
                && PUERTO_MARIADB == puerto(props)) {
            return asistentePrimerArranque(props);
        }

        return pedirConfiguracion(props,
                "No se encuentra un servidor de base de datos en "
                        + props.getProperty("db.host") + ":" + props.getProperty("db.puerto")
                        + ". Si el servidor está en otra dirección, puedes configurarla ahora.");
    }

    private static Properties normalizar(Properties originales) {
        Properties props = new Properties();
        if (originales != null) {
            props.putAll(originales);
        }
        props.putIfAbsent("db.host", "localhost");
        props.putIfAbsent("db.puerto", Integer.toString(PUERTO_MARIADB));
        props.putIfAbsent("db.nombre", NOMBRE_BD);
        props.putIfAbsent("db.user", "root");
        props.putIfAbsent("db.password", "");

        if (props.getProperty("db.url") == null || props.getProperty("db.url").isBlank()) {
            reconstruirUrl(props);
        }
        return props;
    }

    private static Properties asistentePrimerArranque(Properties base) throws Exception {
        String[] opciones = {
                "Descargar e instalar MariaDB automáticamente",
                "Ya tengo un servidor; configurar conexión",
                "Salir"
        };

        while (true) {
            int respuesta = mostrarEnEdt(() -> JOptionPane.showOptionDialog(
                    null,
                    "No se encontró MariaDB en este equipo.\n\n"
                            + "Puedes permitir que el asistente descargue e instale MariaDB "
                            + "y prepare la base de datos. Windows puede solicitar permisos "
                            + "de administrador durante la instalación.\n\n"
                            + "También puedes indicar los datos de un servidor que ya exista.",
                    "Configuración inicial · Control de Tardanzas",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]));

            if (respuesta == 0) {
                try {
                    return instalarMariaDB(base);
                } catch (Exception e) {
                    String[] alFallar = {"Reintentar", "Configurar conexión", "Salir"};
                    int siguiente = mostrarEnEdt(() -> JOptionPane.showOptionDialog(
                            null,
                            "No se pudo completar la instalación automática.\n\n"
                                    + mensajeCorto(e) + "\n\n"
                                    + "La instalación automática requiere Windows Package Manager "
                                    + "(winget) y conexión a internet. También puedes instalar "
                                    + "MariaDB manualmente o probar de nuevo.",
                            "No se pudo instalar MariaDB",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            alFallar,
                            alFallar[0]));
                    if (siguiente == 1) {
                        return pedirConfiguracion(base, "");
                    }
                    if (siguiente != 0) {
                        return null;
                    }
                }
            } else if (respuesta == 1) {
                return pedirConfiguracion(base, "");
            } else {
                return null;
            }
        }
    }

    private static Properties instalarMariaDB(Properties base) throws Exception {
        Properties props = (Properties) base.clone();
        String password = crearClaveAleatoria();
        props.setProperty("db.host", "localhost");
        props.setProperty("db.puerto", Integer.toString(PUERTO_MARIADB));
        props.setProperty("db.nombre", base.getProperty("db.nombre", NOMBRE_BD));
        props.setProperty("db.user", "root");
        props.setProperty("db.password", password);
        reconstruirUrl(props);

        ejecutarConProgreso("Descarga e instalación de MariaDB", log -> {
            log.accept("Solicitando MariaDB Server a Windows Package Manager (winget)…");
            log.accept("La descarga requiere conexión a internet y puede tardar unos minutos.");
            log.accept("Windows puede mostrar una solicitud de permisos para instalar el servicio.");

            List<String> comando = List.of(
                    "winget", "install", "--exact", "--id", "MariaDB.Server",
                    "--silent", "--accept-package-agreements",
                    "--accept-source-agreements", "--override",
                    "/qn PASSWORD=" + password + " SERVICENAME=MariaDB");
            ejecutarProceso(comando, Duration.ofMinutes(15));

            // Si el instalador terminó pero Windows reinicia el servicio, dejar
            // guardada la clave aleatoria para poder reanudar el primer arranque.
            ConfigDB.guardar(props);
            log.accept("Instalador finalizado. Iniciando el servicio MariaDB…");
            iniciarServicioMariaDB();
            esperarPuerto("127.0.0.1", PUERTO_MARIADB, Duration.ofMinutes(3), log);
            log.accept("MariaDB está disponible.");
            return null;
        });

        prepararBaseDeDatos(props);
        mostrarEnEdt(() -> {
            JOptionPane.showMessageDialog(null,
                    "MariaDB está instalado y la base de datos se ha preparado.\n"
                            + "Ya puedes empezar a usar Control de Tardanzas.",
                    "Configuración completada",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        });
        return props;
    }

    private static void prepararBaseDeDatos(Properties props) throws Exception {
        if (!DatabaseBootstrap.esquemaCompleto(props)) {
            ejecutarConProgreso("Preparando la base de datos", log -> {
                DatabaseBootstrap.instalarEsquema(props, log);
                return null;
            });
        }
        ConfigDB.guardar(props);
    }

    private static Properties pedirConfiguracion(Properties inicial, String mensajeInicial)
            throws Exception {
        Properties datos = (Properties) inicial.clone();
        String mensaje = mensajeInicial;

        while (true) {
            Properties valoresActuales = datos;
            String errorActual = mensaje;
            Properties introducidos = mostrarEnEdt(
                    () -> VentanaConfigDB.mostrar(valoresActuales, errorActual));
            if (introducidos == null) {
                return null;
            }
            datos = introducidos;
            try {
                if (!DatabaseBootstrap.puertoDisponible(datos)) {
                    throw new IOException("No responde el servidor indicado. Comprueba el host y el puerto.");
                }
                prepararBaseDeDatos(datos);
                mostrarEnEdt(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Conexión comprobada. La configuración se guardó en tu perfil de usuario.",
                            "Conexión correcta",
                            JOptionPane.INFORMATION_MESSAGE);
                    return null;
                });
                return datos;
            } catch (Exception e) {
                mensaje = "No se pudo conectar o preparar el esquema: " + mensajeCorto(e);
            }
        }
    }

    private static String crearClaveAleatoria() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static void reconstruirUrl(Properties props) {
        String host = props.getProperty("db.host", "localhost").trim();
        String puerto = props.getProperty("db.puerto", Integer.toString(PUERTO_MARIADB)).trim();
        String base = props.getProperty("db.nombre", NOMBRE_BD).trim();
        props.setProperty("db.url", "jdbc:mariadb://" + host + ":" + puerto + "/" + base
                + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Europe/Madrid");
    }

    private static boolean esWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static boolean esServidorLocal(Properties props) {
        String host = props.getProperty("db.host", "localhost").trim().toLowerCase();
        return host.equals("localhost") || host.equals("127.0.0.1")
                || host.equals("::1") || host.equals("0:0:0:0:0:0:0:1");
    }

    private static int puerto(Properties props) {
        try {
            return Integer.parseInt(props.getProperty("db.puerto", "3306"));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static void ejecutarProceso(List<String> comando, Duration limite)
            throws Exception {
        Process proceso;
        try {
            proceso = new ProcessBuilder(comando)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
        } catch (IOException e) {
            throw new IOException("No se encontró winget. Instala o actualiza "
                    + "‘Instalador de aplicación’ desde Microsoft Store, o instala "
                    + "MariaDB manualmente.", e);
        }

        if (!proceso.waitFor(limite.toMillis(), TimeUnit.MILLISECONDS)) {
            proceso.destroyForcibly();
            throw new IOException("winget excedió el tiempo de espera de "
                    + limite.toMinutes() + " minutos.");
        }
        int codigo = proceso.exitValue();
        if (codigo != 0 && codigo != 3010) {
            throw new IOException("El instalador de MariaDB terminó con el código " + codigo + ".");
        }
    }

    private static void iniciarServicioMariaDB() {
        try {
            Process proceso = new ProcessBuilder("net.exe", "start", "MariaDB")
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            proceso.waitFor(30, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // El servicio puede estar ya iniciado. La comprobación del puerto
            // posterior será la fuente de verdad.
        }
    }

    private static void esperarPuerto(String host, int puerto, Duration limite,
                                      Consumer<String> log) throws Exception {
        long fin = System.nanoTime() + limite.toNanos();
        int segundos = 0;
        while (System.nanoTime() < fin) {
            try (Socket socket = new Socket(host, puerto)) {
                log.accept("Conexión local confirmada.");
                return;
            } catch (IOException ignored) {
                // El servicio todavía está iniciando.
            }
            if (segundos % 10 == 0) {
                log.accept("Esperando a que MariaDB inicie… (" + segundos + " s)");
            }
            Thread.sleep(1_000);
            segundos++;
        }
        throw new IOException("MariaDB no respondió en el puerto " + puerto
                + " dentro del tiempo esperado.");
    }

    @FunctionalInterface
    private interface TareaProgreso<T> {
        T ejecutar(Consumer<String> log) throws Exception;
    }

    private static <T> T ejecutarConProgreso(String titulo, TareaProgreso<T> tarea)
            throws Exception {
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        Runnable mostrar = () -> {
            JDialog dialogo = new JDialog((Frame) null, titulo, true);
            dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialogo.setSize(560, 320);
            dialogo.setLocationRelativeTo(null);

            JTextArea salida = new JTextArea();
            salida.setEditable(false);
            salida.setLineWrap(true);
            salida.setWrapStyleWord(true);
            salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JProgressBar progreso = new JProgressBar();
            progreso.setIndeterminate(true);

            JPanel contenido = new JPanel(new BorderLayout(8, 8));
            contenido.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            contenido.add(new JScrollPane(salida), BorderLayout.CENTER);
            contenido.add(progreso, BorderLayout.SOUTH);
            dialogo.setContentPane(contenido);

            SwingWorker<T, String> trabajador = new SwingWorker<>() {
                @Override
                protected T doInBackground() throws Exception {
                    return tarea.ejecutar(this::publish);
                }

                @Override
                protected void process(List<String> mensajes) {
                    for (String mensaje : mensajes) {
                        salida.append(mensaje + System.lineSeparator());
                        salida.setCaretPosition(salida.getDocument().getLength());
                    }
                }

                @Override
                protected void done() {
                    try {
                        resultado.set(get());
                    } catch (Exception e) {
                        Throwable causa = e.getCause() != null ? e.getCause() : e;
                        error.set(causa instanceof Exception
                                ? (Exception) causa : new Exception(causa));
                        salida.append("\nError: " + mensajeCorto(error.get()));
                    } finally {
                        dialogo.dispose();
                    }
                }
            };
            trabajador.execute();
            dialogo.setVisible(true);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            mostrar.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(mostrar);
            } catch (Exception e) {
                throw new Exception("No se pudo abrir la ventana de progreso.", e);
            }
        }

        if (error.get() != null) {
            throw error.get();
        }
        return resultado.get();
    }

    private static <T> T mostrarEnEdt(java.util.concurrent.Callable<T> tarea)
            throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            return tarea.call();
        }
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    resultado.set(tarea.call());
                } catch (Exception e) {
                    error.set(e);
                }
            });
        } catch (Exception e) {
            throw new Exception("No se pudo mostrar el asistente de configuración.", e);
        }
        if (error.get() != null) {
            throw error.get();
        }
        return resultado.get();
    }

    private static String mensajeCorto(Exception e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        String mensaje = causa.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = causa.getClass().getSimpleName();
        }
        mensaje = mensaje.replaceAll("\\s+", " ").trim();
        return mensaje.length() > 240 ? mensaje.substring(0, 240) + "…" : mensaje;
    }
}
