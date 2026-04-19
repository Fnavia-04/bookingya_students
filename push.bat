@echo off
REM Script para hacer push a GitHub y disparar GitHub Actions
REM Uso: push.bat "mensaje del commit"

cd /d "c:\Users\ACER-02509\Downloads\actividad2_bookingya\bookingya_actividad2"

REM Mostrar estado actual
echo.
echo ===== STATUS ACTUAL =====
git status
echo.

REM Preguntar por mensaje del commit
if "%1"=="" (
    set /p MSG="Ingresa el mensaje del commit: "
) else (
    set MSG=%1
)

REM Agregar todos los cambios
echo.
echo ===== AGREGANDO CAMBIOS =====
git add .
echo Cambios agregados.

REM Hacer commit
echo.
echo ===== CREANDO COMMIT =====
git commit -m "%MSG%"

REM Hacer push
echo.
echo ===== HACIENDO PUSH A GITHUB =====
git push origin main

REM Mostrar resultado
echo.
echo ===== RESULTADO =====
echo.
if %errorlevel% equ 0 (
    echo [SUCCESS] Push completado!
    echo.
    echo Abre tu repositorio en GitHub y haz clic en "Actions" para ver el pipeline.
) else (
    echo [ERROR] Hubo un problema con el push.
    echo Verifica tu conexión a internet y que tengas acceso al repositorio.
)

pause
