PROYECTMEMORY: JUEGO DE MEMORIA CON BANDERAS

Proyecto 2 de Desarrollo de Software II
Universidad de Costa Rica, Informática Empresarial

Juego de memoria hecho en Java con Swing, programación orientada a objetos y
el patrón MVC.

Integrantes:
    Ángel Fallas Castro
    Jorshan Quesada Rojas
    Ibuki Pérez Alfaro



1. QUÉ INCLUYE

    1. Modo clásico (parejas): dos jugadores, cada uno con su tablero de 6x6
       (18 parejas de banderas).
    2. Modo patrones: un jugador repite patrones de casillas cada vez más
       largos hasta que falla.
    3. Registro e inicio de sesión con usuario o correo y contraseña, y de
       forma opcional con reconocimiento facial (cámara web).
    4. Puntajes y premios calculados con el tipo de cambio del dólar que
       publica el BCCR, y tabla de Top Jugadores en el menú principal.



2. REQUISITOS

    1. JDK 11 o superior (probado con JDK 21).
    2. Internet la } vez: Maven descarga las librerías de OpenCV y
       JavaCV (unos 150 MB). Después ya no hace falta.
    3. Cámara web: solo si se quiere usar el reconocimiento facial. El juego
       funciona igual sin ella.



3. CÓMO EJECUTAR EL PROGRAMA

La carpeta del proyecto es MemoriaBanderas (contiene el archivo pom.xml).
Elija UNA de las siguientes formas.


3.1 IntelliJ IDEA (Windows, Linux y Mac)

    1. File > Open, y abrir la carpeta ProyectMemory.
    2. Si aparece el aviso para cargar el proyecto Maven, aceptarlo. Si no
       aparece, clic derecho sobre MemoriaBanderas/pom.xml y elegir
       "Add as Maven Project".
    3. Esperar a que termine la barra de progreso de abajo.
    4. Clic derecho sobre src/Main.java y elegir "Run 'Main.main()'"
       (o usar el botón "Jugar" de arriba a la derecha, si aparece).

    Script de librerías: NO hay que ejecutarlo. Maven descarga las librerías
    solo.


3.2 NetBeans (Windows, Linux y Mac)

    1. File > Open Project, y elegir la carpeta MemoriaBanderas (se reconoce
       como proyecto Maven).
    2. Esperar a que termine de descargar las dependencias.
    3. Clic derecho sobre Main.java y elegir "Run File" (o presionar F6).

    Script de librerías: NO hay que ejecutarlo. El archivo nbactions.xml ya
    deja configurado cómo ejecutar el programa.


3.3 Terminal en Linux o Mac

    1. cd MemoriaBanderas
    2. chmod +x ejecutar.sh      (solo la primera vez)
    3. ./ejecutar.sh

    Script de librerías: lo hace solo. Si no hay archivos .jar en la carpeta
    lib, ejecutar.sh llama a lib/descargar_librerias.sh (necesita wget e
    internet).


3.4 Terminal en Windows

    No hay script para Windows. Usar IntelliJ IDEA o NetBeans.


3.5 Cuándo sí se usa lib/descargar_librerias.sh

Solo si se compila a mano o con un IDE sin Maven. El script descarga los .jar
en la carpeta lib y después hay que agregar esa carpeta como biblioteca del
proyecto.

    Linux (por defecto):    ./descargar_librerias.sh
    Windows:                ./descargar_librerias.sh windows-x86_64
    Mac con chip M:         ./descargar_librerias.sh macosx-arm64

Con Maven, que es lo normal, NO se necesita.


3.6 Dónde se guardan los datos

Los datos se guardan en la carpeta desde donde se ejecuta el programa.

    Con IntelliJ o NetBeans:    carpeta MemoriaBanderas
    Con ejecutar.sh:            carpeta MemoriaBanderas/out

Los archivos se crean solos al registrar el primer usuario.



4. CÓMO FUNCIONA EL JUEGO


4.1 Menú principal

    1. Al abrir aparece una animación de introducción. Con cualquier tecla se
       salta.
    2. Arriba de los botones dice "Bienvenido Jugador", o "Bienvenido
       (usuario)" cuando hay una sesión iniciada.
    3. Abajo a la izquierda está la tabla Top Jugadores, ordenada por puntaje.
    4. Abajo a la derecha están los cuatro botones: Registro/Login, Memoria
       (modo clásico), Patrón y Música (silenciar o activar).
    5. En la barra superior están Ayuda > Acerca de (los creadores del
       proyecto) y Cerrar sesión.
    6. La tecla F11 pone la pantalla completa y Esc la quita.


