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

    // ── Nombre del centro (configurable, no hardcodeado) ──────────────────

    private String nombreCentro() {
        return ConfigDB.cargar().getProperty(
                "centro.nombre",
                "[Nombre del centro]");
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

    /**
     * Sincronizado para evitar que el hilo de reintentos
     * y el envío inmediato procesen la misma cola a la vez
     * (duplicar correos a las familias).
     */
    public synchronized void procesarPendientes() {
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

    // ── Resultado de conexión ─────────────────────────────────────────────

    public static class ResultadoConexion {
        public final boolean ok;
        public final String  motivo;
        public final String  detalle;

        private ResultadoConexion(boolean ok,
                                  String motivo,
                                  String detalle) {
            this.ok      = ok;
            this.motivo  = motivo;
            this.detalle = detalle;
        }
    }

    // ── Analizar el error → motivo claro + detalle del servidor ───────────

    private ResultadoConexion analizarError(Exception e) {
        // Recoger TODOS los mensajes de la cadena de excepciones
        Throwable causa = e;
        StringBuilder todas = new StringBuilder();
        StringBuilder raw   = new StringBuilder();
        while (causa != null) {
            if (causa.getMessage() != null) {
                todas.append(causa.getMessage()).append(" ");
                raw.append(causa.getMessage()).append(" | ");
            }
            causa = causa.getCause();
        }
        String completo = todas.toString().toLowerCase();
        String mensajeCrudo = raw.toString().trim();
        if (mensajeCrudo.endsWith("|")) {
            mensajeCrudo = mensajeCrudo.substring(
                    0, mensajeCrudo.length() - 1).trim();
        }

        // Detalle crudo del servidor (ej. 535 5.7.8 Username and Password...)
        String detalle = mensajeCrudo.isEmpty()
                ? e.getClass().getSimpleName()
                : (mensajeCrudo.length() > 500
                        ? mensajeCrudo.substring(0, 500) + "..."
                        : mensajeCrudo);

        // Interpretación en lenguaje claro. El orden importa:
        // primero los casos más específicos, luego los genéricos.
        String motivo = null;

        // ── Gmail: exige contraseña de aplicación ──
        if (completo.contains("application-specific password")
                || completo.contains("application specific password")
                || (completo.contains("5.7.8")
                    && completo.contains("password"))) {
            motivo = "Gmail rechaza la contraseña normal. "
                   + "Activa la verificacion en 2 pasos en tu "
                   + "cuenta Google y crea una CONTRASEÑA DE "
                   + "APLICACION (App Password) para usarla "
                   + "en este programa.";
        }
        // ── Gmail: pide iniciar sesión en el navegador ──
        else if (completo.contains("please log in via your web browser")
                || completo.contains("sign in via your web browser")
                || completo.contains("5.7.9")
                || (completo.contains("534")
                    && completo.contains("browser"))) {
            motivo = "Gmail ha bloqueado este intento de acceso "
                   + "por seguridad. Entra en tu cuenta de Google "
                   + "desde el navegador y activa una contraseña "
                   + "de aplicación.";
        }
        // ── Gmail: fallo temporal de autenticación ──
        else if (completo.contains("5.7.0")
                && (completo.contains("temporary")
                    || completo.contains("try again later"))) {
            motivo = "Gmail está teniendo un fallo temporal de "
                   + "autenticación. Espera unos minutos y vuelve "
                   + "a intentarlo (el programa lo reintenta solo).";
        }
        // ── Office 365: SMTP AUTH deshabilitado ──
        else if (completo.contains("smtpclientauthentication is disabled")
                || completo.contains("5.7.139")) {
            motivo = "SMTP AUTH está deshabilitado en el tenant "
                   + "de Office 365. El administrador debe "
                   + "habilitarlo para este buzón.";
        }
        // ── Office 365 / Outlook: cliente no autenticado ──
        else if (completo.contains("5.7.57")
                || completo.contains("5.7.54")
                || completo.contains("client not authenticated")) {
            motivo = "El servidor (Office 365 / Outlook) no permite "
                   + "el envío SMTP con estas credenciales. Revisa "
                   + "que la cuenta tenga SMTP AUTH habilitado o usa "
                   + "una contraseña de aplicación.";
        }
        // ── Cuenta con MFA / verificación en dos pasos ──
        else if (completo.contains("mfa")
                || completo.contains("multi-factor")
                || completo.contains("multi factor")
                || completo.contains("verification code")) {
            motivo = "La cuenta tiene MFA (verificación en dos "
                   + "pasos) activado. Debes usar una contraseña "
                   + "de aplicación (app password).";
        }
        // ── El servidor rechaza la autenticación (código 535 / AUTH) ──
        else if (completo.contains("535")
                || completo.contains("auth command")
                || completo.contains("authentication failed")
                || completo.contains("authentication unsuccessful")) {
            motivo = "El servidor rechazó el usuario o la contraseña. "
                   + "Revisa las credenciales y que la cuenta permita "
                   + "acceso SMTP (prueba con una contraseña de "
                   + "aplicación).";
        }
        // ── Credenciales genéricas ──
        else if (completo.contains("credentials")
                || (completo.contains("username")
                    && completo.contains("password"))) {
            motivo = "Usuario o contraseña incorrectos, o "
                   + "autenticación rechazada por el servidor.";
        }
        // ── Relay denegado ──
        else if (completo.contains("relay")
                || completo.contains("relaying")) {
            motivo = "El servidor SMTP no permite el envío (relay) "
                   + "desde esta dirección. Revisa que el email "
                   + "remitente pertenezca a la cuenta configurada.";
        }
        // ── Remitente rechazado ──
        else if (completo.contains("5.1.8")
                || (completo.contains("sender")
                    && completo.contains("reject"))) {
            motivo = "El servidor rechaza la dirección del remitente "
                   + "(email remitente). Usa un correo real y "
                   + "verificado en ese servidor.";
        }
        // ── Destinatario no existe o rechazado ──
        else if (completo.contains("5.1.1")
                || completo.contains("recipient address rejected")
                || completo.contains("user unknown")
                || completo.contains("no such user")
                || completo.contains("mailbox unavailable")) {
            motivo = "El destinatario no existe o fue rechazado por el "
                   + "servidor. Revisa la dirección de correo del "
                   + "alumno/familia.";
        }
        // ── Dirección del destinatario mal escrita ──
        else if (completo.contains("5.1.3")
                || completo.contains("bad recipient address")
                || completo.contains("invalid recipient")) {
            motivo = "La dirección del destinatario tiene un formato "
                   + "no válido.";
        }
        // ── Buzón lleno ──
        else if (completo.contains("5.2.2")
                || completo.contains("mailbox full")
                || completo.contains("quota exceeded")) {
            motivo = "El buzón del destinatario está lleno.";
        }
        // ── El servidor pide esperar (saturado / temporal) ──
        else if (completo.contains("421")
                || completo.contains("450")
                || completo.contains("451")
                || completo.contains("try again later")) {
            motivo = "El servidor SMTP está saturado o tiene un fallo "
                   + "temporal. Se reintentará automáticamente en "
                   + "unos minutos.";
        }
        // ── Tiempo agotado ──
        else if (completo.contains("timed out")
                || completo.contains("timeout")) {
            motivo = "Tiempo agotado conectando con el servidor "
                   + "SMTP. Revisa el puerto y que el firewall "
                   + "no lo bloquee.";
        }
        // ── Conexión rechazada ──
        else if (completo.contains("connection refused")) {
            motivo = "Conexión rechazada por el servidor. "
                   + "Puerto incorrecto o servidor no accesible.";
        }
        // ── Certificado SSL no fiable ──
        else if (completo.contains("pkix")
                || completo.contains("certification path")
                || completo.contains("not trusted")
                || completo.contains("certificate")
                || completo.contains("unable to find valid")) {
            motivo = "El certificado SSL del servidor SMTP no es de "
                   + "confianza. Suele deberse a un certificado "
                   + "autofirmado o a un firewall/proxy que "
                   + "intercepta la conexión.";
        }
        // ── SSL/TLS genérico ──
        else if (completo.contains("ssl")
                || completo.contains("tls")
                || completo.contains("handshake")
                || completo.contains("starttls")) {
            motivo = "Error SSL/TLS con el servidor SMTP. Revisa que "
                   + "el puerto use STARTTLS (587) o SSL directo "
                   + "(465) según corresponda.";
        }
        // ── Dirección inválida ──
        else if (completo.contains("invalid address")
                || completo.contains("invalid addresses")) {
            motivo = "Dirección de email no válida.";
        }
        // ── Host desconocido ──
        else if (completo.contains("unknown host")
                || (completo.contains("host")
                    && completo.contains("unknown"))) {
            motivo = "No se puede resolver el nombre del "
                   + "servidor SMTP. Revisa el host.";
        }
        // ── No se pudo conectar ──
        else if (completo.contains("could not connect")
                || completo.contains("connect failed")
                || completo.contains("connection reset")) {
            motivo = "No se puede conectar al servidor SMTP. "
                   + "Revisa host, puerto y red.";
        }

        if (motivo == null)
            motivo = "El servidor no dio más detalles.";

        return new ResultadoConexion(false, motivo, detalle);
    }

    // ── Traducir errores (texto plano, para guardar en la base de datos) ─

    private String traducirError(Exception e) {
        ResultadoConexion r = analizarError(e);
        return "Motivo: " + r.motivo
             + "\n\nDetalle del servidor: " + r.detalle;
    }

    // ── Probar conexión ───────────────────────────────────────────────────

    /**
     * Prueba la conexión SMTP y devuelve un resultado
     * estructurado (ok / motivo / detalle) para la interfaz.
     */
    public ResultadoConexion probarConexionDetallada(Configuracion cfg) {
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
            return new ResultadoConexion(true, null, null);
        } catch (Exception e) {
            return analizarError(e);
        }
    }

    public String probarConexion(Configuracion cfg) {
        ResultadoConexion r = probarConexionDetallada(cfg);
        if (r.ok) return "OK";
        return "Motivo: " + r.motivo
             + "\n\nDetalle del servidor: " + r.detalle;
    }
}
