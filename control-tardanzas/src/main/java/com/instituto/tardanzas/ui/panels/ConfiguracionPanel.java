package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.ConfiguracionDAO;
import com.instituto.tardanzas.model.Configuracion;
import com.instituto.tardanzas.service.EmailService;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import java.awt.*;

public class ConfiguracionPanel extends JPanel {

    private final ConfiguracionDAO configDAO    = new ConfiguracionDAO();
    private final EmailService     emailService;

    private JTextField     tfHost;
    private JTextField     tfPuerto;
    private JTextField     tfUsuario;
    private JPasswordField pfPassword;
    private JTextField     tfRemitente;
    private JTextField     tfNombreRem;

    public ConfiguracionPanel(EmailService emailService) {
        this.emailService = emailService;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new GridBagLayout());
        construirUI();
        cargarConfiguracion();
    }

    private void construirUI() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtils.COLOR_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(8, 8, 8, 8);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(UIUtils.crearTitulo("Configuracion SMTP"), gbc);
        gbc.gridwidth = 1;

        tfHost     = UIUtils.crearCampo(25);
        tfPuerto   = UIUtils.crearCampo(8);
        tfUsuario  = UIUtils.crearCampo(25);
        pfPassword = new JPasswordField(25);
        pfPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pfPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        tfRemitente = UIUtils.crearCampo(25);
        tfNombreRem = UIUtils.crearCampo(25);

        String[]    ets    = {
            "Servidor SMTP:",
            "Puerto SMTP:",
            "Usuario (email):",
            "Contrasena:",
            "Email remitente:",
            "Nombre remitente:"
        };
        Component[] campos = {
            tfHost, tfPuerto, tfUsuario,
            pfPassword, tfRemitente, tfNombreRem
        };

        for (int i = 0; i < ets.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.35;
            JLabel lbl = new JLabel(ets[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            card.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            card.add(campos[i], gbc);
        }

        JLabel nota = new JLabel(
            "<html><i style='color:#888'>" +
            "Office365: host=smtp.office365.com  puerto=587" +
            "</i></html>");
        nota.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridx = 0; gbc.gridy = ets.length + 1;
        gbc.gridwidth = 2;
        card.add(nota, gbc);

        JButton btnProbar  = UIUtils.crearBoton(
                "Probar conexion", UIUtils.COLOR_PRIMARIO);
        JButton btnGuardar = UIUtils.crearBoton(
                "Guardar", UIUtils.COLOR_VERDE);

        btnProbar .addActionListener(e -> probarConexion());
        btnGuardar.addActionListener(e -> guardar());

        JPanel panelBtns = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBtns.setOpaque(false);
        panelBtns.add(btnProbar);
        panelBtns.add(btnGuardar);

        gbc.gridy = ets.length + 2;
        card.add(panelBtns, gbc);

        add(card);
    }

    private void cargarConfiguracion() {
        SwingWorker<Configuracion, Void> w = new SwingWorker<>() {
            @Override protected Configuracion doInBackground()
                    throws Exception {
                return configDAO.obtener();
            }
            @Override protected void done() {
                try {
                    Configuracion cfg = get();
                    if (cfg != null) {
                        tfHost     .setText(cfg.getSmtpHost());
                        tfPuerto   .setText(
                                String.valueOf(cfg.getSmtpPuerto()));
                        tfUsuario  .setText(cfg.getSmtpUsuario());
                        pfPassword .setText(cfg.getSmtpPassword());
                        tfRemitente.setText(cfg.getEmailRemitente());
                        tfNombreRem.setText(cfg.getNombreRemitente());
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void guardar() {
        try {
            Configuracion cfg = recogerDatos();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() throws Exception {
                    configDAO.guardar(cfg);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        UIUtils.mostrarInfo(ConfiguracionPanel.this,
                                "Configuracion guardada correctamente.");
                    } catch (Exception e) {
                        UIUtils.mostrarError(ConfiguracionPanel.this,
                                e.getMessage());
                    }
                }
            };
            w.execute();
        } catch (Exception e) {
            UIUtils.mostrarError(this, e.getMessage());
        }
    }

    private void probarConexion() {
        try {
            Configuracion cfg = recogerDatos();

            JDialog dlg = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Probando...", false);
            JProgressBar bar = new JProgressBar();
            bar.setIndeterminate(true);
            bar.setString("Conectando con " + cfg.getSmtpHost() + "...");
            bar.setStringPainted(true);
            bar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            dlg.add(bar);
            dlg.setSize(350, 85);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);

            SwingWorker<String, Void> w = new SwingWorker<>() {
                @Override protected String doInBackground() {
                    return emailService.probarConexion(cfg);
                }
                @Override protected void done() {
                    dlg.dispose();
                    try {
                        String res = get();
                        if ("OK".equals(res)) {
                            UIUtils.mostrarInfo(ConfiguracionPanel.this,
                                "Conexion SMTP correcta.\n" +
                                "Los correos se enviaran sin problema.");
                        } else {
                            UIUtils.mostrarError(ConfiguracionPanel.this,
                                "Error:\n" + res);
                        }
                    } catch (Exception e) {
                        UIUtils.mostrarError(ConfiguracionPanel.this,
                                e.getMessage());
                    }
                }
            };
            w.execute();

        } catch (Exception e) {
            UIUtils.mostrarError(this, e.getMessage());
        }
    }

    private Configuracion recogerDatos() throws Exception {
        String host   = tfHost.getText().trim();
        String puerto = tfPuerto.getText().trim();
        String user   = tfUsuario.getText().trim();
        String pass   = new String(pfPassword.getPassword()).trim();
        String rem    = tfRemitente.getText().trim();
        String nombre = tfNombreRem.getText().trim();

        if (host.isEmpty())
            throw new Exception("El servidor SMTP no puede estar vacio.");
        if (puerto.isEmpty())
            throw new Exception("El puerto no puede estar vacio.");
        if (user.isEmpty())
            throw new Exception("El usuario no puede estar vacio.");
        if (pass.isEmpty())
            throw new Exception("La contrasena no puede estar vacia.");
        if (rem.isEmpty())
            throw new Exception("El email remitente no puede estar vacio.");
        if (nombre.isEmpty())
            throw new Exception("El nombre remitente no puede estar vacio.");

        int puertoInt;
        try {
            puertoInt = Integer.parseInt(puerto);
        } catch (NumberFormatException e) {
            throw new Exception("El puerto debe ser un numero (587).");
        }

        Configuracion cfg = new Configuracion();
        cfg.setSmtpHost       (host);
        cfg.setSmtpPuerto     (puertoInt);
        cfg.setSmtpUsuario    (user);
        cfg.setSmtpPassword   (pass);
        cfg.setEmailRemitente (rem);
        cfg.setNombreRemitente(nombre);
        return cfg;
    }
}