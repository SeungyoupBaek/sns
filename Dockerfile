FROM openjdk:11-jdk
ARG JAR_FILE=build/libs/*.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=local","/app.jar"]