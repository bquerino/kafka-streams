# Use the official OpenJDK 21 (slim version) as base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file (built by Maven or another build tool)
COPY target/bank-balance-1.0-SNAPSHOT.jar /app/app.jar

# Environment Variables
ENV APPLICATION_ID_CONFIG=bank-balance-app
ENV BOOTSTRAP_SERVERS_CONFIG=broker:29092
ENV AUTO_OFFSET_RESET_CONFIG=earliest
ENV CACHE_MAX_BYTES_BUFFERING_CONFIG=0

# Default command to run the application
CMD ["java", "-jar", "app.jar"]