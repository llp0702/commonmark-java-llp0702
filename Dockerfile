FROM centos:8
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
RUN sh install-requirements.sh
CMD sh test-script.sh
