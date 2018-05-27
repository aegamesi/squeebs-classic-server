FROM openjdk:8-jdk-alpine
COPY . /app
WORKDIR /app
RUN ./gradlew && ./gradlew fatJar && rm -rf /root/.gradle
ENTRYPOINT java -jar build/libs/app-standalone.jar $DB_PATH
