$Host.UI.RawUI.WindowTitle = "MOC HUONG SHOP - LAUNCHER TOAN HE THONG"
Clear-Host

Write-Host "==============================================================" -ForegroundColor Green
Write-Host "       🌿 MỘC HƯƠNG SHOP - HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ 🌿       " -ForegroundColor Yellow
Write-Host "==============================================================" -ForegroundColor Green
Write-Host " Dang khoi dong 6 Microservices va Next.js 14 Frontend..." -ForegroundColor Cyan
Write-Host " Database: Aiven Cloud PostgreSQL" -ForegroundColor DarkCyan
Write-Host "==============================================================" -ForegroundColor Green
Write-Host ""

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 0. Giai phong cac port neu co tien trinh zombie cu
$ports = @(8761, 8080, 8081, 8082, 8083, 8084, 3000)
foreach ($p in $ports) {
    try {
        $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
        if ($conns) {
            foreach ($conn in $conns) {
                if ($conn.OwningProcess -gt 0) {
                    Write-Host " [CLEANUP] Giai phong port $p (PID $($conn.OwningProcess))..." -ForegroundColor DarkYellow
                    Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
                }
            }
        }
    } catch {}
}

# 1. Service Discovery
Write-Host "[1/7] Dang khoi dong SERVICE-DISCOVERY (Eureka - Port 8761)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8761] SERVICE-DISCOVERY && cd /d `"$rootDir\aromatics-platform\service-discovery`" && ..\mvnw.cmd spring-boot:run"

Write-Host "     Dang cho Eureka Server khoi tao trong 12 giay..." -ForegroundColor DarkGray
Start-Sleep -Seconds 12

# 2. API Gateway
Write-Host "[2/7] Dang khoi dong API-GATEWAY (Port 8080)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8080] API-GATEWAY && cd /d `"$rootDir\aromatics-platform\api-gateway`" && ..\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 2

# 3. Identity Service
Write-Host "[3/7] Dang khoi dong IDENTITY-SERVICE (Port 8081)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8081] IDENTITY-SERVICE && cd /d `"$rootDir\aromatics-platform\identity-service`" && ..\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 2

# 4. Catalog Service
Write-Host "[4/7] Dang khoi dong CATALOG-SERVICE (Port 8082 - Ho tro Gemini AI)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8082] CATALOG-SERVICE && cd /d `"$rootDir\aromatics-platform\catalog-service`" && ..\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 2

# 5. Order Service
Write-Host "[5/7] Dang khoi dong ORDER-SERVICE (Port 8083 - VNPay Gateway)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8083] ORDER-SERVICE && cd /d `"$rootDir\aromatics-platform\order-service`" && ..\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 2

# 6. Notification Service
Write-Host "[6/7] Dang khoi dong NOTIFICATION-SERVICE (Port 8084 - Kafka Consumer)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8084] NOTIFICATION-SERVICE && cd /d `"$rootDir\aromatics-platform\notification-service`" && ..\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 2

# 7. Frontend
Write-Host "[7/7] Dang khoi dong MOC-HUONG-FRONTEND (Next.js 14 - Port 3000)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [3000] MOC-HUONG-FRONTEND && cd /d `"$rootDir\moc-huong-frontend`" && npm run dev"

Write-Host ""
Write-Host "==============================================================" -ForegroundColor Green
Write-Host "  ✅ TOAN BO HE THONG DANG DUOC KHOI CHAY TRONG 7 CUA SO!       " -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Green
Write-Host "  - Frontend Cua Hang:     http://localhost:3000" -ForegroundColor White
Write-Host "  - Eureka Registry:       http://localhost:8761" -ForegroundColor White
Write-Host "  - API Gateway Tong:      http://localhost:8080" -ForegroundColor White
Write-Host "  - Swagger Catalog & AI:  http://localhost:8082/swagger-ui.html" -ForegroundColor White
Write-Host "==============================================================" -ForegroundColor Green
Write-Host "  * De tat toan bo he thong, nhap dup vao file: stop-all.bat   " -ForegroundColor Cyan
Write-Host ""
pause
