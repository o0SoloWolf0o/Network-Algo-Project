FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY MyApp.java .

RUN javac MyApp.java

CMD ["java", "MyApp", "server", "1"]
