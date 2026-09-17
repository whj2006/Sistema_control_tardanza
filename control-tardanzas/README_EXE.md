# 🖥️ Opción .EXE Windows — Guía Rápida

Este proyecto Maven ya genera un `.exe` de Windows listo para usar.

## ¿Qué genera?

- `target/CONTROL-RETRASO.exe` → EXE wrapper con Launch4j (necesita Java 17+)
- `target/control-tardanzas-1.0.0-jar-with-dependencies.jar` → Fat JAR
- `CONTROL RETRASO.jar` (en raíz) → Copia del fat JAR (gitignored)

## Requisitos para compilar EXE

- Windows 10/11
- JDK 17+ (Temurin: https://adoptium.net/)
- Maven 3.8+ en PATH (`mvn -v` debe funcionar)
- `src/main/resources/images/icon.ico` (ya incluido, generado desde logo1.png)

## Compilar

### Opción A — Script (recomendado)
```bat
:: Desde la raíz del repo:
generar-exe.bat

:: O desde control-tardanzas/:
build-exe.bat
```

### Opción B — Maven directo
```bat
cd control-tardanzas
mvn clean package
```

### Opción C — GitHub Actions (sin compilar local)
1. Haz push a `main` o crea un tag `v1.0.0`
2. Ve a Actions → último workflow → descarga artefacto `exe-windows`
3. Si es un Release con tag, el EXE se adjunta automáticamente al Release

## ¿Qué hace el EXE?

Configurado en `pom.xml` con `launch4j-maven-plugin`:

```xml
<headerType>gui</headerType> <!-- no abre consola -->
<jar>...-jar-with-dependencies.jar</jar>
<outfile>CONTROL-RETRASO.exe</outfile>
<icon>src/main/resources/images/icon.ico</icon>
<jre>
  <minVersion>17</minVersion>
  <maxHeapSize>1024</maxHeapSize>
</jre>
```

- Wrapper del fat jar, no incluye JRE (requiere Java instalado)
- Mensaje amigable si no hay Java, con link a Adoptium
- Icono y metadata de versión (fileVersion, productName, etc.)

## EXE con JRE embebido (no necesita Java)

Si quieres un instalador que incluya Java:

```bat
:: Solo en Windows con JDK 17:
mvn clean package -Pinstaller
:: Genera en target/dist/

:: O directo con jpackage:
jpackage --name "ControlRetraso" --input target/ --main-jar control-tardanzas-1.0.0-jar-with-dependencies.jar --main-class com.instituto.tardanzas.Main --type exe --icon src/main/resources/images/icon.ico --vendor "IES Jose Ballester Gozalvo" --app-version 1.0.0 --win-dir-chooser --win-menu --win-shortcut
```

## Instalador profesional con Inno Setup

1. Compila EXE con `mvn package`
2. Instala Inno Setup 6: https://jrsoftware.org/isinfo.php
3. Abre `installer.iss` y compila (Ctrl+F9)
4. Obtienes `target/installer/ControlRetraso-Setup-1.0.0.exe`

## Descarga directa

Si no quieres compilar, descarga el EXE ya compilado desde:

https://github.com/whj2006/Sistema_control_tardanza/releases/latest

Busca `CONTROL-RETRASO.exe` en Assets.

## Solución de problemas

- **"Java not found"** → Instala JDK 17 desde https://adoptium.net/
- **"icon.ico not found"** → Ya está en `src/main/resources/images/`, si lo borras genera uno nuevo desde logo1.png con ImageMagick: `convert logo1.png -define icon:auto-resize=256,128,64,48,32,16 icon.ico`
- **"mvn no se reconoce"** → Instala Maven y añade a PATH, o usa `mvnw` si lo añades al proyecto
- **EXE no abre** → Ejecuta `java -jar target/*-jar-with-dependencies.jar` para ver error real, revisa `config.properties`

## Estructura de artefactos

| Archivo | Tamaño aprox | Requiere Java | Descripción |
|---|---|---|---|
| `CONTROL-RETRASO.exe` | ~25MB | Sí (17+) | Wrapper Launch4j, doble clic |
| `*-jar-with-dependencies.jar` | ~25MB | Sí (17+) | Fat JAR multiplataforma |
| `ControlRetraso-1.0.0.exe` (jpackage) | ~60-80MB | No | Instalador con JRE embebido |
| `ControlRetraso-Setup-1.0.0.exe` (Inno) | ~26MB | Sí | Instalador con asistente |
