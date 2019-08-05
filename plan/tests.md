# Automated Testing

JUnit 5 (called JUnit Jupiter) will be the basis of our testing.

## Spring Testing

Spring Boot offers some tools for testing Spring applications (these may or may not be directly related to Spring Testing).  I have begun by following [this guide to 'testing the web layer'](https://spring.io/guides/gs/testing-web/) and hopefully will remember to update this file as I learn more.

There are at least three levels of testing possible:

1. Unit tests on POJO methods (pure JUnit, not Spring)

1. Tests of the "Web Layer" without the whole application context.  These are set up with the following annotations.  Optionally we may just test one controller at a time:
    ```
    @RunWith(SpringRunner.class)
    @WebMvcTest(HomeController.class)
    ```
   I think the idea here is to use mock/stub objects for the Service (or Repository or whatever) so you're really just testing the pass-through from the service to the view via a controller.  See `HomeControllerTest.java` for an example.
   
1. Tests of the "MVC Layer" that test the whole stack except without starting an HTTP server, hence they are faster.  These would be annotated with:
    ```
    @RunWith(SpringRunner.class)
    @SpringBootTest
    @AutoConfigureMockMvc
    ```
   In this case we might test endpoints at the controller using the real service, repository, and model classes.  A typical "integration test".  See my example: `HomeControllerIntegrationTest.java`.
   
1. Whole-system tests that start up the server in the developer's environment, carrying out HTTP requests and testing responses.  These are annotated with the following code.  I'm not sure I want to use them, unless I can write the code for these and re-use it for the next type, below.
    ```
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
    ```

1. Whole-system tests that are run after the app is built, wrapped up in a docker container, and started.  These would also make HTTP requests and test responses.  I don't know how to automate this, yet.  In my ideal scenario, a fresh test database would also be built (at least on the dev machine and test server) to guarantee the integrity of the DB migration scripts, and the tests would expect the app to use that database.  (On the staging server, instead, the application would point to the production database or a copy of it.)

## Current tests

I definitely intend to add simple unit tests throughout (aka "type 1") and ultimately aspire to running the kind of whole-system tests (aka "type 5").  The types in between, I'm not so sure about.  I'm not sure I fully understand the difference between "type 2" and "type 3".  I intend to use "type 2" test classes for each Controller, until a reason for the other types becomes clear.

Following the example in the guide, I also have a class ApplicationTest.class which tests whether the application even loads.  I frankly don't know what level that operates at, if any, between #1-#4!