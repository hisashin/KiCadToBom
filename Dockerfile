FROM maven:latest

COPY . .
RUN mvn package
CMD ["java","-jar","simple/target/simple-1.0-SNAPSHOT.jar"]
