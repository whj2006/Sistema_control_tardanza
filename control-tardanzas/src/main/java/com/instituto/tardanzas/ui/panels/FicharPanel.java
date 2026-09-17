package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.AlumnoDAO;
import com.instituto.tardanzas.dao.CorreoDAO;
import com.instituto.tardanzas.dao.CursoDAO;
import com.instituto.tardanzas.dao.RetrasoDAO;
import com.instituto.tardanzas.model.Alumno;
import com.instituto.tardanzas.model.Curso;
import com.instituto.tardanzas.model.Retraso;
import com.instituto.tardanzas.service.EmailService;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FicharPanel extends JPanel {

    private static final LocalTime HORA_INICIO =
            LocalTime.of(8, 0);
    private static final DateTimeFormatter FMT_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private final RetrasoDAO   retrasoDAO;
    private final EmailService emailService;
    private final Runnable     onRetrasoRegistrado;
    private final AlumnoDAO    alumnoDAO  = new AlumnoDAO();
    private final CursoDAO     cursoDAO   = new CursoDAO();
    private final CorreoDAO    correoDAO  = new CorreoDAO();

    private JTextField        tfNia;
    private JComboBox<Curso>  cbCurso;
    private JComboBox<Alumno> cbAlumno;

    private JPanel  bannerPanel;
    private JLabel  bannerTexto;
    private JLabel  bannerSub;
    private Timer   timerBanner;

    private DefaultTableModel modeloHistorial;
    private JTable            tablaHistorial;
    private JLabel            lblContador;

    private JLayeredPane layeredPane;
    private JPanel       contenidoPanel;

    private final List<Integer> idsHistorial =
            new ArrayList<>();

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ══════════════════════════════════════════════════════════════════════

    public FicharPanel(EmailService emailService,
                       Runnable onRetrasoRegistrado) {
        this.retrasoDAO          = new RetrasoDAO();
        this.emailService        = emailService;
        this.onRetrasoRegistrado = onRetrasoRegistrado;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
        construirUI();
        cargarCursos();
        cargarHistorialHoy();
    }

    public FicharPanel(EmailService emailService) {
        this(emailService, null);
    }

    public void refrescarCursos() {
        SwingUtilities.invokeLater(this::cargarCursos);
    }

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUCCIÓN UI
    // ══════════════════════════════════════════════════════════════════════

    private void construirUI() {
        bannerPanel = new JPanel(
                new GridLayout(2, 1, 0, 2));
        bannerPanel.setBorder(
                new EmptyBorder(10, 15, 10, 15));
        bannerPanel.setVisible(false);

        bannerTexto = new JLabel("",
                SwingConstants.CENTER);
        bannerTexto.setFont(
                new Font("Segoe UI", Font.BOLD, 16));
        bannerTexto.setForeground(Color.WHITE);

        bannerSub = new JLabel("",
                SwingConstants.CENTER);
        bannerSub.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));
        bannerSub.setForeground(
                new Color(255, 255, 255, 210));

        bannerPanel.add(bannerTexto);
        bannerPanel.add(bannerSub);

        JPanel izquierdo =
                new JPanel(new GridLayout(2, 1, 0, 10));
        izquierdo.setOpaque(false);
        izquierdo.setBorder(
                new EmptyBorder(10, 15, 10, 8));
        izquierdo.add(construirTarjetaNia());
        izquierdo.add(construirTarjetaLista());

        JPanel derecho = construirPanelHistorial();

        contenidoPanel =
                new JPanel(new GridLayout(1, 2, 0, 0));
        contenidoPanel.setOpaque(false);
        contenidoPanel.add(izquierdo);
        contenidoPanel.add(derecho);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.add(contenidoPanel,
                JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(bannerPanel,
                JLayeredPane.POPUP_LAYER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(
                    ComponentEvent e) {
                ajustarTamanos();
            }
        });

        add(layeredPane, BorderLayout.CENTER);
    }

    // ── Banner arriba (comportamiento original) ───────────────────────────
    private void ajustarTamanos() {
        int w = layeredPane.getWidth();
        int h = layeredPane.getHeight();
        contenidoPanel.setBounds(0, 0, w, h);
        bannerPanel.setBounds(0, 0, w, 60);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TARJETA NIA
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirTarjetaNia() {
        JPanel card = crearCard();

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(
                new BoxLayout(contenido,
                        BoxLayout.Y_AXIS));

        JLabel lblTitulo =
                new JLabel("Fichar por NIA");
        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARIO);
        lblTitulo.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(
            "<html>Escanea o introduce el NIA "
            + "del alumno para registrar su "
            + "retraso.</html>");
        lblSub.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 220, 220));
        sep.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNia = new JLabel("NIA:");
        lblNia.setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        lblNia.setForeground(new Color(80, 80, 80));
        lblNia.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfNia = new JTextField();
        tfNia.setFont(
                new Font("Segoe UI", Font.BOLD, 28));
        tfNia.setHorizontalAlignment(JTextField.CENTER);
        tfNia.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 50));
        tfNia.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfNia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(189, 195, 199),
                        1, true),
                new EmptyBorder(5, 10, 5, 10)));
        tfNia.addActionListener(e -> ficharPorNia());

        JButton btnRegistrar = UIUtils.crearBoton(
                "Registrar Retraso",
                UIUtils.COLOR_PRIMARIO);
        btnRegistrar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42));
        btnRegistrar.setAlignmentX(
                Component.LEFT_ALIGNMENT);
        btnRegistrar.setFont(
                new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrar.addActionListener(
                e -> ficharPorNia());

        JLabel lblNota = new JLabel(
                "Pulsa Enter o haz clic en el boton");
        lblNota.setFont(
                new Font("Segoe UI", Font.ITALIC, 11));
        lblNota.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        lblNota.setAlignmentX(Component.LEFT_ALIGNMENT);

        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(lblSub);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(sep);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(lblNia);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(tfNia);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(btnRegistrar);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(lblNota);

        card.add(contenido);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TARJETA LISTA
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirTarjetaLista() {
        JPanel card = crearCard();

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(
                new BoxLayout(contenido,
                        BoxLayout.Y_AXIS));

        JLabel lblTitulo =
                new JLabel("Fichar por Lista");
        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARIO);
        lblTitulo.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(
            "<html>Selecciona el curso y el alumno "
            + "de las listas desplegables.</html>");
        lblSub.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 220, 220));
        sep.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCurso = new JLabel("Curso:");
        lblCurso.setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        lblCurso.setForeground(new Color(80, 80, 80));
        lblCurso.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbCurso = new JComboBox<>();
        cbCurso.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));
        cbCurso.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 36));
        cbCurso.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbCurso.addActionListener(e -> {
            Curso sel =
                    (Curso) cbCurso.getSelectedItem();
            if (sel != null && sel.getId() != 0)
                cargarAlumnosDeCurso(sel.getId());
        });

        JLabel lblAlumno = new JLabel("Alumno:");
        lblAlumno.setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        lblAlumno.setForeground(new Color(80, 80, 80));
        lblAlumno.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        cbAlumno = new JComboBox<>();
        cbAlumno.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));
        cbAlumno.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 36));
        cbAlumno.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JButton btnRegistrar = UIUtils.crearBoton(
                "Registrar Retraso",
                UIUtils.COLOR_PRIMARIO);
        btnRegistrar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42));
        btnRegistrar.setAlignmentX(
                Component.LEFT_ALIGNMENT);
        btnRegistrar.setFont(
                new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrar.addActionListener(
                e -> ficharPorLista());

        JLabel lblNota = new JLabel(
            "El curso y alumno deben estar registrados");
        lblNota.setFont(
                new Font("Segoe UI", Font.ITALIC, 11));
        lblNota.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        lblNota.setAlignmentX(Component.LEFT_ALIGNMENT);

        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(lblSub);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(sep);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(lblCurso);
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(cbCurso);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(lblAlumno);
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(cbAlumno);
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(btnRegistrar);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(lblNota);

        card.add(contenido);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // PANEL HISTORIAL — con columna Estado
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirPanelHistorial() {
        JPanel card = new JPanel(
                new BorderLayout(0, 8));
        card.setBackground(UIUtils.COLOR_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 215, 220), 1),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel cabecera =
                new JPanel(new BorderLayout(10, 0));
        cabecera.setOpaque(false);

        JPanel ladoIzq = new JPanel();
        ladoIzq.setOpaque(false);
        ladoIzq.setLayout(
                new BoxLayout(ladoIzq,
                        BoxLayout.Y_AXIS));

        JLabel lblTitulo =
                new JLabel("Historial de Hoy");
        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARIO);
        lblTitulo.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        lblContador = new JLabel("0 retrasos");
        lblContador.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));
        lblContador.setForeground(
                UIUtils.COLOR_TEXTO_CLARO);
        lblContador.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        ladoIzq.add(lblTitulo);
        ladoIzq.add(Box.createVerticalStrut(2));
        ladoIzq.add(lblContador);

        JButton btnEliminar =
                new JButton("Eliminar fichaje");
        btnEliminar.setFont(
                new Font("Segoe UI", Font.BOLD, 11));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setBackground(
                new Color(180, 30, 30));
        btnEliminar.setBorderPainted(false);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));
        btnEliminar.setPreferredSize(
                new Dimension(130, 30));
        btnEliminar.setToolTipText(
            "Selecciona una fila y pulsa para "
            + "eliminar el fichaje erroneo");

        btnEliminar.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(
                    java.awt.event.MouseEvent e) {
                btnEliminar.setBackground(
                        new Color(200, 40, 40));
            }
            @Override
            public void mouseExited(
                    java.awt.event.MouseEvent e) {
                btnEliminar.setBackground(
                        new Color(180, 30, 30));
            }
        });

        btnEliminar.addActionListener(
                e -> eliminarFichajeSeleccionado());

        JPanel ladoDer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0));
        ladoDer.setOpaque(false);
        ladoDer.add(btnEliminar);

        cabecera.add(ladoIzq, BorderLayout.WEST);
        cabecera.add(ladoDer, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 220, 220));

        String[] columnas =
                {"Alumno", "Curso", "Hora",
                 "Min", "Estado"};
        modeloHistorial =
                new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(
                    int r, int c) {
                return false;
            }
        };

        tablaHistorial = new JTable(modeloHistorial);
        UIUtils.estilizarTabla(tablaHistorial);
        tablaHistorial.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tablaHistorial.getColumnModel()
                .getColumn(0).setPreferredWidth(130);
        tablaHistorial.getColumnModel()
                .getColumn(1).setPreferredWidth(70);
        tablaHistorial.getColumnModel()
                .getColumn(2).setPreferredWidth(48);
        tablaHistorial.getColumnModel()
                .getColumn(3).setPreferredWidth(36);
        tablaHistorial.getColumnModel()
                .getColumn(4).setPreferredWidth(80);

        javax.swing.table.DefaultTableCellRenderer
                centrado =
                new javax.swing.table
                    .DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(
                SwingConstants.CENTER);
        tablaHistorial.getColumnModel()
                .getColumn(2).setCellRenderer(centrado);
        tablaHistorial.getColumnModel()
                .getColumn(3).setCellRenderer(centrado);

        tablaHistorial.getColumnModel().getColumn(4)
                .setCellRenderer(
                new javax.swing.table
                        .DefaultTableCellRenderer() {
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
                        case "ENVIADO"   ->
                            new Color(212, 239, 223);
                        case "PENDIENTE" ->
                            new Color(254, 249, 219);
                        case "ERROR"     ->
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
                new JScrollPane(tablaHistorial);
        scroll.setBorder(
                BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(
                UIUtils.COLOR_BLANCO);

        JLabel lblNota = new JLabel(
            "Selecciona una fila para poder eliminarla");
        lblNota.setFont(
                new Font("Segoe UI", Font.ITALIC, 10));
        lblNota.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        lblNota.setHorizontalAlignment(
                SwingConstants.CENTER);
        lblNota.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel cuerpo =
                new JPanel(new BorderLayout(0, 6));
        cuerpo.setOpaque(false);
        cuerpo.add(sep,     BorderLayout.NORTH);
        cuerpo.add(scroll,  BorderLayout.CENTER);
        cuerpo.add(lblNota, BorderLayout.SOUTH);

        card.add(cabecera, BorderLayout.NORTH);
        card.add(cuerpo,   BorderLayout.CENTER);

        JPanel wrapper = new JPanel(
                new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(
                new EmptyBorder(10, 0, 10, 15));
        wrapper.add(card, BorderLayout.CENTER);

        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER CARD
    // ══════════════════════════════════════════════════════════════════════

    private JPanel crearCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtils.COLOR_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 215, 220), 1),
                new EmptyBorder(20, 30, 20, 30)));
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARGAR DATOS
    // ══════════════════════════════════════════════════════════════════════

    private void cargarCursos() {
        SwingWorker<List<Curso>, Void> w =
                new SwingWorker<>() {
            @Override
            protected List<Curso> doInBackground()
                    throws Exception {
                return cursoDAO.listarTodos();
            }
            @Override
            protected void done() {
                try {
                    cbCurso.removeAllItems();
                    cbCurso.addItem(new Curso(
                            0,
                            "-- Selecciona curso --"));
                    get().forEach(cbCurso::addItem);
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void cargarAlumnosDeCurso(int idCurso) {
        if (idCurso == 0) return;
        SwingWorker<List<Alumno>, Void> w =
                new SwingWorker<>() {
            @Override
            protected List<Alumno> doInBackground()
                    throws Exception {
                return alumnoDAO.listarPorCurso(idCurso);
            }
            @Override
            protected void done() {
                try {
                    cbAlumno.removeAllItems();
                    cbAlumno.addItem(new Alumno(
                            "",
                            "-- Selecciona alumno --",
                            "", "", "", "", 0));
                    get().forEach(cbAlumno::addItem);
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    public void cargarHistorialHoy() {
        SwingWorker<List<Retraso>, Void> w =
                new SwingWorker<>() {
            @Override
            protected List<Retraso> doInBackground()
                    throws Exception {
                return retrasoDAO.historialHoy();
            }
            @Override
            protected void done() {
                try {
                    List<Retraso> lista = get();
                    modeloHistorial.setRowCount(0);
                    idsHistorial.clear();

                    for (Retraso r : lista) {
                        String hora = r.getFechaHora()
                                .format(FMT_HORA);
                        String nombre = abreviarNombre(
                                r.getNombreCompleto());

                        String estado = "PENDIENTE";
                        try {
                            estado = correoDAO
                                .obtenerEstadoPorRetraso(
                                        r.getId());
                        } catch (Exception ignored) {}

                        modeloHistorial.addRow(
                                new Object[]{
                                    nombre,
                                    r.getCurso(),
                                    hora,
                                    r.getMinutosTarde(),
                                    estado
                                });
                        idsHistorial.add(r.getId());
                    }

                    int total = lista.size();
                    lblContador.setText(total == 0
                        ? "Sin retrasos hoy"
                        : total + (total == 1
                            ? " retraso registrado"
                            : " retrasos registrados"));

                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private String abreviarNombre(
            String nombreCompleto) {
        if (nombreCompleto == null) return "";
        String[] partes =
                nombreCompleto.trim().split("\\s+");
        if (partes.length <= 2) return nombreCompleto;
        return partes[0] + " " + partes[1] + " "
                + partes[2].charAt(0) + ".";
    }

    // ══════════════════════════════════════════════════════════════════════
    // LÓGICA FICHAJE
    // ══════════════════════════════════════════════════════════════════════

    private void ficharPorNia() {
        String nia = tfNia.getText().trim();

        if (nia.isEmpty()) {
            mostrarBanner(false, "NIA vacio",
                "Introduce o escanea el NIA "
                + "del alumno.");
            return;
        }
        if (!nia.matches("[0-9]+")) {
            mostrarBanner(false, "NIA incorrecto",
                "El NIA solo puede contener numeros.");
            tfNia.setText("");
            tfNia.requestFocus();
            return;
        }
        if (nia.length() < 8) {
            mostrarBanner(false,
                "NIA demasiado corto",
                "El NIA debe tener exactamente "
                + "8 digitos. Has introducido "
                + nia.length() + ".");
            tfNia.setText("");
            tfNia.requestFocus();
            return;
        }
        if (nia.length() > 8) {
            mostrarBanner(false,
                "NIA demasiado largo",
                "El NIA debe tener exactamente "
                + "8 digitos. Has introducido "
                + nia.length() + ".");
            tfNia.setText("");
            tfNia.requestFocus();
            return;
        }

        ejecutarFichaje(nia);
        tfNia.setText("");
        tfNia.requestFocus();
    }

    private void ficharPorLista() {
        Alumno alumnoSel =
                (Alumno) cbAlumno.getSelectedItem();
        if (alumnoSel == null
                || alumnoSel.getNia().isEmpty()) {
            mostrarBanner(false,
                "Selecciona un alumno",
                "Elige el curso y el alumno "
                + "de las listas.");
            return;
        }
        String nia = alumnoSel.getNia().trim();
        if (!nia.matches("[0-9]{8}")) {
            mostrarBanner(false, "NIA incorrecto",
                "El NIA del alumno seleccionado "
                + "no es valido.");
            return;
        }
        ejecutarFichaje(nia);
    }

    private void ejecutarFichaje(String nia) {
        SwingWorker<String, Void> worker =
                new SwingWorker<>() {
            private int     minutosTarde   = 0;
            private boolean exito          = false;
            private boolean alumnoNoExiste = false;

            @Override
            protected String doInBackground() {
                try {
                    LocalTime ahora = LocalTime.now();
                    if (ahora.isAfter(HORA_INICIO)) {
                        minutosTarde = (int)
                            java.time.Duration
                                .between(HORA_INICIO,
                                        ahora)
                                .toMinutes();
                    }
                    retrasoDAO
                        .ficharRetrasoAutomatico(nia);
                    exito = true;
                    return "OK";

                } catch (Exception e) {
                    String msg = e.getMessage();
                    String msgLower = msg == null
                            ? "" : msg.toLowerCase();

                    if (msgLower.contains(
                                "no encontrado")
                            || msgLower.contains(
                                "no existe")
                            || msgLower.contains(
                                "inexistente")
                            || msgLower.contains(
                                "not found")
                            || msgLower.contains(
                                "45000")) {
                        alumnoNoExiste = true;
                    }

                    if (msg != null) {
                        int idx = msg.lastIndexOf(':');
                        if (idx >= 0)
                            msg = msg.substring(
                                    idx + 1).trim();
                    }

                    return msg != null
                            ? msg : "Error desconocido.";
                }
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    if (exito) {
                        mostrarBanner(true,
                            "Retraso registrado "
                            + "correctamente",
                            "NIA: " + nia
                            + "   |   "
                            + minutosTarde
                            + " minuto(s) tarde"
                            + "   |   Correo en 1 min");

                        cargarHistorialHoy();

                        // Notificar al registrar
                        if (onRetrasoRegistrado != null)
                            onRetrasoRegistrado.run();

                        // ── Envío con 1 min de espera ──
                        // Tras el envío refresca TAMBIÉN
                        // el panel de Correos
                        Timer timerEnvio =
                                new Timer(60_000, ev -> {
                            emailService
                                .enviarInmediatamente();

                            // Esperar 3s a que el
                            // envío termine y refrescar
                            // fichaje + correos
                            Timer tRef = new Timer(
                                    3000, ev2 -> {
                                cargarHistorialHoy();
                                // Esto refresca
                                // historial, ranking
                                // Y panel de correos
                                if (onRetrasoRegistrado
                                        != null)
                                    onRetrasoRegistrado
                                        .run();
                            });
                            tRef.setRepeats(false);
                            tRef.start();
                        });
                        timerEnvio.setRepeats(false);
                        timerEnvio.start();

                    } else if (alumnoNoExiste) {
                        mostrarBanner(false,
                            "Alumno no encontrado",
                            "El NIA " + nia
                            + " no esta registrado. "
                            + "Ve a Cursos y Alumnos "
                            + "para registrarlo.");

                    } else {
                        mostrarBanner(false,
                            "No se pudo registrar",
                            resultado);
                    }
                } catch (Exception e) {
                    mostrarBanner(false,
                        "Error inesperado",
                        e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // ELIMINAR FICHAJE
    // ══════════════════════════════════════════════════════════════════════

    private void eliminarFichajeSeleccionado() {
        int filaVista =
                tablaHistorial.getSelectedRow();

        if (filaVista < 0) {
            mostrarBanner(false,
                "Ningun fichaje seleccionado",
                "Haz clic en una fila de la tabla "
                + "antes de pulsar Eliminar.");
            return;
        }

        int filaModelo =
                tablaHistorial
                    .convertRowIndexToModel(filaVista);

        if (filaModelo >= idsHistorial.size()) return;

        int    idRetraso =
                idsHistorial.get(filaModelo);
        String nombre    = (String) modeloHistorial
                            .getValueAt(filaModelo, 0);
        String curso     = (String) modeloHistorial
                            .getValueAt(filaModelo, 1);
        String hora      = (String) modeloHistorial
                            .getValueAt(filaModelo, 2);

        int resp = JOptionPane.showConfirmDialog(
                this,
                "Eliminar este fichaje?\n\n"
                + "Alumno: " + nombre + "\n"
                + "Curso:  " + curso  + "\n"
                + "Hora:   " + hora   + "\n\n"
                + "Si han pasado menos de 1 minuto,\n"
                + "el correo a la familia "
                + "NO se enviara.\n\n"
                + "Esta accion no se puede deshacer.",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp != JOptionPane.YES_OPTION) return;

        final int    idFinal     = idRetraso;
        final String nombreFinal = nombre;

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {
            @Override
            protected Void doInBackground()
                    throws Exception {
                retrasoDAO.eliminarRetraso(idFinal);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    cargarHistorialHoy();

                    mostrarBanner(true,
                        "Fichaje eliminado",
                        "Retraso de " + nombreFinal
                        + " eliminado correctamente.");

                    // Notificar para refrescar
                    // correos, historial y ranking
                    if (onRetrasoRegistrado != null)
                        onRetrasoRegistrado.run();

                } catch (Exception e) {
                    mostrarBanner(false,
                        "Error al eliminar",
                        e.getMessage() != null
                            ? e.getMessage()
                            : "Error desconocido");
                }
            }
        };
        worker.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // BANNER FLOTANTE
    // ══════════════════════════════════════════════════════════════════════

    private void mostrarBanner(boolean ok,
                               String titulo,
                               String subtitulo) {
        bannerPanel.setBackground(ok
                ? UIUtils.COLOR_VERDE
                : UIUtils.COLOR_ACENTO);
        bannerTexto.setText(titulo);
        bannerSub.setText(subtitulo);
        bannerPanel.setVisible(true);
        ajustarTamanos();

        if (timerBanner != null
                && timerBanner.isRunning())
            timerBanner.stop();

        timerBanner = new Timer(4000,
                e -> bannerPanel.setVisible(false));
        timerBanner.setRepeats(false);
        timerBanner.start();
    }
}