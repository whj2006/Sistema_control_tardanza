package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.dao.CursoDAO;
import com.instituto.tardanzas.dao.RetrasoDAO;
import com.instituto.tardanzas.model.Curso;
import com.instituto.tardanzas.model.RankingEntry;
import com.instituto.tardanzas.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RankingPanel extends JPanel {

    private final RetrasoDAO retrasoDAO = new RetrasoDAO();
    private final CursoDAO   cursoDAO   = new CursoDAO();

    private JTable            tabla;
    private DefaultTableModel modelo;
    private JComboBox<String> cbCurso;
    private List<Curso>       cursos;
    private Timer             timerRefresco;

    public RankingPanel() {
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        construirUI();
        cargarCursos();
        iniciarAutoRefresco();
    }

    // ── Métodos públicos para actualización inmediata ─────────────────────

    // Refresca solo los datos del ranking (cuando hay nuevo retraso)
    public void refrescar() {
        SwingUtilities.invokeLater(this::cargarRanking);
    }

    // Refresca cursos Y ranking (cuando se añade/edita/elimina un curso)
    public void refrescarCursos() {
        SwingUtilities.invokeLater(this::cargarCursos);
    }

    private void construirUI() {
        JPanel norte = new JPanel(new BorderLayout(0, 8));
        norte.setOpaque(false);
        norte.add(UIUtils.crearTitulo("Ranking de Tardanzas"),
                BorderLayout.NORTH);

        JPanel filtros =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);

        JLabel lbl = new JLabel("Filtrar por curso:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        cbCurso = new JComboBox<>();
        cbCurso.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbCurso.setPreferredSize(new Dimension(180, 30));
        cbCurso.addActionListener(e -> cargarRanking());

        filtros.add(lbl);
        filtros.add(cbCurso);
        norte.add(filtros, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        String[] cols = {"Pos.", "NIA", "Alumno", "Curso",
                         "Retrasos", "Minutos Perdidos"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        UIUtils.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(1).setMaxWidth(80);
        tabla.getColumnModel().getColumn(4).setMaxWidth(80);
        tabla.getColumnModel().getColumn(5).setMaxWidth(130);

        tabla.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            private final Color ORO    = new Color(255, 245, 157);
            private final Color PLATA  = new Color(224, 224, 224);
            private final Color BRONCE = new Color(255, 224, 178);

            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(switch (row) {
                        case 0  -> ORO;
                        case 1  -> PLATA;
                        case 2  -> BRONCE;
                        default -> row % 2 == 0
                                ? UIUtils.COLOR_BLANCO
                                : new Color(245, 248, 250);
                    });
                    setForeground(UIUtils.COLOR_PRIMARIO);
                }
                setBorder(BorderFactory
                        .createEmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(
                        (col == 4 || col == 5) ? CENTER : LEFT);
                return this;
            }
        });

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.setBorder(
                BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JPanel leyenda =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leyenda.setOpaque(false);
        leyenda.add(chip("1 Puesto", new Color(255, 245, 157)));
        leyenda.add(chip("2 Puesto", new Color(224, 224, 224)));
        leyenda.add(chip("3 Puesto", new Color(255, 224, 178)));

        JLabel lblAuto = new JLabel(
                "Actualizacion automatica cada 15 segundos");
        lblAuto.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblAuto.setForeground(UIUtils.COLOR_TEXTO_CLARO);

        sur.add(leyenda,  BorderLayout.WEST);
        sur.add(lblAuto,  BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);
    }

    // ── Cargar cursos ─────────────────────────────────────────────────────

    private void cargarCursos() {
        SwingWorker<List<Curso>, Void> w = new SwingWorker<>() {
            @Override protected List<Curso> doInBackground()
                    throws Exception {
                return cursoDAO.listarTodos();
            }
            @Override protected void done() {
                try {
                    cursos = get();
                    // Guardar selección actual para restaurarla
                    String selActual =
                            (String) cbCurso.getSelectedItem();

                    // Desconectar listener mientras llenamos el combo
                    // para evitar recargas dobles
                    var listeners = cbCurso.getActionListeners();
                    for (var l : listeners)
                        cbCurso.removeActionListener(l);

                    cbCurso.removeAllItems();
                    cbCurso.addItem("Todos los cursos");
                    cursos.forEach(c -> cbCurso.addItem(
                            c.getNombre()));

                    // Restaurar selección anterior si sigue existiendo
                    boolean restaurado = false;
                    if (selActual != null) {
                        for (int i = 0;
                                i < cbCurso.getItemCount(); i++) {
                            if (cbCurso.getItemAt(i)
                                    .equals(selActual)) {
                                cbCurso.setSelectedIndex(i);
                                restaurado = true;
                                break;
                            }
                        }
                    }
                    if (!restaurado)
                        cbCurso.setSelectedIndex(0);

                    // Reconectar listener
                    for (var l : listeners)
                        cbCurso.addActionListener(l);

                    // Recargar ranking con el filtro actualizado
                    cargarRanking();

                } catch (Exception e) {
                    UIUtils.mostrarError(
                            RankingPanel.this, e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── Cargar ranking ────────────────────────────────────────────────────

    private void cargarRanking() {
        String filtro = (String) cbCurso.getSelectedItem();

        SwingWorker<List<RankingEntry>, Void> w = new SwingWorker<>() {
            @Override protected List<RankingEntry> doInBackground()
                    throws Exception {
                return retrasoDAO.rankingTardanzas();
            }
            @Override protected void done() {
                try {
                    List<RankingEntry> lista = get();
                    modelo.setRowCount(0);
                    int pos = 1;
                    for (RankingEntry e : lista) {
                        if (filtro != null &&
                                !filtro.equals("Todos los cursos") &&
                                !e.getCurso().equals(filtro)) {
                            continue;
                        }
                        modelo.addRow(new Object[]{
                            pos++,
                            e.getNia(),
                            e.getAlumno(),
                            e.getCurso(),
                            e.getTotalRetrasos(),
                            e.getTotalMinutosPerdidos() + " min"
                        });
                    }
                } catch (Exception e) {
                    UIUtils.mostrarError(
                            RankingPanel.this, e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── Auto-refresco ─────────────────────────────────────────────────────

    private void iniciarAutoRefresco() {
        // Cada 15s refresca cursos y ranking completo
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

    // ── Chip leyenda ──────────────────────────────────────────────────────

    private JPanel chip(String texto, Color color) {
        JPanel p = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel cuadro = new JLabel("   ");
        cuadro.setOpaque(true);
        cuadro.setBackground(color);
        cuadro.setBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1));
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        p.add(cuadro);
        p.add(lbl);
        return p;
    }
}
