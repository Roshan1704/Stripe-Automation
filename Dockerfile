FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app
COPY . /app

RUN mvn -B -DskipTests clean compile

CMD ["mvn", "-B", "-Dheadless=true", "test"]
