# Automated Testing

JUnit 5 (called JUnit Jupiter) will be the basis of our testing.

## Spring Testing

Spring Boot offers some tools for testing Spring applications (these may or may not be directly related to Spring Testing).  I have begun by following [this guide to 'testing the web layer'](https://spring.io/guides/gs/testing-web/) and hopefully will remember to update this file as I learn more.

There are at least three levels of testing possible:

1. Unit tests on POJO methods (pure JUnit, not Spring)

1. Tests of the "Web Layer" without the whole application context.  These are set up with the following annotations.  Optionally we may just test one controller at a time:
    ```
    @WebMvcTest(HomeController.class)
    ```
   I think the idea here is to use mock/stub objects for the Service (or Repository or whatever) so you're really just testing the pass-through from the service to the view via a controller.  See `HomeControllerTest.java` for an example.
   
1. Tests of the "MVC Layer" that test the whole stack except without starting an HTTP server, hence they are faster.  These would be annotated with:
    ```
    @SpringBootTest
    @AutoConfigureMockMvc
    ```
   In this case we might test endpoints at the controller using the real service, repository, and model classes.  A typical "integration test".  See my examples: `HomeControllerIntegrationTest.java` and `AgencyControllerIntegrationTest.java`.
   
1. Whole-system tests that start up the server in the developer's environment, carrying out HTTP requests and testing responses.  These are annotated with the following code.  I'm not sure I want to use them, unless I can write the code for these and re-use it for the next type, below.
    ```
    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
    ```

1. Whole-system tests that are run after the app is built, wrapped up in a docker container, and started.  These would also make HTTP requests and test responses.  I don't know how to automate this, yet.  In my ideal scenario, a fresh test database would also be built (at least on the dev machine and test server) to guarantee the integrity of the DB migration scripts, and the tests would expect the app to use that database.  (On the staging server, instead, the application would point to the production database or a copy of it.)

## Current tests

I definitely intend to add simple unit tests throughout (aka "type 1") and ultimately aspire to running the kind of whole-system tests (aka "type 5").  The types in between, I'm not so sure about.  I'm not sure I fully understand the difference between "type 2" and "type 3".  I intend to use "type 2" test classes for each Controller, until a reason for the other types becomes clear.

Following the example in the guide, I also have a class ApplicationTest.class which tests whether the application even loads.  I frankly don't know what level that operates at, if any, between #1-#4!

My "type 3" tests now need a test database to be set up and running on localhost or they will fail.  My next objective is to figure out how to automate this with Docker and a tool like TestContainers.

## Use of profiles

We run our application using Spring Boot profiles to indicate environments.  For example the Application runs with `--spring.profiles.active=dev` configured.  Implementations of Repository classes are decorated with, e.g., `@Profile("dev")` or `@Profile("prod")` so we can have different implementations autowired for different databases.

In testing, decorate the test class with `@ActiveProfiles("dev")`, at least if they're integration tests that touch the repository beans.

Ideally, use Maven to make sure the database is actually created and ready, before running tests that will touch it.

## REST endpoint tests

A first REST endpoint test is coded in `AgencyControllerIntegrationTest.java`.  It combines `MockMvc` with a path expression syntax called [`jsonpath`](https://github.com/dchester/jsonpath) to inspect the contents of responses.