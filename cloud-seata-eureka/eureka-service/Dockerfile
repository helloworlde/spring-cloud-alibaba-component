FROM openjdk:8-jdk-alpine
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
VOLUME /tmp
ADD "build/libs/eureka-service-0.0.1-SNAPSHOT.jar" app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=GMT+08", "-jar","/app.jar"]

EXPOSE 8761