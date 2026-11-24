FROM gradle:9.9-jdk17 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM eclipse-temurin:25-jre
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-betaling-backend-*.jar /data/app.jar
ENTRYPOINT ["java", "-jar", "/data/app.jar"]