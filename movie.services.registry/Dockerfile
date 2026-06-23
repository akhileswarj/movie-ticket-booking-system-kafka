FROM eclipse-temurin:17-alpine
COPY target/movie.services.registry-1.0-SNAPSHOT.jar registry.jar
ENTRYPOINT ["java", "-jar", "/registry.jar"]