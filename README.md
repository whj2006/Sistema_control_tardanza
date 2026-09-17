# Sistema de Control de Tardanzas — IES José Ballester Gozalvo

Aplicación de escritorio en **Java Swing** para registrar, gestionar y notificar los retrasos de alumnos en un centro educativo. Permite fichar tardanzas por NIA, consultar historiales, generar rankings, gestionar cursos/alumnos, importar desde Excel, exportar informes y enviar correos automáticos a familias.

> **Centro:** IES José Ballester Gozalvo  
> **Stack:** Java 17 · Maven · Swing · MariaDB · HikariCP · Jakarta Mail · Apache POI

## ⬇️ Descarga Directa — 1 Clic (Portable, sin instalador, sin admin)

> **Última versión: v1.0.0 — Portable, no pide contraseña de administrador, no modifica el sistema**

<p align="center">

<!-- BOTONES DE DESCARGA DIRECTA - Al hacer clic descarga directamente el archivo -->
<a href="https://github.com/whj2006/Sistema_control_tardanza/releases/latest/download/CONTROL-RETRASO.exe">
<img src="https://img.shields.io/badge/EXE%20Windows%20Portable-DESCARGA%20DIRECTA-0078D6?style=for-the-badge&logo=windows&logoColor=white" alt="Descargar EXE Directo">
</a>
<a href="https://github.com/whj2006/Sistema_control_tardanza/releases/latest/download/control-tardanzas-1.0.0-jar-with-dependencies.jar">
<img src="https://img.shields.io/badge/JAR%20Ejecutable-DESCARGA%20DIRECTA-2ea44f?style=for-the-badge&logo=java&logoColor=white" alt="Descargar JAR Directo">
</a>

<br>

<a href="https://github.com/whj2006/Sistema_control_tardanza/releases">
<img src="https://img.shields.io/badge/Ver%20todas%20las%20versiones-Releases-black?style=for-the-badge&logo=github" alt="Releases">
</a>
<a href="./Manual_ControlRetraso.docx.pdf">
<img src="https://img.shields.io/badge/Manual-PDF-red?style=for-the-badge&logo=adobeacrobatreader&logoColor=white" alt="Manual">
</a>

</p>

<p align="center">
  <img src="https://img.shields.io/badge/Portable-Sin%20instalador%20sin%20admin-brightgreen?style=flat-square" alt="Portable">
  <img src="https://img.shields.io/badge/Requiere-Java%2017+-orange?style=flat-square" alt="Java 17">
  <img src="https://img.shields.io/badge/Plataforma-Windows%20%7C%20Linux%20%7C%20Mac-lightgrey?style=flat-square" alt="Plataforma">
  <img src="https://img.shields.io/badge/UAC-No%20pide%20contraseña-blue?style=flat-square" alt="No UAC">
</p>

### 🚀 Ejecución en 10 segundos (sin instalar nada)

```bash
# 1. Descarga el EXE (botón azul de arriba)
# 2. Doble clic en CONTROL-RETRASO.exe
# ¡Listo! No pide admin, no instala, no modifica el sistema
```

> **¿Por qué no pide contraseña de administrador?**
> - El `CONTROL-RETRASO.exe` es **portable**: es solo un wrapper del JAR con Launch4j, tipo `gui`, sin manifiesto de admin
> - No escribe en `C:\Program Files`, solo crea `config.properties` **junto al EXE** (en Descargas, Escritorio, etc.)
> - No toca registro de Windows ni carpetas del sistema
> - El instalador opcional (`installer.iss`) también está configurado con `PrivilegesRequired=lowest` → se instala en `%LOCALAPPDATA%\Programs` sin UAC

> **¿No tienes Java?** Instala [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17) (sin admin, opción portable disponible) y luego ejecuta el EXE.

### 📥 Si los botones dan 404 (aún no hay Release)

Los botones de descarga directa funcionan cuando hay un Release publicado con assets. Si aún no has publicado uno:

