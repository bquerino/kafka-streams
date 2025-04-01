# Use the official OpenJDK 17 (slim version) as base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app
# Copy the jar file (built by Maven or another build tool)
COPY target/favourite-colour-v2-1.0-SNAPSHOT.jar /app/app.jar

# Environment Variables
ENV APPLICATION_ID_CONFIG=favourite-colour
ENV BOOTSTRAP_SERVERS_CONFIG=broker:29092
ENV AUTO_OFFSET_RESET_CONFIG=earliest

# Default command to run the application
CMD ["java", "-jar", "app.jar"]
