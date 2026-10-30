# Multi-stage or optimized runtime container
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built jar from target directory
COPY target/smart-parking-management-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
