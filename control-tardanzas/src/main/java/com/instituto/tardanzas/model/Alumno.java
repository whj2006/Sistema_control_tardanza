package com.instituto.tardanzas.model;

public class Alumno {

    private String nia;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String emailFamilia1;
    private String emailFamilia2;
    private int    idCurso;
    private String nombreCurso; // solo para mostrar en tablas

    public Alumno() {}

    public Alumno(String nia, String nombre,
                  String apellido1, String apellido2,
                  String emailFamilia1, String emailFamilia2,
                  int idCurso) {
        this.nia           = nia;
        this.nombre        = nombre;
        this.apellido1     = apellido1;
        this.apellido2     = apellido2;
        this.emailFamilia1 = emailFamilia1;
        this.emailFamilia2 = emailFamilia2;
        this.idCurso       = idCurso;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getNia()            { return nia; }
    public void   setNia(String nia)  { this.nia = nia; }

    public String getNombre()                 { return nombre; }
    public void   setNombre(String nombre)    { this.nombre = nombre; }

    public String getApellido1()                  { return apellido1; }
    public void   setApellido1(String apellido1)  { this.apellido1 = apellido1; }

    public String getApellido2()                  { return apellido2; }
    public void   setApellido2(String apellido2)  { this.apellido2 = apellido2; }

    public String getEmailFamilia1()                       { return emailFamilia1; }
    public void   setEmailFamilia1(String emailFamilia1)   { this.emailFamilia1 = emailFamilia1; }

    public String getEmailFamilia2()                       { return emailFamilia2; }
    public void   setEmailFamilia2(String emailFamilia2)   { this.emailFamilia2 = emailFamilia2; }

    public int  getIdCurso()               { return idCurso; }
    public void setIdCurso(int idCurso)    { this.idCurso = idCurso; }

    public String getNombreCurso()                    { return nombreCurso; }
    public void   setNombreCurso(String nombreCurso)  { this.nombreCurso = nombreCurso; }

    // ── Helpers ────────────────────────────────────────────────────────────

    public String getApellidos() {
        StringBuilder sb = new StringBuilder();
        if (apellido1 != null && !apellido1.isBlank())
            sb.append(apellido1);
        if (apellido2 != null && !apellido2.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(apellido2);
        }
        return sb.toString();
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + getApellidos();
    }

    /** Devuelve los dos emails separados por coma (útil para mostrar) */
    public String getEmailsConcatenados() {
        StringBuilder sb = new StringBuilder();
        if (emailFamilia1 != null && !emailFamilia1.isBlank())
            sb.append(emailFamilia1);
        if (emailFamilia2 != null && !emailFamilia2.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(emailFamilia2);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getNombreCompleto().trim() + " [" + nia + "]";
    }
}