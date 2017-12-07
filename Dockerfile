FROM anapsix/alpine-java
COPY kui-web/build/distributions/kui-web-1.0-SNAPSHOT.zip /opt/kui-web/
RUN cd /opt/kui-web/ && unzip -q kui-web-1.0-SNAPSHOT.zip

EXPOSE 8443
WORKDIR /opt/kui-web/kui-web-1.0-SNAPSHOT/
CMD java  -jar /opt/kui-web/kui-web-1.0-SNAPSHOT/kui-web-1.0-SNAPSHOT.jar


