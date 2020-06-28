#! /bin/sh
PARAMS="$@"

git pull

docker run -it \
           --rm  \
           --network game_network \
           -v $(pwd):/project \
           -v /opt/repository/maven:/root/.m2/repository \
           pjzhong/maven-3.6-jdk-11-aliyun mvn $PARAMS

