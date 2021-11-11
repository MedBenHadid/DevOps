#FROM maven:3.8.3-openjdk-17
#WORKDIR /app
#COPY . /app
##RUN mvn dependency:go-offline
##RUN --mount=type=cache,target=/root/.m2 mvn -f /app/pom.xml clean package
#RUN mvn clean install
#EXPOSE 9090
#ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar", "/app/target/timesheet-1.0.jar"]

#FROM adoptopenjdk:16-jre-hotspot as builder
 #WORKDIR app
 #ARG JAR_FILE=target/*.jar
 #COPY ${JAR_FILE} application.jar
 #RUN java -Djarmode=layertools -jar application.jar extract
 #FROM adoptopenjdk:16-jre-hotspot
 #WORKDIR app
 #COPY --from=builder app/dependencies/ ./
 #COPY --from=builder app/snapshot-dependencies/ ./
 #COPY --from=builder app/resources/ ./
 #COPY --from=builder app/application/ ./
 #EXPOSE 9090
 #ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]

FROM openjdk:17
ADD /target/timesheet-*.jar docker-client.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","docker-client.jar"]