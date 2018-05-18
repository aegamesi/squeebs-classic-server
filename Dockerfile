FROM openjdk:8-jdk-alpine
COPY . /app
WORKDIR /app
RUN ./gradlew
RUN ./gradlew fatJar
ENTRYPOINT java -jar build/libs/app-standalone.jar $DB_PATH
