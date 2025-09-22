# Placeholder Dockerfile for DeviceRegistrationAPI
# Replace with actual build steps when the app is ready.
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package spring-boot:repackage

FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the repackaged Spring Boot fat jar explicitly (avoid copying any *.jar.original)
COPY --from=build /workspace/target/device-registration-api-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
EXPOSE 5005
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
