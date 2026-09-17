@echo off
REM Build script para Windows - Genera JAR y EXE
REM Requiere: JDK 17+ y Maven 3.8+ en PATH

echo ============================================
echo  Control de Tardanzas - Build EXE
echo ============================================

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java no encontrado. Instala JDK 17 desde https://adoptium.net/
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven no encontrado. Instala Maven desde https://maven.apache.org/
    pause
    exit /b 1
)

echo [1/3] Limpiando...
call mvn clean

echo [2/3] Compilando JAR + EXE con Launch4j...
call mvn package

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Fallo en la compilacion
    pause
    exit /b 1
)

echo [3/3] Copiando artefactos...
if exist target\CONTROL-RETRASO.exe (
    echo [OK] EXE generado: target\CONTROL-RETRASO.exe
    copy /Y target\CONTROL-RETRASO.exe "..\CONTROL RETRASO.exe"
    echo [OK] Copiado a raiz como CONTROL RETRASO.exe
) else (
    echo [WARN] No se encontro EXE, revisa si icon.ico existe
)

if exist target\control-tardanzas-1.0.0-jar-with-dependencies.jar (
    echo [OK] JAR generado: target\control-tardanzas-1.0.0-jar-with-dependencies.jar
)

echo.
echo ============================================
echo  Build completado!
echo ============================================
echo  Archivos:
echo   - target\CONTROL-RETRASO.exe  (ejecutable Windows, necesita Java 17)
echo   - target\control-tardanzas-1.0.0-jar-with-dependencies.jar (fat jar)
echo   - ..\CONTROL RETRASO.jar (copia en raiz)
echo.
pause
