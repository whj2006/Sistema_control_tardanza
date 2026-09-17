package com.instituto.tardanzas.model;

public class Configuracion {

    private String smtpHost;
    private int    smtpPuerto;
    private String smtpUsuario;
    private String smtpPassword;
    private String emailRemitente;
    private String nombreRemitente;

    public Configuracion() {}

    public String getSmtpHost()                          { return smtpHost; }
    public void   setSmtpHost(String smtpHost)           { this.smtpHost = smtpHost; }

    public int    getSmtpPuerto()                        { return smtpPuerto; }
    public void   setSmtpPuerto(int smtpPuerto)          { this.smtpPuerto = smtpPuerto; }

    public String getSmtpUsuario()                       { return smtpUsuario; }
    public void   setSmtpUsuario(String smtpUsuario)     { this.smtpUsuario = smtpUsuario; }

    public String getSmtpPassword()                      { return smtpPassword; }
    public void   setSmtpPassword(String smtpPassword)   { this.smtpPassword = smtpPassword; }

    public String getEmailRemitente()                        { return emailRemitente; }
    public void   setEmailRemitente(String emailRemitente)   { this.emailRemitente = emailRemitente; }

    public String getNombreRemitente()                         { return nombreRemitente; }
    public void   setNombreRemitente(String nombreRemitente)   { this.nombreRemitente = nombreRemitente; }
}