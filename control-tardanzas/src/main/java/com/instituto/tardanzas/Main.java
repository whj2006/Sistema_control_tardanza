package com.instituto.tardanzas;

import com.instituto.tardanzas.config.AutoSetup;
import com.instituto.tardanzas.config.ConfigDB;
import com.instituto.tardanzas.config.DatabaseConfig;
import com.instituto.tardanzas.service.EmailService;
import com.instituto.tardanzas.ui.MainFrame;

import javax.swing.*;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se mantiene el aspecto estándar de Swing si no se puede aplicar.
        }

        final Properties props;
        try {
            // El asistente comprueba el servidor, crea la base de datos si falta
            // y deja el esquema listo antes de abrir la aplicación.
            props = AutoSetup.resolver(ConfigDB.cargar());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo iniciar Control de Tardanzas.\n\n" + e.getMessage(),
                    "Error de configuración", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (props == null) {
            JOptionPane.showMessageDialog(null,
                    "No se puede iniciar sin una conexión a la base de datos.",
                    "Inicio cancelado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DatabaseConfig.inicializar(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password", ""));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "La base de datos está preparada, pero no se pudo abrir el pool de conexiones.\n\n"
                            + e.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EmailService emailService = new EmailService();
        emailService.iniciar();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(emailService);
            frame.setVisible(true);
        });
    }
}
