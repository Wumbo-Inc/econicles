# Stage 1: Build the app using Maven
FROM maven:3.8.5-openjdk-18 AS build
WORKDIR /build
# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src
# Build the jar (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Create a slim runtime image
FROM openjdk:18-jdk-slim
WORKDIR /app

# Copy the fat jar from the build stage.
COPY --from=build /build/target/econicles-1.1-SNAPSHOT-jar-with-dependencies.jar /app/econicles.jar

# Copy the production SQLite database file.
# In production mode the app expects "main.sqlite3" in the working directory.
COPY src/main/resources/ /app/

# Expose port 80 (the app will run on 80 in production)
EXPOSE 80

# Use the entrypoint to run the application
ENTRYPOINT ["java -jar /app/econicles.jar"]
