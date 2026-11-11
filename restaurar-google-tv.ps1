$adb="C:\platform-tools\adb.exe"; $tv="192.168.1.10:5555"
& $adb -s $tv shell pm enable --user 0 com.google.android.apps.tv.launcherx
& $adb -s $tv shell cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity
& $adb -s $tv shell am start -n com.google.android.apps.tv.launcherx/.home.HomeActivity
