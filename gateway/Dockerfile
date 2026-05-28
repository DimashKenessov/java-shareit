FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]