**Opción 1 - GitHub Actions (artefactos temporales):**
1. Ve a [Actions → último build verde](https://github.com/whj2006/Sistema_control_tardanza/actions/workflows/build.yml)
2. Baja hasta Artifacts → descarga `exe-windows` y `jar-ejecutable`

**Opción 2 - Compilar tú mismo (2 comandos):**
```bash
cd control-tardanzas
mvn clean package
# Genera:
# target/CONTROL-RETRASO.exe (portable, sin admin)
# target/control-tardanzas-1.0.0-jar-with-dependencies.jar
```

> **Nota:** `*.jar`, `*.exe` y `target/` están en `.gitignore` y no se suben al repo. Usa `mvn package` o Releases.

---

## 🖥️ Opción .EXE — Detalles Técnicos (Portable, sin admin)

> **Sí, el proyecto Maven genera un `.exe` nativo portable que NO pide contraseña de administrador.**

<p align="center">
<a href="https://github.com/whj2006/Sistema_control_tardanza/releases/latest/download/CONTROL-RETRASO.exe">
<img src="https://img.shields.io/badge/EXE%20Portable-DESCARGA%20DIRECTA%20SIN%20ADMIN-00A4EF?style=for-the-badge&logo=windows&logoColor=white" alt="EXE Portable">
</a>
</p>

### ¿Qué es el .EXE?

- **Wrapper del JAR con Launch4j**: `CONTROL-RETRASO.exe` (~25MB)
- **Portable 100%**: no instala, no pide admin/UAC, no escribe en Program Files ni registro
- **Sin consola**: tipo `gui`, doble clic y abre la app
- **Con icono** del instituto (`icon.ico`)
- **Comprueba Java 17+**: si no tienes Java, muestra mensaje con link a Adoptium (sin UAC)
- **Mismo JAR por dentro**: toda la lógica de tardanzas, correos, BD, etc.

### 3 formas de obtener el .EXE (todas sin admin)

#### 1️⃣ Descarga directa (1 clic, sin admin)
```
1. Clic en botón azul de arriba → descarga CONTROL-RETRASO.exe
2. Guárdalo en Escritorio o Descargas (no en Program Files para evitar admin)
3. Doble clic → si es primera vez pide datos de BD y crea config.properties junto al EXE
4. ¡No pide contraseña de Windows!
```

#### 2️⃣ Compilar en tu PC (sin admin)
```bat
cd control-tardanzas
mvn clean package
:: Genera target/CONTROL-RETRASO.exe (portable, sin admin)
```
Script rápido:
```bat
generar-exe.bat  :: desde raíz, doble clic
```

#### 3️⃣ GitHub Actions (sin compilar local, sin admin)
Cada push genera el EXE en `windows-latest` sin necesidad de admin. Descárgalo de Actions → Artifacts.

### ⚙️ ¿Se puede configurar la base de datos en el .EXE? ¡Sí!

**El .EXE se configura exactamente igual que el JAR:**

1. **Primera ejecución:**
   - Busca `config.properties` en carpeta del EXE + working dir
   - Si no existe, abre `VentanaConfigDB` para pedir Host, Puerto, BD, Usuario, Pass
   - Prueba conexión y guarda `config.properties` junto al EXE (sin admin si EXE está en Escritorio/Descargas)

2. **SMTP y horarios:** Desde la UI, paneles `Config. SMTP` y `Ajustes` → guardan en BD (`configuracion`, `configuracion_horarios`)

3. **¿Dónde queda config.properties?**
   ```
   Escritorio\CONTROL-RETRASO.exe
   Escritorio\config.properties  ← se crea aquí, sin admin
   ```

> **Importante para evitar UAC:** No pongas el EXE en `C:\Program Files`, ponlo en Escritorio, Descargas o Documentos. Así no necesita permisos de administrador para crear `config.properties`.

### 🛡️ Comparativa: Portable EXE vs Instalador

| Característica | `CONTROL-RETRASO.exe` (Portable) | `ControlRetraso-Setup-*.exe` (Instalador) |
|---|---|---|
| **Pide admin/UAC** | ❌ No | ❌ No (ahora con `PrivilegesRequired=lowest`) |
| **Instala en sistema** | ❌ No, solo 1 archivo | ✅ Sí, en `%LOCALAPPDATA%\Programs` |
| **Modifica registro** | ❌ No | ✅ Solo desinstalador |
| **Ubicación** | Donde lo descargues | `%LOCALAPPDATA%\Programs\Control de Retraso` |
| **config.properties** | Junto al EXE | Junto al EXE en AppData |
| **Recomendado para** | Profesores, uso rápido | Instalación permanente sin admin |

> **Si no quieres que cambie nada en el ordenador, usa el EXE portable.**

---

## 📋 Índice

- [Características](#-características)
- [Arquitectura y Tecnologías](#-arquitectura-y-tecnologías)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Base de Datos](#-base-de-datos)
- [Instalación](#-instalación)
- [Ejecución](#-ejecución)
- [Guía de Uso](#-guía-de-uso)
- [Roles y Permisos](#-roles-y-permisos)
- [Importación / Exportación](#-importación--exportación)
- [Sistema de Correos](#-sistema-de-correos)
- [Configuración](#-configuración)
- [Compilación — JAR y EXE](#-compilación--jar-y-exe)

---

## ✨ Características

### Fichaje
- **Fichaje por NIA:** entrada directa del NIA (8 dígitos).
- **Fichaje por curso/alumno:** selección en cascada de curso → alumno.
- Cálculo automático de minutos de retraso respecto a hora de entrada configurable (por defecto `08:00`).
- Validación: no permite más de un retraso por alumno y día.
- Historial del día visible en el mismo panel con contador en tiempo real.
- Banner visual de confirmación/error.

### Informes
- **Historial completo:** vista `v_historial_completo` con filtros, búsqueda y paginación.
- **Ranking de tardanzas:** alumnos con más retrasos, total de minutos perdidos, filtrado por curso. Vista `v_ranking_tardanzas`.
- **Exportar:** generación de Excel (.xlsx) por rango de fechas, curso o alumno; envío opcional por correo con adjunto.

### Gestión
- **Cursos y Alumnos:** CRUD completo, edición de emails de familia (2 por alumno), cambio de curso, eliminación.
- **Importar:** importación masiva desde Excel (.xlsx) con columnas NIA, nombre, apellidos, emails, curso. Usa `sp_importar_actualizar_alumno` con `ON DUPLICATE KEY UPDATE`.
- **Correos:** panel de estado de correos (`PENDIENTE`, `ENVIADO`, `ERROR`), reintentos manuales y automáticos, detalle de error.

### Sistema
- **Configuración SMTP:** guardado en tabla `configuracion` (host, puerto, usuario, password en `VARBINARY`, remitente). Procedimientos `sp_guardar_configuracion` / `sp_obtener_configuracion`. Test de conexión.
- **Ajustes de Horarios:** tabla `configuracion_horarios` (singleton id=1) para definir hora de entrada del centro.
- **Usuarios y Roles:** 4 niveles de acceso, autenticación, bloqueo de último admin, gestión desde panel.
- **Resetear Datos:** borrado selectivo (retrasos, alumnos, cursos, correos) con confirmación.
- **Configuración DB dinámica:** al iniciar, si no existe `config.properties` o falla la conexión, se muestra `VentanaConfigDB` para pedir host/puerto/nombre/user/pass y probar conexión antes de iniciar HikariCP.

---

## 🏗️ Arquitectura y Tecnologías

**Lenguaje:** Java 17

**Build:** Maven 3.x
- `maven-assembly-plugin` → `jar-with-dependencies` (fat jar ejecutable)

**Dependencias clave (`pom.xml`):**

| Librería | Versión | Uso |
|---|---|---|
| `mariadb-java-client` | 3.3.2 | Driver JDBC MariaDB |
| `HikariCP` | 5.1.0 | Pool de conexiones |
| `jakarta.mail` | 2.0.1 | Envío SMTP |
| `poi-ooxml` | 5.2.5 | Excel import/export |
| `LGoodDatePicker` | 11.2.1 | Date picker en Swing |
| `slf4j-api` + `slf4j-simple` | 2.0.9 | Logging |

**Patrón:**
```
Main.java → ConfigDB (carga config.properties) → DatabaseConfig (HikariCP)
       → EmailService (scheduler) → MainFrame (CardLayout)
       → Panels (Fichar, Historial, Ranking, etc.)
       → DAOs → Stored Procedures / SQL → MariaDB
```

- `model`: POJOs (Alumno, Curso, Retraso, Correo, Usuario, Configuracion, etc.)
- `dao`: acceso a datos via `CallableStatement` a procedimientos almacenados
- `service`: `EmailService` (hilo `email-reintentos` cada 3 min + envío inmediato) y `ExcelImportService`
- `config`: `ConfigDB` (lectura/escritura `config.properties`) y `DatabaseConfig` (Hikari)
- `ui`: `MainFrame` con sidebar + barra superior + `CardLayout`, y 11 panels
- `ui.util.UIUtils`: helpers de estilo (colores corporativos granate `#961E1E`, botones, campos)

---

## 📁 Estructura del Proyecto

```
Sistema_control_tardanza/
├── .github/workflows/build.yml      # CI: genera JAR y EXE en Windows
├── .gitignore                       # Ignora target/, *.jar, *.exe, config.properties
├── Manual_ControlRetraso.docx.pdf   # Manual de usuario
├── README.md                        # Este archivo
├── Gestion_retraso_sql/
│   ├── tabla.sql                    # Solo CREATE TABLE
│   ├── vistas.sql                   # Solo CREATE VIEW
│   ├── procedimiento.sql            # Solo CREATE PROCEDURE
│   └── sql_completo.sql             # Todo en uno (tablas + vistas + SP)
└── control-tardanzas/
    ├── pom.xml                      # Maven + Launch4j (EXE) + Antrun + jpackage profile
    ├── build-exe.bat                # Build JAR+EXE en Windows (doble clic)
    ├── build-exe.sh                 # Build en Linux/Mac
    ├── installer.iss                # Script Inno Setup para instalador
    └── src/main/
        ├── java/com/instituto/tardanzas/
        │   ├── Main.java
        │   ├── config/
        │   │   ├── ConfigDB.java            # Lee/escribe config.properties
        │   │   └── DatabaseConfig.java      # HikariCP pool
        │   ├── dao/
        │   │   ├── AlumnoDAO, CursoDAO, RetrasoDAO, CorreoDAO
        │   │   ├── ConfiguracionDAO, ConfiguracionHorariosDAO
        │   │   ├── UsuarioDAO, DestinatarioDAO, ResetDAO
        │   ├── model/
        │   │   ├── Alumno, Curso, Retraso, Correo, Usuario
        │   │   ├── Configuracion, ConfiguracionHorarios, RankingEntry
        │   ├── service/
        │   │   ├── EmailService.java
        │   │   └── ExcelImportService.java
        │   └── ui/
        │       ├── MainFrame.java
        │       ├── VentanaConfigDB.java
        │       ├── panels/
        │       │   ├── FicharPanel, HistorialPanel, RankingPanel
        │       │   ├── CursosAlumnosPanel, CorreosPanel
        │       │   ├── ImportarPanel, ExportarPanel
        │       │   ├── ConfiguracionPanel, AjustesPanel
        │       │   ├── UsuariosPanel, ResetearDatosPanel
        │       └── util/UIUtils.java
        └── resources/
            ├── images/
            │   ├── logo1.png, logo2.png, logo3.png
            │   └── icon.ico         # Icono para EXE (generado desde logo1.png)
            └── simplelogger.properties
```

---

## 🗄️ Base de Datos

**Motor:** MariaDB / MySQL compatible  
**Charset:** `utf8mb4` / `utf8mb4_spanish_ci`  
**Nombre BD:** `control_tardanzas`

### Tablas principales

- `configuracion_horarios` (id=1, hora_entrada VARCHAR(5) default '08:00')
- `usuario` (id, username UNIQUE, password, rol ENUM, activo, fecha_creacion)
- `curso` (id, nombre UNIQUE)
- `alumno` (nia CHAR(8) PK, nombre, apellido1, apellido2, email_familia1, email_familia2, id_curso FK)
- `retraso` (id, nia FK, fecha_hora DATETIME, minutos_tarde) + índice (nia, fecha_hora)
- `correo` (id, id_retraso FK, email_destino, asunto, mensaje TEXT, fecha_creacion, fecha_envio, estado ENUM PENDIENTE/ENVIADO/ERROR, error_envio)
- `configuracion` (id=1, smtp_host, smtp_puerto, smtp_usuario, smtp_password VARBINARY, email_remitente, nombre_remitente)
- `destinatario_correo` (id, correo UNIQUE) – para exportación

### Vistas

- `v_historial_completo`: join retraso + alumno + curso
- `v_historial_hoy`: WHERE DATE(fecha_hora)=CURDATE()
- `v_ranking_tardanzas`: COUNT retrasos y SUM minutos por alumno
- `v_resumen_cursos`: total alumnos por curso
- `v_estado_correos`: join correo + retraso + alumno para seguimiento

### Procedimientos Almacenados

| SP | Función |
|---|---|
| `sp_fichar_retraso_automatico(p_nia)` | Registra retraso, calcula minutos, crea correos PENDIENTE en transacción |
| `sp_historial_por_alumno` | Historial filtrado por NIA |
| `sp_importar_actualizar_alumno` | UPSERT alumno |
| `sp_crear_curso_si_no_existe` | INSERT IGNORE curso |
| `sp_editar_alumno` / `sp_editar_curso` | Update con validación existencia |
| `sp_listar_alumnos_curso` | Listado ordenado por apellidos |
| `sp_ver_estado_correos` | Filtro por estado o TODOS |
| `sp_guardar_configuracion` / `sp_obtener_configuracion` | Singleton SMTP |
| `sp_autenticar_usuario`, `sp_contar_usuarios`, `sp_listar_usuarios`, `sp_crear_usuario`, `sp_editar_usuario`, `sp_cambiar_password`, `sp_resetear_password`, `sp_eliminar_usuario` | Gestión de usuarios |

> Script completo listo para importar: `Gestion_retraso_sql/sql_completo.sql`

---

## ⚙️ Instalación

### Requisitos
- Java 17+
- Maven 3.8+
- MariaDB 10.4+ o MySQL 8+

### 1. Clonar
```bash
git clone https://github.com/whj2006/Sistema_control_tardanza.git
cd Sistema_control_tardanza
```

### 2. Crear BD
```bash
# Opción rápida (todo en uno)
mysql -u root -p < Gestion_retraso_sql/sql_completo.sql

# O paso a paso
mysql -u root -p < Gestion_retraso_sql/tabla.sql
mysql -u root -p < Gestion_retraso_sql/vistas.sql
mysql -u root -p < Gestion_retraso_sql/procedimiento.sql
```

### 3. Configurar `config.properties` (opcional)
Si no existe, la app lo pedirá gráficamente al iniciar. Formato manual:

```properties
db.host=localhost
db.puerto=3306
db.nombre=control_tardanzas
db.user=root
db.password=
db.url=jdbc\:mariadb\://localhost\:3306/control_tardanzas?useUnicode\=true&characterEncoding\=UTF-8&serverTimezone\=Europe/Madrid
```

### 4. Compilar
```bash
cd control-tardanzas
mvn clean package
# genera target/control-tardanzas-1.0.0.jar y target/control-tardanzas-1.0.0-jar-with-dependencies.jar
```

---

## ▶️ Ejecución

### Desde JAR pre-compilado (raíz)
```bash
java -jar "CONTROL RETRASO.jar"
# o
java -jar control-tardanzas/target/control-tardanzas-1.0.0-jar-with-dependencies.jar
```

### Desde IDE (IntelliJ/Eclipse)
- Importar como proyecto Maven (`control-tardanzas/pom.xml`)
- Main class: `com.instituto.tardanzas.Main`

Al arrancar:
1. Lee `config.properties`
2. Si falla → `VentanaConfigDB` pide datos y prueba con `DriverManager`
3. Guarda config y crea pool HikariCP
4. Inicia `EmailService` (reintentos cada 3 min)
5. Abre `MainFrame`

---

## 📖 Guía de Uso

1. **Fichar Retraso:** introduce NIA o selecciona curso/alumno → Enter / botón Fichar. El sistema calcula minutos tarde y encola correos.
2. **Historial:** busca por NIA, nombre, curso, fecha. Exporta a Excel.
3. **Ranking:** ordena por total retrasos o minutos, filtra por curso.
4. **Cursos y Alumnos:** alta/edición/baja, cambio masivo de curso.
5. **Correos:** monitoriza PENDIENTE/ENVIADO/ERROR, reenvía manualmente, ve mensaje de error traducido.
6. **Importar:** selecciona Excel con cabeceras `NIA, Nombre, Apellido1, Apellido2, Email1, Email2, Curso`. El servicio crea cursos si no existen.
7. **Exportar:** elige rango fechas + curso + formato; opción de enviar a destinatario con adjunto HTML.
8. **Config. SMTP:** guarda host/puerto/usuario/pass. Botón Probar conexión usa `Transport.connect`.
9. **Ajustes:** modifica hora de entrada del centro (afecta cálculo minutos).
10. **Usuarios / Reset:** solo accesible con rol TODO.

---

## 🔐 Roles y Permisos

| Rol | Clave BD | Acceso |
|---|---|---|
| **Todo** | `TODO` | Todos los paneles + gestión usuarios + reset |
| **Fichaje** | `FICHAJE` | Solo Fichar |
| **Fichaje + Informes** | `FICHAJE_INFORMES` | Fichar, Historial, Ranking, Exportar |
| **Fichaje + Informes + Gestión** | `FICHAJE_INFORMES_GESTION` | Todo lo anterior + Cursos/Alumnos, Correos, Importar, Exportar |

- Si `COUNT(usuario)=0` → sin autenticación (modo inicial).
- Al crear primer usuario TODO, se activa login obligatorio para paneles protegidos.
- No se puede eliminar el último usuario TODO activo.

---

## 📤 Importación / Exportación

**Importación Excel** (`ExcelImportService`):
- Usa Apache POI, soporta `.xlsx`
- Columnas esperadas (case-insensitive): NIA (8 dígitos), nombre, apellido1, apellido2, email_familia1, email_familia2, curso
- Lógica: `sp_crear_curso_si_no_existe` → `sp_importar_actualizar_alumno`
- Devuelve `ResultadoImportacion` con contadores de insertados/actualizados/errores

**Exportación:**
- `ExportarPanel` genera Excel con historial filtrado
- Plantilla HTML para correo con adjunto (ver `EmailService.enviarExcelAdjunto`)

---

## ✉️ Sistema de Correos

- Al fichar, `sp_fichar_retraso_automatico` inserta en `correo` 1-2 filas (email1/email2) con estado PENDIENTE y mensaje codificado `nombre||fecha||hora`.
- `EmailService`:
  - `iniciar()` → `ScheduledExecutor` daemon cada 180s + primer intento a 30s
  - `enviarInmediatamente()` → hilo aparte tras cada fichaje
  - `crearSesion()` → STARTTLS, timeout 30s, TLSv1.2
  - `construirHtml()` → plantilla bilingüe (valenciano/castellano) con estilo IES
  - `traducirError()` → mensajes amigables (auth, timeout, SSL, invalid address)
  - `probarConexion()` → test SMTP
- Estados: PENDIENTE → ENVIADO (set fecha_envio) / ERROR (set error_envio)

---

## 🔧 Configuración

- **DB:** `config.properties` en working dir + `ConfigDB.java` + `DatabaseConfig.java` (Hikari 10 max, 2 min idle)
- **SMTP:** tabla `configuracion` + `ConfiguracionDAO` + `ConfiguracionPanel`
- **Horarios:** tabla `configuracion_horarios` + `ConfiguracionHorariosDAO` + `AjustesPanel`
- **Logs:** `simplelogger.properties` (SLF4J Simple)

---

## 📦 Compilación — JAR y EXE (Portable, sin admin)

### Opción 1: JAR ejecutable (multiplataforma, sin admin)
```bash
cd control-tardanzas
mvn clean package -DskipTests
java -jar target/control-tardanzas-1.0.0-jar-with-dependencies.jar
# No pide admin, crea config.properties junto al JAR
```

### Opción 2: EXE Windows Portable con Launch4j (recomendado, sin admin, sin UAC)

**Ya está configurado en `pom.xml` con `launch4j-maven-plugin` 2.5.2 (build verificado en CI).**

**Requisitos en Windows:**
- JDK 17+ instalado (sin necesidad de admin si usas portable de Adoptium)
- Maven 3.8+ en PATH
- `src/main/resources/images/icon.ico` (ya incluido)

**Compilar (sin admin):**

```bat
REM Doble clic:
generar-exe.bat
:: o
cd control-tardanzas
build-exe.bat

REM Manual:
mvn clean package
:: Genera target/CONTROL-RETRASO.exe (portable, 25MB, sin admin, sin UAC)
```

**Qué hace Launch4j (config final que pasa CI):**
- Empaqueta fat jar en `CONTROL-RETRASO.exe` con icono y versión
- `headerType=gui` (no abre consola negra)
- `downloadUrl=https://adoptium.net/` (si no hay Java, abre web)
- `jre minVersion=17, preferJre, heap 128-1024MB`
- `versionInfo` con fileVersion, productName, etc. (sin <messages> que rompía el build)
- **Sin <preCp> y sin <messages>**: esos dos campos causaban fallo en plugin 2.5.2
- **Sin manifiesto de admin**: NO pide UAC/contraseña

> **Portable = No pide admin:** Guarda `config.properties` junto al EXE. Pon el EXE en Escritorio/Descargas, no en Program Files.

### Opción 3: Instalador nativo con JRE embebido (jpackage, sin admin)

Genera instalador `.exe` que incluye Java, no necesita Java previo. Ahora sin UAC:

```bash
# Solo Windows con JDK 17:
mvn clean package -Pinstaller
# Genera target/dist/ControlRetraso-1.0.0.exe (con JRE embebido, sin admin)
```

### Opción 4: Instalador Inno Setup (ahora sin admin)

Actualizado para **NO pedir contraseña de administrador**:

```ini
PrivilegesRequired=lowest
DefaultDirName={localappdata}\Programs\Control de Retraso
```

1. `mvn package` (genera EXE portable)
2. Instala Inno Setup 6
3. Abre `installer.iss` → Compila
4. Obtienes `target/installer/ControlRetraso-Setup-1.0.0-Portable.exe` (instala en %LOCALAPPDATA% sin UAC)

**Scripts incluidos:**
- `generar-exe.bat` (raíz) → genera EXE portable sin admin
- `control-tardanzas/build-exe.bat` / `build-exe.sh`
- `control-tardanzas/installer.iss` (ahora `PrivilegesRequired=lowest`, sin UAC)

### Resumen de artefactos (todos sin admin si se usan en carpeta usuario)

| Archivo | Descripción | Pide admin/UAC | Requiere Java |
|---|---|---|---|
| `target/control-tardanzas-1.0.0-jar-with-dependencies.jar` | Fat JAR portable | ❌ No | Sí 17+ |
| `target/CONTROL-RETRASO.exe` | EXE Launch4j portable | ❌ No | Sí 17+ |
| `target/dist/ControlRetraso-1.0.0.exe` | Instalador jpackage con JRE | ❌ No | No |
| `target/installer/ControlRetraso-Setup-*-Portable.exe` | Instalador Inno sin admin | ❌ No (lowest) | Sí 17+ |

---

## 📄 Licencia y Créditos

Proyecto académico para control de tardanzas del IES José Ballester Gozalvo.  
Incluye logos institucionales en `src/main/resources/images/`.

**Autor:** whj2006  
**Manual:** `Manual_ControlRetraso.docx.pdf`

---

## 🚀 TODO / Mejoras futuras

- Hash de contraseñas (actualmente texto plano en SP)
- Roles personalizables y auditoría
- Notificaciones push / Telegram
- Docker compose para MariaDB + app
- Tests unitarios DAOs
