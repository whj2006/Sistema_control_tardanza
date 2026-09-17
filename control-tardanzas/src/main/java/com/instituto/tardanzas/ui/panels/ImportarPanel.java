package com.instituto.tardanzas.ui.panels;

import com.instituto.tardanzas.service.ExcelImportService;
import com.instituto.tardanzas.ui.util.UIUtils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.*;

public class ImportarPanel extends JPanel {

    private final ExcelImportService importService = new ExcelImportService();
    private final Runnable           onDatosCambiados;

    public ImportarPanel(Runnable onDatosCambiados) {
        this.onDatosCambiados = onDatosCambiados;
        setBackground(UIUtils.COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        construirUI();
    }

    public ImportarPanel() {
        this(null);
    }

    private void notificarCambio() {
        if (onDatosCambiados != null) onDatosCambiados.run();
    }

    private void construirUI() {
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        norte.add(UIUtils.crearTitulo("Importar Alumnos"),
                BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.gridy   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor  = GridBagConstraints.NORTH;
        gbc.insets  = new Insets(0, 0, 0, 0);

        centro.add(construirTarjetaImportar(), gbc);

        // Glue para empujar la tarjeta hacia arriba
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        centro.add(new JPanel() {{ setOpaque(false); }}, gbc);

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

    private JPanel construirTarjetaImportar() {
        JPanel card = crearCard();
        card.setPreferredSize(new Dimension(0, 260));
        card.add(crearTituloTarjeta("Importar\nAlumnos"),
                BorderLayout.WEST);
        card.add(crearDescripcionCentrada(
            "Importa alumnos desde un archivo Excel (.xlsx).\n\n"
            + "Columnas:\n"
            + "  A = NIA (8 dígitos)   ·   B = Nombre   ·   "
            + "C = Apellido 1   ·   D = Apellido 2\n"
            + "  E = Email familia 1   ·   F = Email familia 2 (opcional)   ·   "
            + "G = Curso\n\n"
            + "Si el NIA ya existe el alumno se actualiza.\n"
            + "Los cursos se crean solos si no existen.\n"
            + "Si el Email 2 está vacío, se usará el mismo que el Email 1."
        ), BorderLayout.CENTER);

        JPanel panelBtns = crearPanelBotones();

        JButton btnPlantilla = UIUtils.crearBoton(
                "Descargar plantilla", UIUtils.COLOR_PRIMARIO);
        btnPlantilla.setAlignmentX(CENTER_ALIGNMENT);
        btnPlantilla.setMaximumSize(new Dimension(200, 40));
        btnPlantilla.setPreferredSize(new Dimension(200, 40));
        btnPlantilla.addActionListener(e -> descargarPlantilla());

        JButton btnImportar = UIUtils.crearBoton(
                "Seleccionar Excel", UIUtils.COLOR_VERDE);
        btnImportar.setAlignmentX(CENTER_ALIGNMENT);
        btnImportar.setMaximumSize(new Dimension(200, 40));
        btnImportar.setPreferredSize(new Dimension(200, 40));
        btnImportar.addActionListener(e -> importarExcel());

        panelBtns.add(btnPlantilla);
        panelBtns.add(Box.createVerticalStrut(12));
        panelBtns.add(btnImportar);
        panelBtns.add(Box.createVerticalGlue());

        card.add(panelBtns, BorderLayout.EAST);
        return card;
    }

    // ── IMPORTAR ──────────────────────────────────────────────────────────

    private void importarExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecciona el archivo Excel de alumnos");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Excel 2007-365 (*.xlsx)", "xlsx"));
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = fc.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
            UIUtils.mostrarError(this, "Solo se admiten archivos .xlsx");
            return;
        }

        JDialog dlg = crearDialogoProgreso("Importando alumnos...");

