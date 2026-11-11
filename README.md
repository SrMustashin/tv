# TCL QM6K — Launcher minimalista v0.1 (prototipo)

Dock: Netflix · Prime Video · Disney+ · Max · Apple TV · MUBI · Xuper · YouTube Music.

## Compilar sin Android Studio
1. Crea repositorio de GitHub, sube el contenido de esta carpeta, incluido `.github/workflows/build-apk.yml`.
2. Abre **Actions → Compilar APK → Run workflow**.
3. Descarga artefacto `qm6k-home-apk`; extrae `app-debug.apk` junto a `instalar-prueba.ps1`.
4. Ejecuta PowerShell: `powershell -NoProfile -ExecutionPolicy Bypass -File .\instalar-prueba.ps1`.
5. Inspecciona la captura `qm6k-home-test.png` y prueba apertura de apps y mando real.

**No desactives Google TV todavía.** No hay garantía de que asignar HOME reemplace correctamente el launcher stock en G08. Si se requiere restaurar usa `restaurar-google-tv.ps1`. El código no está compilado ni probado en tu TV; no es un APK listo. La acción Entradas abre configuración de entradas (si el firmware admite el intent), no cambia HDMI directamente. Faltan animación de cristal, SSID Wi-Fi, selector HDMI y wallpaper configurable. El dock es horizontalmente desplazable. Cambia el arreglo `DOCK` de `MainActivity.java` para reordenar apps.
