package com.instituto.tardanzas.ui.panels;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.instituto.tardanzas.dao.RetrasoDAO;
import com.instituto.tardanzas.model.Retraso;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HistorialPanel extends JPanel {

    private final RetrasoDAO retrasoDAO = new RetrasoDAO();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JTable            tabla;
    private DefaultTableModel modelo;
    private JRadioButton      rbHoy, rbTodo, rbAlumno, rbFechas;
    private JTextField        tfNia;
    private DatePicker        dpDesde, dpHasta;
    private JLabel            lblTotal;
    private Timer             timerRefresco;

    public HistorialPanel() {
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        construirUI();
        iniciarAutoRefresco();
    }

    public void refrescar() {
        SwingUtilities.invokeLater(this::recargarSegunFiltro);
    }

    // ══════════════════════════════════════════════════════════════════════
    // DATEPICKER SETTINGS
    // ══════════════════════════════════════════════════════════════════════

    private DatePickerSettings crearDatePickerSettings() {
        DatePickerSettings settings = new DatePickerSettings(
                new Locale("es", "ES"));
        settings.setFormatForDatesCommonEra("dd/MM/yyyy");

        settings.setColor(
                DatePickerSettings.DateArea.BackgroundOverallCalendarPanel,
                Color.WHITE);
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundMonthAndYearMenuLabels,
                Color.WHITE);
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundMonthAndYearNavigationButtons,
                Color.WHITE);
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundTodayLabel,
                Color.WHITE);
        settings.setColor(
                DatePickerSettings.DateArea.TextTodayLabel,
                new Color(80, 80, 80));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarBackgroundNormalDates,
                Color.WHITE);
        settings.setColor(
                DatePickerSettings.DateArea.CalendarTextNormalDates,
                new Color(50, 50, 50));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarBackgroundSelectedDate,
                new Color(180, 50, 50));
        settings.setColor(
                DatePickerSettings.DateArea.BackgroundCalendarPanelLabelsOnHover,
                new Color(240, 240, 240));
        settings.setColor(
                DatePickerSettings.DateArea.CalendarBackgroundVetoedDates,
                Color.WHITE);
        settings.setColor(
                DatePickerSettings.DateArea.TextFieldBackgroundValidDate,
                Color.WHITE);
        settings.setColor(
                DatePickerSettings.DateArea.TextFieldBackgroundInvalidDate,
                new Color(255, 235, 235));

        settings.setAllowKeyboardEditing(false);

        return settings;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUIR UI
    // ══════════════════════════════════════════════════════════════════════

    private void construirUI() {
        JPanel norte = new JPanel(new BorderLayout(0, 8));
        norte.setOpaque(false);
        norte.add(UIUtils.crearTitulo("Historial de Tardanzas"),
                BorderLayout.NORTH);

        rbHoy    = new JRadioButton("Hoy", true);
        rbTodo   = new JRadioButton("Completo");
        rbFechas = new JRadioButton("Por fechas:");
        rbAlumno = new JRadioButton("Por alumno (NIA):");

        for (JRadioButton rb : new JRadioButton[]{
                rbHoy, rbTodo, rbFechas, rbAlumno}) {
            rb.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13));
            rb.setOpaque(false);
            rb.setCursor(Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR));
        }

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbHoy);
        grupo.add(rbTodo);
        grupo.add(rbFechas);
        grupo.add(rbAlumno);

        tfNia = UIUtils.crearCampo(8);
        tfNia.setEnabled(false);
        tfNia.setPreferredSize(new Dimension(80, 28));

        dpDesde = new DatePicker(crearDatePickerSettings());
        dpHasta = new DatePicker(crearDatePickerSettings());

        dpDesde.setDate(LocalDate.now());
        dpHasta.setDate(LocalDate.now());

        dpDesde.setEnabled(false);
        dpHasta.setEnabled(false);

        dpDesde.setPreferredSize(new Dimension(130, 28));
        dpHasta.setPreferredSize(new Dimension(130, 28));

        rbHoy.addActionListener(e -> {
            tfNia.setEnabled(false);
            dpDesde.setEnabled(false);
            dpHasta.setEnabled(false);
            cargarHistorialHoy();
        });
        rbTodo.addActionListener(e -> {
            tfNia.setEnabled(false);
            dpDesde.setEnabled(false);
            dpHasta.setEnabled(false);
            cargarHistorialCompleto();
        });
        rbFechas.addActionListener(e -> {
            tfNia.setEnabled(false);
            dpDesde.setEnabled(true);
            dpHasta.setEnabled(true);
            cargarHistorialFechas();
        });
        rbAlumno.addActionListener(e -> {
            tfNia.setEnabled(true);
            dpDesde.setEnabled(false);
            dpHasta.setEnabled(false);
            tfNia.requestFocus();
        });

        tfNia.addActionListener(e -> cargarHistorialAlumno());
        tfNia.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            private Timer delayTimer;
            @Override public void insertUpdate(
                    javax.swing.event.DocumentEvent e) {
                disparar();
            }
            @Override public void removeUpdate(
                    javax.swing.event.DocumentEvent e) {
                disparar();
            }
            @Override public void changedUpdate(
                    javax.swing.event.DocumentEvent e) {
                disparar();
            }
            private void disparar() {
                if (delayTimer != null) delayTimer.stop();
                delayTimer = new Timer(600, ev -> {
                    if (rbAlumno.isSelected() &&
                            tfNia.getText().trim()
                                    .length() == 8)
                        cargarHistorialAlumno();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });

        dpDesde.addDateChangeListener(e -> {
            if (rbFechas.isSelected())
                cargarHistorialFechas();
        });
        dpHasta.addDateChangeListener(e -> {
            if (rbFechas.isSelected())
                cargarHistorialFechas();
        });

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        JSeparator sep = new JSeparator(
                SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 20));
        sep.setForeground(new Color(200, 200, 200));

        JPanel filtros = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setOpaque(false);

        filtros.add(rbHoy);
        filtros.add(rbTodo);
        filtros.add(rbFechas);
        filtros.add(lblDesde);
        filtros.add(dpDesde);
        filtros.add(lblHasta);
        filtros.add(dpHasta);
        filtros.add(sep);
        filtros.add(rbAlumno);
        filtros.add(tfNia);

        norte.add(filtros, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        String[] columnas = {"ID", "Fecha y Hora", "NIA",
                "Alumno", "Curso", "Minutos Tarde"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(
                    int r, int c) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        UIUtils.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(2).setMaxWidth(80);
        tabla.getColumnModel().getColumn(5)
                .setPreferredWidth(110);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.setBorder(BorderFactory.createEmptyBorder(
                4, 0, 0, 0));

        lblTotal = new JLabel("Total: 0 registros");
        lblTotal.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));
        lblTotal.setForeground(UIUtils.COLOR_TEXTO_CLARO);

        JLabel lblAuto = new JLabel(
                "Actualizacion automatica cada 15 segundos");
        lblAuto.setFont(
                new Font("Segoe UI", Font.ITALIC, 11));
        lblAuto.setForeground(UIUtils.COLOR_TEXTO_CLARO);

        sur.add(lblTotal, BorderLayout.WEST);
        sur.add(lblAuto,  BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════════════
    // AUTO-REFRESCO
    // ══════════════════════════════════════════════════════════════════════

    private void iniciarAutoRefresco() {
        cargarHistorialHoy();

        timerRefresco = new Timer(15_000,
                e -> recargarSegunFiltro());
        timerRefresco.start();

        addAncestorListener(
                new javax.swing.event.AncestorListener() {
            @Override public void ancestorAdded(
                    javax.swing.event.AncestorEvent e) {
                timerRefresco.start();
            }
            @Override public void ancestorRemoved(
                    javax.swing.event.AncestorEvent e) {
                timerRefresco.stop();
            }
            @Override public void ancestorMoved(
                    javax.swing.event.AncestorEvent e) {}
        });
    }

    private void recargarSegunFiltro() {
        if      (rbHoy   .isSelected()) cargarHistorialHoy();
        else if (rbTodo  .isSelected()) cargarHistorialCompleto();
        else if (rbFechas.isSelected()) cargarHistorialFechas();
        else if (rbAlumno.isSelected()) cargarHistorialAlumno();
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARGAS
    // ══════════════════════════════════════════════════════════════════════

    private void cargarHistorialHoy() {
        ejecutarCarga(() -> retrasoDAO.historialHoy());
    }

    private void cargarHistorialCompleto() {
        ejecutarCarga(() -> retrasoDAO.historialCompleto());
    }

    private void cargarHistorialAlumno() {
        String nia = tfNia.getText().trim();
        if (nia.isEmpty()) {
            modelo.setRowCount(0);
            lblTotal.setText("Total: 0 registros");
            return;
        }
        ejecutarCarga(
                () -> retrasoDAO.historialPorAlumno(nia));
    }

    private void cargarHistorialFechas() {
        LocalDate desde = dpDesde.getDate();
        LocalDate hasta = dpHasta.getDate();

        if (desde == null || hasta == null) return;

        if (desde.isAfter(hasta)) {
            // No mostrar error en auto-refresco
            return;
        }

        ejecutarCarga(() ->
                retrasoDAO.historialPorFechas(
                        desde, hasta));
    }

    @FunctionalInterface
    private interface Loader {
        List<Retraso> load() throws Exception;
    }

    private void ejecutarCarga(Loader loader) {
        SwingWorker<List<Retraso>, Void> w =
                new SwingWorker<>() {
            @Override
            protected List<Retraso> doInBackground()
                    throws Exception {
                return loader.load();
            }
            @Override protected void done() {
                try {
                    poblarTabla(get());
                } catch (Exception e) {
                    // No mostrar popup — solo log
                    // (puede pasar durante reset o cuando la BD
                    // está temporalmente inaccesible)
                    System.err.println(
                            "[HistorialPanel] Error al cargar: "
                            + e.getMessage());
                    modelo.setRowCount(0);
                    lblTotal.setText("Total: 0 registros");
                }
            }
        };
        w.execute();
    }

    private void poblarTabla(List<Retraso> lista) {
        modelo.setRowCount(0);
        if (lista == null) {
            lblTotal.setText("Total: 0 registros");
            return;
        }
        for (Retraso r : lista) {
            String fechaHora = r.getFechaHora() != null
                    ? r.getFechaHora().format(FMT) : "-";
            modelo.addRow(new Object[]{
                r.getId(),
                fechaHora,
                r.getNia() != null ? r.getNia() : "-",
                r.getNombreCompleto() != null ?
                        r.getNombreCompleto() : "-",
                r.getCurso() != null ? r.getCurso() : "-",
                r.getMinutosTarde() + " min"
            });
        }
        lblTotal.setText(
                "Total: " + lista.size() + " registros");
    }
}
