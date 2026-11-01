# ==============================================================================
# STAGE 1: BUILD TẤT CẢ 6 MICROSERVICES BẰNG MAVEN REACTOR
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

COPY aromatics-platform/pom.xml .
COPY aromatics-platform/service-discovery/pom.xml service-discovery/pom.xml
COPY aromatics-platform/api-gateway/pom.xml api-gateway/pom.xml
COPY aromatics-platform/identity-service/pom.xml identity-service/pom.xml
COPY aromatics-platform/catalog-service/pom.xml catalog-service/pom.xml
COPY aromatics-platform/order-service/pom.xml order-service/pom.xml
COPY aromatics-platform/notification-service/pom.xml notification-service/pom.xml

COPY aromatics-platform/service-discovery/src service-discovery/src
COPY aromatics-platform/api-gateway/src api-gateway/src
COPY aromatics-platform/identity-service/src identity-service/src
COPY aromatics-platform/catalog-service/src catalog-service/src
COPY aromatics-platform/order-service/src order-service/src
COPY aromatics-platform/notification-service/src notification-service/src

RUN mvn clean package -DskipTests -B

# ==============================================================================
# STAGE 2: ALL-IN-ONE MICROSERVICES ORCHESTRATOR CHO RENDER CLOUD
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache bash curl redis

COPY --from=build /build/service-discovery/target/*.jar ./service-discovery.jar
COPY --from=build /build/api-gateway/target/*.jar ./api-gateway.jar
COPY --from=build /build/identity-service/target/*.jar ./identity-service.jar
COPY --from=build /build/catalog-service/target/*.jar ./catalog-service.jar
COPY --from=build /build/order-service/target/*.jar ./order-service.jar
COPY --from=build /build/notification-service/target/*.jar ./notification-service.jar

COPY aromatics-platform/start-render.sh ./start-render.sh
RUN chmod +x ./start-render.sh

EXPOSE 8080
ENTRYPOINT ["./start-render.sh"]
