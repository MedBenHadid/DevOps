FROM openjdk:17
ADD /target/timesheet-*.jar docker-client.jar
EXPOSE 9092
ENTRYPOINT ["java","-jar","docker-client.jar"]