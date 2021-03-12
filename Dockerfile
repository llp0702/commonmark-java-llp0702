FROM gradle:6.8.2-jdk15 AS BUILD
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon
RUN mv build/distributions/ssg-1.0-SNAPSHOT.tar