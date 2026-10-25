FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY green-link-core green-link-core
COPY green-link-admin-api green-link-admin-api
RUN ./gradlew :green-link-admin-api:bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build /app/green-link-admin-api/build/libs/green-link-admin-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
