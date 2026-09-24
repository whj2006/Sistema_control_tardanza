package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.config.ConfigDB;
import com.instituto.tardanzas.dao.ConfiguracionHorariosDAO;
import com.instituto.tardanzas.model.ConfiguracionHorarios;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AjustesPanel extends JPanel {

    private final ConfiguracionHorariosDAO dao =
            new ConfiguracionHorariosDAO();

    private JSpinner   spinEntradaH;
    private JSpinner   spinEntradaM;
    private JTextField tfNombreCentro;
    private JLabel     lblEstado;

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════

    public AjustesPanel() {
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        construirUI();
        cargarDatos();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI
    // ══════════════════════════════════════════════════════════════════════

    private void construirUI() {

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(
                new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 215, 220), 1),
                new EmptyBorder(30, 40, 35, 40)));

        // ── Título ────────────────────────────────────────────────────────
        JLabel lblTitulo = new JLabel("Ajustes del Sistema");
        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(40, 55, 75));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep1 = separador();

        // ── Descripción ───────────────────────────────────────────────────
        JTextArea txtDesc = new JTextArea(
            "Configura los datos del centro y el\n"
          + "horario del sistema:\n\n"
          + "Nombre del centro:\n"
          + "Se muestra en la barra superior y en\n"
          + "los correos enviados a las familias.\n\n"
          + "Hora de entrada:\n"
          + "Determina desde que hora se considera\n"
          + "que un alumno llega tarde y cuantos\n"
          + "minutos de retraso acumula.\n\n"
          + "El correo a la familia se enviara\n"
          + "automaticamente al registrar el retraso.");
        txtDesc.setEditable(false);
        txtDesc.setOpaque(false);
        txtDesc.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));
        txtDesc.setForeground(new Color(80, 80, 80));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep2 = separador();

        // ── Fila: Nombre del centro ───────────────────────────────────────
        tfNombreCentro = new JTextField();
        JPanel filaCentro = crearFilaTexto(
                "Nombre del centro:",
                tfNombreCentro);

        JSeparator sep3 = separador();

        // ── Fila: Hora de entrada ─────────────────────────────────────────
        spinEntradaH = crearSpinner(0, 23);
        spinEntradaM = crearSpinner(0, 59);
        JPanel filaEntrada = crearFilaHora(
                "Hora de entrada al centro:",
                UIUtils.COLOR_PRIMARIO,
                spinEntradaH,
                spinEntradaM);

        JSeparator sep4 = separador();

        // ── Botón guardar ─────────────────────────────────────────────────
        JButton btnGuardar = UIUtils.crearBoton(
                "Guardar Ajustes",
                UIUtils.COLOR_PRIMARIO);
        btnGuardar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45));
        btnGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuardar.setFont(
                new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.addActionListener(e -> guardar());

        // ── Estado ────────────────────────────────────────────────────────
        lblEstado = new JLabel(" ");
        lblEstado.setFont(
                new Font("Segoe UI", Font.BOLD, 12));
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Montar tarjeta ────────────────────────────────────────────────
        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(sep1);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(txtDesc);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(sep2);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(filaCentro);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(sep3);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(filaEntrada);
        tarjeta.add(Box.createVerticalStrut(22));
        tarjeta.add(sep4);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(btnGuardar);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(lblEstado);
        tarjeta.add(Box.createVerticalStrut(10));

        // ── Wrapper centrado ──────────────────────────────────────────────
        tarjeta.setMaximumSize(
                new Dimension(540, Integer.MAX_VALUE));
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel centroPanel = new JPanel(new GridBagLayout());
        centroPanel.setOpaque(false);
        centroPanel.add(tarjeta);

        JScrollPane scroll = new JScrollPane(centroPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UIUtils.COLOR_FONDO);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    // FILA DE TEXTO
    // ══════════════════════════════════════════════════════════════════════

    private JPanel crearFilaTexto(String etiqueta,
                                  JTextField campo) {
        JPanel fila = new JPanel();
        fila.setOpaque(false);
        fila.setLayout(
                new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 50));

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UIUtils.COLOR_PRIMARIO);
        lbl.setPreferredSize(new Dimension(240, 36));
        lbl.setMinimumSize(new Dimension(240, 36));
        lbl.setMaximumSize(new Dimension(240, 36));

        campo.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(220, 36));
        campo.setMaximumSize(new Dimension(220, 36));

        fila.add(lbl);
        fila.add(Box.createHorizontalStrut(10));
        fila.add(campo);

        return fila;
    }

    // ══════════════════════════════════════════════════════════════════════
    // FILA DE HORA
    // ══════════════════════════════════════════════════════════════════════

    private JPanel crearFilaHora(String etiqueta,
                                 Color color,
                                 JSpinner spinH,
                                 JSpinner spinM) {
        JPanel fila = new JPanel();
        fila.setOpaque(false);
        fila.setLayout(
                new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 50));

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(color);
        lbl.setPreferredSize(new Dimension(240, 36));
        lbl.setMinimumSize(new Dimension(240, 36));
        lbl.setMaximumSize(new Dimension(240, 36));

        JLabel lblDos = new JLabel(":");
        lblDos.setFont(
                new Font("Segoe UI", Font.BOLD, 20));
        lblDos.setBorder(new EmptyBorder(0, 4, 0, 4));

        JPanel panelSpinners = new JPanel();
        panelSpinners.setOpaque(false);
        panelSpinners.setLayout(
                new BoxLayout(panelSpinners,
                        BoxLayout.X_AXIS));
        panelSpinners.add(spinH);
        panelSpinners.add(lblDos);
        panelSpinners.add(spinM);

        fila.add(lbl);
        fila.add(Box.createHorizontalStrut(10));
        fila.add(panelSpinners);

        return fila;
    }

    private JSpinner crearSpinner(int min, int max) {
        JSpinner sp = new JSpinner(
                new SpinnerNumberModel(min, min, max, 1));
        sp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sp.setPreferredSize(new Dimension(65, 38));
        sp.setMinimumSize(new Dimension(65, 38));
        sp.setMaximumSize(new Dimension(65, 38));
        JSpinner.NumberEditor editor =
                new JSpinner.NumberEditor(sp, "00");
        sp.setEditor(editor);
        return sp;
    }

    private JSeparator separador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 225, 230));
        sep.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARGAR DATOS
    // ══════════════════════════════════════════════════════════════════════

    private void cargarDatos() {
        SwingWorker<ConfiguracionHorarios, Void> w =
                new SwingWorker<>() {
            @Override
            protected ConfiguracionHorarios
                    doInBackground() throws Exception {
                return dao.obtener();
            }
            @Override
            protected void done() {
                try {
                    ConfiguracionHorarios h = get();
                    aplicarHora(h.getHoraEntrada(),
                            spinEntradaH, spinEntradaM);
                    tfNombreCentro.setText(ConfigDB.cargar()
                            .getProperty("centro.nombre", ""));
                    mostrarEstado("Ajustes cargados.", true);
                } catch (Exception e) {
                    mostrarEstado(
                            "Error al cargar: "
                            + e.getMessage(), false);
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GUARDAR
    // ══════════════════════════════════════════════════════════════════════

    private void guardar() {
        int entH = (int) spinEntradaH.getValue();
        int entM = (int) spinEntradaM.getValue();
        String horaEntrada =
                String.format("%02d:%02d", entH, entM);
        String nombreCentro =
                tfNombreCentro.getText().trim();

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground()
                    throws Exception {
                dao.guardar(new ConfiguracionHorarios(
                        horaEntrada));

                java.util.Properties props =
                        ConfigDB.cargar();
                props.setProperty("centro.nombre",
                        nombreCentro);
                ConfigDB.guardar(props);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    mostrarEstado(
                        "Ajustes guardados. "
                        + "Entrada: " + horaEntrada,
                        true);
                    JOptionPane.showMessageDialog(
                        AjustesPanel.this,
                        "Ajustes guardados.\n\n"
                        + "Nombre del centro: "
                        + nombreCentro + "\n"
                        + "Hora de entrada: "
                        + horaEntrada,
                        "Guardado",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    mostrarEstado(
                        "Error al guardar: "
                        + e.getMessage(), false);
                    UIUtils.mostrarError(
                        AjustesPanel.this,
                        "Error al guardar:\n"
                        + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private void aplicarHora(String horaStr,
                             JSpinner spinH,
                             JSpinner spinM) {
        try {
            String[] p = horaStr.split(":");
            spinH.setValue(Integer.parseInt(p[0]));
            spinM.setValue(Integer.parseInt(p[1]));
        } catch (Exception ignored) {}
    }

    private void mostrarEstado(String msg, boolean ok) {
        lblEstado.setText(msg);
        lblEstado.setForeground(ok
                ? new Color(30, 130, 76)
                : new Color(180, 30, 30));
    }
}