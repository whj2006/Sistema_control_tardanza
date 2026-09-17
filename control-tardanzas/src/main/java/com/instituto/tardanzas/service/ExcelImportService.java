package com.instituto.tardanzas.service;

import com.instituto.tardanzas.dao.AlumnoDAO;
import com.instituto.tardanzas.dao.CursoDAO;
import com.instituto.tardanzas.model.Alumno;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelImportService {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final CursoDAO  cursoDAO  = new CursoDAO();

    public static class ResultadoImportacion {
        public int          procesados    = 0;
        public int          errores       = 0;
        public List<String> mensajesError = new ArrayList<>();

        public String resumen() {
            return String.format(
                "Importacion completada:\n" +
                "  Filas procesadas correctamente: %d\n" +
                "  Filas con error:                %d",
                procesados, errores);
        }
    }

    public ResultadoImportacion importar(File archivo) throws Exception {
        ResultadoImportacion resultado = new ResultadoImportacion();

        try (FileInputStream fis      = new FileInputStream(archivo);
             Workbook        workbook = new XSSFWorkbook(fis)) {

            Sheet hoja = workbook.getSheetAt(0);

            boolean primeraFila = true;

            for (Row fila : hoja) {
                if (filaVacia(fila)) continue;

                if (primeraFila) {
                    primeraFila = false;
                    if (esCabecera(fila)) continue;
                }

                procesarFila(fila, resultado);
            }
        }
        return resultado;
    }

    private void procesarFila(Row fila, ResultadoImportacion resultado) {
        int numFila = fila.getRowNum() + 1;
        try {
            String nia         = leerCelda(fila, 0);
            String nombre      = leerCelda(fila, 1);
            String apellido1   = leerCelda(fila, 2);
            String apellido2   = leerCelda(fila, 3);
            String email1      = leerCelda(fila, 4);
            String email2      = leerCelda(fila, 5);
            String nombreCurso = leerCelda(fila, 6);

            // ── Validaciones obligatorias ─────────────────────────────────
            if (nia.isEmpty())
                throw new Exception("NIA vacio");
            if (nombre.isEmpty())
                throw new Exception("Nombre vacio");
            if (apellido1.isEmpty())
                throw new Exception("Apellido 1 vacio");
            if (nombreCurso.isEmpty())
                throw new Exception("Curso vacio");
            if (nia.length() > 8)
                throw new Exception("NIA supera 8 caracteres");

            // ── Apellido 2 opcional ───────────────────────────────────────
            if (apellido2 == null) apellido2 = "";

            // ── Emails: normalizar primero, validar despues ───────────────
            String email1Final = normalizarEmail(email1);
            String email2Final = normalizarEmail(email2);

            // Solo validar si tienen contenido real tras normalizar
            if (email1Final != null && !email1Final.contains("@"))
                throw new Exception(
                        "Email familia 1 no valido: " + email1Final);
            if (email2Final != null && !email2Final.contains("@"))
                throw new Exception(
                        "Email familia 2 no valido: " + email2Final);

            // ── Crear curso si no existe ──────────────────────────────────
            cursoDAO.crearSiNoExiste(nombreCurso);
            int idCurso = cursoDAO.obtenerIdPorNombre(nombreCurso);

            // ── Insertar o actualizar alumno ──────────────────────────────
            Alumno alumno = new Alumno(
                    nia, nombre, apellido1, apellido2,
                    email1Final, email2Final, idCurso);
            alumnoDAO.importarActualizar(alumno);

            resultado.procesados++;

        } catch (Exception e) {
            resultado.errores++;
            resultado.mensajesError.add(
                    "Fila " + numFila + ": " + e.getMessage());
        }
    }

    /**
     * Normaliza un email:
     * - Elimina TODOS los tipos de espacios y caracteres invisibles
     * - Si queda vacío devuelve null
     * - Si no contiene @ devuelve null (se trata como vacío)
     * - Convierte a minúsculas
     */
    private String normalizarEmail(String email) {
        if (email == null) return null;

        // Eliminar espacios normales, no separables, tabs, etc.
        String limpio = email
                .replace("\u00A0", "")   // espacio no separable
                .replace("\t", "")       // tab
                .replace("\r", "")       // retorno de carro
                .replace("\n", "")       // salto de linea
                .trim();                 // espacios normales

        if (limpio.isEmpty()) return null;

        // Si no tiene @ no es un email válido → tratarlo como vacío
        if (!limpio.contains("@")) return null;

        return limpio.toLowerCase();
    }

    /**
     * Lee una celda como texto limpio.
     * Nunca devuelve null — devuelve "" si está vacía.
     */
    private String leerCelda(Row fila, int columna) {
        Cell celda = fila.getCell(
                columna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (celda == null) return "";

        String valor = switch (celda.getCellType()) {
            case STRING -> {
                String s = celda.getStringCellValue();
                yield s == null ? "" : s;
            }
            case NUMERIC -> {
                double val = celda.getNumericCellValue();
                yield String.valueOf((long) val);
            }
            case BOOLEAN ->
                String.valueOf(celda.getBooleanCellValue());
            case FORMULA -> {
                try {
                    String s = celda.getStringCellValue();
                    yield s == null ? "" : s;
                } catch (Exception e) {
                    yield String.valueOf((long) celda.getNumericCellValue());
                }
            }
            case BLANK  -> "";
            default     -> "";
        };

        // Limpiar caracteres invisibles y espacios de todo tipo
        return valor
                .replace("\u00A0", "")  // espacio no separable
                .replace("\t", "")      // tab
                .replace("\r", "")      // retorno de carro
                .replace("\n", "")      // salto de linea
                .trim();
    }

    private boolean esCabecera(Row fila) {
        String primera = leerCelda(fila, 0).toLowerCase();
        return primera.contains("nia")
            || primera.contains("id")
            || primera.contains("alumno")
            || primera.contains("nombre");
    }

    private boolean filaVacia(Row fila) {
        if (fila == null) return true;
        for (int i = 0; i < 7; i++) {
            if (!leerCelda(fila, i).isEmpty()) return false;
        }
        return true;
    }
}