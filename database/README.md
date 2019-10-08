## Database README

### Current state (a work in progress)

How the database(s) work is still being figured out.  This is the current state of things.

I am able to create a Postgres database in a Docker container (without installing Postgres or any of its tools locally) with this command on my Windows laptop:

    docker run -p 5432:5432 --name myGraniteDB -e POSTGRES_PASSWORD=root -d postgres

I then log into the container and use `psql` interactively to create the tables and load them with data.

    docker exec -it myGraniteDB bash
    # psql -U postgres

DDL scripts can be found in the `ddl/` subdirectory.  They are named with a four-digit number prefix followed by meaningful text, so they can be sorted alphabetically but still give clues to their purposes. The first script is `0001_creation.sql`.  For now, I am pasting the code into `psql` interactively.

### Goals

My intent is that changes to the database after the first permanent release of Granite will be defined as incremental changes (i.e. CREATE, DROP, and ALTER TABLE) in sequentially-numbered files.  This gives us version control over the database schema; running all the files up to any point in the version history of this repo will give us the database that existed at that point.  These could be considered *migration* files.  You may consider also creating *downgrade* files that reverse each incremental change, to be used if we wish to revert the database to an earlier version, but I think that may be a lot of extra work for little benefit.

We now use the `docker-maven-plugin` to generate a brand-new database from scratch, using these DDL files, when building Granite on a test server.  That ensures that the latest version of the code works with the latest version of the database (and that no other database changes are being done on the side, without being version controlled).  The `Dockerfile` in this directory defines the basic parameters.  SQL or shell files copied into the container's `docker-entrypoint-initdb.d/` directory will be run in alphabetical order when the container is first started, so the naming convention is to start with a four-digit number keeping your updates in the proper order, then add any descriptive name you like, e.g.:

    0001_creation.sql
    0002_add_awesome_new_tables.sql
    ...

To the lucky developer who needs to make the 10,000th update: I'm sure you can figure out a way to add a fifth digit to all the existing filenames and bill your client extra for it.

I assume that the production database will not be under the control of the development team, so the production database will not be updated directly from this repo.  After successful testing, the DBA will receive the DDL files from this repo, with confidence that they have been tested, and run the DDL scripts in production.

An optional step would be to run the application's code on a staging server after the testing server.  The staging server would use a staging database managed by the DBA, so the *new* code could be tested with the *old* database schema, so you could confirm that it fails gracefully rather than crashing.  Thereafter, operations could push code to production and *then* update the production database, rather than having to try to make both changes in production simultaneously.

### Two options for database testing

As a summary of my goals:

- I want to be able to spin up a freshly-generated database during testing, either locally or on a CI server.
- I also want to be able to run the application normally, on my dev machine, on a test server, or a production server, with a different database for each environment.
- I want the build process to be independent of local things like IDE settings.

Two options:

- Using the `docker-maven-plugin` to spin up a test database on localhost:5432 for the integration-test phase of the maven build.  Database connection is defined in `application-test.yml` (for the "test" Spring profile; there are other profiles such as "dev" for regular running of the app on my local computer).  The upsides here are that it is all run by Maven, hence independent of my environment and potentially very scriptable on, say, a CI server; and that the database connection is specified the same way for tests and for normal operation, in one of the `application-*.yaml` files.  The drawbacks are:

  - IntelliJ can run the tests [but can't give me test-by-test output](https://stackoverflow.com/questions/58222014/how-to-visualize-output-of-junit-integration-tests-in-intellij-when-using-docker) because the tests don't run in the "test" phase.  I don't want to be too dependent on my IDE but this is a very nice feature and they all should have it.
  - The Dockerfile and the database connection specifications are in two different locations from each other and from the test, making it maybe less clear what's being tested.
  - This may preclude me from using the `docker-maven-plugin` from doing something *else* useful, like building an image of my project itself at the end of the Maven lifecycle.

- Alternatively I could use [TestContainers](https://www.testcontainers.org/) to build and run docker containers from within JUnit test classes.  This has the advantage of doing the integration tests in the same "phase" as the unit tests, and letting me run them directly via the IDE instead of using Maven.  I do want it all to work with Maven but it's also nice to use the IDE to run specific subsets of tests.  With TestContainers I can get that test-by-test feedback from IntelliJ.  Also, it combines database connection code and container specification within the test classes, for maximum readability.  Finally, it frees up the `docker-maven-plugin` for other uses, such as packaging my application itself.  The drawbacks:

  - The tests may connect to the database in a different way from the regular application.  I'll need to figure out how to establish the database connection in an understandable way for both contexts (testing and running).
  - I think this requires more of the computer running the tests; at a minimum, they must have Docker configured and perhaps associated with whatever IDE they're using.  It may be trickier for CI servers.  I was hoping to eventually containerize the whole build/test process, for example using Maven in a container, and this seems like it would be messier.
  
 The first option is currently implemented.  After this commit, I'm going to attempt the second approach.
 
 ### More on the above
 
 I have now rigged up AgencyControllerIntegrationTest to use Testcontainers.  This has turned up a couple of new problems:
 
 - Testcontainers wants a generic Postgres image to create a PostgreSQLContainer, not a customized on like mine with specified username, password, and with a directory of init scripts.  I am forced instead to create a GenericContainer but this is clunkier + requires more boilerplate code.
 
   - It may be possible to trick Testcontainers into thinking my image is a Postgres image, but I'm not sure yet.
   
 - I cannot easily build the image on the fly with Testcontainers.  It can be done, but you have to pass a filesystem path or classpath to the Dockerfile, and I don't think that would be consistent across machines.  Also, I'd have to move this "/database" directory down into /src/test/resources/... or somewhere, which I oppose.  The database is more than just a "stub" or other resource.  I am therefore *manually* generating new versions of the database's image, and pushing them to Dockerhub.  Testcontainers pulls them down from there.
 
   - Maybe it's possible to use the docker-maven-plugin to re-build and push the image before the test phase.  That would help with, e.g., consistency in naming.
   - Another issue is that the image name and version is specified in the test class.  Ideally it could be pulled in from a property.
   
I'll leave all this thinking-out-loud here until I find a resolution that I like.