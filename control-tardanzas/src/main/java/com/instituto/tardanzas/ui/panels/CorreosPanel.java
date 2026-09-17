package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.CorreoDAO;
import com.instituto.tardanzas.model.Correo;
import com.instituto.tardanzas.service.EmailService;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CorreosPanel extends JPanel {

    private final CorreoDAO    correoDAO;
    private final EmailService emailService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm");

    private JTable            tabla;
    private DefaultTableModel modelo;
    private JComboBox<String> cbFiltro;
    private Timer             timerRefresco;
    private JLabel            lblContador;
    private JLabel            lblUltimaAct;

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════

    public CorreosPanel(EmailService emailService) {
        this.correoDAO    = new CorreoDAO();
        this.emailService = emailService;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15));
        construirUI();
        cargarCorreos();
        iniciarAutoRefresco();
    }

    // ── Método público para actualización externa ─────────────────────────

    public void refrescar() {
        SwingUtilities.invokeLater(this::cargarCorreos);
    }

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUIR UI
    // ══════════════════════════════════════════════════════════════════════

    private void construirUI() {

        // ── Norte: título + toolbar ───────────────────────────────────────
        JPanel norte =
                new JPanel(new BorderLayout(0, 8));
        norte.setOpaque(false);
        norte.add(
                UIUtils.crearTitulo("Estado de Correos"),
                BorderLayout.NORTH);

        JPanel toolbar = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JLabel lblEstado = new JLabel("Filtrar:");
        lblEstado.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        cbFiltro = new JComboBox<>(new String[]{
                "TODOS", "PENDIENTE",
                "ENVIADO", "ERROR"});
        cbFiltro.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));
        cbFiltro.setPreferredSize(
                new Dimension(130, 30));
        cbFiltro.addActionListener(
                e -> cargarCorreos());

        JButton btnReintentar = UIUtils.crearBoton(
                "Reintentar error",
                UIUtils.COLOR_NARANJA);
        JButton btnEnviarAhora = UIUtils.crearBoton(
                "Enviar ahora",
                UIUtils.COLOR_VERDE);

        btnReintentar.addActionListener(
                e -> reintentar());
        btnEnviarAhora.addActionListener(
                e -> enviarAhora());

        toolbar.add(lblEstado);
        toolbar.add(cbFiltro);
        toolbar.add(btnReintentar);
        toolbar.add(btnEnviarAhora);

        norte.add(toolbar, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // ── Tabla ─────────────────────────────────────────────────────────
        String[] cols = {
                "ID", "Estado", "Alumno",
                "Email", "Asunto",
                "Creacion", "Envio"};

        modelo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(
                    int r, int c) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        UIUtils.estilizarTabla(tabla);

        tabla.getColumnModel()
                .getColumn(0).setMaxWidth(50);
        tabla.getColumnModel()
                .getColumn(1).setPreferredWidth(90);
        tabla.getColumnModel()
                .getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel()
                .getColumn(3).setPreferredWidth(150);
        tabla.getColumnModel()
                .getColumn(4).setPreferredWidth(200);
        tabla.getColumnModel()
                .getColumn(5).setPreferredWidth(130);
        tabla.getColumnModel()
                .getColumn(6).setPreferredWidth(130);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        // Renderer de color para columna Estado
        tabla.getColumnModel().getColumn(1)
                .setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component
                    getTableCellRendererComponent(
                    JTable t, Object val,
                    boolean sel, boolean foc,
                    int row, int col) {

                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);

                String estado = val != null
                        ? val.toString() : "";

                if (!sel) {
                    setBackground(switch (estado) {
                        case "ENVIADO" ->
                            new Color(212, 239, 223);
                        case "PENDIENTE" ->
                            new Color(254, 249, 219);
                        case "ERROR" ->
                            new Color(250, 219, 216);
                        default ->
                            UIUtils.COLOR_BLANCO;
                    });
                    setForeground(
                            UIUtils.COLOR_PRIMARIO);
                }

                setFont(getFont()
                        .deriveFont(Font.BOLD));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        JScrollPane scroll =
                new JScrollPane(tabla);
        scroll.setBorder(
                BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // ── Sur: contador + última actualización ──────────────────────────
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.setBorder(BorderFactory.createEmptyBorder(
                5, 0, 0, 0));

        lblContador = new JLabel("Total: 0 correos");
        lblContador.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));
        lblContador.setForeground(
                UIUtils.COLOR_TEXTO_CLARO);

        lblUltimaAct = new JLabel(
            "Actualizacion automatica cada 30 segundos");
        lblUltimaAct.setFont(
                new Font("Segoe UI", Font.ITALIC, 11));
        lblUltimaAct.setForeground(
                UIUtils.COLOR_TEXTO_CLARO);

        sur.add(lblContador,  BorderLayout.WEST);
        sur.add(lblUltimaAct, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARGAR CORREOS
    // ══════════════════════════════════════════════════════════════════════

    private void cargarCorreos() {
        String estado =
                (String) cbFiltro.getSelectedItem();

        SwingWorker<List<Correo>, Void> w =
                new SwingWorker<>() {
            @Override
            protected List<Correo> doInBackground()
                    throws Exception {
                // Usa obtenerPorEstado (nombre correcto)
                return correoDAO.obtenerPorEstado(
                        estado);
            }
            @Override
            protected void done() {
                try {
                    List<Correo> lista = get();
                    modelo.setRowCount(0);

                    for (Correo c : lista) {
                        modelo.addRow(new Object[]{
                            c.getId(),
                            c.getEstado(),
                            c.getNombreAlumno(),
                            c.getEmailFamilia(),
                            c.getAsunto(),
                            c.getFechaCreacion() != null
                                ? c.getFechaCreacion()
                                    .format(FMT)
                                : "-",
                            c.getFechaEnvio() != null
                                ? c.getFechaEnvio()
                                    .format(FMT)
                                : "-"
                        });
                    }

                    lblContador.setText(
                        "Total: "
                        + modelo.getRowCount()
                        + " correo(s)");

                    lblUltimaAct.setText(
                        "Ultima actualizacion: "
                        + java.time.LocalTime.now()
                            .format(DateTimeFormatter
                                .ofPattern("HH:mm:ss")));

                } catch (Exception e) {
                    UIUtils.mostrarError(
                            CorreosPanel.this,
                            "Error al cargar correos: "
                            + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // AUTO-REFRESCO
    // ══════════════════════════════════════════════════════════════════════

    private void iniciarAutoRefresco() {
        timerRefresco = new Timer(30_000,
                e -> cargarCorreos());
        timerRefresco.start();

        addAncestorListener(
            new javax.swing.event.AncestorListener() {
                @Override
                public void ancestorAdded(
                        javax.swing.event
                            .AncestorEvent e) {
                    timerRefresco.start();
                }
                @Override
                public void ancestorRemoved(
                        javax.swing.event
                            .AncestorEvent e) {
                    timerRefresco.stop();
                }
                @Override
                public void ancestorMoved(
                        javax.swing.event
                            .AncestorEvent e) {}
            });
    }

    // ══════════════════════════════════════════════════════════════════════
    // REINTENTAR
    // ══════════════════════════════════════════════════════════════════════

    private void reintentar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this,
                "Selecciona un correo con "
                + "estado ERROR.");
            return;
        }

        String estado =
                (String) modelo.getValueAt(fila, 1);
        if (!"ERROR".equals(estado)) {
            UIUtils.mostrarError(this,
                "Solo se pueden reintentar correos "
                + "en estado ERROR.");
            return;
        }

        int id = (int) modelo.getValueAt(fila, 0);

        SwingWorker<Void, Void> w =
                new SwingWorker<>() {
            @Override
            protected Void doInBackground()
                    throws Exception {
                // Usa reintentar (nombre correcto)
                correoDAO.reintentar(id);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    // Intentar envío en hilo separado
                    new Thread(() -> {
                        emailService.procesarPendientes();
                        SwingUtilities.invokeLater(
                                () -> cargarCorreos());
                    }, "correo-reintento").start();

                    UIUtils.mostrarInfo(
                        CorreosPanel.this,
                        "Reintentando envio "
                        + "del correo...");

                } catch (Exception e) {
                    UIUtils.mostrarError(
                        CorreosPanel.this,
                        "Error al reintentar: "
                        + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // ENVIAR AHORA
    // ══════════════════════════════════════════════════════════════════════

    private void enviarAhora() {
        UIUtils.mostrarInfo(this,
            "Iniciando envio de correos pendientes...");

        new Thread(() -> {
            emailService.procesarPendientes();
            SwingUtilities.invokeLater(
                    () -> cargarCorreos());
        }, "correo-ahora").start();
    }
}