4.2 Registrarse e iniciar sesión

    1. Botón Registro/Login, y luego "Registrarse".
    2. Llenar nombre, apellidos, correo, usuario, contraseña (dos veces) y
       fecha de nacimiento. La foto de perfil es opcional.
    3. Opcional: botón "Registrar Datos Faciales". Se abre la cámara y se
       toman 10 muestras del rostro automáticamente.
    4. Al terminar el registro, la sesión se inicia sola.
    5. Para entrar después, escribir usuario o correo y contraseña, y
       presionar "Ingresar". También se puede usar el botón "Ingresar con
       Rostro".
    6. Si el rostro falla 3 veces seguidas, el acceso facial se bloquea
       3 minutos. Con la contraseña se puede entrar igual.
    7. Para jugar de a dos, el segundo jugador inicia sesión también desde el
       mismo botón. El primero en entrar es el Jugador 1 y el segundo es el
       Jugador 2.


4.3 Modo clásico (parejas)

    1. Cada jugador tiene su tablero de 6x6. Primero hay unos segundos para
       memorizar las cartas.
    2. Cada turno dura 10 segundos. Un acierto suma 7 segundos y un fallo
       pasa el turno al otro jugador.
    3. Cada jugador tiene un botón de pista que muestra las cartas 3 segundos
       y que se puede usar una sola vez por partida.
    4. Gana quien complete sus 18 parejas primero. Al terminar, el juego
       pregunta "¿Desea registrar este puntaje?". Si responde que sí, se
       guardan los intentos y se suman los puntos a la cuenta de ese jugador
       (el Tablero 1 corresponde al primer usuario con sesión y el Tablero 2
       al segundo).
    5. El botón "Finalizar intento" abre la pregunta "Comenzar de nuevo" o
       "Volver al menú principal".


4.4 Modo patrones

    1. Se muestra un patrón de 3 casillas. Si se repite bien, el siguiente
       tiene una casilla más, y así sucesivamente.
    2. Hay barras de tiempo total y de tiempo entre casillas.
    3. Al fallar aparece la animación de derrota y el mensaje "¡Fallaste el
       patrón!", y se suman los puntos a la cuenta del jugador.
    4. El botón "Terminar intento" cierra la partida antes de fallar,
       también sumando los puntos.
    5. Si hay dos usuarios con sesión, antes de empezar aparece la pantalla
       "Selección de Jugador" para elegir quién juega.


4.5 Puntos y premios

    1. El premio se calcula así: 1 dividido entre el puntaje, multiplicado
       por 100 y por el tipo de cambio del dólar del BCCR.
    2. Si no hay internet, se usa el último valor guardado o un valor por
       defecto.
    3. Los puntos solo se guardan para usuarios con sesión iniciada. Sin
       sesión se puede jugar, pero no queda registro.


4.6 Salir de la sesión

Menú superior "Cerrar sesión". Muestra "Sesión cerrada" y el menú vuelve a
decir "Bienvenido Jugador".



5. ESTRUCTURA DEL PROYECTO (MVC)

ProyectMemory
    README.txt
    MemoriaBanderas
        pom.xml                  Maven: dependencias y perfiles por sistema operativo
        nbactions.xml            Acciones de ejecución para NetBeans
        ejecutar.sh              Compila y ejecuta desde terminal (Linux y Mac)
        lib
            descargar_librerias.sh    Solo si no se usa Maven
        src
            Main.java            Punto de entrada
            BccrApiClient.java   Prueba independiente de la API del BCCR
            model                Lógica y datos: usuarios, puntajes, parejas,
                                 patrón, rostros y tipo de cambio
            view                 Ventanas Swing
            controller           Eventos y flujo: login, registro, facial,
                                 juegos y música
            Recursos             Imágenes, GIF, sonidos y el clasificador de
                                 rostros

Archivos que crea el programa (no se suben a Git):

    usuarios.txt    Cuentas y puntajes
    rostros.dat     Datos faciales



6. RECONOCIMIENTO FACIAL (OPCIONAL)

    1. La cámara detecta el rostro con OpenCV (clasificador Haar de rostros
       frontales).
    2. El rostro se recorta, se pasa a escala de grises y se reduce a
       100x100 píxeles (10 000 valores).
    3. Al registrarse se promedian 10 muestras para reducir el ruido y se
       guardan en rostros.dat.
    4. Al iniciar sesión se compara el rostro con los registrados usando la
       distancia euclidiana. Si es menor al umbral (3000), se da acceso.



7. SI ALGO FALLA

    1. Error "package org.bytedeco... does not exist", o el botón de ejecutar
       no aparece.
       El proyecto Maven no está importado. Cargar MemoriaBanderas/pom.xml
       como proyecto Maven.

    2. La cámara no abre.
       Revisar que otro programa no la esté usando. Mientras tanto se puede
       entrar con usuario y contraseña.

    3. No suena nada.
       El equipo no tiene un dispositivo de audio configurado para Java (por
       ejemplo, IntelliJ instalado como Flatpak en Linux). El resto del
       juego no depende del sonido.

    4. No se encuentra el clasificador de rostros.
       El archivo haarcascade_frontalface_default.xml debe estar en
       src/Recursos/Facial. El script lib/descargar_librerias.sh lo
       descarga.