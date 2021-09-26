FROM fintlabsacr.azurecr.io/betaling-frontend:latest as client

FROM gradle:7.2.0-jdk11 as builder
USER root
COPY . .
COPY --from=client /src/build/ src/main/resources/static/
RUN gradle --no-daemon build

FROM gcr.io/distroless/java:11
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-betaling-*.jar /data/fint-betaling.jar
CMD ["/data/fint-betaling.jar"]
