FROM openjdk:14
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
CMD sh test-script.sh
