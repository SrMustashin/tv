# TCL Apple Home v2 — rediseño tvOS minimalista

Diseño solo con ocho favoritos en la pantalla principal, una banda horizontal visible sin corte, selección con mando, reloj, fecha y botón **Todas las apps** para entrar a una cuadrícula secundaria. El fondo reacciona de manera sutil al elemento seleccionado.

Orden de favoritos: Netflix, Prime Video, Disney+, Max, Apple TV, MUBI, Xuper y YouTube Music.

## Actualizar un repositorio GitHub que ya compila

**Lo más seguro es reemplazar únicamente `app/src/main/java/com/qm6k/applehome/MainActivity.java`** por el archivo incluido en esta carpeta. No reemplaces tu `.github/workflows/build.yml` que ya funciona.

1. En GitHub entra al archivo `app/src/main/java/com/qm6k/applehome/MainActivity.java`.
2. Pulsa lápiz (editar), selecciona todo y pega el contenido del nuevo `MainActivity.java`.
3. Pulsa `Commit changes` sobre la rama `main`. El workflow actual debe iniciar automáticamente.
4. En **Actions**, abre la nueva ejecución y descarga el artefacto `qm6k-home-apk`; extrae `app-debug.apk`.
5. En Windows ejecuta: `C:\platform-tools\adb.exe connect 192.168.1.10:5555`, luego `C:\platform-tools\adb.exe install -r "C:\ruta\a\app-debug.apk"` y finalmente `C:\platform-tools\adb.exe shell am start -n com.qm6k.applehome/.MainActivity`.

## Seguridad y límites

- **No desactives** Google TV ni TCL SystemUI. El launcher original permanece como respaldo.
- No cambia los permisos, aplicaciones, configuración del televisor ni la asignación de HOME.
- El botón Entradas abre la pantalla de configuración de entradas si el firmware admite su Intent; aún no es selector directo de HDMI.
- El código fue revisado estructuralmente, pero no compilado ni probado en el televisor en este entorno. Utiliza Actions y comparte los logs si falla la compilación.
