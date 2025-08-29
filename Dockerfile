FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/TIL-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]