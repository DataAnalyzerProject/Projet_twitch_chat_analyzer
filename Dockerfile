# Use an official Java runtime as the base image
FROM ubuntu:18.04

COPY . /app
WORKDIR /app
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y ant && \
    apt-get clean;
RUN ./mvnw package
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/target/Main-0.0.1-SNAPSHOT.jar"]
