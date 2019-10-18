# Use of Docker

Docker is a new tool for me and I am exploring ways it can be used to simplify building, testing, deployment, etc.

## What we currently do

- The application and its database are published as Docker images.  I use the **fabric8** `docker-maven-plugin` to build both images in the Maven *package* phase and to push them to Dockerhub in the *deploy* phase.  The images are [joeclark77/granite](https://cloud.docker.com/u/joeclark77/repository/docker/joeclark77/granite) and [joeclark77/granite-db](https://cloud.docker.com/u/joeclark77/repository/docker/joeclark77/granite-db).
  
  My intention is that the **granite** image would be used to deploy the app for local testing, CI testing, and in production.  I don't assume that the application's production database would be run in a Docker container, but the **granite-db** image can be used both in automated testing and to spin up a temporary database for fiddling around on a developer's workstation.

- I use the [**Testcontainers**](https://www.testcontainers.org/) library to spin up database instances in containers during automated testing.  More details on how to write these tests can be found under [Testing with the Database](https://github.com/joeclark-phd/granite/tree/master/database#testing-with-the-database).

## What I have tried but didn't keep

- For automated testing with Docker, my first approach was to use the **fabric8** `docker-maven-plugin` to create a Docker instance of the database for Maven's *integration-test* phase.  You can see how this was done by inspecting pom.xml and other files at [commit e887d66](https://github.com/joeclark-phd/granite/tree/e887d661a232d7b4d0b7071adf6dbba63454789a).  I switched to using Testcontainers for a few reasons:
  - The Testcontainers approach puts its code and configuration all in the same place--the test class--rather than splitting it between application.yaml, pom.xml, and the test class.  It's more verbose and requires more copy-pasting, but is also more transparent.
  - The integration tests in the first approach could only be run by Maven, whereas the Testcontainers-based tests can be run in my IDE.
  - I wanted to use the `docker-maven-plugin` to build the project itself, and it didn't seem to support building one image in one phase and another image in another phase.

- I tried to run Maven itself from a docker image, so I could compile the app with a Java version I didn't have (say, Java 11 or 13 or the latest experimental build).  This would also make it easier to set up a continuous integration server, since we wouldn't have to manually update its Java and Maven versions... all it would need would be Docker.  The Dockerfile looked something like this:

    ```
    FROM maven:3.6.2-jdk-11 AS MAVEN_BUILD
    COPY pom.xml /tmp/
    COPY src /tmp/src
    COPY database /tmp/database
    WORKDIR /tmp/
    RUN mvn package
    
    FROM openjdk:11-jdk-alpine
    VOLUME /tmp
    
    COPY --from=MAVEN_BUILD /tmp/target/dependency /app/lib
    COPY --from=MAVEN_BUILD /tmp/target/classes /app
    
    ENTRYPOINT ["java","-cp","app:app/lib/*","net.joeclark.webapps.granite.Application"]
    ```

  It failed because the Maven image (`maven:3.6.2-jdk-11`) couldn't run the Testcontainers tests -- that is, it didn't have a Docker daemon inside the Docker image.  This could be overcome by creating a custom container with Docker and Maven to do the first stage, or somehow giving the container access to the Docker daemon on the host machine.  Or by removing the Testcontainers tests.

- I also tried the approach of running Maven from a container interactively, giving it access to my working directory.  This would allow me to specify the Maven and Java versions for compile/build, while storing the intermediate files on my machine, an acceptable tradeoff.  The command was like this:

    ```
    docker run -it --rm --name my-maven-project -v %cd%:/usr/src/mymaven -w /usr/src/mymaven maven:3.6.2-jdk-11 mvn clean package
    ```
  
  It didn't work because my workstation refused to share access to the C drive.  That may just be one workstation's problem.  (Note that on UNIX you'd replace `%cd%` with `$(pwd)`.)
