package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.DestinatarioDAO;
import com.instituto.tardanzas.dao.RetrasoDAO;
import com.instituto.tardanzas.model.RankingEntry;
import com.instituto.tardanzas.model.Retraso;
import com.instituto.tardanzas.service.EmailService;
import com.instituto.tardanzas.ui.util.UIUtils;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExportarPanel extends JPanel {

    private final RetrasoDAO      retrasoDAO      = new RetrasoDAO();
    private final DestinatarioDAO destinatarioDAO = new DestinatarioDAO();
    private final EmailService    emailService;

    private JPanel     panelListaCorreos;
    private JTextField tfNuevoCorreo;
    private JSpinner   spinnerSemanas;
    private JLabel     lblRangoSemana;
    private DatePicker dpDia;

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    public ExportarPanel(EmailService emailService) {
        this.emailService = emailService;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        construirUI();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI PRINCIPAL
    // ══════════════════════════════════════════════════════════════════════

    private void construirUI() {
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        norte.add(UIUtils.crearTitulo("Exportar Datos"),
                BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets  = new Insets(0, 0, 10, 0);

        gbc.gridy = 0; gbc.weighty = 1.0;
        centro.add(construirTarjetaDestinatarios(), gbc);

        gbc.gridy = 1; gbc.weighty = 1.0;
        centro.add(construirTarjetaExportarCompleto(), gbc);

        gbc.gridy = 2; gbc.weighty = 1.0;
        centro.add(construirTarjetaExportarSemanal(), gbc);

        gbc.gridy = 3; gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        centro.add(construirTarjetaExportarDia(), gbc);

        JScrollPane scroll = new JScrollPane(centro);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.getViewport().addComponentListener(
                new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int ancho = scroll.getViewport().getWidth();
                centro.setPreferredSize(new Dimension(
                        ancho, centro.getPreferredSize().height));
                centro.revalidate();
            }
        });

        add(scroll, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TARJETA DESTINATARIOS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirTarjetaDestinatarios() {
        JPanel card = crearCard();
        card.add(crearTituloTarjeta("Correo de\nDestinatario"),
                BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JPanel filaAnadir = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaAnadir.setOpaque(false);

        JLabel lblNuevo = new JLabel("Nuevo correo:");
        lblNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tfNuevoCorreo = UIUtils.crearCampo(25);
        tfNuevoCorreo.setPreferredSize(new Dimension(250, 30));
        tfNuevoCorreo.setToolTipText(
                "Ejemplo: secretaria@iesballester.es");

        JButton btnAnadir = UIUtils.crearBoton(
                "+ Añadir", UIUtils.COLOR_VERDE);
        btnAnadir.setPreferredSize(new Dimension(100, 30));
        btnAnadir.addActionListener(e -> anadirCorreo());
        tfNuevoCorreo.addActionListener(e -> anadirCorreo());

        filaAnadir.add(lblNuevo);
        filaAnadir.add(tfNuevoCorreo);
        filaAnadir.add(btnAnadir);

        panelListaCorreos = new JPanel();
        panelListaCorreos.setLayout(
                new BoxLayout(panelListaCorreos, BoxLayout.Y_AXIS));
        panelListaCorreos.setOpaque(false);

        centro.add(filaAnadir, BorderLayout.NORTH);
        centro.add(panelListaCorreos, BorderLayout.CENTER);

        card.add(centro, BorderLayout.CENTER);
        cargarDestinatarios();
        return card;
    }

    private void cargarDestinatarios() {
        SwingWorker<List<String[]>, Void> w = new SwingWorker<>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                return destinatarioDAO.obtenerTodos();
            }
            @Override
            protected void done() {
                try {
                    panelListaCorreos.removeAll();
                    for (String[] dest : get()) {
                        panelListaCorreos.add(crearFilaCorreo(
                                Integer.parseInt(dest[0]), dest[1]));
                        panelListaCorreos.add(Box.createVerticalStrut(4));
                    }
                    panelListaCorreos.revalidate();
                    panelListaCorreos.repaint();
                } catch (Exception e) {
                    UIUtils.mostrarError(ExportarPanel.this,
                            "Error cargando destinatarios:\n" + e.getMessage());
                }
            }
        };
        w.execute();
    }

    private JPanel crearFilaCorreo(int id, String correo) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JCheckBox chk = new JCheckBox(correo, true);
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chk.setOpaque(false);
        chk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chk.putClientProperty("correo", correo);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setBackground(new Color(180, 50, 50));
        btnEliminar.setBorderPainted(false);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setPreferredSize(new Dimension(80, 24));
        btnEliminar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> eliminarCorreo(id));

        fila.add(chk);
        fila.add(btnEliminar);
        return fila;
    }

    private void anadirCorreo() {
        String correo = tfNuevoCorreo.getText().trim();
        if (correo.isEmpty()) {
            UIUtils.mostrarError(this,
                    "Escribe un correo antes de añadir.");
            return;
        }
        if (!correo.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            UIUtils.mostrarError(this,
                    "El correo no tiene formato válido.\n"
                    + "Ejemplo: secretaria@iesballester.es");
            return;
        }

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                destinatarioDAO.agregar(correo);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    tfNuevoCorreo.setText("");
                    cargarDestinatarios();
                } catch (Exception e) {
                    UIUtils.mostrarError(ExportarPanel.this,
                            "Error al añadir correo.\n"
                            + "Es posible que ya exista.");
                }
            }
        };
        w.execute();
    }

    private void eliminarCorreo(int id) {
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Eliminar este destinatario?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                destinatarioDAO.eliminar(id);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    cargarDestinatarios();
                } catch (Exception e) {
                    UIUtils.mostrarError(ExportarPanel.this,
                            "Error al eliminar.");
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TARJETA EXPORTAR COMPLETO
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirTarjetaExportarCompleto() {
        JPanel card = crearCard();
        card.add(crearTituloTarjeta(
                "Exportar\nHistorial y\nRanking\nCompleto"),
                BorderLayout.WEST);

        card.add(crearDescripcionCentrada(
            "Exporta todos los retrasos registrados "
            + "desde el inicio hasta hoy.\n\n"
            + "El archivo incluye dos hojas:\n"
            + "- Historial completo con todos los registros\n"
            + "- Ranking completo de todos los alumnos"
        ), BorderLayout.CENTER);

        JPanel panelBtns = crearPanelBotones();

        JButton btnLocal = UIUtils.crearBoton(
                "Guardar en local", UIUtils.COLOR_NARANJA);
        btnLocal.setAlignmentX(CENTER_ALIGNMENT);
        btnLocal.setMaximumSize(new Dimension(200, 38));
        btnLocal.setPreferredSize(new Dimension(200, 38));
        btnLocal.addActionListener(e -> exportarCompleto(false));

        JButton btnCorreo = UIUtils.crearBoton(
                "Enviar por correo", UIUtils.COLOR_ACTIVO);
        btnCorreo.setAlignmentX(CENTER_ALIGNMENT);
        btnCorreo.setMaximumSize(new Dimension(200, 38));
        btnCorreo.setPreferredSize(new Dimension(200, 38));
        btnCorreo.addActionListener(e -> exportarCompleto(true));

        panelBtns.add(btnLocal);
        panelBtns.add(Box.createVerticalStrut(8));
        panelBtns.add(btnCorreo);
        panelBtns.add(Box.createVerticalGlue());

        card.add(panelBtns, BorderLayout.EAST);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TARJETA EXPORTAR SEMANAL
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirTarjetaExportarSemanal() {
        JPanel card = crearCard();
        card.add(crearTituloTarjeta(
                "Exportar\nHistorial y\nRanking\nSemanal"),
                BorderLayout.WEST);

        card.add(crearDescripcionCentrada(
            "Exporta los retrasos de una semana concreta.\n\n"
            + "El archivo incluye dos hojas:\n"
            + "- Historial de la semana seleccionada\n"
            + "- Ranking de esa semana"
        ), BorderLayout.CENTER);

        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(
                new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setOpaque(false);
        panelDerecho.setMinimumSize(new Dimension(160, 0));
        panelDerecho.setPreferredSize(new Dimension(210, 0));
        panelDerecho.setBorder(
                BorderFactory.createEmptyBorder(0, 15, 0, 0));

        panelDerecho.add(Box.createVerticalGlue());

        JPanel filaSemana = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 5, 0));
        filaSemana.setOpaque(false);
        filaSemana.setMaximumSize(new Dimension(210, 28));

        JLabel lblSemana = new JLabel("Semanas atrás:");
        lblSemana.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        spinnerSemanas = new JSpinner(
                new SpinnerNumberModel(0, 0, 52, 1));
        spinnerSemanas.setPreferredSize(new Dimension(50, 24));
        spinnerSemanas.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        filaSemana.add(lblSemana);
        filaSemana.add(spinnerSemanas);

        lblRangoSemana = new JLabel(obtenerRangoSemana(0));
        lblRangoSemana.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRangoSemana.setForeground(UIUtils.COLOR_PRIMARIO);
        lblRangoSemana.setAlignmentX(CENTER_ALIGNMENT);
        lblRangoSemana.setHorizontalAlignment(SwingConstants.CENTER);
        lblRangoSemana.setMaximumSize(new Dimension(210, 18));

        spinnerSemanas.addChangeListener(e ->
            lblRangoSemana.setText(obtenerRangoSemana(
                    (int) spinnerSemanas.getValue())));

        panelDerecho.add(filaSemana);
        panelDerecho.add(Box.createVerticalStrut(2));
        panelDerecho.add(lblRangoSemana);
        panelDerecho.add(Box.createVerticalStrut(10));

        JButton btnLocal = UIUtils.crearBoton(
                "Guardar en local", UIUtils.COLOR_NARANJA);
        btnLocal.setAlignmentX(CENTER_ALIGNMENT);
        btnLocal.setMaximumSize(new Dimension(200, 38));
        btnLocal.setPreferredSize(new Dimension(200, 38));
        btnLocal.addActionListener(e -> exportarSemanal(false));

        JButton btnCorreo = UIUtils.crearBoton(
                "Enviar por correo", UIUtils.COLOR_ACTIVO);
        btnCorreo.setAlignmentX(CENTER_ALIGNMENT);
        btnCorreo.setMaximumSize(new Dimension(200, 38));
        btnCorreo.setPreferredSize(new Dimension(200, 38));
        btnCorreo.addActionListener(e -> exportarSemanal(true));

        panelDerecho.add(btnLocal);
        panelDerecho.add(Box.createVerticalStrut(8));
        panelDerecho.add(btnCorreo);
        panelDerecho.add(Box.createVerticalGlue());

        card.add(panelDerecho, BorderLayout.EAST);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TARJETA EXPORTAR POR DÍA
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirTarjetaExportarDia() {
        JPanel card = crearCard();
        card.add(crearTituloTarjeta(
                "Exportar\nHistorial y\nRanking\npor Día"),
                BorderLayout.WEST);

        card.add(crearDescripcionCentrada(
            "Exporta los retrasos de un día concreto.\n\n"
            + "El archivo incluye dos hojas:\n"
            + "- Historial del día seleccionado\n"
            + "- Ranking de ese día"
        ), BorderLayout.CENTER);

        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(
                new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setOpaque(false);
        panelDerecho.setMinimumSize(new Dimension(160, 0));
        panelDerecho.setPreferredSize(new Dimension(210, 0));
        panelDerecho.setBorder(
                BorderFactory.createEmptyBorder(0, 15, 0, 0));

        panelDerecho.add(Box.createVerticalGlue());

        JPanel filaDia = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 5, 0));
        filaDia.setOpaque(false);
        filaDia.setMaximumSize(new Dimension(210, 28));

        JLabel lblDia = new JLabel("Día:");
        lblDia.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        dpDia = new DatePicker(crearDatePickerSettings());
        dpDia.setDate(LocalDate.now());
        dpDia.setPreferredSize(new Dimension(125, 24));

        filaDia.add(lblDia);
        filaDia.add(dpDia);

        panelDerecho.add(filaDia);
        panelDerecho.add(Box.createVerticalStrut(10));

        JButton btnLocal = UIUtils.crearBoton(
                "Guardar en local", UIUtils.COLOR_NARANJA);
        btnLocal.setAlignmentX(CENTER_ALIGNMENT);
        btnLocal.setMaximumSize(new Dimension(200, 38));
        btnLocal.setPreferredSize(new Dimension(200, 38));
        btnLocal.addActionListener(e -> exportarDia(false));

        JButton btnCorreo = UIUtils.crearBoton(
                "Enviar por correo", UIUtils.COLOR_ACTIVO);
        btnCorreo.setAlignmentX(CENTER_ALIGNMENT);
        btnCorreo.setMaximumSize(new Dimension(200, 38));
        btnCorreo.setPreferredSize(new Dimension(200, 38));
        btnCorreo.addActionListener(e -> exportarDia(true));

        panelDerecho.add(btnLocal);
        panelDerecho.add(Box.createVerticalStrut(8));
        panelDerecho.add(btnCorreo);
        panelDerecho.add(Box.createVerticalGlue());

        card.add(panelDerecho, BorderLayout.EAST);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LÓGICA EXPORTAR
    // ══════════════════════════════════════════════════════════════════════

    private void exportarCompleto(boolean porCorreo) {
        if (porCorreo && !validarDestinatarios()) return;

        String nombreArchivo = "historial_ranking_completo_"
                + LocalDateTime.now().format(FMT_ARCHIVO) + ".xlsx";

        File destino = porCorreo
                ? new File(System.getProperty("java.io.tmpdir"), nombreArchivo)
                : elegirArchivoDestino(nombreArchivo);
        if (destino == null) return;

        JDialog dlg = crearDialogoProgreso("Generando Excel...");

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Retraso> datos = retrasoDAO.historialCompleto();
                List<RankingEntry> ranking = calcularRankingDeLista(datos);
                generarExcelHistorial(destino, datos, ranking,
                        "Historial y Ranking Completo", null, null);
                return null;
            }
            @Override
            protected void done() {
                dlg.dispose();
                try {
                    get();
                    if (porCorreo) enviarATodos(destino,
                            "Historial y Ranking Completo");
                    else mostrarExitoExportacion(destino);
                } catch (Exception e) {
                    UIUtils.mostrarError(ExportarPanel.this,
                            "Error al exportar:\n" + e.getMessage());
                }
            }
        };
        w.execute();
    }

    private void exportarSemanal(boolean porCorreo) {
        if (porCorreo && !validarDestinatarios()) return;

        int semanas = (int) spinnerSemanas.getValue();
        LocalDate fin    = LocalDate.now().minusWeeks(semanas);
        LocalDate inicio = fin.minusDays(6);

        String nombreArchivo = "historial_ranking_semana_"
                + inicio.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                + "_" + fin.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                + ".xlsx";

        File destino = porCorreo
                ? new File(System.getProperty("java.io.tmpdir"), nombreArchivo)
                : elegirArchivoDestino(nombreArchivo);
        if (destino == null) return;

        JDialog dlg = crearDialogoProgreso("Generando Excel semanal...");

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Retraso> datos =
                        retrasoDAO.historialPorFechas(inicio, fin);
                List<RankingEntry> ranking = calcularRankingDeLista(datos);
                generarExcelHistorial(destino, datos, ranking,
                        "Historial y Ranking Semanal", inicio, fin);
                return null;
            }
            @Override
            protected void done() {
                dlg.dispose();
                try {
                    get();
                    if (porCorreo) enviarATodos(destino,
                            "Historial y Ranking Semanal ("
                            + inicio.format(DateTimeFormatter.ofPattern("dd/MM"))
                            + " - "
                            + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + ")");
                    else mostrarExitoExportacion(destino);
                } catch (Exception e) {
                    UIUtils.mostrarError(ExportarPanel.this,
                            "Error al exportar:\n" + e.getMessage());
                }
            }
        };
        w.execute();
    }

    private void exportarDia(boolean porCorreo) {
        if (porCorreo && !validarDestinatarios()) return;

        LocalDate dia = dpDia.getDate();
        if (dia == null) {
            UIUtils.mostrarError(this, "Selecciona un día.");
            return;
        }

        String nombreArchivo = "historial_ranking_dia_"
                + dia.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                + "_" + LocalDateTime.now().format(FMT_ARCHIVO) + ".xlsx";

        File destino = porCorreo
                ? new File(System.getProperty("java.io.tmpdir"), nombreArchivo)
                : elegirArchivoDestino(nombreArchivo);
        if (destino == null) return;

        JDialog dlg = crearDialogoProgreso("Generando Excel del día...");

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Retraso> datos =
                        retrasoDAO.historialPorFechas(dia, dia);
                List<RankingEntry> ranking = calcularRankingDeLista(datos);
                generarExcelHistorial(destino, datos, ranking,
                        "Historial y Ranking del día "
                        + dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        dia, dia);
                return null;
            }
            @Override
            protected void done() {
                dlg.dispose();
                try {
                    get();
                    if (porCorreo) enviarATodos(destino,
                            "Historial y Ranking del día "
                            + dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    else mostrarExitoExportacion(destino);
                } catch (Exception e) {
                    UIUtils.mostrarError(ExportarPanel.this,
                            "Error al exportar:\n" + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // ENVÍO A DESTINATARIOS
    // ══════════════════════════════════════════════════════════════════════

    private boolean validarDestinatarios() {
        boolean haySeleccionado = false;
        for (Component comp : panelListaCorreos.getComponents()) {
            if (comp instanceof JPanel panelFila) {
                for (Component hijo : panelFila.getComponents()) {
                    if (hijo instanceof JCheckBox chk && chk.isSelected()) {
                        haySeleccionado = true;
                        break;
                    }
                }
            }
            if (haySeleccionado) break;
        }
        if (!haySeleccionado) {
            UIUtils.mostrarError(this,
                    "No hay destinatarios seleccionados.\n"
                    + "Añade y marca al menos un correo "
                    + "en la sección 'Correo de Destinatario'.");
            return false;
        }
        return true;
    }

    private void enviarATodos(File archivo, String asunto) {
        List<String> seleccionados = new ArrayList<>();
        for (Component comp : panelListaCorreos.getComponents()) {
            if (comp instanceof JPanel panelFila) {
                for (Component hijo : panelFila.getComponents()) {
                    if (hijo instanceof JCheckBox chk && chk.isSelected()) {
                        seleccionados.add(
                                (String) chk.getClientProperty("correo"));
                    }
                }
            }
        }
        if (seleccionados.isEmpty()) {
            UIUtils.mostrarError(this,
                    "No hay destinatarios seleccionados.\n"
                    + "Marca al menos un correo.");
            return;
        }

        JDialog dlg = crearDialogoProgreso("Enviando correos...");

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int enviados = 0;
                for (String correo : seleccionados) {
                    emailService.enviarExcelAdjunto(correo, asunto, archivo);
                    enviados++;
                }
                return enviados;
            }
            @Override
            protected void done() {
                dlg.dispose();
                try {
                    int enviados = get();
                    JOptionPane.showMessageDialog(ExportarPanel.this,
                            "Excel enviado correctamente\na " + enviados
                            + " destinatario(s).",
                            "Correos enviados",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    UIUtils.mostrarError(ExportarPanel.this,
                            "Error al enviar:\n" + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERADOR EXCEL
    // ══════════════════════════════════════════════════════════════════════

    private void generarExcelHistorial(File destino, List<Retraso> historial,
            List<RankingEntry> ranking, String titulo,
            LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            crearHojaHistorial(wb, wb.createSheet("Historial"),
                    historial, titulo, fechaInicio, fechaFin);
            crearHojaRanking(wb, wb.createSheet("Ranking"), ranking);
            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }
        }
    }

    private void crearHojaHistorial(XSSFWorkbook wb, XSSFSheet hoja,
            List<Retraso> datos, String titulo,
            LocalDate fechaInicio, LocalDate fechaFin) {

        CellStyle estTitulo    = crearEstiloTitulo(wb);
        CellStyle estSubtitulo = crearEstiloSubtitulo(wb);
        CellStyle estCabecera  = crearEstiloCabecera(wb);
        CellStyle estNormal    = crearEstiloNormal(wb);
        CellStyle estAlternado = crearEstiloAlternado(wb);
        CellStyle estNumero    = crearEstiloNumero(wb);

        int fila = 0;

        Row rowT = hoja.createRow(fila++);
        rowT.setHeightInPoints(30);
        Cell cT = rowT.createCell(0);
        cT.setCellValue("CONTROL DE RETRASO — " + titulo.toUpperCase());
        cT.setCellStyle(estTitulo);
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        Row rowS = hoja.createRow(fila++);
        Cell cS  = rowS.createCell(0);
        if (fechaInicio != null) {
            String desde = fechaInicio.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String hasta = fechaFin.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            cS.setCellValue(desde.equals(hasta)
                    ? "Día: " + desde
                    : "Periodo: " + desde + " — " + hasta);
        } else {
            cS.setCellValue("Generado el: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        cS.setCellStyle(estSubtitulo);
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        Row rowR = hoja.createRow(fila++);
        Cell cR  = rowR.createCell(0);
        cR.setCellValue("Total de retrasos: " + datos.size());
        cR.setCellStyle(estSubtitulo);
        hoja.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));

        fila++;

        Row rowCab = hoja.createRow(fila++);
        rowCab.setHeightInPoints(22);
        String[] cabs = {"ID", "Fecha y Hora", "NIA",
                "Alumno", "Curso", "Minutos Tarde"};
        for (int i = 0; i < cabs.length; i++) {
            Cell c = rowCab.createCell(i);
            c.setCellValue(cabs[i]);
            c.setCellStyle(estCabecera);
        }

        int numFila = 0;
        for (Retraso r : datos) {
            Row row = hoja.createRow(fila++);
            row.setHeightInPoints(18);
            CellStyle estF = numFila % 2 == 0 ? estNormal : estAlternado;

            row.createCell(0).setCellValue(r.getId());
            row.getCell(0).setCellStyle(estNumero);
            row.createCell(1).setCellValue(r.getFechaHora().format(FMT_FECHA));
            row.getCell(1).setCellStyle(estF);
            row.createCell(2).setCellValue(r.getNia());
            row.getCell(2).setCellStyle(estF);
            row.createCell(3).setCellValue(r.getNombreCompleto());
            row.getCell(3).setCellStyle(estF);
            row.createCell(4).setCellValue(r.getCurso());
            row.getCell(4).setCellStyle(estF);
            row.createCell(5).setCellValue(r.getMinutosTarde());
            row.getCell(5).setCellStyle(estNumero);
            numFila++;
        }

        hoja.setColumnWidth(0, 2000);
        hoja.setColumnWidth(1, 5000);
        hoja.setColumnWidth(2, 3000);
        hoja.setColumnWidth(3, 8000);
        hoja.setColumnWidth(4, 5000);
        hoja.setColumnWidth(5, 4000);
        hoja.createFreezePane(0, 5);
    }

    private void crearHojaRanking(XSSFWorkbook wb, XSSFSheet hoja,
            List<RankingEntry> ranking) {

        CellStyle estTitulo    = crearEstiloTitulo(wb);
        CellStyle estCabecera  = crearEstiloCabecera(wb);
        CellStyle estNormal    = crearEstiloNormal(wb);
        CellStyle estAlternado = crearEstiloAlternado(wb);

        CellStyle estOro = crearEstiloPodio(wb, new XSSFColor(
                new byte[]{(byte)255,(byte)245,(byte)157}, null));
        CellStyle estPlata = crearEstiloPodio(wb, new XSSFColor(
                new byte[]{(byte)224,(byte)224,(byte)224}, null));
        CellStyle estBronce = crearEstiloPodio(wb, new XSSFColor(
                new byte[]{(byte)255,(byte)224,(byte)178}, null));

        int fila = 0;

        Row rowT = hoja.createRow(fila++);
        rowT.setHeightInPoints(28);
        Cell cT = rowT.createCell(0);
        cT.setCellValue("RANKING DE RETRASOS");
        cT.setCellStyle(estTitulo);
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        fila++;

        Row rowCab = hoja.createRow(fila++);
        rowCab.setHeightInPoints(22);
        String[] cabs = {"Posición", "NIA", "Alumno",
                "Curso", "Retrasos", "Min. Perdidos"};
        for (int i = 0; i < cabs.length; i++) {
            Cell c = rowCab.createCell(i);
            c.setCellValue(cabs[i]);
            c.setCellStyle(estCabecera);
        }

        int pos = 1;
        for (RankingEntry e : ranking) {
            Row row = hoja.createRow(fila++);
            row.setHeightInPoints(18);

            CellStyle estF;
            if      (pos == 1) estF = estOro;
            else if (pos == 2) estF = estPlata;
            else if (pos == 3) estF = estBronce;
            else estF = pos % 2 == 0 ? estAlternado : estNormal;

            row.createCell(0).setCellValue(pos++);
            row.getCell(0).setCellStyle(estF);
            row.createCell(1).setCellValue(e.getNia());
            row.getCell(1).setCellStyle(estF);
            row.createCell(2).setCellValue(e.getAlumno());
            row.getCell(2).setCellStyle(estF);
            row.createCell(3).setCellValue(e.getCurso());
            row.getCell(3).setCellStyle(estF);
            row.createCell(4).setCellValue(e.getTotalRetrasos());
            row.getCell(4).setCellStyle(estF);
            row.createCell(5).setCellValue(e.getTotalMinutosPerdidos());
            row.getCell(5).setCellStyle(estF);
        }

        hoja.setColumnWidth(0, 3000);
        hoja.setColumnWidth(1, 3000);
        hoja.setColumnWidth(2, 8000);
        hoja.setColumnWidth(3, 5000);
        hoja.setColumnWidth(4, 3500);
        hoja.setColumnWidth(5, 3500);
        hoja.createFreezePane(0, 3);
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS CÁLCULO
    // ══════════════════════════════════════════════════════════════════════

    private List<RankingEntry> calcularRankingDeLista(List<Retraso> retrasos) {
        java.util.Map<String, RankingEntry> mapa = new java.util.LinkedHashMap<>();
        for (Retraso r : retrasos) {
            mapa.computeIfAbsent(r.getNia(), k -> {
                RankingEntry e = new RankingEntry();
                e.setNia(r.getNia());
                e.setAlumno(r.getNombreCompleto());
                e.setCurso(r.getCurso());
                return e;
            });
            RankingEntry entry = mapa.get(r.getNia());
            entry.setTotalRetrasos(entry.getTotalRetrasos() + 1);
            entry.setTotalMinutosPerdidos(
                    entry.getTotalMinutosPerdidos() + r.getMinutosTarde());
        }
        return mapa.values().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getTotalRetrasos(), a.getTotalRetrasos()))
                .toList();
    }

    private String obtenerRangoSemana(int semanasAtras) {
        LocalDate fin    = LocalDate.now().minusWeeks(semanasAtras);
        LocalDate inicio = fin.minusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return inicio.format(fmt) + " — " + fin.format(fmt);
    }

    // ══════════════════════════════════════════════════════════════════════
    // ESTILOS EXCEL
    // ══════════════════════════════════════════════════════════════════════

    private CellStyle crearEstiloTitulo(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)44,(byte)62,(byte)80}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short)14);
        f.setColor(new XSSFColor(
                new byte[]{(byte)255,(byte)255,(byte)255}, null));
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle crearEstiloSubtitulo(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)52,(byte)73,(byte)94}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short)11);
        f.setColor(new XSSFColor(
                new byte[]{(byte)255,(byte)255,(byte)255}, null));
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle crearEstiloCabecera(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)52,(byte)152,(byte)219}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short)11);
        f.setColor(new XSSFColor(
                new byte[]{(byte)255,(byte)255,(byte)255}, null));
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle crearEstiloNormal(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)255,(byte)255,(byte)255}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont(); f.setFontHeightInPoints((short)11);
        s.setFont(f);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBorderLeft(BorderStyle.HAIR);
        s.setBorderRight(BorderStyle.HAIR);
        return s;
    }

    private CellStyle crearEstiloAlternado(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)235,(byte)245,(byte)251}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont(); f.setFontHeightInPoints((short)11);
        s.setFont(f);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBorderLeft(BorderStyle.HAIR);
        s.setBorderRight(BorderStyle.HAIR);
        return s;
    }

    private CellStyle crearEstiloNumero(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)255,(byte)255,(byte)255}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont(); f.setFontHeightInPoints((short)11);
        s.setFont(f);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBorderLeft(BorderStyle.HAIR);
        s.setBorderRight(BorderStyle.HAIR);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle crearEstiloPodio(XSSFWorkbook wb, XSSFColor color) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(color);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short)11);
        s.setFont(f);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBorderLeft(BorderStyle.HAIR);
        s.setBorderRight(BorderStyle.HAIR);
        return s;
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ══════════════════════════════════════════════════════════════════════

    private DatePickerSettings crearDatePickerSettings() {
        DatePickerSettings settings = new DatePickerSettings(
                new Locale("es", "ES"));
        settings.setFormatForDatesCommonEra("dd/MM/yyyy");
        settings.setColor(DatePickerSettings.DateArea
                .BackgroundOverallCalendarPanel, Color.WHITE);
        settings.setColor(DatePickerSettings.DateArea
                .CalendarBackgroundSelectedDate, new Color(180, 50, 50));
        settings.setColor(DatePickerSettings.DateArea
                .TextFieldBackgroundValidDate, Color.WHITE);
        settings.setAllowKeyboardEditing(false);
        return settings;
    }

    private JPanel crearCard() {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(UIUtils.COLOR_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        return card;
    }

    private JPanel crearTituloTarjeta(String texto) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setMinimumSize(new Dimension(100, 0));
        panel.setPreferredSize(new Dimension(150, 0));
        panel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, new Color(210, 215, 220)));

        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;'>"
                + texto.replace("\n", "<br>") + "</div></html>");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(UIUtils.COLOR_PRIMARIO);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 15);
        panel.add(lbl, gc);
        return panel;
    }

    private JPanel crearDescripcionCentrada(String texto) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JTextArea ta = new JTextArea(texto);
        ta.setEditable(false);
        ta.setOpaque(true);
        ta.setBackground(new Color(248, 249, 250));
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ta.setForeground(new Color(70, 70, 70));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        wrapper.add(ta, gc);
        return wrapper;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMinimumSize(new Dimension(160, 0));
        panel.setPreferredSize(new Dimension(210, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JFileChooser crearFileChooserGuardar(String nombreSugerido) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar archivo Excel");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Excel 2007-365 (*.xlsx)", "xlsx"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File(nombreSugerido));
        return fc;
    }

    private File asegurarExtension(File archivo) {
        if (!archivo.getName().toLowerCase().endsWith(".xlsx"))
            return new File(archivo.getAbsolutePath() + ".xlsx");
        return archivo;
    }

    private File elegirArchivoDestino(String nombreSugerido) {
        JFileChooser fc = crearFileChooserGuardar(nombreSugerido);
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return null;
        return asegurarExtension(fc.getSelectedFile());
    }

    private JDialog crearDialogoProgreso(String mensaje) {
        JDialog dlg = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                mensaje, false);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setString(mensaje);
        bar.setStringPainted(true);
        bar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        dlg.add(bar);
        dlg.setSize(320, 85);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return dlg;
    }

    private void mostrarExitoExportacion(File archivo) {
        int resp = JOptionPane.showConfirmDialog(this,
                "Archivo generado correctamente:\n"
                + archivo.getAbsolutePath()
                + "\n\n¿Abrir la carpeta?",
                "Exportación completada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().open(archivo.getParentFile());
            } catch (Exception ex) {
                System.err.println("No se pudo abrir: " + ex.getMessage());
            }
        }
    }
}