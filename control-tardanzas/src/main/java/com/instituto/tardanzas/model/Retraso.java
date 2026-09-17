package com.instituto.tardanzas.model;

import java.time.LocalDateTime;

public class Retraso {

    private int           id;
    private String        nia;
    private LocalDateTime fechaHora;
    private int           minutosTarde;

    // Campos extra de vistas
    private String nombreCompleto;
    private String curso;

    public Retraso() {}

    public int           getId()                      { return id; }
    public void          setId(int id)                { this.id = id; }

    public String        getNia()                     { return nia; }
    public void          setNia(String nia)           { this.nia = nia; }

    public LocalDateTime getFechaHora()                           { return fechaHora; }
    public void          setFechaHora(LocalDateTime fechaHora)    { this.fechaHora = fechaHora; }

    public int  getMinutosTarde()                       { return minutosTarde; }
    public void setMinutosTarde(int minutosTarde)       { this.minutosTarde = minutosTarde; }

    public String getNombreCompleto()                          { return nombreCompleto; }
    public void   setNombreCompleto(String nombreCompleto)     { this.nombreCompleto = nombreCompleto; }

    public String getCurso()                { return curso; }
    public void   setCurso(String curso)    { this.curso = curso; }
}
