FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="Mộc Hương Shop <contact@mochuong.vn>"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --chown=spring:spring ${JAR_FILE} app.jar

EXPOSE 8084
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
