FROM jenkins/jenkins:lts

MAINTAINER Rion Dooley<deardooley@gmail.com>

# Install jq and Agave CLI
USER root
RUN apt-get update && apt-get install -y curl openssl && \
    cd /usr/local && \
    wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 && \
    mv /usr/local/jq-linux64 /usr/local/bin/jq && \
    chmod +x /usr/local/bin/jq

# Install Agave CLI and init default tenant
RUN git clone -b develop --depth  1  https://github.com/agaveplatform/agave-cli.git /usr/local/share/agave-cli

#COPY jenkins_home.zip /usr/share/jenkins/jenkins_home.zip

#RUN cd /usr/share/jenkins && \
#    unzip jenkins_home.zip && \
#    rm -f jenkins_home.zip && \
#    rm -rf jenkins_home/__MACOSX && \
#    cp -rf jenkins_home/* ref/ && \
#    chown -R jenkins:jenkins ref  && \
#    rm -rf jenkins_home

# Drop back to the regular jenkins user - good practice
USER jenkins

ENV PATH $PATH:/usr/local/jq/bin:/usr/local/share/agave-cli/bin
ENV AGAVE_JSON_PARSER jq
ENV AGAVE_CLI_COMPLETION_SHOW_FILES no
ENV AGAVE_CLI_COMPLETION_SHOW_FILE_PATHS no
ENV AGAVE_CLI_COMPLETION_CACHE_LIFETIME 0

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt.override
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt.override

ENV JAVA_OPTS "-Duser.home=/var/jenkins_home -Djenkins.install.runSetupWizard=false "
ENV JENKINS_OPTS "--prefix=/jenkins"

COPY docker_entrypoint.sh /docker_entrypoint.sh

COPY init.groovy.d/executors.groovy /usr/share/jenkins/ref/init.groovy.d/executors.groovy
COPY init.groovy.d/setup-users.groovy /usr/share/jenkins/ref/init.groovy.d/setup-users.groovy

ENTRYPOINT ["/sbin/tini", "--", "/docker_entrypoint.sh"]

CMD ["/usr/local/bin/jenkins.sh"]

