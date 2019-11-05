# Use of Docker

Docker is a new tool for me and I am exploring ways it can be used to simplify building, testing, deployment, etc.  There are three main ways I want to be able to run the app:

1. Running the classes from my IDE, not from a container, and connecting to a database running on localhost.  Database connection info is stored in a configuration file such as `application-dev.yml`.
2. Running the app in a container, connecting to a database of my choice, passing in the connection info via environment variables in the `docker run` commmand.  The database may live in a container or may not, and the app shouldn't need to know.  This can be used for local testing or for production deployments, since the secret connection info is never stored in source code.
3. Spinning up a fresh database and a fresh app in containers at the same time; this would be for testing and continuous integration.

## Quick Start

### Spin up an instance of the database with test data on your local machine

This is necessary for the first two usage cases listed above.

    docker container run -d -p 5432:5432 --name myGraniteDB joeclark77/granite-db:latest
    
By the way, if you want to log into the database console with `psql` to do some customization or just take a look around, do this:

    docker exec -it myGraniteDB psql -U granite

### Launch the app from an IDE

In IntelliJ IDEA, create a **Run Configuration** that identifies `net.joeclark.webapps.granite.Application` as the **main class**, and adds this **program argument**: `--spring.profiles.active=dev`.  That'll tell it to load the connection info from `application-dev.yml` which points to a database at `localhost:5432`.

Now you can point your browser at localhost:8080 and use the application.  Two users are initialized by default: `admin` with password `super` and SUPER privileges, and `joe` with password `pass` and AGENT privileges.

### Launch a containerized instance of the app

You tell the container where the database is by passing in environment variables.  In this case I'm connecting to my local instance.  For Docker, "`localhost`" refers to the container, not the host machine, so you substitute "**`host.docker.internal`**" to point a URL at the host machine's ports:

    docker container run -p 8080:8080
      -e POSTGRES_DB_URL='jdbc:postgresql://host.docker.internal:5432/granite' 
      -e POSTGRES_DB_USERNAME='granite' 
      -e POSTGRES_DB_PASSWORD='test' 
      joeclark77/granite:latest

As above, you can point the browser at localhost:8080 to log in.  Or alter the `-p` argument to publish the app on a different port, for example `-p 80:8080` will allow you to access the app at localhost:80.

### Launch fresh containers of the database and app together (using Docker-Compose)

Navigate to the root directory of this project and enter:

    docker-compose up
    
That's it!

Docker-Compose uses the file `docker-compose.yml` for its instructions (so that's where you'd change the port mapping), and will run the app using the "compose" profile (see `application-compose.yml` to configure it.)

### Launch fresh containers of the database and app together (manually - deprecated)

First, create a network for the database container and application container to be able to see each other.  You only need to do this once:

    docker network create -d bridge --subnet 192.168.0.0/24 --gateway 192.168.0.1 mynet
    
Then, spin up a database container in the custom network:

    docker container run -d --net=mynet --name myGraniteDB joeclark77/granite-db:latest
    
Now, spin up the application in a container:

    docker container run --net=mynet -p 8080:8080
      -e SPRING_PROFILES_ACTIVE='dockernet' 
      joeclark77/granite:latest

This too can be accessed at localhost:8080, or you can publish it to a different port, as noted above.

**Note:** I think the Docker-Compose method (above) is a better way to accomplish the same thing.  These instructions and the `dockernet` profile are preserved because this info might be handy for some future situations.

## Current uses of Docker

- The application and its database are published as Docker images.  I use the **fabric8** `docker-maven-plugin` to build both images in the Maven *package* phase and to push them to Dockerhub in the *deploy* phase.  The images are [joeclark77/granite](https://cloud.docker.com/u/joeclark77/repository/docker/joeclark77/granite) and [joeclark77/granite-db](https://cloud.docker.com/u/joeclark77/repository/docker/joeclark77/granite-db).
  
  My intention is that the **granite** image would be used to deploy the app for local testing, CI testing, and in production.  I don't assume that the application's production database would be run in a Docker container, but the **granite-db** image can be used both in automated testing and to spin up a temporary database for fiddling around on a developer's workstation.

- I use the [**Testcontainers**](https://www.testcontainers.org/) library to spin up database instances in containers during automated testing.  More details on how to write these tests can be found under [Testing with the Database](https://github.com/joeclark-phd/granite/tree/master/database#testing-with-the-database).

## Other things I have tried but didn't keep

- For automated testing with Docker, my first approach was to use the **fabric8** `docker-maven-plugin` to create a Docker instance of the database for Maven's *integration-test* phase.  You can see how this was done by inspecting pom.xml and other files at [commit e887d66](https://github.com/joeclark-phd/granite/tree/e887d661a232d7b4d0b7071adf6dbba63454789a).  I switched to using Testcontainers for a few reasons:
  - The Testcontainers approach puts its code and configuration all in the same place--the test class--rather than splitting it between `application.yaml`, `pom.xml`, and the test class.  It's more verbose and requires more copy-pasting, but is also more transparent.
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
