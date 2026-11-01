$Host.UI.RawUI.WindowTitle = "MOC HUONG SHOP - DONG GOI VA CHAY NHANH"
Clear-Host

Write-Host "==============================================================" -ForegroundColor Yellow
Write-Host "       ⚡ MỘC HƯƠNG SHOP - ĐÓNG GÓI VÀ CHẠY SIÊU TỐC ⚡       " -ForegroundColor Yellow
Write-Host "==============================================================" -ForegroundColor Yellow
Write-Host ""

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "[1/2] Dang dong goi 6 Microservices thanh file .jar (bo qua test)..." -ForegroundColor Cyan
Set-Location "$rootDir\aromatics-platform"
& .\mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Dong goi that bai! Kiem tra lai ma nguon." -ForegroundColor Red
    pause
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "[2/2] Dang khoi dong truc tiep 6 file .jar va Next.js Frontend..." -ForegroundColor Cyan

# 1. Service Discovery
Write-Host "[1/7] Chay Service Discovery..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8761] EUREKA && cd /d `"$rootDir\aromatics-platform\service-discovery\target`" && java -jar service-discovery-0.0.1-SNAPSHOT.jar"

Write-Host "     Cho Eureka san sang (8 giay)..." -ForegroundColor DarkGray
Start-Sleep -Seconds 8

# 2. Gateway
Write-Host "[2/7] Chay API Gateway..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8080] GATEWAY && cd /d `"$rootDir\aromatics-platform\api-gateway\target`" && java -jar api-gateway-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# 3. Identity
Write-Host "[3/7] Chay Identity Service..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8081] IDENTITY && cd /d `"$rootDir\aromatics-platform\identity-service\target`" && java -jar identity-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# 4. Catalog
Write-Host "[4/7] Chay Catalog Service..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8082] CATALOG && cd /d `"$rootDir\aromatics-platform\catalog-service\target`" && java -jar catalog-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# 5. Order
Write-Host "[5/7] Chay Order Service..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8083] ORDER && cd /d `"$rootDir\aromatics-platform\order-service\target`" && java -jar order-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# 6. Notification
Write-Host "[6/7] Chay Notification Service..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [8084] NOTIFICATION && cd /d `"$rootDir\aromatics-platform\notification-service\target`" && java -jar notification-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# 7. Frontend
Write-Host "[7/7] Chay Frontend Next.js..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k title [3000] FRONTEND && cd /d `"$rootDir\moc-huong-frontend`" && npm run dev"

Write-Host ""
Write-Host "==============================================================" -ForegroundColor Green
Write-Host "  ✅ HE THONG DA DUOC KHOI DONG TRUC TIEP TU JAR THANH CONG!   " -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Green
Write-Host "  - Frontend: http://localhost:3000" -ForegroundColor White
Write-Host "  - Eureka:   http://localhost:8761" -ForegroundColor White
Write-Host "  - Gateway:  http://localhost:8080" -ForegroundColor White
Write-Host "==============================================================" -ForegroundColor Green
Write-Host ""
pause
