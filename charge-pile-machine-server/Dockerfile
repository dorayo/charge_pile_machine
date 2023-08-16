FROM amazoncorretto:8u382
USER root
WORKDIR /opt/app

ARG JAR_NAME=application
ARG JAR_FILE=target/${JAR_NAME}.jar

ENV TERM=dumb

COPY ${JAR_FILE} application.jar
COPY docker-entrypoint.sh docker-entrypoint.sh

EXPOSE 8881
EXPOSE 8886

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx1024m -Dspring.profiles.active=default,dev,linux -Dspring.devtools.restart.enabled=false"

ENTRYPOINT ["/bin/bash","docker-entrypoint.sh"]