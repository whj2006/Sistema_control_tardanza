package com.instituto.tardanzas.model;

public class RankingEntry {

    private String nia;
    private String alumno;
    private String curso;
    private int    totalRetrasos;
    private int    totalMinutosPerdidos;

    public RankingEntry() {}

    public String getNia()                      { return nia; }
    public void   setNia(String nia)            { this.nia = nia; }

    public String getAlumno()                   { return alumno; }
    public void   setAlumno(String alumno)      { this.alumno = alumno; }

    public String getCurso()                    { return curso; }
    public void   setCurso(String curso)        { this.curso = curso; }

    public int  getTotalRetrasos()                        { return totalRetrasos; }
    public void setTotalRetrasos(int totalRetrasos)       { this.totalRetrasos = totalRetrasos; }

    public int  getTotalMinutosPerdidos()                          { return totalMinutosPerdidos; }
    public void setTotalMinutosPerdidos(int totalMinutosPerdidos)  { this.totalMinutosPerdidos = totalMinutosPerdidos; }
}