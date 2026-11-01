@echo off
set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%run-all.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Co loi xay ra khi chay script launcher.
    pause
)
