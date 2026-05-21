FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/install/server /app
EXPOSE 8080
ENTRYPOINT ["./bin/server"]
