# Use the official Gradle image to create a build stage
FROM gradle:8.8-jdk21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and project files
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Copy the source code
COPY src/ src/

# Build the application
RUN ./gradlew build -x test

# Use the official OpenJDK image for the runtime
FROM openjdk:21-jdk-slim

# Install FFmpeg
RUN apt-get update && apt-get install -y ffmpeg

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Copy the service account key file
COPY service-account-file.json /app/service-account-file.json

# Set the environment variable for Google Cloud credentials
ENV GOOGLE_APPLICATION_CREDENTIALS="/app/service-account-file.json"

# Set the entry point to run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
