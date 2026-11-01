@echo off
chcp 65001 >nul
title [MOC HUONG SHOP] - DUNG TOAN BO HE THONG

echo ==============================================================================
echo        🛑 MỘC HƯƠNG SHOP - DỪNG TOÀN BỘ MICROSERVICES & FRONTEND 🛑
echo ==============================================================================
echo.

set PORTS=8761 8080 8081 8082 8083 8084 3000

echo Dang kiem tra va giai phong cac cong mang (Ports: %PORTS%)...
echo.

for %%P in (%PORTS%) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%P ^| findstr LISTENING 2^>nul') do (
        echo [KILL] Dang dong tien trinh PID %%a dang chiem Port %%P...
        taskkill /F /PID %%a >nul 2>&1
    )
)

:: Đóng các cửa sổ cmd chạy maven / node nếu còn sót lại theo tiêu đề
taskkill /F /FI "WINDOWTITLE eq *SERVICE-DISCOVERY*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq *API-GATEWAY*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq *IDENTITY-SERVICE*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq *CATALOG-SERVICE*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq *ORDER-SERVICE*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq *NOTIFICATION-SERVICE*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq *MOC-HUONG-FRONTEND*" >nul 2>&1

echo.
echo ==============================================================================
echo  ✅ ĐÃ DỪNG TOÀN BỘ TIẾN TRÌNH VÀ GIẢI PHÓNG TOÀN BỘ PORTS THÀNH CÔNG!
echo ==============================================================================
echo.
pause
