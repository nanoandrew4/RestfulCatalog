FROM openjdk:8
ADD restfulcatalog.jar restfulcatalog.jar
ADD init.sql /docker-entrypoint-initdb.d
RUN bash -c 'touch /restfulcatalog.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=container","-jar","/restfulcatalog.jar"]