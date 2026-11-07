@echo off
echo Conectando celular (5554) con el reloj...
adb -s emulator-5554 forward tcp:8080 tcp:8080
echo Listo. Ahora abre la app del reloj.
pause