# ==============================================================================
# STAGE 1: MAVEN BUILD (Tự động biên dịch .jar khi deploy từ GitHub lên Render)
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# STAGE 2: PRODUCTION RUNTIME (Alpine JRE 21 siêu nhẹ & tối ưu RAM cho Render)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="Mộc Hương Shop <contact@mochuong.vn>"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build --chown=spring:spring /workspace/target/*.jar app.jar

EXPOSE 8083
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=72.0 -XX:+UseSerialGC -Xss256k -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["java", "-jar", "app.jar"]
