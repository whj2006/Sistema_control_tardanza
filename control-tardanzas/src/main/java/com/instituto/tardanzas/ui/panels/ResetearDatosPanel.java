package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.ResetDAO;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import java.awt.*;

public class ResetearDatosPanel extends JPanel {

    private final ResetDAO resetDAO = new ResetDAO();
    private final Runnable onDatosReseteados;

    // Constructor con callback
    public ResetearDatosPanel(Runnable onDatosReseteados) {
        this.onDatosReseteados = onDatosReseteados;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15));
        construirUI();
    }

    // Constructor sin callback (compatibilidad)
    public ResetearDatosPanel() {
        this(null);
    }

    private void notificarReset() {
        if (onDatosReseteados != null)
            onDatosReseteados.run();
    }

    private void construirUI() {
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(
                tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 215, 220), 1),
                BorderFactory.createEmptyBorder(
                        30, 40, 40, 40)));
        tarjeta.setPreferredSize(new Dimension(520, 420));
        tarjeta.setMaximumSize(new Dimension(520, 420));

        JLabel lblTitulo = new JLabel("Resetear Datos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(40, 55, 75));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 225, 230));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtAviso = new JTextArea(
                "Esta acción eliminará TODOS los datos "
                + "del sistema:\n\n"
                + "• Todos los cursos\n"
                + "• Todos los alumnos\n"
                + "• Todos los retrasos registrados\n"
                + "• Todos los correos enviados a familias\n"
                + "• Todos los destinatarios de exportación\n\n"
                + "Esta acción NO se puede deshacer.");
        txtAviso.setEditable(false);
        txtAviso.setOpaque(false);
        txtAviso.setFont(new Font("Segoe UI",
                Font.PLAIN, 14));
        txtAviso.setForeground(new Color(60, 60, 60));
        txtAviso.setLineWrap(true);
        txtAviso.setWrapStyleWord(true);
        txtAviso.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtAviso.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, Integer.MAX_VALUE));

        JButton btnReset = new JButton(
                "RESETEAR TODOS LOS DATOS");
        btnReset.setFont(new Font("Segoe UI",
                Font.BOLD, 14));
        btnReset.setForeground(Color.WHITE);
        btnReset.setBackground(new Color(180, 30, 30));
        btnReset.setBorderPainted(false);
        btnReset.setFocusPainted(false);
        btnReset.setMaximumSize(new Dimension(300, 45));
        btnReset.setPreferredSize(new Dimension(300, 45));
        btnReset.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));

        btnReset.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(
                    java.awt.event.MouseEvent e) {
                btnReset.setBackground(
                        new Color(200, 40, 40));
            }
            @Override
            public void mouseExited(
                    java.awt.event.MouseEvent e) {
                btnReset.setBackground(
                        new Color(180, 30, 30));
            }
        });

        btnReset.addActionListener(e -> ejecutarReset());

        JPanel panelBoton = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBoton.setOpaque(false);
        panelBoton.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelBoton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panelBoton.add(btnReset);

        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(sep);
        tarjeta.add(Box.createVerticalStrut(20));
        tarjeta.add(txtAviso);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(panelBoton);

        centro.add(tarjeta);
        add(centro, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    // EJECUTAR RESET
    // ══════════════════════════════════════════════════════════════════════

    private void ejecutarReset() {
        int resp1 = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres BORRAR\n"
                + "TODOS los datos del sistema?\n\n"
                + "Esta acción NO se puede deshacer.",
                "Confirmar Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp1 != JOptionPane.YES_OPTION) return;

        String confirmacion = JOptionPane.showInputDialog(
                this,
                "Para confirmar, escribe RESET en mayúsculas:",
                "Confirmación final",
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == null
                || !confirmacion.equals("RESET")) {
            JOptionPane.showMessageDialog(this,
                    "Reset cancelado.\n"
                    + "No has escrito RESET correctamente.",
                    "Cancelado",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = crearDialogoProgreso(
                "Eliminando datos...");

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground()
                    throws Exception {
                resetDAO.resetearTodo();
                return null;
            }
            @Override
            protected void done() {
                dlg.dispose();
                try {
                    get();

                    // Notificar al MainFrame para que refresque
                    // todos los paneles (con un pequeño delay para
                    // garantizar que la BD ya está limpia)
                    Timer t = new Timer(300, ev -> notificarReset());
                    t.setRepeats(false);
                    t.start();

                    JOptionPane.showMessageDialog(
                            ResetearDatosPanel.this,
                            "Todos los datos han sido "
                            + "eliminados correctamente.\n\n"
                            + "El sistema está limpio.",
                            "Reset completado",
                            JOptionPane
                                .INFORMATION_MESSAGE);
                } catch (Exception e) {
                    UIUtils.mostrarError(
                            ResetearDatosPanel.this,
                            "Error durante el reset:\n"
                            + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════════════════════════════════════

    private JDialog crearDialogoProgreso(String mensaje) {
        JDialog dlg = new JDialog(
                (Frame) SwingUtilities
                        .getWindowAncestor(this),
                mensaje, false);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setString(mensaje);
        bar.setStringPainted(true);
        bar.setBorder(BorderFactory.createEmptyBorder(
                15, 20, 15, 20));
        dlg.add(bar);
        dlg.setSize(320, 85);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return dlg;
    }
}

