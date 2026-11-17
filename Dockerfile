FROM gradle:8.5-jdk17 as builder
USER root
COPY . .
RUN gradle -x test --no-daemon build

FROM eclipse-temurin:17-jre
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-betaling-backend-*.jar /data/app.jar
ENTRYPOINT ["java", "-jar", "/data/app.jar"]