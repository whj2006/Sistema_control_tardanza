-- ============================================================
-- CONTROL DE TARDANZAS - Esquema completo de base de datos
-- MariaDB / MySQL compatible. Re-ejecutable sin borrar datos.
-- ============================================================

CREATE DATABASE IF NOT EXISTS control_tardanzas
CHARACTER SET utf8mb4
COLLATE utf8mb4_spanish_ci;

USE control_tardanzas;

-- ================================================
-- TABLAS
-- ================================================

CREATE TABLE IF NOT EXISTS configuracion_horarios (
    id              INT          PRIMARY KEY DEFAULT 1,
    hora_entrada    VARCHAR(5)   NOT NULL DEFAULT '08:00',
    CHECK (id = 1)  -- Solo una fila
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS usuario (
    id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    rol            ENUM('TODO','FICHAJE','FICHAJE_INFORMES',
                        'FICHAJE_INFORMES_GESTION')
                   NOT NULL DEFAULT 'FICHAJE',
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS destinatario_correo (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS curso (
    id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50)  NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alumno (
    nia            CHAR(8)      PRIMARY KEY,
    nombre         VARCHAR(50)  NOT NULL,
    apellido1      VARCHAR(100) NOT NULL,
    apellido2      VARCHAR(100) NOT NULL,
    email_familia1 VARCHAR(100) NULL DEFAULT NULL,
    email_familia2 VARCHAR(100) NULL DEFAULT NULL,
    id_curso       INT UNSIGNED NOT NULL,

    CONSTRAINT chk_nia_formato
        CHECK (nia REGEXP '^[0-9]{8}$'),

    CONSTRAINT fk_alumno_curso
        FOREIGN KEY (id_curso)
        REFERENCES curso(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS retraso (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nia           CHAR(8)      NOT NULL,
    fecha_hora    DATETIME     NOT NULL,
    minutos_tarde INT UNSIGNED NOT NULL,

    CONSTRAINT fk_retraso_alumno
        FOREIGN KEY (nia)
        REFERENCES alumno(nia)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    KEY idx_retraso_nia_fecha (nia, fecha_hora)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS correo (
    id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_retraso     INT UNSIGNED  NOT NULL,
    email_destino  VARCHAR(100)  NOT NULL,
    asunto         VARCHAR(150)  NOT NULL,
    mensaje        TEXT          NOT NULL,
    fecha_creacion DATETIME      DEFAULT CURRENT_TIMESTAMP,
    fecha_envio    DATETIME      NULL,
    estado         ENUM('PENDIENTE','ENVIADO','ERROR') DEFAULT 'PENDIENTE',
    error_envio    TEXT,

    CONSTRAINT fk_correo_retraso
        FOREIGN KEY (id_retraso)
        REFERENCES retraso(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    KEY idx_correo_estado (estado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS configuracion (
    id               INT UNSIGNED   PRIMARY KEY DEFAULT 1,
    smtp_host        VARCHAR(100)   NOT NULL,
    smtp_puerto      INT UNSIGNED   NOT NULL,
    smtp_usuario     VARCHAR(200)   NOT NULL,
    smtp_password    VARBINARY(255) NOT NULL,
    email_remitente  VARCHAR(200)   NOT NULL,
    nombre_remitente VARCHAR(100)   NOT NULL,
    CONSTRAINT check_single_row CHECK (id = 1)
) ENGINE=InnoDB;

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

-- ================================================
-- PROCEDIMIENTOS (idempotentes)
-- ================================================

DELIMITER ;
DROP PROCEDURE IF EXISTS sp_fichar_retraso_automatico;
DROP PROCEDURE IF EXISTS sp_historial_por_alumno;
DROP PROCEDURE IF EXISTS sp_importar_actualizar_alumno;
DROP PROCEDURE IF EXISTS sp_crear_curso_si_no_existe;
DROP PROCEDURE IF EXISTS sp_editar_alumno;
DROP PROCEDURE IF EXISTS sp_editar_curso;
DROP PROCEDURE IF EXISTS sp_listar_alumnos_curso;
DROP PROCEDURE IF EXISTS sp_ver_estado_correos;
DROP PROCEDURE IF EXISTS sp_guardar_configuracion;
DROP PROCEDURE IF EXISTS sp_obtener_configuracion;
DROP PROCEDURE IF EXISTS sp_autenticar_usuario;
DROP PROCEDURE IF EXISTS sp_contar_usuarios;
DROP PROCEDURE IF EXISTS sp_listar_usuarios;
DROP PROCEDURE IF EXISTS sp_crear_usuario;
DROP PROCEDURE IF EXISTS sp_editar_usuario;
DROP PROCEDURE IF EXISTS sp_cambiar_password;
DROP PROCEDURE IF EXISTS sp_resetear_password;
DROP PROCEDURE IF EXISTS sp_eliminar_usuario;

DELIMITER //

-- ================================================
-- PROCEDIMIENTOS
-- ================================================


-- ── A. Fichar retraso automatico ─────────────────────────────────────────
CREATE PROCEDURE sp_fichar_retraso_automatico(
    IN p_nia CHAR(8)
)
BEGIN
    DECLARE v_hora_entrada  TIME DEFAULT '08:00:00';
    DECLARE v_minutos_tarde INT;
    DECLARE v_id_retraso    INT UNSIGNED;
    DECLARE v_nombre_alumno VARCHAR(255);
    DECLARE v_email1        VARCHAR(100);
    DECLARE v_email2        VARCHAR(100);
    DECLARE v_asunto        VARCHAR(150);
    DECLARE v_mensaje       TEXT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF NOT EXISTS (SELECT 1 FROM alumno WHERE nia = p_nia) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: El NIA no pertenece a ningun alumno registrado.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM retraso
        WHERE nia = p_nia
          AND DATE(fecha_hora) = CURDATE()
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El alumno ya tiene un retraso registrado hoy.';
    END IF;

    SET v_minutos_tarde = TIMESTAMPDIFF(MINUTE, v_hora_entrada, CURTIME());
    IF v_minutos_tarde < 0 THEN
        SET v_minutos_tarde = 0;
    END IF;

    SELECT
        CONCAT(nombre, ' ', apellido1, ' ', apellido2),
        email_familia1,
        email_familia2
    INTO v_nombre_alumno, v_email1, v_email2
    FROM alumno
    WHERE nia = p_nia;

    SET v_asunto  = CONCAT('Avis de retard de l''alumne/a ', v_nombre_alumno);
    SET v_mensaje = CONCAT(
        v_nombre_alumno,                '||',
        DATE_FORMAT(NOW(), '%d/%m/%Y'), '||',
        DATE_FORMAT(NOW(), '%H:%i')
    );

    START TRANSACTION;

        INSERT INTO retraso (nia, fecha_hora, minutos_tarde)
        VALUES (p_nia, NOW(), v_minutos_tarde);

        SET v_id_retraso = LAST_INSERT_ID();

        IF v_email1 IS NOT NULL AND TRIM(v_email1) <> '' THEN
            INSERT INTO correo (id_retraso, email_destino, asunto, mensaje, estado)
            VALUES (v_id_retraso, v_email1, v_asunto, v_mensaje, 'PENDIENTE');
        END IF;

        IF v_email2 IS NOT NULL
           AND TRIM(v_email2) <> ''
           AND (v_email1 IS NULL OR v_email2 <> v_email1) THEN
            INSERT INTO correo (id_retraso, email_destino, asunto, mensaje, estado)
            VALUES (v_id_retraso, v_email2, v_asunto, v_mensaje, 'PENDIENTE');
        END IF;

    COMMIT;
END //

-- ── B. Historial por alumno ───────────────────────────────────────────────
CREATE PROCEDURE sp_historial_por_alumno(
    IN p_nia CHAR(8)
)
BEGIN
    SELECT *
    FROM v_historial_completo
    WHERE nia = p_nia
    ORDER BY fecha_hora DESC;
END //

-- ── C. Importar o actualizar alumno ──────────────────────────────────────
CREATE PROCEDURE sp_importar_actualizar_alumno(
    IN p_nia            CHAR(8),
    IN p_nombre         VARCHAR(50),
    IN p_apellido1      VARCHAR(100),
    IN p_apellido2      VARCHAR(100),
    IN p_email_familia1 VARCHAR(100),
    IN p_email_familia2 VARCHAR(100),
    IN p_id_curso       INT UNSIGNED
)
BEGIN
    INSERT INTO alumno (
        nia, nombre, apellido1, apellido2,
        email_familia1, email_familia2, id_curso
    )
    VALUES (
        p_nia, p_nombre, p_apellido1, p_apellido2,
        p_email_familia1, p_email_familia2, p_id_curso
    )
    ON DUPLICATE KEY UPDATE
        nombre         = p_nombre,
        apellido1      = p_apellido1,
        apellido2      = p_apellido2,
        email_familia1 = p_email_familia1,
        email_familia2 = p_email_familia2,
        id_curso       = p_id_curso;
END //

-- ── D. Crear curso si no existe ───────────────────────────────────────────
CREATE PROCEDURE sp_crear_curso_si_no_existe(
    IN p_nombre_curso VARCHAR(50)
)
BEGIN
    INSERT IGNORE INTO curso (nombre) VALUES (p_nombre_curso);
END //

-- ── E. Editar alumno ──────────────────────────────────────────────────────
CREATE PROCEDURE sp_editar_alumno(
    IN p_nia             CHAR(8),
    IN p_nuevo_nombre    VARCHAR(50),
    IN p_nuevo_apellido1 VARCHAR(100),
    IN p_nuevo_apellido2 VARCHAR(100),
    IN p_nuevo_email1    VARCHAR(100),
    IN p_nuevo_email2    VARCHAR(100),
    IN p_nuevo_id_curso  INT UNSIGNED
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM alumno WHERE nia = p_nia) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: El NIA no existe.';
    END IF;

    UPDATE alumno
    SET nombre         = p_nuevo_nombre,
        apellido1      = p_nuevo_apellido1,
        apellido2      = p_nuevo_apellido2,
        email_familia1 = p_nuevo_email1,
        email_familia2 = p_nuevo_email2,
        id_curso       = p_nuevo_id_curso
    WHERE nia = p_nia;
END //

-- ── F. Editar curso ───────────────────────────────────────────────────────
CREATE PROCEDURE sp_editar_curso(
    IN p_id_curso     INT UNSIGNED,
    IN p_nuevo_nombre VARCHAR(50)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM curso WHERE id = p_id_curso) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: El curso no existe.';
    END IF;

    UPDATE curso
    SET nombre = p_nuevo_nombre
    WHERE id = p_id_curso;
END //

-- ── G. Listar alumnos de un curso ─────────────────────────────────────────
CREATE PROCEDURE sp_listar_alumnos_curso(
    IN p_id_curso INT UNSIGNED
)
BEGIN
    SELECT
        nia,
        nombre,
        apellido1,
        apellido2,
        email_familia1,
        email_familia2
    FROM alumno
    WHERE id_curso = p_id_curso
    ORDER BY apellido1 ASC, apellido2 ASC, nombre ASC;
END //

-- ── H. Ver estado correos ─────────────────────────────────────────────────
CREATE PROCEDURE sp_ver_estado_correos(
    IN p_estado VARCHAR(20)
)
BEGIN
    IF p_estado NOT IN ('TODOS', 'PENDIENTE', 'ENVIADO', 'ERROR') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Estado no valido. Use: TODOS, PENDIENTE, ENVIADO o ERROR';
    END IF;

    IF p_estado = 'TODOS' THEN
        SELECT * FROM v_estado_correos
        ORDER BY fecha_creacion DESC;
    ELSE
        SELECT * FROM v_estado_correos
        WHERE estado = p_estado
        ORDER BY fecha_creacion DESC;
    END IF;
END //

-- ── I. Guardar configuracion SMTP ─────────────────────────────────────────
CREATE PROCEDURE sp_guardar_configuracion(
    IN p_smtp_host        VARCHAR(100),
    IN p_smtp_puerto      INT UNSIGNED,
    IN p_smtp_usuario     VARCHAR(200),
    IN p_smtp_password    VARBINARY(255),
    IN p_email_remitente  VARCHAR(200),
    IN p_nombre_remitente VARCHAR(100)
)
BEGIN
    INSERT INTO configuracion (
        id, smtp_host, smtp_puerto, smtp_usuario,
        smtp_password, email_remitente, nombre_remitente
    )
    VALUES (
        1, p_smtp_host, p_smtp_puerto, p_smtp_usuario,
        p_smtp_password, p_email_remitente, p_nombre_remitente
    )
    ON DUPLICATE KEY UPDATE
        smtp_host        = p_smtp_host,
        smtp_puerto      = p_smtp_puerto,
        smtp_usuario     = p_smtp_usuario,
        smtp_password    = p_smtp_password,
        email_remitente  = p_email_remitente,
        nombre_remitente = p_nombre_remitente;
END //

-- ── J. Obtener configuracion ──────────────────────────────────────────────
CREATE PROCEDURE sp_obtener_configuracion()
BEGIN
    SELECT
        id,
        smtp_host,
        smtp_puerto,
        smtp_usuario,
        smtp_password,
        email_remitente,
        nombre_remitente
    FROM configuracion
    WHERE id = 1;
END //

-- ── K. Autenticar usuario ─────────────────────────────────────────────────
CREATE PROCEDURE sp_autenticar_usuario(
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(255)
)
BEGIN
    SELECT id, username, rol
    FROM usuario
    WHERE username = p_username
      AND password = p_password
      AND activo   = 1;
END //

-- ── L. Contar usuarios ────────────────────────────────────────────────────
CREATE PROCEDURE sp_contar_usuarios()
BEGIN
    SELECT COUNT(*) AS total FROM usuario;
END //

-- ── M. Listar usuarios ────────────────────────────────────────────────────
CREATE PROCEDURE sp_listar_usuarios()
BEGIN
    SELECT id, username, rol, activo, fecha_creacion
    FROM usuario
    ORDER BY rol ASC, username ASC;
END //

-- ── N. Crear usuario ──────────────────────────────────────────────────────
CREATE PROCEDURE sp_crear_usuario(
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(255),
    IN p_rol      VARCHAR(30)
)
BEGIN
    IF EXISTS (SELECT 1 FROM usuario WHERE username = p_username) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El nombre de usuario ya existe.';
    END IF;

    INSERT INTO usuario (username, password, rol)
    VALUES (p_username, p_password, p_rol);
END //

-- ── O. Editar usuario ─────────────────────────────────────────────────────
CREATE PROCEDURE sp_editar_usuario(
    IN p_id       INT UNSIGNED,
    IN p_username VARCHAR(50),
    IN p_rol      VARCHAR(30),
    IN p_activo   TINYINT(1)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id = p_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El usuario no existe.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM usuario
        WHERE username = p_username AND id <> p_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El nombre de usuario ya esta en uso.';
    END IF;

    UPDATE usuario
    SET username = p_username,
        rol      = p_rol,
        activo   = p_activo
    WHERE id = p_id;
END //

-- ── P. Cambiar contrasena ─────────────────────────────────────────────────
CREATE PROCEDURE sp_cambiar_password(
    IN p_id              INT UNSIGNED,
    IN p_password_actual VARCHAR(255),
    IN p_password_nueva  VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM usuario
        WHERE id = p_id AND password = p_password_actual
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La contrasena actual no es correcta.';
    END IF;

    UPDATE usuario
    SET password = p_password_nueva
    WHERE id = p_id;
END //

-- ── Q. Resetear contrasena ────────────────────────────────────────────────
CREATE PROCEDURE sp_resetear_password(
    IN p_id             INT UNSIGNED,
    IN p_password_nueva VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id = p_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El usuario no existe.';
    END IF;

    UPDATE usuario
    SET password = p_password_nueva
    WHERE id = p_id;
END //

-- ── R. Eliminar usuario ───────────────────────────────────────────────────
CREATE PROCEDURE sp_eliminar_usuario(
    IN p_id INT UNSIGNED
)
BEGIN
    DECLARE v_rol   VARCHAR(30);
    DECLARE v_todos INT;

    SELECT rol INTO v_rol FROM usuario WHERE id = p_id;

    IF v_rol IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El usuario no existe.';
    END IF;

    IF v_rol = 'TODO' THEN
        SELECT COUNT(*) INTO v_todos
        FROM usuario
        WHERE rol    = 'TODO'
          AND activo = 1
          AND id    <> p_id;

        IF v_todos < 1 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No se puede eliminar el ultimo usuario con acceso total.';
        END IF;
    END IF;

    DELETE FROM usuario WHERE id = p_id;
END //

DELIMITER ;
