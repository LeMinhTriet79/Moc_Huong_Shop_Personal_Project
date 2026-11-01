#!/usr/bin/env bash
set -e

GATEWAY_PORT="${PORT:-8080}"
echo "🌿 [MỘC HƯƠNG RENDER ORCHESTRATOR] Khởi động trên Render PORT=${GATEWAY_PORT}..."

# 1. Khởi động Redis nội bộ
redis-server --daemonize yes --maxmemory 32mb --maxmemory-policy allkeys-lru || true

# JVM Options tối ưu cho Render Free Tier (512MB RAM / 0.1 vCPU)
JVM_COMMON="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:CICompilerCount=1 -Xss256k -Dspring.main.lazy-initialization=true -Dspring.jmx.enabled=false -Djava.security.egd=file:/dev/./urandom"

# 2. Khởi động API Gateway TRƯỚC TIÊN để mở cổng PORT ngay lập tức (trong 5 giây)
echo "🌐 [1/6] Khởi động API Gateway trên cổng Render PORT=${GATEWAY_PORT}..."
java $JVM_COMMON -Xmx90m -Dserver.port=${GATEWAY_PORT} -jar api-gateway.jar &
GATEWAY_PID=$!

# Đợi 5 giây để API Gateway bind port trước
sleep 5

# 3. Khởi động Service Discovery (Eureka)
echo "🚀 [2/6] Khởi động Service Discovery (Eureka :8761)..."
java $JVM_COMMON -Xmx70m -Dserver.port=8761 -jar service-discovery.jar &
sleep 4

# 4. Khởi động các Microservices nghiệp vụ
echo "🚀 [3/6] Khởi động Identity Service (:8081)..."
java $JVM_COMMON -Xmx90m -Dserver.port=8081 -jar identity-service.jar &

echo "🚀 [4/6] Khởi động Catalog Service (:8082)..."
java $JVM_COMMON -Xmx110m -Dserver.port=8082 -jar catalog-service.jar &

echo "🚀 [5/6] Khởi động Order Service (:8083)..."
java $JVM_COMMON -Xmx90m -Dserver.port=8083 -jar order-service.jar &

echo "🚀 [6/6] Khởi động Notification Service (:8084)..."
java $JVM_COMMON -Xmx70m -Dserver.port=8084 -jar notification-service.jar &

echo "✨ [MỘC HƯƠNG PLATFORM] Toàn bộ 6 Microservices đã được kích hoạt thành công!"

# Giữ container chạy theo tiến trình API Gateway
wait $GATEWAY_PID
