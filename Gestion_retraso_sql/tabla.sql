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
