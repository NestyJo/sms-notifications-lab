# ---------- build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy sources and build the runnable jar
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the Spring Boot fat jar produced above
COPY --from=build /app/target/notification-lab-service.jar app.jar

# Default runtime configuration
ENV SPRING_PROFILES_ACTIVE=default \
    SERVER_PORT=9091 \
    DB_HOST=192.168.30.246 \
    DB_PORT=3306 \
    DB_NAME=labnotifications \
    DB_USERNAME=ernest \
    DB_PASSWORD=ernest_12345678 \
    LAB_ORDERS_BASE_URL=http://192.168.235.250/labsms/swagger/labtestresults \
    LAB_ORDERS_SOURCE=remote

EXPOSE 9091

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
