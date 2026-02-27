# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew && \
    ./gradlew bootJar -x test && \
    cp $(find build/libs -name "*.jar" ! -name "*plain*") /workspace/app.jar

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
