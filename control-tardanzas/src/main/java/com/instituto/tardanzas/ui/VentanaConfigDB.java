package com.instituto.tardanzas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class VentanaConfigDB {

    /**
     * Muestra el panel de configuración de base de datos.
     * Devuelve un Properties con los datos, o null si cancela.
     */
    public static Properties mostrar(Properties propsActuales, String mensajeError) {

        // Rellenar con los valores actuales si los hay
        JTextField hostField = new JTextField(
                propsActuales.getProperty("db.host", "localhost"));
        JTextField puertoField = new JTextField(
                propsActuales.getProperty("db.puerto", "3306"));
        JTextField bdField = new JTextField(
                propsActuales.getProperty("db.nombre", "control_tardanzas"));
        JTextField userField = new JTextField(
                propsActuales.getProperty("db.user", "root"));
        JPasswordField passField = new JPasswordField(
                propsActuales.getProperty("db.password", ""));

        // Panel del formulario
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: mensaje de error (si hay)
        if (mensajeError != null && !mensajeError.isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            JLabel lblError = new JLabel("⚠ " + mensajeError);
            lblError.setForeground(new Color(180, 0, 0));
            lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));
            formulario.add(lblError, gbc);
            gbc.gridwidth = 1;
        }

        // Filas del formulario
        int fila = (mensajeError != null && !mensajeError.isEmpty()) ? 1 : 0;

        agregarFila(formulario, gbc, "Host:", hostField, fila++);
        agregarFila(formulario, gbc, "Puerto:", puertoField, fila++);
        agregarFila(formulario, gbc, "Base de datos:", bdField, fila++);
        agregarFila(formulario, gbc, "Usuario:", userField, fila++);
        agregarFila(formulario, gbc, "Contraseña:", passField, fila);

        // Mostrar dialogo
        int resultado = JOptionPane.showConfirmDialog(
                null,
                formulario,
                "Configuración de Base de Datos - MariaDB",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return null; // El usuario canceló
        }

        // Construir la URL y el Properties
        String host   = hostField.getText().trim();
        String puerto = puertoField.getText().trim();
        String bd     = bdField.getText().trim();
        String url    = "jdbc:mariadb://" + host + ":" + puerto + "/" + bd
                + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Europe/Madrid";

        Properties props = new Properties();
        props.setProperty("db.url",      url);
        props.setProperty("db.user",     userField.getText().trim());
        props.setProperty("db.password", new String(passField.getPassword()));
        // Guardamos también por separado para rellenar el form la próxima vez
        props.setProperty("db.host",     host);
        props.setProperty("db.puerto",   puerto);
        props.setProperty("db.nombre",   bd);

        return props;
    }

    private static void agregarFila(JPanel panel, GridBagConstraints gbc,
                                     String etiqueta, JComponent campo, int fila) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        campo.setPreferredSize(new Dimension(220, 28));
        panel.add(campo, gbc);
    }
}