<div align="center">

# Control de Tardanzas

**Aplicación de escritorio para registrar los retrasos del alumnado y gestionar los avisos a sus familias.**

IES José Ballester Gozalvo

[![Última versión](https://img.shields.io/github/v/release/whj2006/Sistema_control_tardanza?display_name=tag&label=versi%C3%B3n)](https://github.com/whj2006/Sistema_control_tardanza/releases/latest)
[![Windows 10/11](https://img.shields.io/badge/Windows-10%20%7C%2011-0078D4?logo=windows&logoColor=white)](#instalación-en-windows)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](#compilar-desde-el-código)
[![Build de Windows](https://github.com/whj2006/Sistema_control_tardanza/actions/workflows/build-windows.yml/badge.svg)](https://github.com/whj2006/Sistema_control_tardanza/actions/workflows/build-windows.yml)

---

## Descargar para Windows

<a href="https://github.com/whj2006/Sistema_control_tardanza/releases/latest/download/ControlTardanzas-Setup.exe">
  <img alt="Descargar ControlTardanzas-Setup.exe" src="https://img.shields.io/badge/Descargar_instalador-ControlTardanzas--Setup.exe-962626?style=for-the-badge&logo=windows&logoColor=white">
</a>

<br>

[Ver versiones y archivos publicados](https://github.com/whj2006/Sistema_control_tardanza/releases)

</div>

> El botón descarga el instalador de la **última versión publicada**. Si las mejoras de esta rama aún no se han publicado, se descargará la release anterior; al publicar una etiqueta `v*`, GitHub Actions generará y adjuntará el nuevo `.exe` automáticamente.

## Resumen

Control de Tardanzas permite al personal del centro registrar las entradas tardías, consultar historiales, administrar cursos y alumnado, y enviar avisos por correo electrónico. La aplicación incluye un asistente de primer inicio para preparar la base de datos sin tener que importar scripts SQL manualmente.

## Instalación en Windows

1. Descarga **ControlTardanzas-Setup.exe** desde el botón superior y ejecútalo.
2. Completa el asistente de instalación y abre **Control de Tardanzas** desde el acceso directo.
3. En el primer inicio, la aplicación comprueba la conexión a MariaDB/MySQL y prepara la base de datos `control_tardanzas` si hace falta.
4. Si no hay un servidor MariaDB local, el asistente ofrece descargar e instalar MariaDB mediante **winget**. Windows puede solicitar autorización para instalar el servicio.
5. Configura el correo SMTP del centro desde la aplicación si deseas enviar avisos a las familias.

### Qué prepara automáticamente el instalador y la aplicación

- El instalador generado por el flujo actual incluye un runtime de **Java 17**; no es necesario instalar Java por separado.
- Si no detecta MariaDB local, la aplicación puede descargarlo con **Windows Package Manager (winget)** después de pedir confirmación. Se requiere conexión a Internet y, para instalar el servicio, permisos de administrador.
- Crea la base de datos y configura las tablas, vistas y procedimientos almacenados que utiliza la aplicación.
- Comprueba el esquema antes de iniciar. Si faltan objetos, vuelve a crear los que faltan y actualiza las vistas y procedimientos incluidos con la versión.
- Guarda la configuración de conexión para los siguientes inicios.

La preparación del esquema **no borra los registros existentes**. Las tablas se crean solo si no existen; las vistas se reemplazan y los procedimientos se actualizan. Esto no sustituye una migración de columnas personalizada de una base de datos modificada manualmente.

> **¿Ya tienes MariaDB/MySQL?** El asistente puede usarlo. Si requiere usuario o contraseña, te pedirá los datos de conexión. Para crear o completar el esquema, esa cuenta debe tener permisos para crear la base de datos, tablas, vistas e instrucciones almacenadas. También puedes usar una base de datos alojada en otro equipo.

### Requisitos

- Windows 10/11 de 64 bits.
- Conexión a Internet solo para descargar MariaDB mediante winget o para utilizar el correo electrónico.
- Si MariaDB ya está instalado, no se necesita winget. Si winget no está disponible, instala MariaDB manualmente y vuelve a abrir la aplicación.

## Funcionalidades

| Módulo | Descripción |
| --- | --- |
| **Fichaje** | Registra retrasos por NIA o seleccionando curso y alumno; muestra el estado de los avisos. |
| **Historial** | Consulta y filtra los retrasos registrados. |
| **Ranking** | Resume los retrasos y minutos acumulados por alumno. |
| **Cursos y alumnado** | Gestiona cursos, alumnos y correos de contacto familiares. |
| **Importación** | Importa alumnado desde archivos Excel. |
| **Exportación** | Genera informes de retrasos para su consulta o envío. |
| **Correos** | Consulta el estado de los avisos y gestiona los envíos pendientes o fallidos. |
| **Configuración** | Ajusta el horario de entrada y los datos SMTP del centro. |
| **Usuarios y permisos** | Administra cuentas y acceso a los distintos módulos. |
| **Mantenimiento** | Permite reiniciar los datos de retrasos desde la propia aplicación. |

### Perfiles de acceso

La aplicación contempla perfiles de fichaje, fichaje e informes, gestión y administrador. Cada perfil habilita las secciones correspondientes; los permisos se administran desde el módulo de usuarios.

## Base de datos y configuración

El esquema se encuentra en [`Gestion_retraso_sql/`](Gestion_retraso_sql/):

- `tabla.sql`: tablas de usuarios, cursos, alumnado, retrasos, correo y configuración.
- `vistas.sql`: vistas de historial, ranking, resumen de cursos y estado de correos.
- `procedimiento.sql`: procedimientos almacenados utilizados por la aplicación.
- `sql_completo.sql`: script completo para preparar la base de datos manualmente.

Si prefieres configurarla a mano, ejecuta el script completo con una cuenta que pueda crear la base de datos:

```bash
mariadb -u root -p < Gestion_retraso_sql/sql_completo.sql
```

La aplicación guarda las credenciales de conexión en el perfil del usuario (en Windows: `%APPDATA%\ControlTardanzas\config.properties`). Ese archivo contiene datos de acceso: no lo compartas ni lo subas a Git. La desinstalación del programa **no elimina** la base de datos ni los datos del alumnado.

Para realizar una copia de seguridad desde una terminal con MariaDB instalado:

```bash
mariadb-dump -u root -p control_tardanzas > copia-control-tardanzas.sql
```

## Uso del correo electrónico

Los avisos necesitan una cuenta SMTP válida. Después de iniciar la aplicación, configura el servidor, puerto, usuario y remitente en el módulo de configuración y realiza una prueba de envío. El asistente de base de datos no configura ni valida la cuenta de correo.

## Compilar desde el código

### Requisitos de desarrollo

- JDK 17.
- Maven 3.8 o posterior.
- Para generar el instalador de Windows: Windows 10/11 y **Inno Setup 6**.

### Compilar el JAR

```bash
cd control-tardanzas
mvn -B clean verify
```

El JAR ejecutable con dependencias se genera en `control-tardanzas/target/`. Para ejecutarlo directamente se requiere Java 17:

```bash
java -jar target/control-tardanzas-1.1.0-jar-with-dependencies.jar
```

### Generar el instalador EXE en Windows

Con JDK 17, Maven e Inno Setup 6 instalados, ejecuta desde PowerShell en la raíz del proyecto:

```powershell
powershell -ExecutionPolicy Bypass -File .\control-tardanzas\build-windows.ps1
```

El script ejecuta las pruebas, construye la aplicación con un runtime de Java incluido y genera `dist/ControlTardanzas-Setup.exe`.

Para publicar una versión, crea y envía una etiqueta, por ejemplo:

```bash
git tag v1.1.0
git push origin v1.1.0
```

GitHub Actions compila el instalador y lo adjunta a la nueva versión en **Releases**.

## Estructura del proyecto

```text
.
├── README.md
├── Gestion_retraso_sql/           # Esquema SQL para instalación automática o manual
├── instalador/                    # Definición de ControlTardanzas-Setup.exe
├── .github/workflows/             # Compilación del instalador en Windows
└── control-tardanzas/
    ├── pom.xml                    # Dependencias y compilación Maven
    ├── build-windows.ps1           # Empaquetado EXE para Windows
    └── src/main/java/              # Aplicación Java Swing
```

## Solución de problemas

- **No se pudo instalar MariaDB:** confirma que tienes conexión a Internet y winget (Instalador de aplicación de Microsoft). También puedes instalar MariaDB manualmente desde [mariadb.org](https://mariadb.org/download/) y reiniciar la aplicación.
- **Acceso denegado o faltan permisos SQL:** configura un usuario con permisos para crear el esquema, o pide al administrador de la base de datos que lo prepare con `sql_completo.sql`.
- **La aplicación no encuentra el servidor:** comprueba el host, el puerto y que el servicio MariaDB/MySQL esté iniciado. En instalaciones de red, permite el acceso desde el equipo donde se ejecuta la aplicación.
- **Los avisos no llegan:** revisa la configuración SMTP, la dirección de correo de la familia y el estado del mensaje en el módulo de correos.
- **Windows muestra SmartScreen:** los ejecutables no firmados pueden mostrar una advertencia de editor desconocido. Descarga únicamente desde la página oficial de [Releases](https://github.com/whj2006/Sistema_control_tardanza/releases).

## Soporte

Para informar de un error o solicitar una mejora, abre un [Issue](https://github.com/whj2006/Sistema_control_tardanza/issues) e incluye la versión de Windows, la versión del programa y el mensaje de error. No adjuntes el archivo local de configuración ni contraseñas.
