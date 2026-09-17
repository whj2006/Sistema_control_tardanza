package com.instituto.tardanzas;

import com.instituto.tardanzas.config.ConfigDB;
import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.service.EmailService;
import com.instituto.tardanzas.ui.MainFrame;
import com.instituto.tardanzas.ui.VentanaConfigDB;

import javax.swing.*;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {

        // Look & Feel del sistema
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // ── 1. Intentar cargar configuración guardada ─────────────────────
        Properties props = ConfigDB.cargar();
        String mensajeError = "";

        // Si hay config guardada, probar si funciona
        if (!props.isEmpty()) {
            try {
                ConfigDB.probarConexion(props);
                // Conexión OK, inicializar el pool
                DatabaseConfig.inicializar(
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password")
                );
                mensajeError = ""; // va bien, salimos del bucle directamente
            } catch (Exception e) {
                // La config guardada ya no funciona
                mensajeError = "La configuración guardada no funciona. Revisa los datos.";
                props = new Properties(); // resetear para pedir de nuevo
            }
        }

        // ── 2. Si no hay config válida, pedir al usuario ──────────────────
        while (props.isEmpty() || !props.containsKey("db.url")) {
            props = VentanaConfigDB.mostrar(props, mensajeError);

            if (props == null) {
                // El usuario canceló
                JOptionPane.showMessageDialog(null,
                        "No se puede iniciar sin configuración de base de datos.",
                        "Saliendo", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }

            try {
                ConfigDB.probarConexion(props);

                // Conexión OK → guardar y continuar
                ConfigDB.guardar(props);
                DatabaseConfig.inicializar(
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password")
                );

                JOptionPane.showMessageDialog(null,
                        "✔ Conexión correcta. Configuración guardada.",
                        "Conexión OK", JOptionPane.INFORMATION_MESSAGE);

                mensajeError = ""; // salir del while

            } catch (Exception e) {
                mensajeError = "No se pudo conectar: " + e.getMessage();
                // El while volverá a pedir los datos
            }
        }

        // ── 3. Todo OK, lanzar la aplicación ─────────────────────────────
        final Properties propsFinal = props;
        EmailService emailService = new EmailService();
        emailService.iniciar();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(emailService);
            frame.setVisible(true);
        });
    }
}