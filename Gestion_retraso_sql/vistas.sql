-- ================================================
-- VISTAS
-- ================================================

CREATE OR REPLACE VIEW v_historial_completo AS
SELECT
    r.id   AS id_retraso,
    r.fecha_hora,
    a.nia,
    CONCAT(a.nombre, ' ', a.apellido1, ' ', a.apellido2) AS nombre_completo,
    c.nombre AS curso,
    r.minutos_tarde
FROM retraso r
JOIN alumno a ON r.nia      = a.nia
JOIN curso  c ON a.id_curso = c.id;

CREATE OR REPLACE VIEW v_historial_hoy AS
SELECT *
FROM v_historial_completo
WHERE DATE(fecha_hora) = CURDATE();

CREATE OR REPLACE VIEW v_ranking_tardanzas AS
SELECT
    a.nia,
    CONCAT(a.nombre, ' ', a.apellido1, ' ', a.apellido2) AS alumno,
    c.nombre AS curso,
    COUNT(r.id)                       AS total_retrasos,
    COALESCE(SUM(r.minutos_tarde), 0) AS total_minutos_perdidos
FROM alumno a
LEFT JOIN retraso r ON a.nia      = r.nia
JOIN      curso   c ON a.id_curso = c.id
GROUP BY a.nia, a.nombre, a.apellido1, a.apellido2, c.nombre;

CREATE OR REPLACE VIEW v_resumen_cursos AS
SELECT
    c.id,
    c.nombre,
    COUNT(a.nia) AS total_alumnos
FROM curso c
LEFT JOIN alumno a ON c.id = a.id_curso
GROUP BY c.id, c.nombre
ORDER BY c.nombre ASC;

CREATE OR REPLACE VIEW v_estado_correos AS
SELECT
    c.id            AS id_correo,
    c.estado,
    a.nia,
    CONCAT(a.nombre, ' ', a.apellido1, ' ', a.apellido2) AS nombre_alumno,
    c.email_destino AS email_familia,
    r.fecha_hora    AS fecha_retraso,
    r.minutos_tarde,
    c.asunto,
    c.fecha_creacion,
    c.fecha_envio,
    c.error_envio
FROM correo  c
JOIN retraso r ON c.id_retraso = r.id
JOIN alumno  a ON r.nia        = a.nia;