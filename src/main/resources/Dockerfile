FROM ubuntu:latest

RUN mkdir -p /opt/actor-generator/conf/

ADD actor-kafka-kudu.sh /opt/actor-generator/actor-kafka-kudu.sh
ADD actorgen_2.11-1.0.jar /opt/actor-generator/actorgen_2.11-1.0.jar
ADD kudu.avsc kudufeeder.conf logback.xml /opt/actor-generator/conf/

RUN ln -s /opt/actor-generator/actor-kafka-kudu.sh /etc/init.d/actor-kafka-kudu

#RUN apt-get install add-apt-repository
#RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update
RUN apt-get -y install vim
RUN apt-get -y install scala
RUN apt-get -y install openjdk-8-jre
RUN chmod +x /etc/init.d/actor-kafka-kudu

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/jre/
CMD /bin/bash