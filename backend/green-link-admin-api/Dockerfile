FROM eclipse-temurin:25-jre-noble
ENV TZ=Asia/Seoul
WORKDIR /app
COPY build/libs/green-link-admin-api.jar app.jar
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
