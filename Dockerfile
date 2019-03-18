FROM fintlabs.azurecr.io/fordring:latest as client

FROM gradle:4.10.2-jdk8-alpine as builder
USER root
COPY . .
COPY --from=client /src/build/ src/main/resources/static/
RUN gradle --no-daemon build

FROM gcr.io/distroless/java
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fint-betaling-*.jar /data/fint-betaling.jar
CMD ["/data/fint-betaling.jar"]
