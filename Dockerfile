# syntax=docker/dockerfile:1

# ---- build stage: compile the application and build the jar ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

# Copy the Gradle wrapper and build files first so Docker can cache the
# dependency download layer and reuse it when only the sources change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon downloadDependencies

# Copy the sources and build only the jar (the CI job already runs the tests).
COPY src ./src
RUN ./gradlew --no-daemon jar

# ---- runtime stage: keep only the JRE, the jar and a sample file ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Run as an unprivileged user instead of root.
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /src/build/libs/word-stats.jar ./word-stats.jar
COPY samples/sample.txt ./sample.txt
USER app

# `docker run <image>` analyses the bundled sample; pass another file (for
# example with `-v`) and flags after the image name to override the default.
ENTRYPOINT ["java", "-jar", "/app/word-stats.jar"]
CMD ["/app/sample.txt", "--top", "5"]
