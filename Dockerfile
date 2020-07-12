FROM openjdk:11-jdk-slim
MAINTAINER  Karol Meksuła <meksula.karol.93@gmail.com>

ARG APP_NAME=easeci
ARG APP_VERSION=0.0.1-SNAPSHOT
ARG COMPILATION_DIR=/usr/src/${APP_NAME}
ARG JAR_DIR=/usr/lib/${APP_NAME}
ARG JAR_NAME=easeci-core-${APP_VERSION}-all.jar
ARG JAR_FULL_PATH=${JAR_DIR}/${JAR_NAME}

RUN apt-get update \
 && apt-get install -y procps git net-tools curl

# Main directory for EaseCI files
RUN mkdir -p ${JAR_DIR} \
 && mkdir -p ${COMPILATION_DIR}

RUN git clone --depth=50 --branch=master https://github.com/easeci/easeci-core-java.git ${COMPILATION_DIR}

# Compile source code and move .jar file to destination
WORKDIR ${COMPILATION_DIR}
RUN ./gradlew build -x test
RUN cp $(pwd)/build/libs/easeci-core-${APP_VERSION}-all.jar ${JAR_DIR}

EXPOSE 5050
WORKDIR ${JAR_DIR}
# rename jar to easeci-core.jar
RUN mv easeci-core-${APP_VERSION}-all.jar easeci-core.jar

# cleanup after compilation
RUN rm -rf /usr/src/easeci/

CMD ["java", "-jar", "easeci-core.jar"]
