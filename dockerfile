FROM openjdk:11

COPY . /app
WORKDIR /app

RUN javac TSPParallelBFS.java

CMD ["java", "TSPParallelBFS"]