        SwingWorker<ExcelImportService.ResultadoImportacion, Void> w =
                new SwingWorker<>() {
            @Override
            protected ExcelImportService.ResultadoImportacion
                    doInBackground() throws Exception {
                return importService.importar(archivo);
            }
            @Override
            protected void done() {
                dlg.dispose();
                try {
                    mostrarResultadoImportacion(get());
                    notificarCambio();
                } catch (Exception e) {
                    UIUtils.mostrarError(ImportarPanel.this,
                            "Error al importar:\n" + e.getMessage());
                }
            }
        };
        w.execute();
    }

    private void mostrarResultadoImportacion(
            ExcelImportService.ResultadoImportacion res) {
        StringBuilder sb = new StringBuilder(res.resumen());
        if (!res.mensajesError.isEmpty()) {
            sb.append("\n\nDetalle de errores:");
            res.mensajesError.forEach(m -> sb.append("\n  - ").append(m));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(500, 260));

        JOptionPane.showMessageDialog(this, sp,
                "Resultado de Importación",
                res.errores > 0
                        ? JOptionPane.WARNING_MESSAGE
                        : JOptionPane.INFORMATION_MESSAGE);
    }

    // ── PLANTILLA ─────────────────────────────────────────────────────────

    private void descargarPlantilla() {
        JFileChooser fc = crearFileChooserGuardar("plantilla_alumnos.xlsx");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File destino = asegurarExtension(fc.getSelectedFile());

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (XSSFWorkbook wb = new XSSFWorkbook()) {
                    XSSFSheet hoja = wb.createSheet("Alumnos");
                    CellStyle estCab  = crearEstiloCabecera(wb);
                    CellStyle estNorm = crearEstiloNormal(wb);
                    CellStyle estAlt  = crearEstiloAlternado(wb);

                    Row rowCab = hoja.createRow(0);
                    rowCab.setHeightInPoints(22);
                    String[] cabs = {
                        "NIA", "Nombre", "Apellido 1", "Apellido 2",
                        "Email Familia 1", "Email Familia 2", "Curso"
                    };
                    for (int i = 0; i < cabs.length; i++) {
                        Cell c = rowCab.createCell(i);
                        c.setCellValue(cabs[i]);
                        c.setCellStyle(estCab);
                    }

                    Object[][] ejemplos = {
                        {"12345678","Juan","Garcia","Lopez",
                         "padre1@gmail.com","madre1@gmail.com","1 DAM"},
                        {"87654321","Maria","Martinez","Ruiz",
                         "padre2@gmail.com","madre2@gmail.com","1 DAM"},
                        {"11223344","Pedro","Sanchez","Vega",
                         "familia3@gmail.com","","2 DAM"},
                        {"99887766","Ana","Lopez","Torres",
                         "padre4@gmail.com","madre4@gmail.com","1 ASIR"}
                    };
                    for (int i = 0; i < ejemplos.length; i++) {
                        Row row = hoja.createRow(i + 1);
                        CellStyle est = i % 2 == 0 ? estNorm : estAlt;
                        for (int j = 0; j < ejemplos[i].length; j++) {
                            Cell c = row.createCell(j);
                            c.setCellValue(ejemplos[i][j].toString());
                            c.setCellStyle(est);
                        }
                    }

                    hoja.setColumnWidth(0, 3500);   // NIA
                    hoja.setColumnWidth(1, 4000);   // Nombre
                    hoja.setColumnWidth(2, 4500);   // Apellido 1
                    hoja.setColumnWidth(3, 4500);   // Apellido 2
                    hoja.setColumnWidth(4, 6500);   // Email 1
                    hoja.setColumnWidth(5, 6500);   // Email 2
                    hoja.setColumnWidth(6, 4000);   // Curso

                    try (FileOutputStream fos = new FileOutputStream(destino)) {
                        wb.write(fos);
                    }
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    mostrarExitoExportacion(destino);
                } catch (Exception e) {
                    UIUtils.mostrarError(ImportarPanel.this,
                            "Error al crear plantilla:\n" + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // ── ESTILOS EXCEL ─────────────────────────────────────────────────────

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

    // ── HELPERS UI ────────────────────────────────────────────────────────

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
                "Operación completada",
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