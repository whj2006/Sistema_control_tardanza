package com.instituto.tardanzas.model;

public class Curso {

    private int    id;
    private String nombre;
    private int    totalAlumnos;

    public Curso() {}

    public Curso(int id, String nombre) {
        this.id     = id;
        this.nombre = nombre;
    }

    public int    getId()                { return id; }
    public void   setId(int id)          { this.id = id; }

    public String getNombre()                 { return nombre; }
    public void   setNombre(String nombre)    { this.nombre = nombre; }

    public int  getTotalAlumnos()                    { return totalAlumnos; }
    public void setTotalAlumnos(int totalAlumnos)    { this.totalAlumnos = totalAlumnos; }

    @Override
    public String toString() { return nombre; }
}