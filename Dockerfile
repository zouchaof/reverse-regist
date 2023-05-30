FROM java:8u45-jre
MAINTAINER chaozou
#EXPOSE 9090
RUN mkdir /usr/local/register
COPY register-server-web/target/register-server-web.jar /usr/local/register
ENTRYPOINT ["java","-jar","/usr/local/register/register-server-web.jar"]
