FROM amazoncorretto:17-alpine
COPY /target/*.jar test-docker.jar
ENTRYPOINT ["java","-jar","test-docker.jar"]