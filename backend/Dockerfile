# Start with a base image that includes JDK 17 for building the application
FROM openjdk:17-slim as build
WORKDIR /workspace/app

# Copy Maven wrapper and pom.xml files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Debug: List files in the working directory
RUN ls -la
RUN ls -la .mvn

# Copy the source code of your application
COPY src src

# Install dependencies and build the application without running tests
# Run Maven Wrapper to build the project
RUN chmod +x ./mvnw && ./mvnw install -DskipTests -e -X

# Use a base image with JRE 17 for running the application
FROM openjdk:17-slim
VOLUME /tmp
# Copy the built jar file from the build stage
COPY --from=build /workspace/app/target/*.jar app.jar
# Copy the keycloak_users.csv file
COPY keycloak_users.csv /workspace/app/keycloak_users.csv
# Set the entrypoint to run the jar with increased heap size and JMX options
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dcom.sun.management.jmxremote=true","-Dcom.sun.management.jmxremote.port=9010","-Dcom.sun.management.jmxremote.rmi.port=9011","-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.ssl=false","-Djava.rmi.server.hostname=0.0.0.0","-Xms4g","-Xmx8g","-XX:MaxMetaspaceSize=512m","-XX:+UseG1GC","-XX:MaxGCPauseMillis=100","-XX:G1ReservePercent=20","-jar","/app.jar"]

