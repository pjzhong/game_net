FROM openjdk:11-jre-slim as builder
WORKDIR /project
ARG JAR_FILE=target/game-net-0.1.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:11-jre-slim
WORKDIR /project
COPY --from=builder project/dependencies/ ./
COPY --from=builder project/spring-boot-loader/ ./
COPY --from=builder project/snapshot-dependencies/ ./
COPY --from=builder project/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]