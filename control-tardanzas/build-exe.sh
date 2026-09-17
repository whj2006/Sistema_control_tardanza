#!/bin/bash
# Build script Linux/Mac - Genera JAR y prepara para EXE
# En Linux no genera EXE nativo con Launch4j sin wine, pero si el JAR
# Para EXE real, ejecutar en Windows o usar jpackage en Windows

set -e

echo "============================================"
echo " Control de Tardanzas - Build"
echo "============================================"

if ! command -v java &> /dev/null; then
    echo "[ERROR] Java no encontrado. Instala JDK 17"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "[ERROR] Maven no encontrado"
    exit 1
fi

echo "[1/2] Limpiando..."
mvn clean

echo "[2/2] Compilando JAR (+ EXE si icon.ico y launch4j disponible)..."
mvn package

echo ""
echo "============================================"
echo " Build completado!"
echo "============================================"
echo "Archivos generados:"
ls -lh target/*.jar 2>/dev/null || echo "No JAR found"
ls -lh target/*.exe 2>/dev/null || echo "EXE solo se genera 100% en Windows (Launch4j). En Linux el JAR es funcional."
ls -lh "../CONTROL RETRASO.jar" 2>/dev/null && echo "Copia en raiz OK"

echo ""
echo "Para instalador nativo con runtime embebido (solo Windows con JDK 17):"
echo "  mvn package -Pinstaller"
