@echo off
REM Generador de EXE para Control de Retraso - Acceso directo en raiz
REM Llama al build real en control-tardanzas/

echo ============================================
echo  Control de Retraso - Generar EXE Windows
echo ============================================
echo.

if not exist "control-tardanzas\pom.xml" (
    echo [ERROR] No se encontro control-tardanzas\pom.xml
    echo Ejecuta este .bat desde la raiz del repositorio
    pause
    exit /b 1
)

cd control-tardanzas
call build-exe.bat
cd ..

echo.
echo ============================================
echo  Si todo fue bien, encontraras:
echo   - control-tardanzas\target\CONTROL-RETRASO.exe
echo   - CONTROL RETRASO.jar (en raiz, copia del fat jar)
echo ============================================
pause
