@echo off
chcp 65001 >nul
title [MOC HUONG SHOP] - LAUNCHER TOAN HE THONG (FE + BE)

echo ==============================================================================
echo        🌿 MỘC HƯƠNG SHOP - HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ TRẦM HƯƠNG 🌿
echo ==============================================================================
echo  Dang khoi dong 6 Microservices (Spring Boot 3.2.5) va Next.js 14 Frontend...
echo ==============================================================================
echo.

set ROOT_DIR=%~dp0

:: 1. Service Discovery (Eureka Server)
echo [1/7] Dang khoi dong SERVICE-DISCOVERY (Eureka Server - Port 8761)...
start "1. [PORT 8761] SERVICE-DISCOVERY" cmd /k "title [8761] SERVICE-DISCOVERY && cd /d "%ROOT_DIR%aromatics-platform\service-discovery" && ..\mvnw.cmd spring-boot:run"

echo     Dang cho Eureka Server san sang khoi tao Registry (10 giay)...
timeout /t 10 /nobreak >nul

:: 2. API Gateway
echo [2/7] Dang khoi dong API-GATEWAY (Port 8080)...
start "2. [PORT 8080] API-GATEWAY" cmd /k "title [8080] API-GATEWAY && cd /d "%ROOT_DIR%aromatics-platform\api-gateway" && ..\mvnw.cmd spring-boot:run"

:: 3. Identity Service
echo [3/7] Dang khoi dong IDENTITY-SERVICE (Port 8081)...
start "3. [PORT 8081] IDENTITY-SERVICE" cmd /k "title [8081] IDENTITY-SERVICE && cd /d "%ROOT_DIR%aromatics-platform\identity-service" && ..\mvnw.cmd spring-boot:run"

:: 4. Catalog Service
echo [4/7] Dang khoi dong CATALOG-SERVICE (Port 8082 - Ho tro Gemini AI)...
start "4. [PORT 8082] CATALOG-SERVICE" cmd /k "title [8082] CATALOG-SERVICE && cd /d "%ROOT_DIR%aromatics-platform\catalog-service" && ..\mvnw.cmd spring-boot:run"

:: 5. Order Service
echo [5/7] Dang khoi dong ORDER-SERVICE (Port 8083 - VNPay Gateway)...
start "5. [PORT 8083] ORDER-SERVICE" cmd /k "title [8083] ORDER-SERVICE && cd /d "%ROOT_DIR%aromatics-platform\order-service" && ..\mvnw.cmd spring-boot:run"

:: 6. Notification Service
echo [6/7] Dang khoi dong NOTIFICATION-SERVICE (Port 8084 - Kafka Consumer)...
start "6. [PORT 8084] NOTIFICATION-SERVICE" cmd /k "title [8084] NOTIFICATION-SERVICE && cd /d "%ROOT_DIR%aromatics-platform\notification-service" && ..\mvnw.cmd spring-boot:run"

:: 7. Frontend Next.js
echo [7/7] Dang khoi dong MOC-HUONG-FRONTEND (Next.js 14 - Port 3000)...
start "7. [PORT 3000] MOC-HUONG-FRONTEND" cmd /k "title [3000] MOC-HUONG-FRONTEND && cd /d "%ROOT_DIR%moc-huong-frontend" && npm run dev"

echo.
echo ==============================================================================
echo  ✅ TOÀN BỘ HỆ THỐNG ĐANG ĐƯỢC KHỞI CHẠY TRONG 7 CỬA SỔ RIÊNG BIỆT!
echo ==============================================================================
echo  - Cửa hàng & Trang Quản Trị:   http://localhost:3000
echo  - Eureka Dashboard:            http://localhost:8761
echo  - API Gateway Tổng:            http://localhost:8080
echo  - Swagger Catalog Service:     http://localhost:8082/swagger-ui.html
echo ==============================================================================
echo  * Mẹo: Để dừng toàn bộ các tiến trình nhanh chóng, nhấp đúp file 'stop-all.bat'.
echo.
pause
