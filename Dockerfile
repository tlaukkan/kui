FROM anapsix/alpine-java
COPY white-ice-web-server/build/distributions/white-ice-web-server-1.0-SNAPSHOT.zip /opt/white-ice-web-server/
RUN cd /opt/white-ice-web-server/ && unzip -q white-ice-web-server-1.0-SNAPSHOT.zip

EXPOSE 8443
WORKDIR /opt/white-ice-web-server/white-ice-web-server-1.0-SNAPSHOT/
CMD java  -jar /opt/white-ice-web-server/white-ice-web-server-1.0-SNAPSHOT/white-ice-web-server-1.0-SNAPSHOT.jar


