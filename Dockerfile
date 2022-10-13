FROM openjdk:17-jdk-bullseye
COPY . /var/www/java
WORKDIR /var/www/java
ENV CLASSPATH=/var/www/java/conjur-api-3.0.3-SNAPSHOT-with-dependencies.jar:${CLASSPATH}
ENV CLASSPATH=/var/www/java/jdbc-postgresql.jar:${CLASSPATH}
RUN chmod 777 -R /var/www/java
RUN javac testapp.java
CMD ["java", "testapp"]
