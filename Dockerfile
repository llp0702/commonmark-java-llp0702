FROM gradle:6.8.2-jdk15 AS BUILD
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon
COPY build/distributions/ssg-1.0-SNAPSHOT.tar .
RUN tar -xvf ssg-1.0-SNAPSHOT.tar
CMD sh ssg-1.0-SNAPSHOT/bin/ssg --h