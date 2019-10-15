FROM openjdk:8-jdk-alpine
VOLUME /tmp

# copy dependencies into image; this will change less often so we may get some benefit from the Docker cache here
COPY target/dependency /app/lib

# copy the application sources; this will be the layer that probably changes every time
COPY target/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","net.joeclark.webapps.granite.Application"]
