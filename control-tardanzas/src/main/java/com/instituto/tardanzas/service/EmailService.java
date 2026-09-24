package com.instituto.tardanzas.service;

import com.instituto.tardanzas.config.ConfigDB;
import com.instituto.tardanzas.dao.ConfiguracionDAO;
import com.instituto.tardanzas.dao.CorreoDAO;
import com.instituto.tardanzas.model.Configuracion;
import com.instituto.tardanzas.model.Correo;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailService {

    private final CorreoDAO      correoDAO = new CorreoDAO();
    private final ConfiguracionDAO configDAO = new ConfiguracionDAO();

    private ScheduledExecutorService reintentosScheduler;

    // ── Iniciar ───────────────────────────────────────────────────────────

    public void iniciar() {
        // Reintentos automáticos cada 3 minutos
        // para correos PENDIENTE o ERROR
        reintentosScheduler =
                Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r,
                            "email-reintentos");
                    t.setDaemon(true);
                    return t;
                });

        reintentosScheduler.scheduleAtFixedRate(
                this::procesarPendientes,
                30,    // primer intento a los 30s
                180,   // cada 3 minutos
                TimeUnit.SECONDS);

        System.out.println("[EmailService] Iniciado. "
            + "Reintentos automaticos cada 3 minutos.");
    }

    public void detener() {
        if (reintentosScheduler != null)
            reintentosScheduler.shutdownNow();
    }

    // ── Nombre del centro (configurable) ──────────────────────────────────

    private String nombreCentro() {
        return ConfigDB.cargar().getProperty(
                "centro.nombre",
                "Control de Tardanzas");
    }

    // ── Envío inmediato al fichar ─────────────────────────────────────────

    /**
     * Llamado desde FicharPanel justo después de
     * registrar un retraso. Procesa los pendientes
     * en un hilo separado para no bloquear la UI.
     */
    public void enviarInmediatamente() {
        new Thread(() -> {
            procesarPendientes();
        }, "email-inmediato").start();
    }

    // ── Procesar pendientes ───────────────────────────────────────────────

    public void procesarPendientes() {
        try {
            Configuracion cfg = configDAO.obtener();
            if (cfg == null) {
                System.err.println(
                    "[EmailService] Sin configuracion SMTP.");
                return;
            }
            List<Correo> pendientes =
                    correoDAO.obtenerPendientes();
            if (pendientes.isEmpty()) return;

            System.out.println("[EmailService] Enviando "
                    + pendientes.size() + " correo(s)...");

            Session session = crearSesion(cfg);
            for (Correo correo : pendientes)
                enviarCorreo(session, cfg, correo);

        } catch (Exception e) {
            System.err.println(
                "[EmailService] Error: " + e.getMessage());
        }
    }

    // ── Enviar Excel adjunto ──────────────────────────────────────────────

    public void enviarExcelAdjunto(
            String destinatario,
            String asunto,
            File   archivoAdjunto) throws Exception {

        Configuracion cfg = configDAO.obtener();
        if (cfg == null)
            throw new Exception(
                "No hay configuración SMTP. "
                + "Ve a Configuración y guarda los datos SMTP.");

        Session session = crearSesion(cfg);
        MimeMessage msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(
                cfg.getEmailRemitente(),
                cfg.getNombreRemitente(), "UTF-8"));
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(destinatario));
        msg.setSubject(asunto, "UTF-8");
        msg.setSentDate(new Date());

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(
            "<!DOCTYPE html><html lang='es'>"
            + "<head><meta charset='UTF-8'></head>"
            + "<body style='font-family:Arial,sans-serif;"
            + "background:#f5f5f5;padding:20px'>"
            + "<div style='max-width:600px;margin:auto;"
            + "background:#fff;border-radius:8px;padding:30px;"
            + "border-top:4px solid #781414'>"
            + "<h2 style='color:#781414'>"
            + nombreCentro() + "</h2>"
            + "<h3 style='color:#2c3e50'>Informe de Retrasos</h3>"
            + "<p>Adjunto encontrará el informe solicitado.</p>"
            + "<p style='background:#f8f9fa;padding:10px;"
            + "border-left:4px solid #781414;"
            + "font-weight:bold'>" + asunto + "</p>"
            + "<p>Generado el: <b>"
            + LocalDateTime.now().format(DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm"))
            + "</b></p>"
            + "<hr style='border:none;border-top:1px solid #eee;"
            + "margin:20px 0'>"
            + "<p style='font-size:12px;color:#999'>"
            + "Este mensaje ha sido generado automáticamente "
            + "por el sistema de Control de Retraso del centro."
            + "<br>No responda a este correo.</p>"
            + "</div></body></html>",
            "text/html; charset=UTF-8");

        MimeBodyPart adjuntoPart = new MimeBodyPart();
        adjuntoPart.attachFile(archivoAdjunto);
        adjuntoPart.setFileName(archivoAdjunto.getName());

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(htmlPart);
        mp.addBodyPart(adjuntoPart);
        msg.setContent(mp);

        Transport.send(msg);
        System.out.println("[EmailService] Excel enviado a: "
                + destinatario);
    }

    // ── Crear sesión SMTP ─────────────────────────────────────────────────

    private Session crearSesion(Configuracion cfg) {
        Properties props = new Properties();
        props.put("mail.smtp.host",
                cfg.getSmtpHost());
        props.put("mail.smtp.port",
                String.valueOf(cfg.getSmtpPuerto()));
        props.put("mail.smtp.auth",              "true");
        props.put("mail.smtp.starttls.enable",   "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols",     "TLSv1.2");
        props.put("mail.smtp.ssl.trust",
                cfg.getSmtpHost());
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout",           "30000");
        props.put("mail.smtp.writetimeout",      "30000");

        return Session.getInstance(props,
                new Authenticator() {
            @Override
            protected PasswordAuthentication
                    getPasswordAuthentication() {
                return new PasswordAuthentication(
                        cfg.getSmtpUsuario(),
                        cfg.getSmtpPassword());
            }
        });
    }

    // ── Enviar correo de retraso ──────────────────────────────────────────

    private void enviarCorreo(Session session,
                              Configuracion cfg,
                              Correo correo) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(
                    cfg.getEmailRemitente(),
                    cfg.getNombreRemitente(), "UTF-8"));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(
                            correo.getEmailFamilia()));
            msg.setSubject(correo.getAsunto(), "UTF-8");
            msg.setSentDate(new Date());

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(
                    construirHtml(correo),
                    "text/html; charset=UTF-8");

            Multipart mp =
                    new MimeMultipart("alternative");
            mp.addBodyPart(htmlPart);
            msg.setContent(mp);

            Transport.send(msg);
            correoDAO.marcarEnviado(correo.getId());
            System.out.println(
                    "[EmailService] Enviado OK → "
                    + correo.getEmailFamilia());

        } catch (Exception e) {
            String error = traducirError(e);
            try {
                correoDAO.marcarError(
                        correo.getId(), error);
            } catch (Exception ex) {
                System.err.println(
                    "[EmailService] No pudo marcar "
                    + "error: " + ex.getMessage());
            }
            System.err.println(
                "[EmailService] FALLO id="
                + correo.getId() + " → " + error);
        }
    }

    // ── HTML del correo de retraso ────────────────────────────────────────

    private String construirHtml(Correo correo) {
        String nombre = correo.getMensaje();
        String fecha  = "";
        String hora   = "";

        String[] partes =
                correo.getMensaje().split("\\|\\|");
        if (partes.length >= 3) {
            nombre = partes[0].trim();
            fecha  = partes[1].trim();
            hora   = partes[2].trim();
        }

        return "<!DOCTYPE html>"
             + "<html lang='es'>"
             + "<head><meta charset='UTF-8'></head>"
             + "<body style='font-family:Arial,sans-serif;"
             + "font-size:15px;background:#f5f5f5;"
             + "padding:20px'>"
             + "<div style='max-width:600px;margin:auto;"
             + "background:#fff;border-radius:8px;"
             + "padding:30px;border-top:4px solid #781414'>"
             + "<h2 style='color:#781414;margin-bottom:20px'>"
             + nombreCentro() + "</h2>"
             + "<h3 style='color:#2c3e50;margin-bottom:25px'>"
             + correo.getAsunto() + "</h3>"
             + "<p>Benvolgut/da pare/mare/tutor/a legal,</p>"
             + "<p>Us informem que l'alumne/a <b>"
             + nombre + "</b> "
             + "ha arribat amb retard al centre el dia <b>"
             + fecha + "</b> a les <b>" + hora + "</b>.</p>"
             + "<p style='color:#555;font-size:13px'>"
             + "No respondre aquest correu. "
             + "Quedem a la vostra disposició per a "
             + "qualsevol aclariment.</p>"
             + "<p>Atentament,<br>"
             + "<b>" + nombreCentro().toUpperCase()
             + ".</b></p>"
             + "<hr style='border:none;border-top:2px "
             + "solid #781414;margin:25px 0'>"
             + "<p>Estimado/a padre/madre/tutor/a legal,</p>"
             + "<p>Le informamos de que el/la alumno/a "
             + "<b>" + nombre + "</b>, ha llegado con retraso "
             + "al centro el día <b>" + fecha + "</b> a las "
             + "<b>" + hora + "</b>.</p>"
             + "<p style='color:#555;font-size:13px'>"
             + "No responder a este correo. "
             + "Quedamos a su disposición para "
             + "cualquier aclaración.</p>"
             + "<p>Atentamente,<br>"
             + "<b>" + nombreCentro().toUpperCase()
             + ".</b></p>"
             + "<hr style='border:none;border-top:1px "
             + "solid #eee;margin:20px 0'>"
             + "<p style='font-size:11px;color:#999;"
             + "text-align:center'>"
             + "Correo generat automàticament pel sistema "
             + "de Control de Retard del centre.<br>"
             + "Correo generado automáticamente por el "
             + "sistema de Control de Retraso del centro."
             + "</p>"
             + "</div></body></html>";
    }

    // ── Traducir errores ──────────────────────────────────────────────────

    private String traducirError(Exception e) {
        Throwable causa = e;
        StringBuilder todas = new StringBuilder();
        while (causa != null) {
            if (causa.getMessage() != null)
                todas.append(causa.getMessage())
                     .append(" ");
            causa = causa.getCause();
        }
        String completo = todas.toString().toLowerCase();

        if (completo.contains("authentication")
                || completo.contains("credentials")
                || completo.contains("535")
                || completo.contains("username"))
            return "Usuario o contraseña incorrectos.";
        if (completo.contains("timed out")
                || completo.contains("timeout"))
            return "Tiempo agotado conectando con "
                 + "el servidor SMTP.";
        if (completo.contains("connection refused"))
            return "Conexión rechazada por el servidor.";
        if (completo.contains("ssl")
                || completo.contains("tls"))
            return "Error SSL/TLS con el servidor SMTP.";
        if (completo.contains("invalid address"))
            return "Dirección de email del destinatario "
                 + "no válida.";

        String msg = e.getMessage();
        if (msg == null)
            return e.getClass().getSimpleName();
        return msg.length() > 200
                ? msg.substring(0, 200) : msg;
    }

    // ── Probar conexión ───────────────────────────────────────────────────

    public String probarConexion(Configuracion cfg) {
        try {
            Session session = crearSesion(cfg);
            try (Transport transport =
                    session.getTransport("smtp")) {
                transport.connect(
                        cfg.getSmtpHost(),
                        cfg.getSmtpPuerto(),
                        cfg.getSmtpUsuario(),
                        cfg.getSmtpPassword());
            }
            return "OK";
        } catch (Exception e) {
            return traducirError(e);
        }
    }
}