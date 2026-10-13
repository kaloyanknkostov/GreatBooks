# ---- Build stage: compile the Spring Boot fat jar with the Gradle wrapper ----
FROM docker.io/library/eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy the wrapper and build scripts first so dependency resolution is cached
# until one of these files changes.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null

COPY src ./src
RUN ./gradlew --no-daemon bootJar \
    && cp build/libs/*-SNAPSHOT.jar /workspace/app.jar

# ---- Runtime stage: JRE only, runs as a non-root user ----
FROM docker.io/library/eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --home /app app
COPY --from=build --chown=app:app /workspace/app.jar ./app.jar
USER app

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
