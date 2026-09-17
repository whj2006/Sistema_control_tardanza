package com.instituto.tardanzas.model;

public class ConfiguracionHorarios {

    private int    id;
    private String horaEntrada; // "HH:mm"

    public ConfiguracionHorarios() {}

    public ConfiguracionHorarios(String horaEntrada) {
        this.id          = 1;
        this.horaEntrada = horaEntrada;
    }

    public int    getId()          { return id; }
    public String getHoraEntrada() { return horaEntrada; }

    public void setId(int id)            { this.id = id; }
    public void setHoraEntrada(String h) { this.horaEntrada = h; }
}
