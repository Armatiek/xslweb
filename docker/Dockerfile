# Use docker image containing Apache Tomcat version 10.0.14 and OpenJDK version 17.0.1+12: 
FROM tomcat:10.0.14-jre17-temurin

# Set the context path: this means XSLWeb wil run on http://localhost:8080/$CONTEXT_PATH where ROOT means http://localhost:8080 
ENV CONTEXT_PATH ROOT

ENV PORT 8080
ENV INSTALL_DIR /usr/local/xslweb
ENV XSLWEB_VERSION v4.2.1
ENV XSLWEB_VERSION_DIR $INSTALL_DIR/$XSLWEB_VERSION
ENV XSLWEB_FILE xslweb-war-$XSLWEB_VERSION.tar.gz
ENV XSLWEB_URL https://armatiek.nl/downloads/xslweb/$XSLWEB_FILE

# This directory needs to be mounted as a volume when starting the container, pointing to your local xslweb home dir
ENV CATALINA_OPTS "$CATALINA_OPTS -Dxslweb.home=$INSTALL_DIR/home -Dorg.terracotta.quartz.skipUpdateCheck=true"

# Expose port 8080
EXPOSE $PORT
RUN chmod +x /usr/local/tomcat/bin/catalina.sh

# Copy setenv.sh containing the classpath extension to the XSLWeb <xslweb-home>/config directory:
COPY setenv.sh /usr/local/tomcat/bin
RUN chmod +x /usr/local/tomcat/bin/setenv.sh

# Copy the context configuration containg the "jakartaConverter" converter definition (for the automatic javax.* to jakarta.* conversion needed for Tomcat's 10 Servlet API 5)
RUN mkdir -p /usr/local/tomcat/conf/Catalina/localhost
COPY context.xml /usr/local/tomcat/conf/Catalina/localhost/$CONTEXT_PATH.xml

RUN mkdir -p $XSLWEB_VERSION_DIR

RUN curl $XSLWEB_URL --output $INSTALL_DIR/$XSLWEB_FILE
RUN tar -xzvf $INSTALL_DIR/$XSLWEB_FILE -C $XSLWEB_VERSION_DIR
RUN cp $XSLWEB_VERSION_DIR/xslweb.war /usr/local/tomcat/webapps/$CONTEXT_PATH.war

CMD ["catalina.sh", "run"]