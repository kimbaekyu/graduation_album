FROM eclipse-temurin:17-jdk
ARG JAR_FILE=build/libs/graduation_album-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Spring: 8080, H2 TCP server: 9092
#EXPOSE 8080 9092

# MariaDB
EXPOSE 8080

# 도커파일이 도커엔진을 통해서 컨테이너로 올라갈 때,
# 도커 컨테이너의 시스템 진입점이 어디인지를 알려준다.
ENTRYPOINT ["java","-jar","app.jar"]