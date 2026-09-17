package com.instituto.tardanzas.model;

import java.time.LocalDateTime;

public class Usuario {

    private int           id;
    private String        username;
    private String        password;
    private String        rol;
    private boolean       activo;
    private LocalDateTime fechaCreacion;

    public Usuario() {}

    public Usuario(int id, String username, String rol) {
        this.id       = id;
        this.username = username;
        this.rol      = rol;
    }

    public int           getId()                             { return id; }
    public void          setId(int id)                       { this.id = id; }

    public String        getUsername()                        { return username; }
    public void          setUsername(String username)         { this.username = username; }

    public String        getPassword()                       { return password; }
    public void          setPassword(String password)        { this.password = password; }

    public String        getRol()                            { return rol; }
    public void          setRol(String rol)                  { this.rol = rol; }

    public boolean       isActivo()                          { return activo; }
    public void          setActivo(boolean activo)           { this.activo = activo; }

    public LocalDateTime getFechaCreacion()                  { return fechaCreacion; }
    public void          setFechaCreacion(LocalDateTime fc)  { this.fechaCreacion = fc; }

    public boolean esTodo()                  { return "TODO".equals(rol); }
    public boolean esFichaje()               { return "FICHAJE".equals(rol); }
    public boolean esFichajeInformes()       { return "FICHAJE_INFORMES".equals(rol); }
    public boolean esFichajeInformesGestion(){ return "FICHAJE_INFORMES_GESTION".equals(rol); }

    /**
     * Devuelve el nombre legible del rol.
     */
    public String getRolLegible() {
        return switch (rol) {
            case "TODO"                       -> "Todo";
            case "FICHAJE"                    -> "Fichaje";
            case "FICHAJE_INFORMES"           -> "Fichaje + Informes";
            case "FICHAJE_INFORMES_GESTION"   -> "Fichaje + Informes + Gestion";
            default                           -> rol;
        };
    }

    @Override
    public String toString() {
        return username + " (" + getRolLegible() + ")";
    }
}
