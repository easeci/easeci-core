FROM openjdk:17-jdk-slim as build
MAINTAINER  Karol Meksuła <meksula.karol.93@gmail.com>

ARG APP_NAME=easeci
ARG APP_VERSION=0.0.1-SNAPSHOT
ARG COMPILATION_DIR=/usr/src/${APP_NAME}
ARG JAR_DIR=/usr/lib/${APP_NAME}
ARG JAR_NAME=easeci-core-${APP_VERSION}-all.jar
ARG JAR_FULL_PATH=${JAR_DIR}/${JAR_NAME}

# Compile source code and move .jar file to destination
COPY . ${COMPILATION_DIR}
WORKDIR ${COMPILATION_DIR}

RUN ./gradlew build -x test

FROM openjdk:17-jdk-slim as runtime
ARG APP_NAME=easeci
ARG APP_VERSION=0.0.1-SNAPSHOT
ARG COMPILATION_DIR=/usr/src/easeci
ARG WORKDIR=$/usr/src/easeci/build/libs/
ARG EXECUTABLE_ARTIFACT=/usr/src/easeci/build/libs/easeci-core-0.0.1-SNAPSHOT-all.jar

EXPOSE 9000

WORKDIR /opt/app
COPY --from=0 ${EXECUTABLE_ARTIFACT} .
RUN mv easeci-core-0.0.1-SNAPSHOT-all.jar easeci-core.jar

ENTRYPOINT ["java", "-jar", "/opt/app/easeci-core.jar"]
