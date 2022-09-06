FROM eclipse-temurin:11-jdk-alpine as builder
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml file
COPY pom.xml .

# Fix permissions for mvnw executable
RUN chmod +x mvnw

# Download all dependecies
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY ./src ./src
COPY ./pom.xml ./pom.xml

RUN ./mvnw package -DskipTests -Dmaven.gitcommitid.skip=true

FROM tomcat:jre11-temurin

# Delete existing webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy builded application to the stage
COPY --from=builder /app/target/reschedule-tsu-spring.war /usr/local/tomcat/webapps/reschedule-tsu-spring.war

RUN mkdir -p /app/cache
RUN chmod ug+rw /app/cache
RUN chown www-data /app/cache

EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /usr/local/tomcat/webapps/reschedule-tsu-spring.war" ]