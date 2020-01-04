FROM openjdk:11-jre

COPY ./target/scala-2.13/pileworx-workshop-team-service-assembly-*.jar /app/libs/pileworx-workshop-team.jar
COPY ./start.sh /app/libs/start.sh

EXPOSE 8080
ENTRYPOINT ["/app/libs/start.sh"]