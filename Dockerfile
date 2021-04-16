FROM gradle:6.8.3-jdk15
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
RUN gradle clean build codeCoverageReport
