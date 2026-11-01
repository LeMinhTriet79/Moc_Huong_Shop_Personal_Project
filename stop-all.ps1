$Host.UI.RawUI.WindowTitle = "MOC HUONG SHOP - DUNG HE THONG"
Clear-Host

Write-Host "==============================================================" -ForegroundColor Red
Write-Host "        🛑 DỪNG TOÀN BỘ MICROSERVICES & FRONTEND 🛑          " -ForegroundColor Yellow
Write-Host "==============================================================" -ForegroundColor Red
Write-Host ""

$ports = @(8761, 8080, 8081, 8082, 8083, 8084, 3000)

foreach ($port in $ports) {
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conns) {
            foreach ($conn in $conns) {
                $pidToKill = $conn.OwningProcess
                if ($pidToKill -gt 0) {
                    $pName = (Get-Process -Id $pidToKill -ErrorAction SilentlyContinue).ProcessName
                    Write-Host "[KILL] Dong tien trinh $pName (PID $pidToKill) tren Port $port..." -ForegroundColor Yellow
                    Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
                }
            }
        }
    } catch {
        # ignore errors
    }
}

Write-Host ""
Write-Host "==============================================================" -ForegroundColor Green
Write-Host "  ✅ ĐÃ DỪNG TOÀN BỘ VÀ GIẢI PHÓNG TOÀN BỘ CỔNG MẠNG THÀNH CÔNG!" -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Green
Write-Host ""
pause
