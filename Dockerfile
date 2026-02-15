# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS deps
WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline

FROM maven:3.9.9-eclipse-temurin-17 AS test-runner
WORKDIR /workspace
COPY --from=deps /root/.m2 /root/.m2
COPY . .

# Default to deterministic unit tests. Override SUITE_XML for API/UI suites.
ENV SUITE_XML=testng-unit.xml
CMD ["sh", "-c", "mvn -B clean test -Dsurefire.suiteXmlFiles=${SUITE_XML}"]
