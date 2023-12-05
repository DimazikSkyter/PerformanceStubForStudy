FROM openjdk:8-jre-alpine

WORKDIR /app

COPY build/libs/your-connector.jar /app/your-connector.jar
COPY connect-standalone.properties /app/connect-standalone.properties

CMD ["./bin/connect-standalone.sh", "connect-standalone.properties"]
