#!/bin/bash
# Descarga las librerías de JavaCV (OpenCV para Java) que necesita el reconocimiento facial.
# Uso:   ./descargar_librerias.sh                 (Linux, por defecto)
#        ./descargar_librerias.sh windows-x86_64  (Windows)
#        ./descargar_librerias.sh macosx-arm64    (Mac con chip M)
cd "$(dirname "$0")" || exit 1

PLATAFORMA=${1:-linux-x86_64}
JAVACV=1.5.10
OPENCV=4.9.0-$JAVACV
OPENBLAS=0.3.26-$JAVACV
BASE=https://repo1.maven.org/maven2/org/bytedeco

bajar() {
    local archivo
    archivo=$(basename "$1")
    if [ -s "$archivo" ]; then
        echo "Ya existe: $archivo"
        return
    fi
    echo "Descargando $archivo ..."
    # Se baja a un archivo temporal para no dejar un .jar a medias si se corta la descarga
    wget -q --show-progress -O "$archivo.parte" "$1" && mv "$archivo.parte" "$archivo" || { rm -f "$archivo.parte"; echo "ERROR al descargar $archivo"; exit 1; }
}

bajar $BASE/javacv/$JAVACV/javacv-$JAVACV.jar
bajar $BASE/javacpp/$JAVACV/javacpp-$JAVACV.jar
bajar $BASE/javacpp/$JAVACV/javacpp-$JAVACV-$PLATAFORMA.jar
bajar $BASE/opencv/$OPENCV/opencv-$OPENCV.jar
bajar $BASE/opencv/$OPENCV/opencv-$OPENCV-$PLATAFORMA.jar
bajar $BASE/openblas/$OPENBLAS/openblas-$OPENBLAS.jar
bajar $BASE/openblas/$OPENBLAS/openblas-$OPENBLAS-$PLATAFORMA.jar

# Clasificador Haar de rostros frontales (lo usa OpenCV para detectar la cara). Va dentro del proyecto.
DESTINO_CASCADA=../src/Recursos/Facial
CASCADA=haarcascade_frontalface_default.xml
mkdir -p "$DESTINO_CASCADA"
if [ -s "$DESTINO_CASCADA/$CASCADA" ]; then
    echo "Ya existe: $CASCADA"
else
    echo "Descargando $CASCADA ..."
    wget -q --show-progress -O "$DESTINO_CASCADA/$CASCADA" "https://raw.githubusercontent.com/opencv/opencv/4.x/data/haarcascades/$CASCADA" \
        || { rm -f "$DESTINO_CASCADA/$CASCADA"; echo "ERROR al descargar $CASCADA"; exit 1; }
fi

echo "Listo. Librerías en: $(pwd)"
