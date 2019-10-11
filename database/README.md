## Granite's Database Backend

Granite employs a PostgreSQL back end.  The URL, username, and password of the database are specified in [`application.yaml`](https://github.com/joeclark-phd/granite/blob/master/src/main/resources/application.yml) and it is expected that you'll override them in profile-specific configuration files, e.g., `application-prod.yaml`.

The `Dockerfile` in this directory defines the basic parameters for building a new instance the database with an up-to-date schema, based on one of official Docker [postgres](https://hub.docker.com/_/postgres) images.  To spin one up in your local environment, if you have Docker installed you should be able to do this:

    docker container run -d -p 5432:5432 --name myGraniteDB joeclark77/granite-db:latest

### Evolving the database schema

The Docker image contains all files from the `ddl/` directory.  These scripts run in alphabetical order when a new container is first launch, and they build up the database schema to its latest form.  

It is *not* assumed that the production database will be under the control of the development team, and *not* assumed that your DBA will use Docker.
  
To evolve the database, you will want to provide your DBA with a "migration script" containing just the incremental changes for the latest update (i.e. `CREATE`, `DROP`, and `ALTER TABLE` statements).  Therefore, to change the database, *leave the existing scripts alone* and add a new one!  The naming convention is that each script start with four numeric digits to keep them in order:

    0001_creation.sql
    0002_add_awesome_new_tables.sql
    ...

After successful testing, the DBA will receive the migration script from this repo, with confidence that it has been tested (more on testing below), and can run the script in production.

### Building a new database image

To build a new version of the database's Docker image, from this working directory try a command like this:

    docker image build -t joeclark77/granite-db:latest .
    
You could even push it to Dockerhub with:

    docker push joeclark77/granite-db:latest
 
I don't generally do this manually, though, because I have configured the Maven build process to build the latest image and push it to Dockerhub during the 'deploy' phase, using the Maven artifact's version number.  This keeps version numbers of the [granite-db](https://cloud.docker.com/repository/docker/joeclark77/granite-db) image in sync with version numbers of the Java project's [granite](https://cloud.docker.com/repository/docker/joeclark77/granite) image.

### Testing with the database

The Java project contains a suite of automated tests.  Some of these test code that hits the database.  Those tests use the [Testcontainers](https://www.testcontainers.org/) library to spin up temporary test database containers, using Docker, for the duration of one or more tests.  This requires a fair amount of code to be added to the beginning of the test class; see for example this code from [AgencyControllerIntegrationTest.java](https://github.com/joeclark-phd/granite/blob/master/src/test/java/net/joeclark/webapps/granite/agency/AgencyControllerIntegrationTest.java):

```
@Testcontainers // We'll use testcontainers to spin up a database in Docker to test with.
@ContextConfiguration(initializers = { AgencyControllerIntegrationTest.Initializer.class }) // The child class Initializer dynamically loads DB connection properties as environment variables.
@SpringBootTest
@AutoConfigureMockMvc
class AgencyControllerIntegrationTest {


    @Container
    // Spin up a fresh database in a docker container just for the tests below
    private static final GenericContainer dbContainer = new GenericContainer(
            new ImageFromDockerfile().withFileFromPath(".", Paths.get("./database")) // Re-generate the database image at test time, so it always reflects the latest changes
    ).withExposedPorts(5432);


    // This static child class overrides the default database configuration to connect us to the temporary test database.
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + "jdbc:postgresql://" + dbContainer.getContainerIpAddress() + ":" + dbContainer.getFirstMappedPort() + "/granite",
                    "spring.datasource.username=" + "granite",
                    "spring.datasource.password=" + "test"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    ...(test cases)...
```

I've concluded that, even though this is quite a bit to copy and paste, it beats alternative configuration methods because it makes explicit what it's doing, right within the test class.

Note that Testcontainers will spin up temporary databases directly from the `Dockerfile` and DDL scripts in this directory, *not* by pulling a version from Dockerhub.  Therefore, you are testing whether your latest changes to the Java code work with your latest changes to the database.  That's a key reason for having both the code and the database together in version control.

TODO: You may want to find out how also to test *new* code with an *older* schema, or vice versa, if the app and the database aren't always being updated at the same time.  This could be part of a CI process.