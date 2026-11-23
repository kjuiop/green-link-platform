FROM eclipse-temurin:25-jre-noble
ENV TZ=Asia/Seoul
WORKDIR /app
COPY build/libs/green-shop-api.jar app.jar
ENTRYPOINT ["java", "-Xmx192m", "-XX:MaxMetaspaceSize=128m", "-jar", "app.jar"]
