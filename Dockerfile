FROM openjdk:11-jre-slim
WORKDIR /project
COPY target/release/ /project/
COPY start.sh /project/
CMD ["/bin/sh", "/project/start.sh", "-f"]
