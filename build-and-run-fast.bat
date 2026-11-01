@echo off
chcp 65001 >nul
title [MOC HUONG SHOP] - DONG GOI VA CHAY NHANH (JAR MODE)

echo ==============================================================================
echo        ⚡ MỘC HƯƠNG SHOP - ĐÓNG GÓI JAR VÀ KHỞI ĐỘNG SIÊU TỐC ⚡
echo ==============================================================================
echo.

set ROOT_DIR=%~dp0

echo [BƯỚC 1/2] Đang đóng gói 6 Microservices thành file .jar (bỏ qua tests)...
cd /d "%ROOT_DIR%aromatics-platform"
call .\mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Đóng gói thất bại! Vui lòng kiểm tra lại mã nguồn.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ==============================================================================
echo [BƯỚC 2/2] Đang khởi động trực tiếp từ file JAR đã tối ưu...
echo ==============================================================================

:: 1. Service Discovery
echo [1/7] Chạy Service Discovery...
start "1. [8761] EUREKA" cmd /k "title [8761] EUREKA && cd /d "%ROOT_DIR%aromatics-platform\service-discovery\target" && java -jar service-discovery-0.0.1-SNAPSHOT.jar"

echo     Chờ Eureka sẵn sàng (8 giây)...
timeout /t 8 /nobreak >nul

:: 2. API Gateway
echo [2/7] Chạy API Gateway...
start "2. [8080] GATEWAY" cmd /k "title [8080] GATEWAY && cd /d "%ROOT_DIR%aromatics-platform\api-gateway\target" && java -jar api-gateway-0.0.1-SNAPSHOT.jar"

:: 3. Identity Service
echo [3/7] Chạy Identity Service...
start "3. [8081] IDENTITY" cmd /k "title [8081] IDENTITY && cd /d "%ROOT_DIR%aromatics-platform\identity-service\target" && java -jar identity-service-0.0.1-SNAPSHOT.jar"

:: 4. Catalog Service
echo [4/7] Chạy Catalog Service...
start "4. [8082] CATALOG" cmd /k "title [8082] CATALOG && cd /d "%ROOT_DIR%aromatics-platform\catalog-service\target" && java -jar catalog-service-0.0.1-SNAPSHOT.jar"

:: 5. Order Service
echo [5/7] Chạy Order Service...
start "5. [8083] ORDER" cmd /k "title [8083] ORDER && cd /d "%ROOT_DIR%aromatics-platform\order-service\target" && java -jar order-service-0.0.1-SNAPSHOT.jar"

:: 6. Notification Service
echo [6/7] Chạy Notification Service...
start "6. [8084] NOTIFICATION" cmd /k "title [8084] NOTIFICATION && cd /d "%ROOT_DIR%aromatics-platform\notification-service\target" && java -jar notification-service-0.0.1-SNAPSHOT.jar"

:: 7. Frontend Next.js
echo [7/7] Chạy Frontend Next.js...
start "7. [3000] FRONTEND" cmd /k "title [3000] FRONTEND && cd /d "%ROOT_DIR%moc-huong-frontend" && npm run dev"

echo.
echo ==============================================================================
echo  ✅ HỆ THỐNG ĐÃ ĐƯỢC KHỞI ĐỘNG SIÊU TỐC VỚI 6 JAR + NEXT.JS!
echo ==============================================================================
echo  - Frontend: http://localhost:3000
echo  - Eureka:   http://localhost:8761
echo  - Gateway:  http://localhost:8080
echo ==============================================================================
echo.
pause
