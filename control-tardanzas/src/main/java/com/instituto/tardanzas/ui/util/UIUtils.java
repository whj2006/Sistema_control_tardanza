package com.instituto.tardanzas.ui.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UIUtils {

    public static final Color COLOR_PRIMARIO      = new Color(44,  62,  80);
    public static final Color COLOR_ACENTO        = new Color(192, 57,  43);
    public static final Color COLOR_FONDO         = new Color(236, 240, 241);
    public static final Color COLOR_BLANCO        = Color.WHITE;
    public static final Color COLOR_TEXTO_CLARO   = new Color(149, 165, 166);
    public static final Color COLOR_VERDE         = new Color(39,  174, 96);
    public static final Color COLOR_NARANJA       = new Color(230, 126, 34);
    public static final Color COLOR_ACTIVO        = new Color(52,  152, 219);
    public static final Color COLOR_HOVER_SIDEBAR = new Color(60,  85,  110);
    public static final Color COLOR_CABECERA_TABLA = new Color(44, 62, 80);

    private UIUtils() {}

    public static JButton crearBoton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 34));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(fondo.darker());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(fondo);
            }
        });
        return btn;
    }

    public static JButton crearBotonPequeno(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 28));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(fondo.darker());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(fondo);
            }
        });
        return btn;
    }

    // ── Tabla — cabecera SIEMPRE visible ─────────────────────────────────

    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(174, 214, 241));
        tabla.setSelectionForeground(COLOR_PRIMARIO);
        tabla.setBackground(COLOR_BLANCO);

        // Cabecera con fondo oscuro y letra blanca — siempre visible
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(COLOR_CABECERA_TABLA);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setReorderingAllowed(false);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Renderer cabecera explícito para forzar colores
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setBackground(COLOR_CABECERA_TABLA);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1,
                                new Color(70, 90, 110)),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // Renderer filas alternas
        tabla.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0
                            ? COLOR_BLANCO
                            : new Color(245, 248, 250));
                    setForeground(COLOR_PRIMARIO);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    public static JTextField crearCampo(int columnas) {
        JTextField tf = new JTextField(columnas);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    public static JLabel crearTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(COLOR_PRIMARIO);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        return lbl;
    }

    public static void mostrarError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void mostrarInfo(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg,
                "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }
}