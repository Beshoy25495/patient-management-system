# ===== STAGE 1: Build =====

## build image using maven as a base image (builder) that will contain the maven dependencies and java runtime environment (JDK 21)
FROM maven:3.9.9-eclipse-temurin-21 AS builder

## set the working directory inside the container
WORKDIR /app

## copy the pom.xml and the src directory to the builder image (for caching)
COPY pom.xml .

## build and install all the dependencies that are required to build the application (pom.xml)
## dependency:go-offline: this means caching the dependencies in the local repository unless a change is detected in the pom.xml
## -B flag to run in batch mode
RUN mvn dependency:go-offline -B

## copy the src directory to the builder image (for caching)
COPY src ./src

RUN mvn clean package

# ===== STAGE 2: Run =====

# create a new image using the java runtime environment (JDK 21) as a base image
FROM openjdk:21-jdk As runner

## set the working directory inside the container
WORKDIR /app

## copy the built jar file from the builder image to the runner image
COPY --from=builder ./app/target/patient-service-0.0.1-SNAPSHOT.jar ./app.jar

## expose the port that the application is running on
EXPOSE 4000

## run the application
ENTRYPOINT ["java", "-jar", "app.jar"]




