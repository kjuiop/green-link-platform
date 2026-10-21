FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY build/libs/green-link-admin-api.jar app.jar
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
