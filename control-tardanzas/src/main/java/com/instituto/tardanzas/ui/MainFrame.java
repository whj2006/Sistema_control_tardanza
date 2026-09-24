package com.instituto.tardanzas.ui;

import com.instituto.tardanzas.config.ConfigDB;
import com.instituto.tardanzas.dao.UsuarioDAO;
import com.instituto.tardanzas.model.Usuario;
import com.instituto.tardanzas.service.EmailService;
import com.instituto.tardanzas.ui.panels.AjustesPanel;
import com.instituto.tardanzas.ui.panels.ConfiguracionPanel;
import com.instituto.tardanzas.ui.panels.CorreosPanel;
import com.instituto.tardanzas.ui.panels.CursosAlumnosPanel;
import com.instituto.tardanzas.ui.panels.ExportarPanel;
import com.instituto.tardanzas.ui.panels.FicharPanel;
import com.instituto.tardanzas.ui.panels.HistorialPanel;
import com.instituto.tardanzas.ui.panels.ImportarPanel;
import com.instituto.tardanzas.ui.panels.RankingPanel;
import com.instituto.tardanzas.ui.panels.ResetearDatosPanel;
import com.instituto.tardanzas.ui.panels.UsuariosPanel;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainFrame extends JFrame {

    private final EmailService         emailService;
    private JPanel                     panelContenido;
    private CardLayout                 cardLayout;

    private final Map<String, JButton> botonesNav      =
            new HashMap<>();
    private final Map<String, JPanel>  panelesCargados =
            new HashMap<>();
    private String                     panelActivo     = "";

    private JLabel lblHora;
    private JLabel lblFecha;
    private JLabel lblUsuarioInfo;

    private static final Color COLOR_BARRA   =
            new Color(150, 38, 38);
    private static final Color COLOR_SIDEBAR =
            new Color(175, 52, 52);

    // ── IDs de paneles ────────────────────────────────────────────────────
    private static final String P_FICHAR         = "FICHAR";
    private static final String P_HISTORIAL      = "HISTORIAL";
    private static final String P_RANKING        = "RANKING";
    private static final String P_CURSOS_ALUMNOS = "CURSOS_ALUMNOS";
    private static final String P_CORREOS        = "CORREOS";
    private static final String P_IMPORTAR       = "IMPORTAR";
    private static final String P_EXPORTAR       = "EXPORTAR";
    private static final String P_CONFIG         = "CONFIG";
    private static final String P_RESET          = "RESET";
    private static final String P_USUARIOS       = "USUARIOS";
    private static final String P_AJUSTES        = "AJUSTES";

    public static final String PANEL_CURSOS_ALUMNOS =
            "CURSOS_ALUMNOS";

    // ── Sistema de usuarios ───────────────────────────────────────────────
    private final UsuarioDAO usuarioDAO    = new UsuarioDAO();
    private Usuario          usuarioActual = null;
    private boolean          hayUsuarios   = false;

    // ── Permisos por rol ──────────────────────────────────────────────────
    private static final Set<String> PANELES_FICHAJE =
            Set.of(P_FICHAR);

    private static final Set<String> PANELES_FICHAJE_INFORMES =
            Set.of(P_FICHAR, P_HISTORIAL,
                   P_RANKING, P_EXPORTAR);

    private static final Set<String>
            PANELES_FICHAJE_INFORMES_GESTION =
            Set.of(P_FICHAR, P_HISTORIAL, P_RANKING,
                   P_CURSOS_ALUMNOS, P_CORREOS,
                   P_IMPORTAR, P_EXPORTAR);

    // ── Referencias a paneles ─────────────────────────────────────────────
    private HistorialPanel     historialPanel;
    private RankingPanel       rankingPanel;
    private CursosAlumnosPanel cursosAlumnosPanel;
    private FicharPanel        ficharPanel;
    private CorreosPanel       correosPanel;
    private ImportarPanel      importarPanel;
    private ExportarPanel      exportarPanel;
    private UsuariosPanel      usuariosPanel;

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════

    public MainFrame(EmailService emailService) {
        this.emailService = emailService;
        configurarVentana();
        comprobarUsuarios();
        construirUI();
        mostrarPanel(P_FICHAR);
    }

    private void configurarVentana() {
        setTitle("Control de Retraso");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 680);
        setMinimumSize(new Dimension(900, 520));
        setLocationRelativeTo(null);

        addWindowListener(
                new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(
                    java.awt.event.WindowEvent e) {
                emailService.detener();
                com.instituto.tardanzas.config
                        .DatabaseConfig.closePool();
            }
        });
    }

    private void comprobarUsuarios() {
        try {
            hayUsuarios =
                    usuarioDAO.contarUsuarios() > 0;
        } catch (Exception e) {
            hayUsuarios = false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // CONSTRUIR UI
    // ══════════════════════════════════════════════════════════════════════

    private void construirUI() {
        setLayout(new BorderLayout());
        add(construirBarraSuperior(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(construirSidebar(),
                BorderLayout.WEST);
        centro.add(construirPanelContenido(),
                BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    // BARRA SUPERIOR
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(COLOR_BARRA);
        barra.setPreferredSize(new Dimension(0, 85));
        barra.setBorder(
                BorderFactory.createEmptyBorder(
                        0, 15, 0, 20));

        // ── Centro ────────────────────────────────────────────────────────
        JPanel centroPanel =
                new JPanel(new GridBagLayout());
        centroPanel.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(
                new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblInstituto =
                new JLabel(ConfigDB.cargar().getProperty(
                        "centro.nombre",
                        "[Nombre del centro]"));
        lblInstituto.setFont(
                new Font("Segoe UI", Font.BOLD, 18));
        lblInstituto.setForeground(Color.WHITE);
        lblInstituto.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        JLabel lblApp =
                new JLabel("Control de Retraso");
        lblApp.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));
        lblApp.setForeground(new Color(220, 180, 180));
        lblApp.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        textos.add(lblInstituto);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblApp);
        centroPanel.add(textos);
        barra.add(centroPanel, BorderLayout.CENTER);

        // ── Derecha ───────────────────────────────────────────────────────
        JPanel derWrapper =
                new JPanel(new GridBagLayout());
        derWrapper.setOpaque(false);

        JPanel derContenido = new JPanel();
        derContenido.setOpaque(false);
        derContenido.setLayout(
                new BoxLayout(derContenido,
                        BoxLayout.Y_AXIS));

        lblHora = new JLabel();
        lblHora.setFont(
                new Font("Segoe UI", Font.BOLD, 18));
        lblHora.setForeground(Color.WHITE);
        lblHora.setAlignmentX(
                Component.RIGHT_ALIGNMENT);

        lblFecha = new JLabel();
        lblFecha.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));
        lblFecha.setForeground(Color.WHITE);
        lblFecha.setAlignmentX(
                Component.RIGHT_ALIGNMENT);

        lblUsuarioInfo = new JLabel("");
        lblUsuarioInfo.setFont(
                new Font("Segoe UI", Font.ITALIC, 11));
        lblUsuarioInfo.setForeground(
                new Color(255, 200, 200));
        lblUsuarioInfo.setAlignmentX(
                Component.RIGHT_ALIGNMENT);

        derContenido.add(lblHora);
        derContenido.add(Box.createVerticalStrut(2));
        derContenido.add(lblFecha);
        derContenido.add(Box.createVerticalStrut(2));
        derContenido.add(lblUsuarioInfo);

        derWrapper.add(derContenido);
        barra.add(derWrapper, BorderLayout.EAST);

        actualizarFechaHora();
        new Timer(1000,
                e -> actualizarFechaHora()).start();

        return barra;
    }

    private void actualizarFechaHora() {
        lblHora.setText(LocalTime.now()
                .format(DateTimeFormatter
                        .ofPattern("HH:mm:ss")));
        lblFecha.setText(LocalDate.now()
                .format(DateTimeFormatter
                        .ofPattern("dd/MM/yyyy")));
    }

    private void actualizarInfoUsuario() {
        if (usuarioActual != null) {
            lblUsuarioInfo.setText(
                    usuarioActual.getUsername()
                    + " ["
                    + usuarioActual.getRolLegible()
                    + "]");
        } else {
            lblUsuarioInfo.setText(
                    hayUsuarios
                            ? "No autenticado" : "");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setLayout(
                new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 6, 10, 6));

        sidebar.add(seccionLabel("FICHAJE"));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Fichar Retraso",
                P_FICHAR));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(separador());
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(seccionLabel("INFORMES"));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Historial",
                P_HISTORIAL));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Ranking",
                P_RANKING));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(separador());
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(seccionLabel("GESTION"));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Cursos y Alumnos",
                P_CURSOS_ALUMNOS));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Correos",
                P_CORREOS));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Importar",
                P_IMPORTAR));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Exportar",
                P_EXPORTAR));
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(separador());
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(seccionLabel("SISTEMA"));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Config. SMTP",
                P_CONFIG));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Ajustes",
                P_AJUSTES));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Usuarios",
                P_USUARIOS));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonNav("Resetear Datos",
                P_RESET));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(separador());
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(botonSesion("Cambiar Usuario",
                e -> cambiarUsuario()));
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(botonSesion("Cerrar Sesion",
                e -> cerrarSesion()));
        sidebar.add(Box.createVerticalStrut(5));

        return sidebar;
    }

    private JButton botonNav(String texto,
                             String panelId) {
        JButton btn = new JButton(texto);
        btn.setForeground(UIUtils.COLOR_BLANCO);
        btn.setBackground(COLOR_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(178, 28));
        btn.setPreferredSize(new Dimension(178, 28));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(
                BorderFactory.createEmptyBorder(
                        4, 12, 4, 4));

        Color hoverColor = new Color(200, 70, 70);
        btn.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(
                    java.awt.event.MouseEvent e) {
                if (!panelId.equals(panelActivo))
                    btn.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(
                    java.awt.event.MouseEvent e) {
                if (!panelId.equals(panelActivo))
                    btn.setBackground(COLOR_SIDEBAR);
            }
        });

        btn.addActionListener(
                e -> mostrarPanel(panelId));
        botonesNav.put(panelId, btn);
        return btn;
    }

    private JButton botonSesion(String texto,
            java.awt.event.ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setForeground(new Color(255, 180, 180));
        btn.setBackground(new Color(155, 42, 42));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(
                new Font("Segoe UI", Font.PLAIN, 11));
        btn.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(178, 26));
        btn.setPreferredSize(new Dimension(178, 26));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(
                BorderFactory.createEmptyBorder(
                        4, 12, 4, 4));
        btn.addActionListener(accion);
        btn.addMouseListener(
                new java.awt.event.MouseAdapter() {
            final Color base  = new Color(155, 42, 42);
            final Color hover = new Color(200, 60, 60);
            @Override
            public void mouseEntered(
                    java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
            }
            @Override
            public void mouseExited(
                    java.awt.event.MouseEvent e) {
                btn.setBackground(base);
            }
        });
        return btn;
    }

    private JLabel seccionLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(
                new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(220, 180, 180));
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        lbl.setBorder(
                BorderFactory.createEmptyBorder(
                        0, 12, 0, 0));
        lbl.setMaximumSize(new Dimension(178, 14));
        return lbl;
    }

    private JSeparator separador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 80, 80));
        sep.setMaximumSize(new Dimension(178, 1));
        return sep;
    }

    // ══════════════════════════════════════════════════════════════════════
    // PANEL CONTENIDO
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirPanelContenido() {
        cardLayout     = new CardLayout();
        panelContenido = new JPanel(cardLayout);

        historialPanel     = new HistorialPanel();
        rankingPanel       = new RankingPanel();
        cursosAlumnosPanel = new CursosAlumnosPanel(
                this::onCursosAlumnosCambiados);
        correosPanel       = new CorreosPanel(
                emailService);
        importarPanel      = new ImportarPanel(
                this::onDatosCambiados);
        exportarPanel      = new ExportarPanel(
                emailService);
        ficharPanel        = new FicharPanel(
                emailService,
                this::onRetrasoRegistrado);

        panelContenido.add(ficharPanel,
                P_FICHAR);
        panelContenido.add(historialPanel,
                P_HISTORIAL);
        panelContenido.add(rankingPanel,
                P_RANKING);
        panelContenido.add(cursosAlumnosPanel,
                P_CURSOS_ALUMNOS);
        panelContenido.add(correosPanel,
                P_CORREOS);
        panelContenido.add(importarPanel,
                P_IMPORTAR);
        panelContenido.add(exportarPanel,
                P_EXPORTAR);

        panelesCargados.put(P_FICHAR,
                ficharPanel);
        panelesCargados.put(P_HISTORIAL,
                historialPanel);
        panelesCargados.put(P_RANKING,
                rankingPanel);
        panelesCargados.put(P_CURSOS_ALUMNOS,
                cursosAlumnosPanel);
        panelesCargados.put(P_CORREOS,
                correosPanel);
        panelesCargados.put(P_IMPORTAR,
                importarPanel);
        panelesCargados.put(P_EXPORTAR,
                exportarPanel);

        JPanel placeholderConfig = new JPanel();
        placeholderConfig.setBackground(
                UIUtils.COLOR_FONDO);
        panelContenido.add(placeholderConfig,
                P_CONFIG);

        return panelContenido;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CONTROL DE ACCESO
    // ══════════════════════════════════════════════════════════════════════

    private boolean requiereAutenticacion(
            String panelId) {
        if (!hayUsuarios) return false;
        return !P_FICHAR.equals(panelId);
    }

    private boolean tienePermiso(String panelId) {
        if (!hayUsuarios) return true;
        if (usuarioActual == null) return false;
        if (usuarioActual.esTodo()) return true;

        String rol = usuarioActual.getRol();
        return switch (rol) {
            case "FICHAJE" ->
                PANELES_FICHAJE.contains(panelId);
            case "FICHAJE_INFORMES" ->
                PANELES_FICHAJE_INFORMES.contains(
                        panelId);
            case "FICHAJE_INFORMES_GESTION" ->
                PANELES_FICHAJE_INFORMES_GESTION
                        .contains(panelId);
            default -> false;
        };
    }

    private boolean pedirLogin() {
        if (!hayUsuarios) return true;
        if (usuarioActual != null) return true;
        return mostrarDialogoLogin();
    }

    private boolean mostrarDialogoLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory
                .createEmptyBorder(15, 15, 10, 15));

        GridBagConstraints gbc =
                new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitulo =
                new JLabel("Iniciar Sesion");
        lblTitulo.setFont(
                new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARIO);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel(
                "Introduce tus credenciales "
                + "para acceder");
        lblSub.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        gbc.gridy = 1;
        panel.add(lblSub, gbc);

        gbc.gridwidth = 1;
        gbc.anchor    = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblUser, gbc);

        JTextField tfUser = UIUtils.crearCampo(18);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(tfUser, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Contrasena:");
        lblPass.setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblPass, gbc);

        JPasswordField pfPass =
                new JPasswordField(18);
        pfPass.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));
        pfPass.setBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(189, 195, 199),
                        1, true),
                BorderFactory.createEmptyBorder(
                        5, 8, 5, 8)));
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(pfPass, gbc);

        Object[] opciones =
                {"Iniciar Sesion", "Cancelar"};
        int resp = JOptionPane.showOptionDialog(
                this, panel, "Autenticacion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, opciones, opciones[0]);

        if (resp == 0) {
            String user = tfUser.getText().trim();
            String pass = new String(
                    pfPass.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                UIUtils.mostrarError(this,
                    "Usuario y contrasena "
                    + "son obligatorios.");
                return false;
            }

            try {
                Usuario u =
                        usuarioDAO.autenticar(
                                user, pass);
                if (u != null) {
                    usuarioActual = u;
                    actualizarInfoUsuario();
                    return true;
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Usuario o contrasena "
                            + "incorrectos.",
                            "Acceso Denegado",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (Exception e) {
                UIUtils.mostrarError(this,
                    "Error al autenticar: "
                    + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private void cambiarUsuario() {
        if (!hayUsuarios) {
            UIUtils.mostrarInfo(this,
                "No hay usuarios registrados.\n"
                + "Crea uno desde Usuarios.");
            return;
        }
        usuarioActual = null;
        actualizarInfoUsuario();
        if (!mostrarDialogoLogin())
            mostrarPanel(P_FICHAR);
    }

    private void cerrarSesion() {
        usuarioActual = null;
        actualizarInfoUsuario();
        mostrarPanel(P_FICHAR);
    }

    // ══════════════════════════════════════════════════════════════════════
    // CALLBACKS
    // ══════════════════════════════════════════════════════════════════════

    private void onRetrasoRegistrado() {
        historialPanel.refrescar();
        rankingPanel.refrescar();
        correosPanel.refrescar();
        Timer t = new Timer(5000,
                e -> correosPanel.refrescar());
        t.setRepeats(false);
        t.start();
    }

    private void onCursosAlumnosCambiados() {
        rankingPanel.refrescarCursos();
        ficharPanel.refrescarCursos();
        correosPanel.refrescar();
    }

    private void onDatosCambiados() {
        cursosAlumnosPanel.refrescar();
        rankingPanel.refrescarCursos();
        ficharPanel.refrescarCursos();
        correosPanel.refrescar();
    }

    private void onDatosReseteados() {
        cursosAlumnosPanel.refrescar();
        rankingPanel.refrescar();
        rankingPanel.refrescarCursos();
        ficharPanel.refrescarCursos();
        ficharPanel.cargarHistorialHoy();
        historialPanel.refrescar();
        correosPanel.refrescar();
    }

    // ══════════════════════════════════════════════════════════════════════
    // NAVEGACIÓN
    // ══════════════════════════════════════════════════════════════════════

    private void mostrarPanel(String id) {
        comprobarUsuarios();

        if (requiereAutenticacion(id) && !pedirLogin())
            return;

        if (requiereAutenticacion(id)
                && !tienePermiso(id)) {
            JOptionPane.showMessageDialog(this,
                "No tienes permisos para acceder "
                + "a esta seccion.\n"
                + "Tu acceso ("
                + usuarioActual.getRolLegible()
                + ") no incluye este apartado.",
                "Acceso Denegado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!panelActivo.isEmpty()) {
            JButton anterior =
                    botonesNav.get(panelActivo);
            if (anterior != null) {
                anterior.setBackground(COLOR_SIDEBAR);
                anterior.setFont(anterior.getFont()
                        .deriveFont(Font.PLAIN, 12f));
            }
        }

        JButton activo = botonesNav.get(id);
        if (activo != null) {
            activo.setBackground(
                    new Color(200, 70, 70));
            activo.setFont(activo.getFont()
                    .deriveFont(Font.BOLD, 12f));
        }

        if (!panelesCargados.containsKey(id)) {
            JPanel panel = crearPanel(id);
            panelContenido.add(panel, id);
            panelesCargados.put(id, panel);
        }

        panelActivo = id;
        cardLayout.show(panelContenido, id);
    }

    public void navegarA(String id) {
        mostrarPanel(id);
    }

    private JPanel crearPanel(String id) {
        return switch (id) {
            case P_CONFIG ->
                new ConfiguracionPanel(emailService);
            // ── CORREGIDO: AjustesPanel sin emailService ──
            case P_AJUSTES ->
                new AjustesPanel();
            case P_RESET ->
                new ResetearDatosPanel(
                        this::onDatosReseteados);
            case P_USUARIOS -> {
                usuariosPanel =
                        new UsuariosPanel(usuarioActual);
                yield usuariosPanel;
            }
            default -> new JPanel();
        };
    }
}