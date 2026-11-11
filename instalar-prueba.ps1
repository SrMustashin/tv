$adb="C:\platform-tools\adb.exe"; $tv="192.168.1.10:5555"; $apk=Join-Path $PSScriptRoot "app-debug.apk"
if (!(Test-Path $apk)) {throw "Coloca app-debug.apk junto a este script"}
& $adb connect $tv
& $adb -s $tv install -r $apk
if ($LASTEXITCODE -ne 0) {throw "No se pudo instalar"}
& $adb -s $tv shell am start -n com.qm6k.applehome/.MainActivity
& $adb -s $tv shell screencap -p /sdcard/Download/qm6k-home-test.png
& $adb -s $tv pull /sdcard/Download/qm6k-home-test.png (Join-Path $PSScriptRoot "qm6k-home-test.png")
