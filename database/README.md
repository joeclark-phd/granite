## Database README

### Manual creation of database

How the database(s) work is still being figured out.  There are a lot of notes in this README and some are out of date.

I am able to create a Postgres database in a Docker container (without installing Postgres or any of its tools locally) with this command on my Windows laptop:

    docker run -p 5432:5432 --name myGraniteDB -e POSTGRES_PASSWORD=root -d postgres

I then log into the container and use `psql` interactively to create the tables and load them with data.

    docker exec -it myGraniteDB bash
    # psql -U postgres

DDL scripts can be found in the `ddl/` subdirectory.  They are named with a four-digit number prefix followed by meaningful text, so they can be sorted alphabetically but still give clues to their purposes. The first script is `0001_creation.sql`.  At first, I just pasted the code into `psql` interactively.

### A custom image

The `Dockerfile` in this directory defines the basic parameters for building a custom image based on one of the Docker Hub "library" Postgres images.  It copies all files from the `ddl/` directory into the container's `docker-entrypoint-initdb.d/` directory, and these will be run in alphabetical order when the container is first started, so the naming convention is to start with a four-digit number keeping your updates in the proper order, then add any descriptive name you like, e.g.:

    0001_creation.sql
    0002_add_awesome_new_tables.sql
    ...

To the lucky developer who needs to make the 10,000th update: I'm sure you can figure out a way to add a fifth digit to all the existing filenames and bill your client extra for it.

I assume that the production database will not be under the control of the development team, so the production database will not be updated directly from this repo.  After successful testing, the DBA will receive the DDL files from this repo, with confidence that they have been tested, and run the DDL scripts in production.

My intent is that changes to the database after the first permanent release of Granite will be defined as incremental changes (i.e. CREATE, DROP, and ALTER TABLE) in sequentially-numbered files.  This gives us version control over the database schema; running all the files up to any point in the version history of this repo will give us the database that existed at that point.  These could be considered *migration* files.  You may consider also creating *downgrade* files that reverse each incremental change, to be used if we wish to revert the database to an earlier version, but I think that may be a lot of extra work for little benefit.

You can manually build an image from this directory like so:

    docker image build -t joeclark77/granite-db:0.1-SNAPSHOT .

### Two options for database testing

As a summary of my goals:

- I want to be able to spin up a freshly-generated database during testing, either locally or on a CI server.
- I also want to be able to run the application normally, on my dev machine, on a test server, or a production server, with a different database for each environment.
- I want the build process to be independent of local things like IDE settings.

Two options:

- Using the `docker-maven-plugin` to spin up a test database on localhost:5432 for the integration-test phase of the maven build.  Database connection is defined in `application-test.yml` (for the "test" Spring profile; there are other profiles such as "dev" for regular running of the app on my local computer).  The upsides here are that it is all run by Maven, hence independent of my environment and potentially very scriptable on, say, a CI server; and that the database connection is specified the same way for tests and for normal operation, in one of the `application-*.yaml` files.  Another benefit is that the database image is always named according to a strict naming convention, if I want to push it to Docker Hub or another registry as part of the build.  The drawbacks are:

  - IntelliJ can run the tests [but can't give me test-by-test output](https://stackoverflow.com/questions/58222014/how-to-visualize-output-of-junit-integration-tests-in-intellij-when-using-docker) because the tests don't run in the "test" phase.  I don't want to be too dependent on my IDE but this is a very nice feature and they all should have it.
  - The Dockerfile and the database connection specifications are in two different locations from each other and from the test, making it maybe less clear what's being tested.
  - This may preclude me from using the `docker-maven-plugin` from doing something *else* useful, like building an image of my project itself at the end of the Maven lifecycle.

- Alternatively I could use [TestContainers](https://www.testcontainers.org/) to build and run docker containers from within JUnit test classes.  This has the advantage of doing the integration tests in the same "phase" as the unit tests, and letting me run them directly via the IDE instead of using Maven.  I do want it all to work with Maven but it's also nice to use the IDE to run specific subsets of tests.  With TestContainers I can get that test-by-test feedback from IntelliJ.  Also, it combines database connection code and container specification within the test classes, for maximum readability.  Finally, it frees up the `docker-maven-plugin` for other uses, such as packaging my application itself.  The drawbacks:

  - The tests may connect to the database in a different way from the regular application.  I'll need to figure out how to establish the database connection in an understandable way for both contexts (testing and running).
  - I think this requires more of the computer running the tests; at a minimum, they must have Docker configured and perhaps associated with whatever IDE they're using.  It may be trickier for CI servers.  I was hoping to eventually containerize the whole build/test process, for example using Maven in a container, and this seems like it would be messier.
  
 I tried the first approach (commit [e887d66](https://github.com/joeclark-phd/granite/tree/e887d661a232d7b4d0b7071adf6dbba63454789a) was the last commit using it).  Now I use the second.
 
 ### More on the above
 
 I have now rigged up AgencyControllerIntegrationTest to use Testcontainers.  This has turned up a couple of new problems:
 
 - Testcontainers wants a generic Postgres image to create a PostgreSQLContainer, not a customized one like mine with specified username, password, and with a directory of init scripts.  I am forced instead to create a GenericContainer but this is clunkier + requires more boilerplate code.
 
   - It may be possible to trick Testcontainers into thinking my image is a Postgres image, but I'm not sure yet.
   
 - I *can* build the image on the fly with Testcontainers, but you have to pass a filesystem path or classpath to the Dockerfile, and I don't know if that will work consistently across machines. 
 
 - I find that I want to use Testcontainers' JDBC URL connection approach, so I can cut 15 or more lines of code out of each test class (see [AgencyControllerIntegrationTest.java](https://github.com/joeclark-phd/granite/blob/master/src/test/java/net/joeclark/webapps/granite/agency/AgencyControllerIntegrationTest.java)), but I think you have to use a "library" postgres image for that.