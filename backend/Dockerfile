FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/contact-backend-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
