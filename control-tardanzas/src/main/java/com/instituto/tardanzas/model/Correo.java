package com.instituto.tardanzas.model;

import java.time.LocalDateTime;

public class Correo {

    private int           id;
    private int           idRetraso;
    private String        emailDestino;   // ← NUEVO: a quién se envía este correo concreto
    private String        asunto;
    private String        mensaje;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
    private String        estado;
    private String        errorEnvio;

    // Campos extra de vista
    private String nia;
    private String nombreAlumno;
    private LocalDateTime fechaRetraso;
    private int    minutosTarde;

    public Correo() {}

    // ── Getters / Setters ──────────────────────────────────────────────────

    public int    getId()                  { return id; }
    public void   setId(int id)            { this.id = id; }

    public int    getIdRetraso()               { return idRetraso; }
    public void   setIdRetraso(int idRetraso)  { this.idRetraso = idRetraso; }

    public String getEmailDestino()                     { return emailDestino; }
    public void   setEmailDestino(String emailDestino)  { this.emailDestino = emailDestino; }

    public String getAsunto()                  { return asunto; }
    public void   setAsunto(String asunto)     { this.asunto = asunto; }

    public String getMensaje()                  { return mensaje; }
    public void   setMensaje(String mensaje)    { this.mensaje = mensaje; }

    public LocalDateTime getFechaCreacion()                               { return fechaCreacion; }
    public void          setFechaCreacion(LocalDateTime fechaCreacion)    { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEnvio()                             { return fechaEnvio; }
    public void          setFechaEnvio(LocalDateTime fechaEnvio)     { this.fechaEnvio = fechaEnvio; }

    public String getEstado()                  { return estado; }
    public void   setEstado(String estado)     { this.estado = estado; }

    public String getErrorEnvio()                    { return errorEnvio; }
    public void   setErrorEnvio(String errorEnvio)   { this.errorEnvio = errorEnvio; }

    public String getNia()               { return nia; }
    public void   setNia(String nia)     { this.nia = nia; }

    public String getNombreAlumno()                      { return nombreAlumno; }
    public void   setNombreAlumno(String nombreAlumno)   { this.nombreAlumno = nombreAlumno; }

    public LocalDateTime getFechaRetraso()                              { return fechaRetraso; }
    public void          setFechaRetraso(LocalDateTime fechaRetraso)    { this.fechaRetraso = fechaRetraso; }

    public int  getMinutosTarde()                       { return minutosTarde; }
    public void setMinutosTarde(int minutosTarde)       { this.minutosTarde = minutosTarde; }

    // ── Alias para compatibilidad con código existente ────────────────────
    // (algunas partes del UI siguen llamando a getEmailFamilia)

    public String getEmailFamilia() {
        return emailDestino;
    }

    public void setEmailFamilia(String emailFamilia) {
        this.emailDestino = emailFamilia;
    }
}
