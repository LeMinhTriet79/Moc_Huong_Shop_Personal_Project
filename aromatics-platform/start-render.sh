#!/usr/bin/env bash
set -e

echo "🌿 [MỘC HƯƠNG RENDER ORCHESTRATOR] Đang khởi động Redis nội bộ..."
redis-server --daemonize yes --maxmemory 64mb --maxmemory-policy allkeys-lru || true

JVM_OPTS="-XX:+UseSerialGC -Xss256k -XX:TieredStopAtLevel=1 -Djava.security.egd=file:/dev/./urandom"
GATEWAY_PORT="${PORT:-8080}"

echo "🚀 [1/6] Khởi động Service Discovery (Eureka :8761)..."
java $JVM_OPTS -Xmx120m -Dserver.port=8761 -jar service-discovery.jar &
sleep 8

echo "🚀 [2/6] Khởi động Identity Service (:8081)..."
java $JVM_OPTS -Xmx160m -Dserver.port=8081 -jar identity-service.jar &

echo "🚀 [3/6] Khởi động Catalog Service (:8082)..."
java $JVM_OPTS -Xmx180m -Dserver.port=8082 -jar catalog-service.jar &

echo "🚀 [4/6] Khởi động Order Service (:8083)..."
java $JVM_OPTS -Xmx160m -Dserver.port=8083 -jar order-service.jar &

echo "🚀 [5/6] Khởi động Notification Service (:8084)..."
java $JVM_OPTS -Xmx120m -Dserver.port=8084 -jar notification-service.jar &

sleep 10
echo "🌐 [6/6] Khởi động API Gateway trên cổng Render PORT=${GATEWAY_PORT}..."
exec java $JVM_OPTS -Xmx160m -Dserver.port=${GATEWAY_PORT} -jar api-gateway.jar
