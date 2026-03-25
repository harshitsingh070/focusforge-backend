FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . .

RUN ./mvnw clean package

CMD java -jar target/*.jar