FROM openjdk:11
FROM maven:3.8-openjdk-11
ARG JAR_FILE=./target/diff-api.jar
ADD ${JAR_FILE} app.jar
ADD config.yaml config.yaml
ENTRYPOINT ["java","-jar","/app.jar"]
