package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.UsuarioDAO;
import com.instituto.tardanzas.model.Usuario;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuariosPanel extends JPanel {

    private final UsuarioDAO  usuarioDAO = new UsuarioDAO();
    private final Usuario     usuarioActual;
    private DefaultTableModel modelo;
    private JTable            tabla;

    // Roles con nombre legible para los combos
    private static final String[][] ROLES = {
            {"TODO",                       "Todo"},
            {"FICHAJE",                    "Fichaje"},
            {"FICHAJE_INFORMES",           "Fichaje + Informes"},
            {"FICHAJE_INFORMES_GESTION",   "Fichaje + Informes + Gestion"}
    };

    public UsuariosPanel(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
        construirUI();
        cargarUsuarios();
    }

    private void construirUI() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UIUtils.COLOR_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1),
                new EmptyBorder(20, 25, 20, 25)));

        // ── Cabecera ──────────────────────────────────────────────────────
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);

        JPanel izqCab = new JPanel();
        izqCab.setOpaque(false);
        izqCab.setLayout(new BoxLayout(izqCab, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Gestion de Usuarios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARIO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(
                "Crea, edita y administra los usuarios del sistema");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIUtils.COLOR_TEXTO_CLARO);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        izqCab.add(lblTitulo);
        izqCab.add(Box.createVerticalStrut(4));
        izqCab.add(lblSub);

        cabecera.add(izqCab, BorderLayout.WEST);

        // ── Tabla ─────────────────────────────────────────────────────────
        String[] columnas = {"ID", "Usuario", "Rol", "Activo", "Fecha Creacion"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modelo);
        UIUtils.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(140);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UIUtils.COLOR_BLANCO);

        // ── Botones inferiores ────────────────────────────────────────────
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBtns.setOpaque(false);
        panelBtns.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnNuevo = UIUtils.crearBoton(
                "Nuevo Usuario", UIUtils.COLOR_VERDE);
        btnNuevo.addActionListener(e -> mostrarDialogoCrear());

        JButton btnEditar = UIUtils.crearBoton(
                "Editar", UIUtils.COLOR_PRIMARIO);
        btnEditar.addActionListener(e -> editarSeleccionado());

        JButton btnResetPass = UIUtils.crearBoton(
                "Resetear Contrasena", UIUtils.COLOR_NARANJA);
        btnResetPass.addActionListener(e -> resetearPasswordSeleccionado());

        JButton btnEliminar = UIUtils.crearBoton(
                "Eliminar", UIUtils.COLOR_ACENTO);
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        panelBtns.add(btnNuevo);
        panelBtns.add(btnEditar);
        panelBtns.add(btnResetPass);
        panelBtns.add(btnEliminar);

        card.add(cabecera,  BorderLayout.NORTH);
        card.add(scroll,    BorderLayout.CENTER);
        card.add(panelBtns, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(15, 15, 15, 15));
        wrapper.add(card, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARGAR DATOS
    // ══════════════════════════════════════════════════════════════════════

    public void cargarUsuarios() {
        SwingWorker<List<Usuario>, Void> w = new SwingWorker<>() {
            @Override protected List<Usuario> doInBackground() throws Exception {
                return usuarioDAO.listarTodos();
            }
            @Override protected void done() {
                try {
                    modelo.setRowCount(0);
                    for (Usuario u : get()) {
                        modelo.addRow(new Object[]{
                                u.getId(),
                                u.getUsername(),
                                u.getRolLegible(),
                                u.isActivo() ? "Si" : "No",
                                u.getFechaCreacion() != null
                                        ? u.getFechaCreacion()
                                            .format(java.time.format
                                                .DateTimeFormatter
                                                .ofPattern("dd/MM/yyyy HH:mm"))
                                        : ""
                        });
                    }
                } catch (Exception e) {
                    UIUtils.mostrarError(UsuariosPanel.this, e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // CREAR USUARIO
    // ══════════════════════════════════════════════════════════════════════

    private void mostrarDialogoCrear() {

        // Comprobar si es el primer usuario
        boolean primerUsuario = false;
        try {
            primerUsuario = usuarioDAO.contarUsuarios() == 0;
        } catch (Exception ignored) {}

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 5, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        int fila = 0;

        // Aviso si es el primer usuario
        if (primerUsuario) {
            JLabel lblAviso = new JLabel(
                    "<html><b>Primer usuario del sistema</b><br>" +
                    "Debe tener acceso <b>Todo</b> obligatoriamente.</html>");
            lblAviso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblAviso.setForeground(UIUtils.COLOR_ACENTO);
            gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 2;
            panel.add(lblAviso, gbc);
            gbc.gridwidth = 1;
            fila++;
        }

        // Usuario
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblUser, gbc);

        JTextField tfUsuario = UIUtils.crearCampo(20);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(tfUsuario, gbc);
        fila++;

        // Contrasena
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Contrasena:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblPass, gbc);

        JPasswordField pfClave = crearPasswordField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(pfClave, gbc);
        fila++;

        // Repetir
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.3;
        JLabel lblRep = new JLabel("Repetir:");
        lblRep.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblRep, gbc);

        JPasswordField pfRepetir = crearPasswordField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(pfRepetir, gbc);
        fila++;

        // Rol
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.3;
        JLabel lblRol = new JLabel("Acceso:");
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblRol, gbc);

        JComboBox<String> cbRol = new JComboBox<>();
        for (String[] r : ROLES) {
            cbRol.addItem(r[1]); // nombre legible
        }
        cbRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.weightx = 0.7;

        if (primerUsuario) {
            cbRol.setSelectedItem("Todo");
            cbRol.setEnabled(false);
        }

        panel.add(cbRol, gbc);

        int resp = JOptionPane.showConfirmDialog(
                this, panel, "Nuevo Usuario",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resp != JOptionPane.OK_OPTION) return;

        String usuario = tfUsuario.getText().trim();
        String clave   = new String(pfClave.getPassword()).trim();
        String repetir = new String(pfRepetir.getPassword()).trim();

        // Obtener rol interno desde el nombre legible seleccionado
        String rolLegible = (String) cbRol.getSelectedItem();
        String rolInterno = obtenerRolInterno(rolLegible);

        if (primerUsuario) {
            rolInterno = "TODO";
        }

        if (usuario.isEmpty() || clave.isEmpty()) {
            UIUtils.mostrarError(this, "Usuario y contrasena son obligatorios.");
            return;
        }
        if (usuario.contains(" ")) {
            UIUtils.mostrarError(this,
                    "El nombre de usuario no puede contener espacios.");
            return;
        }
        if (clave.length() < 4) {
            UIUtils.mostrarError(this,
                    "La contrasena debe tener al menos 4 caracteres.");
            return;
        }
        if (!clave.equals(repetir)) {
            UIUtils.mostrarError(this, "Las contrasenas no coinciden.");
            return;
        }

        final String rolFinal = rolInterno;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                usuarioDAO.crear(usuario, clave, rolFinal);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    UIUtils.mostrarInfo(UsuariosPanel.this,
                            "Usuario creado correctamente.");
                    cargarUsuarios();
                } catch (Exception e) {
                    UIUtils.mostrarError(UsuariosPanel.this,
                            e.getCause() != null
                                    ? e.getCause().getMessage()
                                    : e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // EDITAR USUARIO
    // ══════════════════════════════════════════════════════════════════════

    private void editarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this, "Selecciona un usuario de la tabla.");
            return;
        }

        int     id         = (int)    modelo.getValueAt(fila, 0);
        String  username   = (String) modelo.getValueAt(fila, 1);
        String  rolLegible = (String) modelo.getValueAt(fila, 2);
        boolean activo     = "Si".equals(modelo.getValueAt(fila, 3));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 5, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        // Usuario
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblUser, gbc);

        JTextField tfUsuario = UIUtils.crearCampo(20);
        tfUsuario.setText(username);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(tfUsuario, gbc);

        // Acceso
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblRol = new JLabel("Acceso:");
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblRol, gbc);

        JComboBox<String> cbRol = new JComboBox<>();
        for (String[] r : ROLES) {
            cbRol.addItem(r[1]);
        }
        cbRol.setSelectedItem(rolLegible);
        cbRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(cbRol, gbc);

        // Estado
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblEst = new JLabel("Estado:");
        lblEst.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblEst, gbc);

        JCheckBox chkActivo = new JCheckBox("Activo", activo);
        chkActivo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkActivo.setOpaque(false);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(chkActivo, gbc);

        int resp = JOptionPane.showConfirmDialog(
                this, panel, "Editar Usuario",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resp != JOptionPane.OK_OPTION) return;

        String  nuevoUser   = tfUsuario.getText().trim();
        String  nuevoRolLeg = (String) cbRol.getSelectedItem();
        String  nuevoRol    = obtenerRolInterno(nuevoRolLeg);
        boolean nuevoActivo = chkActivo.isSelected();

        if (nuevoUser.isEmpty()) {
            UIUtils.mostrarError(this, "El nombre de usuario es obligatorio.");
            return;
        }

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                usuarioDAO.editar(id, nuevoUser, nuevoRol, nuevoActivo);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    UIUtils.mostrarInfo(UsuariosPanel.this,
                            "Usuario editado correctamente.");
                    cargarUsuarios();
                } catch (Exception e) {
                    UIUtils.mostrarError(UsuariosPanel.this,
                            e.getCause() != null
                                    ? e.getCause().getMessage()
                                    : e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // RESETEAR CONTRASENA
    // ══════════════════════════════════════════════════════════════════════

    private void resetearPasswordSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this, "Selecciona un usuario de la tabla.");
            return;
        }

        int    id       = (int)    modelo.getValueAt(fila, 0);
        String username = (String) modelo.getValueAt(fila, 1);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 5, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        JLabel lblInfo = new JLabel(
                "<html>Resetear contrasena de <b>" + username + "</b></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblInfo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel l1 = new JLabel("Nueva:");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(l1, gbc);

        JPasswordField pfNueva = crearPasswordField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(pfNueva, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel l2 = new JLabel("Repetir:");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(l2, gbc);

        JPasswordField pfRepetir = crearPasswordField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(pfRepetir, gbc);

        int resp = JOptionPane.showConfirmDialog(
                this, panel, "Resetear Contrasena",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resp != JOptionPane.OK_OPTION) return;

        String nueva   = new String(pfNueva.getPassword()).trim();
        String repetir = new String(pfRepetir.getPassword()).trim();

        if (nueva.isEmpty() || nueva.length() < 4) {
            UIUtils.mostrarError(this,
                    "La contrasena debe tener al menos 4 caracteres.");
            return;
        }
        if (!nueva.equals(repetir)) {
            UIUtils.mostrarError(this, "Las contrasenas no coinciden.");
            return;
        }

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                usuarioDAO.resetearPassword(id, nueva);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    UIUtils.mostrarInfo(UsuariosPanel.this,
                            "Contrasena reseteada correctamente.");
                } catch (Exception e) {
                    UIUtils.mostrarError(UsuariosPanel.this,
                            e.getCause() != null
                                    ? e.getCause().getMessage()
                                    : e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // ELIMINAR USUARIO
    // ══════════════════════════════════════════════════════════════════════

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UIUtils.mostrarError(this, "Selecciona un usuario de la tabla.");
            return;
        }

        int    id       = (int)    modelo.getValueAt(fila, 0);
        String username = (String) modelo.getValueAt(fila, 1);

        if (usuarioActual != null && id == usuarioActual.getId()) {
            UIUtils.mostrarError(this, "No puedes eliminar tu propio usuario.");
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
                "Eliminar el usuario \"" + username + "\"?\n" +
                "Esta accion no se puede deshacer.",
                "Confirmar Eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                usuarioDAO.eliminar(id);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    UIUtils.mostrarInfo(UsuariosPanel.this,
                            "Usuario eliminado correctamente.");
                    cargarUsuarios();
                } catch (Exception e) {
                    UIUtils.mostrarError(UsuariosPanel.this,
                            e.getCause() != null
                                    ? e.getCause().getMessage()
                                    : e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private String obtenerRolInterno(String legible) {
        for (String[] r : ROLES) {
            if (r[1].equals(legible)) return r[0];
        }
        return "FICHAJE";
    }

    private JPasswordField crearPasswordField() {
        JPasswordField pf = new JPasswordField(20);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return pf;
    }
}