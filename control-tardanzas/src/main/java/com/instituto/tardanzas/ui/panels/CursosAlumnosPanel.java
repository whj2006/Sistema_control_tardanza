package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.AlumnoDAO;
import com.instituto.tardanzas.dao.CursoDAO;
import com.instituto.tardanzas.model.Alumno;
import com.instituto.tardanzas.model.Curso;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CursosAlumnosPanel extends JPanel {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final CursoDAO  cursoDAO  = new CursoDAO();

    private final Runnable onDatosCambiados;

    private JTable            tablaCursos;
    private DefaultTableModel modeloCursos;
    private JLabel            lblTotalCursos;

    private JTable            tablaAlumnos;
    private DefaultTableModel modeloAlumnos;
    private JLabel            lblTotalAlumnos;
    private JLabel            lblCursoActual;

    private List<Curso> cursos;
    private Curso       cursoSeleccionado = null;
    private Timer       timerRefresco;

    public CursosAlumnosPanel(Runnable onDatosCambiados) {
        this.onDatosCambiados = onDatosCambiados;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        construirUI();
        cargarCursos();
        iniciarAutoRefresco();
    }

    public CursosAlumnosPanel() {
        this(null);
    }

    private void notificarCambio() {
        if (onDatosCambiados != null)
            onDatosCambiados.run();
    }

    public void refrescar() {
        cargarCursos();
    }

    private void construirUI() {
        add(UIUtils.crearTitulo("Cursos y Alumnos"),
                BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                construirLadoCursos(),
                construirLadoAlumnos());

        split.setDividerLocation(380);
        split.setDividerSize(8);
        split.setResizeWeight(0.3);
        split.setBorder(null);
        split.setOpaque(false);

        add(split, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    // LADO IZQUIERDO — CURSOS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirLadoCursos() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UIUtils.COLOR_BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 215, 220), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false);

        JLabel titulo = new JLabel("Cursos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(UIUtils.COLOR_PRIMARIO);
        cabecera.add(titulo, BorderLayout.NORTH);

        JPanel btnsCurso =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnsCurso.setOpaque(false);

        JButton btnNuevo    = UIUtils.crearBotonPequeno(
                "Nuevo",    UIUtils.COLOR_VERDE);
        JButton btnEditar   = UIUtils.crearBotonPequeno(
                "Editar",   UIUtils.COLOR_NARANJA);
        JButton btnEliminar = UIUtils.crearBotonPequeno(
                "Eliminar", UIUtils.COLOR_ACENTO);

        btnNuevo   .addActionListener(
                e -> abrirFormularioCurso(null));
        btnEditar  .addActionListener(
                e -> editarCursoSeleccionado());
        btnEliminar.addActionListener(
                e -> eliminarCursoSeleccionado());

        btnsCurso.add(btnNuevo);
        btnsCurso.add(btnEditar);
        btnsCurso.add(btnEliminar);
        cabecera.add(btnsCurso, BorderLayout.SOUTH);

        panel.add(cabecera, BorderLayout.NORTH);

        String[] cols = {"ID", "Nombre del Curso", "Alumnos"};
        modeloCursos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tablaCursos = new JTable(modeloCursos);
        UIUtils.estilizarTabla(tablaCursos);
        tablaCursos.getColumnModel().getColumn(0).setMaxWidth(40);
        tablaCursos.getColumnModel().getColumn(2).setMaxWidth(65);
        tablaCursos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tablaCursos.getSelectionModel()
                .addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaCursos.getSelectedRow();
                if (fila >= 0 && cursos != null
                        && fila < cursos.size()) {
                    cursoSeleccionado = cursos.get(fila);
                    lblCursoActual.setText(
                            "Alumnos de: " +
                            cursoSeleccionado.getNombre());
                    cargarAlumnosDeCurso(
                            cursoSeleccionado.getId());
                } else {
                    cursoSeleccionado = null;
                    modeloAlumnos.setRowCount(0);
                    lblCursoActual.setText(
                            "Selecciona un curso");
                    lblTotalAlumnos.setText("0 alumnos");
                }
            }
        });

        panel.add(new JScrollPane(tablaCursos),
                BorderLayout.CENTER);

        lblTotalCursos = new JLabel("0 cursos");
        lblTotalCursos.setFont(
                new Font("Segoe UI", Font.ITALIC, 11));
        lblTotalCursos.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        panel.add(lblTotalCursos, BorderLayout.SOUTH);

        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LADO DERECHO — ALUMNOS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel construirLadoAlumnos() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UIUtils.COLOR_BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 215, 220), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false);

        lblCursoActual = new JLabel("Selecciona un curso");
        lblCursoActual.setFont(
                new Font("Segoe UI", Font.BOLD, 16));
        lblCursoActual.setForeground(UIUtils.COLOR_PRIMARIO);
        cabecera.add(lblCursoActual, BorderLayout.NORTH);

        JPanel btnsAlumno =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnsAlumno.setOpaque(false);

        JButton btnNuevo    = UIUtils.crearBotonPequeno(
                "Nuevo",    UIUtils.COLOR_VERDE);
        JButton btnEditar   = UIUtils.crearBotonPequeno(
                "Editar",   UIUtils.COLOR_NARANJA);
        JButton btnEliminar = UIUtils.crearBotonPequeno(
                "Eliminar", UIUtils.COLOR_ACENTO);

        btnNuevo   .addActionListener(
                e -> abrirFormularioAlumno(null));
        btnEditar  .addActionListener(
                e -> editarAlumnoSeleccionado());
        btnEliminar.addActionListener(
                e -> eliminarAlumnoSeleccionado());

        btnsAlumno.add(btnNuevo);
        btnsAlumno.add(btnEditar);
        btnsAlumno.add(btnEliminar);
        cabecera.add(btnsAlumno, BorderLayout.SOUTH);

        panel.add(cabecera, BorderLayout.NORTH);

        String[] cols = {"NIA", "Nombre", "Apellido 1", "Apellido 2",
                         "Email 1", "Email 2"};
        modeloAlumnos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tablaAlumnos = new JTable(modeloAlumnos);
        UIUtils.estilizarTabla(tablaAlumnos);
        tablaAlumnos.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaAlumnos.getColumnModel().getColumn(1).setPreferredWidth(90);
        tablaAlumnos.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaAlumnos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaAlumnos.getColumnModel().getColumn(4).setPreferredWidth(160);
        tablaAlumnos.getColumnModel().getColumn(5).setPreferredWidth(160);
        tablaAlumnos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(tablaAlumnos),
                BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        lblTotalAlumnos = new JLabel("0 alumnos");
        lblTotalAlumnos.setFont(
                new Font("Segoe UI", Font.ITALIC, 11));
        lblTotalAlumnos.setForeground(UIUtils.COLOR_TEXTO_CLARO);

        JLabel lblAuto = new JLabel(
                "Actualizacion automatica cada 15s");
        lblAuto.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblAuto.setForeground(UIUtils.COLOR_TEXTO_CLARO);

        pie.add(lblTotalAlumnos, BorderLayout.WEST);
        pie.add(lblAuto,         BorderLayout.EAST);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    // ── Cargar cursos ─────────────────────────────────────────────────────

    private void cargarCursos() {
        int idActual = cursoSeleccionado != null
                ? cursoSeleccionado.getId() : -1;

        SwingWorker<List<Curso>, Void> w = new SwingWorker<>() {
            @Override protected List<Curso> doInBackground()
                    throws Exception {
                return cursoDAO.listarTodos();
            }
            @Override protected void done() {
                try {
                    cursos = get();
                    modeloCursos.setRowCount(0);

                    int filaRestaurar = -1;
                    for (int i = 0; i < cursos.size(); i++) {
                        Curso c = cursos.get(i);
                        modeloCursos.addRow(new Object[]{
                            c.getId(), c.getNombre(),
                            c.getTotalAlumnos()
                        });
                        if (c.getId() == idActual)
                            filaRestaurar = i;
                    }

                    lblTotalCursos.setText(
                            cursos.size() + " cursos");

                    if (filaRestaurar >= 0) {
                        tablaCursos.setRowSelectionInterval(
                                filaRestaurar, filaRestaurar);
                    } else if (!cursos.isEmpty()) {
                        tablaCursos.setRowSelectionInterval(0, 0);
                    }

                } catch (Exception e) {
                    UIUtils.mostrarError(
                            CursosAlumnosPanel.this,
                            e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── Cargar alumnos del curso ──────────────────────────────────────────

    private void cargarAlumnosDeCurso(int idCurso) {
        SwingWorker<List<Alumno>, Void> w = new SwingWorker<>() {
            @Override protected List<Alumno> doInBackground()
                    throws Exception {
                return alumnoDAO.listarPorCurso(idCurso);
            }
            @Override protected void done() {
                try {
                    modeloAlumnos.setRowCount(0);
                    List<Alumno> lista = get();
                    for (Alumno a : lista) {
                        modeloAlumnos.addRow(new Object[]{
                            a.getNia(),
                            a.getNombre(),
                            a.getApellido1(),
                            a.getApellido2(),
                            a.getEmailFamilia1() != null ?
                                    a.getEmailFamilia1() : "",
                            a.getEmailFamilia2() != null ?
                                    a.getEmailFamilia2() : ""
                        });
                    }
                    lblTotalAlumnos.setText(
                            lista.size() + " alumnos");
                } catch (Exception e) {
                    UIUtils.mostrarError(
                            CursosAlumnosPanel.this,
                            e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── Auto-refresco ─────────────────────────────────────────────────────

    private void iniciarAutoRefresco() {
        timerRefresco = new Timer(15_000, e -> cargarCursos());
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

    // ══════════════════════════════════════════════════════════════════════
    // CRUD CURSOS
    // ══════════════════════════════════════════════════════════════════════

    private void editarCursoSeleccionado() {
        int fila = tablaCursos.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this, "Selecciona un curso.");
            return;
        }
        abrirFormularioCurso(cursos.get(fila));
    }

    private void eliminarCursoSeleccionado() {
        int fila = tablaCursos.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this, "Selecciona un curso.");
            return;
        }
        Curso c = cursos.get(fila);

        if (c.getTotalAlumnos() > 0) {
            JPanel panelError = new JPanel(new BorderLayout());
            panelError.setBackground(Color.WHITE);
            panelError.setBorder(
                    BorderFactory.createEmptyBorder(
                            10, 10, 5, 10));

            JLabel lblError = new JLabel(
                    "<html>" +
                    "<b style='font-size:13px;color:#c0392b'>" +
                    "No se puede eliminar el curso</b><br><br>" +
                    "El curso <b>\"" + c.getNombre() +
                    "\"</b> tiene <b>" +
                    c.getTotalAlumnos() + " alumno(s)</b> " +
                    "registrados.<br><br>" +
                    "Elimina primero todos los alumnos " +
                    "del curso e inténtalo de nuevo." +
                    "</html>");
            lblError.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panelError.add(lblError, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(
                    this,
                    panelError,
                    "Curso con alumnos",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panelConfirm = new JPanel(new BorderLayout());
        panelConfirm.setBackground(Color.WHITE);
        panelConfirm.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel lblMsg = new JLabel(
                "<html>" +
                "<b style='font-size:13px;color:#c0392b'>" +
                "Eliminar curso</b><br><br>" +
                "¿Estás seguro de que quieres eliminar<br>" +
                "el curso <b>\"" + c.getNombre() +
                "\"</b>?<br><br>" +
                "Esta acción no se puede deshacer." +
                "</html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelConfirm.add(lblMsg, BorderLayout.CENTER);

        Object[] opciones = {"Eliminar", "Cancelar"};
        int resp = JOptionPane.showOptionDialog(
                this,
                panelConfirm,
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[1]);

        if (resp == 0) {
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground()
                        throws Exception {
                    cursoDAO.eliminar(c.getId());
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        cursoSeleccionado = null;
                        modeloAlumnos.setRowCount(0);
                        lblCursoActual.setText(
                                "Selecciona un curso");
                        lblTotalAlumnos.setText("0 alumnos");
                        cargarCursos();
                        notificarCambio();
                    } catch (Exception e) {
                        UIUtils.mostrarError(
                                CursosAlumnosPanel.this,
                                e.getMessage());
                    }
                }
            };
            w.execute();
        }
    }

    private void abrirFormularioCurso(Curso curso) {
        boolean esNuevo = curso == null;

        JDialog dlg = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                esNuevo ? "Nuevo Curso" : "Editar Curso", true);
        dlg.setSize(360, 150);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.COLOR_BLANCO);
        panel.setBorder(
                BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField tfNombre = UIUtils.crearCampo(20);
        if (!esNuevo) tfNombre.setText(curso.getNombre());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        panel.add(new JLabel("Nombre del curso:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        panel.add(tfNombre, gbc);

        JButton btnGuardar  = UIUtils.crearBotonPequeno(
                "Guardar",  UIUtils.COLOR_VERDE);
        JButton btnCancelar = UIUtils.crearBotonPequeno(
                "Cancelar", UIUtils.COLOR_TEXTO_CLARO);

        btnCancelar.addActionListener(e -> dlg.dispose());
        btnGuardar.addActionListener(e -> {
            String nombre = tfNombre.getText().trim();
            if (nombre.isEmpty()) {
                UIUtils.mostrarError(dlg,
                        "El nombre no puede estar vacio.");
                return;
            }
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground()
                        throws Exception {
                    if (esNuevo)
                        cursoDAO.crearSiNoExiste(nombre);
                    else
                        cursoDAO.editar(curso.getId(), nombre);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        dlg.dispose();
                        cargarCursos();
                        notificarCambio();
                    } catch (Exception ex) {
                        UIUtils.mostrarError(dlg,
                                ex.getMessage());
                    }
                }
            };
            w.execute();
        });

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.5;
        panel.add(btnCancelar, gbc);
        gbc.gridx = 1;
        panel.add(btnGuardar, gbc);

        dlg.add(panel);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    // CRUD ALUMNOS
    // ══════════════════════════════════════════════════════════════════════

    private void editarAlumnoSeleccionado() {
        int fila = tablaAlumnos.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this, "Selecciona un alumno.");
            return;
        }
        String nia = (String) modeloAlumnos.getValueAt(fila, 0);

        SwingWorker<Alumno, Void> w = new SwingWorker<>() {
            @Override protected Alumno doInBackground()
                    throws Exception {
                return alumnoDAO.buscarPorNia(nia);
            }
            @Override protected void done() {
                try { abrirFormularioAlumno(get()); }
                catch (Exception e) {
                    UIUtils.mostrarError(
                            CursosAlumnosPanel.this,
                            e.getMessage());
                }
            }
        };
        w.execute();
    }

    private void eliminarAlumnoSeleccionado() {
        int fila = tablaAlumnos.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this, "Selecciona un alumno.");
            return;
        }
        String nia    = (String) modeloAlumnos.getValueAt(fila, 0);
        String nombre = modeloAlumnos.getValueAt(fila, 1) + " "
                      + modeloAlumnos.getValueAt(fila, 2) + " "
                      + modeloAlumnos.getValueAt(fila, 3);

        JPanel panelConfirm = new JPanel(new BorderLayout());
        panelConfirm.setBackground(Color.WHITE);
        panelConfirm.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel lblMsg = new JLabel(
                "<html>" +
                "<b style='font-size:13px;color:#c0392b'>" +
                "Eliminar alumno</b><br><br>" +
                "¿Estás seguro de que quieres eliminar a<br>" +
                "<b>\"" + nombre + "\"</b>?<br><br>" +
                "Se borrarán todos sus retrasos registrados.<br>" +
                "Esta acción no se puede deshacer." +
                "</html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelConfirm.add(lblMsg, BorderLayout.CENTER);

        Object[] opciones = {"Eliminar", "Cancelar"};
        int resp = JOptionPane.showOptionDialog(
                this,
                panelConfirm,
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[1]);

        if (resp == 0) {
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground()
                        throws Exception {
                    alumnoDAO.eliminar(nia);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        cargarCursos();
                        notificarCambio();
                    } catch (Exception e) {
                        UIUtils.mostrarError(
                                CursosAlumnosPanel.this,
                                e.getMessage());
                    }
                }
            };
            w.execute();
        }
    }

    private void abrirFormularioAlumno(Alumno alumno) {
        boolean esNuevo = alumno == null;

        if (esNuevo && cursoSeleccionado == null) {
            UIUtils.mostrarError(this,
                    "Selecciona primero un curso " +
                    "en la lista de la izquierda.");
            return;
        }

        JDialog dlg = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                esNuevo ? "Nuevo Alumno" : "Editar Alumno", true);
        dlg.setSize(480, 450);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.COLOR_BLANCO);
        panel.setBorder(
                BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 7, 6, 7);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField tfNia       = UIUtils.crearCampo(15);
        JTextField tfNombre    = UIUtils.crearCampo(15);
        JTextField tfApellido1 = UIUtils.crearCampo(15);
        JTextField tfApellido2 = UIUtils.crearCampo(15);
        JTextField tfEmail1    = UIUtils.crearCampo(15);
        JTextField tfEmail2    = UIUtils.crearCampo(15);

        JLabel lblCurso = new JLabel(
                esNuevo ? cursoSeleccionado.getNombre()
                        : alumno.getNombreCurso());
        lblCurso.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCurso.setForeground(UIUtils.COLOR_PRIMARIO);

        if (!esNuevo) {
            tfNia.setText(alumno.getNia());
            tfNia.setEditable(false);
            tfNia.setBackground(new Color(240, 240, 240));
            tfNombre.setText(alumno.getNombre());
            tfApellido1.setText(alumno.getApellido1());
            tfApellido2.setText(alumno.getApellido2());
            tfEmail1.setText(alumno.getEmailFamilia1() != null ?
                    alumno.getEmailFamilia1() : "");
            tfEmail2.setText(alumno.getEmailFamilia2() != null ?
                    alumno.getEmailFamilia2() : "");
        }

        String[]    ets    = {"NIA (8 digitos):", "Nombre:",
                              "Apellido 1:", "Apellido 2:",
                              "Email familia 1 (opcional):",
                              "Email familia 2 (opcional):",
                              "Curso:"};
        Component[] campos = {tfNia, tfNombre, tfApellido1, tfApellido2,
                              tfEmail1, tfEmail2, lblCurso};

        for (int i = 0; i < ets.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.42;
            panel.add(new JLabel(ets[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.58;
            panel.add(campos[i], gbc);
        }

        JLabel notaNia = new JLabel(
                "Exactamente 8 digitos numericos");
        notaNia.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        notaNia.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        gbc.gridx = 1; gbc.gridy = ets.length;
        panel.add(notaNia, gbc);

        JLabel notaEmail = new JLabel(
                "Si no hay emails, no se enviaran correos");
        notaEmail.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        notaEmail.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        gbc.gridx = 1; gbc.gridy = ets.length + 1;
        panel.add(notaEmail, gbc);

        JButton btnGuardar  = UIUtils.crearBotonPequeno(
                "Guardar",  UIUtils.COLOR_VERDE);
        JButton btnCancelar = UIUtils.crearBotonPequeno(
                "Cancelar", UIUtils.COLOR_TEXTO_CLARO);

        btnCancelar.addActionListener(e -> dlg.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                String nia      = tfNia.getText().trim();
                String nombre   = tfNombre.getText().trim();
                String apell1   = tfApellido1.getText().trim();
                String apell2   = tfApellido2.getText().trim();
                String email1   = tfEmail1.getText().trim();
                String email2   = tfEmail2.getText().trim();

                validarNia(nia);

                if (nombre.isEmpty())
                    throw new Exception(
                            "El nombre no puede estar vacio.");
                if (apell1.isEmpty())
                    throw new Exception(
                            "El apellido 1 no puede estar vacio.");
                if (apell2.isEmpty())
                    throw new Exception(
                            "El apellido 2 no puede estar vacio.");

                // Emails opcionales — validar SOLO si están presentes
                if (!email1.isEmpty()) {
                    validarEmail(email1, "Email familia 1");
                }
                if (!email2.isEmpty()) {
                    validarEmail(email2, "Email familia 2");
                }

                // Si están vacíos, se guardan como null
                String email1Final = email1.isEmpty() ? null : email1;
                String email2Final = email2.isEmpty() ? null : email2;

                int idCurso = esNuevo
                        ? cursoSeleccionado.getId()
                        : alumno.getIdCurso();

                Alumno a = new Alumno(
                        nia, nombre, apell1, apell2,
                        email1Final, email2Final, idCurso);

                SwingWorker<Void, Void> w = new SwingWorker<>() {
                    @Override protected Void doInBackground()
                            throws Exception {
                        if (esNuevo)
                            alumnoDAO.importarActualizar(a);
                        else
                            alumnoDAO.editar(a);
                        return null;
                    }
                    @Override protected void done() {
                        try {
                            get();
                            dlg.dispose();
                            cargarCursos();
                            notificarCambio();
                        } catch (Exception ex) {
                            String msg = ex.getMessage();
                            if (msg != null &&
                                    msg.contains("check_nia")) {
                                UIUtils.mostrarError(dlg,
                                    "NIA invalido.\n" +
                                    "Debe tener exactamente " +
                                    "8 digitos.");
                            } else if (msg != null &&
                                    msg.contains("check_email")) {
                                UIUtils.mostrarError(dlg,
                                    "Email invalido.\n" +
                                    "Formato: " +
                                    "usuario@dominio.com");
                            } else {
                                UIUtils.mostrarError(dlg, msg);
                            }
                        }
                    }
                };
                w.execute();

            } catch (Exception ex) {
                UIUtils.mostrarError(dlg, ex.getMessage());
            }
        });

        gbc.gridx = 0; gbc.gridy = ets.length + 2;
        gbc.weightx = 0.5;
        panel.add(btnCancelar, gbc);
        gbc.gridx = 1;
        panel.add(btnGuardar, gbc);

        dlg.add(panel);
        dlg.setVisible(true);
    }

    // ── Validaciones ──────────────────────────────────────────────────────

    private void validarNia(String nia) throws Exception {
        if (nia.isEmpty())
            throw new Exception("El NIA no puede estar vacio.");
        if (!nia.matches("^[0-9]{8}$"))
            throw new Exception(
                "NIA invalido: \"" + nia + "\"\n" +
                "Debe tener exactamente 8 digitos numericos.\n" +
                "Ejemplo: 12345678");
    }

    private void validarEmail(String email, String campo) throws Exception {
        if (!email.matches("^[^@]+@[^@]+\\.[^@]+$"))
            throw new Exception(
                campo + " invalido: \"" + email + "\"\n" +
                "Formato requerido: usuario@dominio.com");
    }
}