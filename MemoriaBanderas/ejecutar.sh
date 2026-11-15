#!/bin/bash
# Compila y ejecuta el juego desde la terminal (Linux y Mac).   Uso:  ./ejecutar.sh
# Si faltan las librerías de reconocimiento facial en lib/, las descarga primero.
cd "$(dirname "$0")" || exit 1

if ! ls lib/*.jar >/dev/null 2>&1; then
    case "$(uname -s)-$(uname -m)" in
        Darwin-arm64)  PLATAFORMA=macosx-arm64 ;;
        Darwin-x86_64) PLATAFORMA=macosx-x86_64 ;;
        *)             PLATAFORMA=linux-x86_64 ;;
    esac
    echo "Faltan las librerías de OpenCV en lib/. Descargando ($PLATAFORMA)..."
    bash lib/descargar_librerias.sh "$PLATAFORMA" || exit 1
fi

rm -rf out && mkdir out
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java") || exit 1
cp -r src/Recursos out/
# Se ejecuta desde out/ porque ahí se crean y leen usuarios.txt y rostros.dat
cd out && java -cp ".:../lib/*" Main
