@echo off
set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%build-and-run-fast.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Co loi xay ra khi dong goi hoac chay he thong.
    pause
)
