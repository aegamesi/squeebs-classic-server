FROM openjdk:8-jdk-alpine
COPY . /app
WORKDIR /app
RUN ./gradlew && ./gradlew stage && rm -rf /root/.gradle
ENTRYPOINT java -jar /app/build/libs/app.jar $DB_PATH
