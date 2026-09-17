# Sistema de Control de Tardanzas — IES José Ballester Gozalvo

Aplicación de escritorio en **Java Swing** para registrar, gestionar y notificar los retrasos de alumnos en un centro educativo. Permite fichar tardanzas por NIA, consultar historiales, generar rankings, gestionar cursos/alumnos, importar desde Excel, exportar informes y enviar correos automáticos a familias.

> **Centro:** IES José Ballester Gozalvo  
> **Stack:** Java 17 · Maven · Swing · MariaDB · HikariCP · Jakarta Mail · Apache POI

## ⬇️ Descarga Directa

> **Última versión: v1.0.0 — Los JARs/EXEs ya no están en el repo, se generan vía Maven o se descargan desde Releases**

<p align="center">

[![GitHub Releases](https://img.shields.io/badge/Descargar%20desde-Releases%20Oficiales-2ea44f?style=for-the-badge&logo=github&logoColor=white)](https://github.com/whj2006/Sistema_control_tardanza/releases)
[![Descargar JAR](https://img.shields.io/badge/JAR-Fat%20Jar%20con%20dependencias-0078D6?style=for-the-badge&logo=java&logoColor=white)](https://github.com/whj2006/Sistema_control_tardanza/releases/latest)
[![Descargar EXE](https://img.shields.io/badge/EXE-Windows%20Launch4j-00A4EF?style=for-the-badge&logo=windows&logoColor=white)](https://github.com/whj2006/Sistema_control_tardanza/releases/latest)
[![Manual PDF](https://img.shields.io/badge/Manual-PDF-red?style=for-the-badge&logo=adobeacrobatreader&logoColor=white)](./Manual_ControlRetraso.docx.pdf)

</p>

<p align="center">
  <img src="https://img.shields.io/badge/Requiere-Java%2017+-orange?style=flat-square" alt="Java 17">
  <img src="https://img.shields.io/badge/Plataforma-Windows%20%7C%20Linux%20%7C%20Mac-lightgrey?style=flat-square" alt="Plataforma">
  <img src="https://img.shields.io/badge/Maven-3.8+-blue?style=flat-square&logo=apachemaven" alt="Maven">
  <img src="https://img.shields.io/badge/Build-GitHub%20Actions-black?style=flat-square&logo=githubactions" alt="Actions">
</p>

### Cómo obtener el ejecutable

**Opción A - Desde Releases (recomendado, sin compilar):**
1. Ve a [Releases](https://github.com/whj2006/Sistema_control_tardanza/releases)
2. Descarga `CONTROL-RETRASO.exe` o `control-tardanzas-1.0.0-jar-with-dependencies.jar`
3. Ejecuta:
```bash
java -jar control-tardanzas-1.0.0-jar-with-dependencies.jar
# o doble clic en CONTROL-RETRASO.exe si tienes Java 17+
```

**Opción B - Compilar tú mismo:**
```bash
cd control-tardanzas
mvn clean package
# Genera target/CONTROL-RETRASO.exe y target/*-jar-with-dependencies.jar
```

> **¿No tienes Java?** Instálalo desde [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17) y luego ejecuta el JAR o el EXE.

> **Nota:** `*.jar`, `*.exe` y `target/` están en `.gitignore` y ya no se suben al repositorio. Usa `mvn package` o descarga desde Releases. El workflow de GitHub Actions genera ambos artefactos automáticamente en cada push a `main` y en cada tag `v*`.

---

## 🖥️ Opción .EXE — Windows (¡NUEVO!)

> **Sí, ahora el proyecto Maven genera un `.exe` nativo de Windows listo para doble clic.**

<p align="center">

<a href="https://github.com/whj2006/Sistema_control_tardanza/releases/latest">
<img src="https://img.shields.io/badge/EXE%20Windows-DESCARGAR%20CONTROL--RETRASO.exe-0078D6?style=for-the-badge&logo=windows&logoColor=white" alt="Descargar EXE">
</a>

</p>

### ¿Qué es el .EXE?

- Es un **wrapper del JAR con Launch4j**: `CONTROL-RETRASO.exe` (≈ 25MB)
- **No necesitas consola**: tipo `gui`, doble clic y abre la app
- **Con icono** del instituto (`icon.ico` generado desde `logo1.png`)
- **Comprueba Java 17+**: si no tienes Java, te muestra un mensaje con link a [Adoptium](https://adoptium.net/)
- **Mismo JAR por dentro**: toda la lógica de tardanzas, correos, etc.

### 3 formas de obtener el .EXE

#### 1️⃣ Descargar desde Releases (más fácil, 1 clic)
```
1. Ve a https://github.com/whj2006/Sistema_control_tardanza/releases/latest
2. Descarga CONTROL-RETRASO.exe
3. Doble clic (requiere Java 17 instalado)
```

#### 2️⃣ Compilar en tu PC Windows (2 comandos)
```bat
cd control-tardanzas
mvn clean package
:: Genera:
:: target/CONTROL-RETRASO.exe
:: target/control-tardanzas-1.0.0-jar-with-dependencies.jar
```
O usa el script:
```bat
:: En la raíz del repo:
generar-exe.bat
:: o
control-tardanzasuild-exe.bat
```

#### 3️⃣ GitHub Actions lo genera solo
Cada push a `main` o tag `v*` ejecuta el workflow `.github/workflows/build.yml` en `windows-latest` y sube el EXE como artefacto. Si creas un Release con tag, el EXE se adjunta automáticamente.

### Requisitos del .EXE

| Requisito | Detalle |
|---|---|
| **SO** | Windows 10/11 (64 bits) |
| **Java** | Java 17+ JRE/JDK instalado (Temurin recomendado) |
| **RAM** | 128MB inicial, 1024MB máx (configurable en pom.xml) |
| **BD** | MariaDB/MySQL accesible (misma que JAR) |

### ⚙️ ¿Se puede configurar la base de datos en el .EXE?

**¡Sí, 100%! El .EXE es exactamente el mismo JAR pero empaquetado.** Se configura igual:

1. **Primera ejecución del EXE:**
   - Busca `config.properties` en:
     - Carpeta donde está el `CONTROL-RETRASO.exe`
     - Directorio de trabajo actual
   - Si no lo encuentra, **abre automáticamente `VentanaConfigDB`** (ventana gráfica) para pedir:
     - Host (ej: `localhost`)
     - Puerto (ej: `3306`)
     - Nombre BD (ej: `control_tardanzas`)
     - Usuario y contraseña
   - Prueba la conexión con `DriverManager` y si es OK, guarda `config.properties` **junto al EXE** y en working dir

2. **Configuración SMTP y horarios:**
   - Una vez conectado a la BD, toda la configuración extra está en la BD, no en archivos:
     - **SMTP:** Panel `Config. SMTP` → guarda en tabla `configuracion` (host, puerto, usuario, pass en VARBINARY, remitente). Botón "Probar conexión"
     - **Hora entrada:** Panel `Ajustes` → tabla `configuracion_horarios` (ej: `08:00`)
   - Así que el EXE configura todo desde la UI, igual que el JAR

3. **¿Dónde queda `config.properties`?**
   - Junto al EXE: `C:\Programas\ControlRetraso\config.properties` (si instalas con Inno Setup)
   - O en la misma carpeta si lo descargas suelto
   - Ejemplo de contenido:
     ```properties
     db.host=localhost
     db.puerto=3306
     db.nombre=control_tardanzas
     db.user=root
     db.password=
     db.url=jdbc\:mariadb\://localhost\:3306/control_tardanzas?useUnicode\=true&characterEncoding\=UTF-8&serverTimezone\=Europe/Madrid
     ```
   - Puedes editarlo a mano o borrarlo para que vuelva a pedir datos

4. **Mejora incluida para EXE:**
   - `ConfigDB.java` ahora busca el archivo en **dos sitios**: working dir + directorio del JAR/EXE (via `getProtectionDomain().getCodeSource().getLocation()`)
   - Al guardar, lo guarda en **ambos sitios** para asegurar que el EXE siempre lo encuentre, incluso si se lanza desde acceso directo

> **En resumen:** El EXE se configura exactamente igual que el JAR. No pierdes ninguna funcionalidad.

> **¿Quieres un EXE que NO necesite Java?** Usa `mvn package -Pinstaller` (jpackage) o compila `installer.iss` con Inno Setup → genera instalador con JRE embebido.



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

## 📦 Compilación — JAR y EXE

### Opción 1: JAR ejecutable (multiplataforma)
```bash
cd control-tardanzas
mvn clean package -DskipTests
java -jar target/control-tardanzas-1.0.0-jar-with-dependencies.jar
```
El plugin `maven-antrun-plugin` copia automáticamente el JAR a `../CONTROL RETRASO.jar`.

### Opción 2: EXE Windows con Launch4j (recomendado)

Sí, este es un proyecto **Maven** y se puede convertir a `.exe`. Ya está configurado en el `pom.xml` con `launch4j-maven-plugin`.

**Requisitos en Windows:**
- JDK 17+ instalado
- Maven 3.8+ en PATH
- `src/main/resources/images/icon.ico` (ya generado desde logo1.png)

**Compilar:**

```bat
REM En Windows, doble clic o desde cmd:
cd control-tardanzas
build-exe.bat

REM O manual:
mvn clean package
REM Genera:
REM target/CONTROL-RETRASO.exe  (wrapper del fat jar, necesita Java 17 instalado)
REM target/control-tardanzas-1.0.0-jar-with-dependencies.jar
```

**Qué hace Launch4j:**
- Empaqueta el fat jar en un `CONTROL-RETRASO.exe` con icono, versión y metadata
- Tipo `gui` (no abre consola)
- Comprueba Java 17+ y muestra mensaje con link a Adoptium si no está instalado
- Heap: 128MB inicial, 1024MB máximo

> El EXE **NO incluye JRE**, requiere Java instalado. Para un instalador con JRE embebido usa la Opción 3.

### Opción 3: Instalador nativo con JRE embebido (jpackage)

Genera un instalador `.exe` / `.msi` que incluye el runtime Java, no necesita Java previo.

**Solo funciona en Windows con JDK 17:**

```bash
# En Windows:
mvn clean package -Pinstaller
# Genera en target/dist/ el instalador nativo

# O con jpackage directo:
jpackage --name "ControlRetraso" \
  --input target/ \
  --main-jar control-tardanzas-1.0.0-jar-with-dependencies.jar \
  --main-class com.instituto.tardanzas.Main \
  --type exe \
  --icon src/main/resources/images/icon.ico \
  --vendor "IES Jose Ballester Gozalvo" \
  --app-version 1.0.0 \
  --win-dir-chooser --win-menu --win-shortcut
```

### Opción 4: Instalador con Inno Setup (instalador profesional)

1. Compila el EXE con `mvn package`
2. Instala [Inno Setup 6](https://jrsoftware.org/isinfo.php)
3. Abre `control-tardanzas/installer.iss` y compila
4. Obtienes `target/installer/ControlRetraso-Setup-1.0.0.exe` con asistente, icono en escritorio, desinstalador, etc.

**Scripts incluidos:**
- `control-tardanzas/build-exe.bat` → Build completo en Windows (JAR + EXE)
- `control-tardanzas/build-exe.sh` → Build en Linux/Mac (JAR, EXE solo en Windows)
- `control-tardanzas/installer.iss` → Script Inno Setup

### Resumen de artefactos generados

| Archivo | Descripción | Requiere Java |
|---|---|---|
| `CONTROL RETRASO.jar` | Fat jar, doble clic | Sí |
| `target/CONTROL-RETRASO.exe` | EXE wrapper Launch4j | Sí (Java 17+) |
| `target/dist/ControlRetraso-1.0.0.exe` | Instalador jpackage con JRE | No |
| `target/installer/ControlRetraso-Setup-1.0.0.exe` | Instalador Inno Setup | Sí (o con JRE si usas jpackage) |

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
