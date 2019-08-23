# granite

This project is intended to be a model or starter for a Java web application that will be highly-maintainable over a long period of time (20+ years).

## business requirements

- application security must meet standards
- accessibility (web front-end) must meet standards for visually-impaired
- usability on mobile/tablet screens "nice to have", maybe a requirement soon
- web app must handle thousands of daily users, millions of transactions annually
- must provide a REST API for at least one 3rd party providing a web front-end to our back-end
- multi-language support would be "nice to have"
- backup and recovery must be solidly planned
- application likely to last 20+ years and may be maintained by developers of widely varied skill levels

## goals

A web application that lives a long time will be touched by a lot of people.  It will see numerous version changes in programming languages, databases, etc., and will sometimes have to make those changes urgently due to security vulnerabilities.  It should avoid unnecessary complexity that will make it harder for later developers to orient themselves.  It should have a solid logging framework, automated testing, security, accessibility, instrumentation (e.g. for performance analytics), and documentation from the very beginning.  That's a lot of up-front work before really developing the app's functionality, but it will lead to a profound reduction of risk and cost in years to come.  This repo will allow that up-front work to be re-usable for more than one project.

It should have a build process defined in code (i.e. Maven) and an environment defined in code (i.e. Docker) so that these will be communicated and kept up to date via version control.  The build process should be independent of a developer's IDE (or really anything outside the version-controlled code repo).  Differences in environments should be kept out of the code, so the same application can run in all environments.  (I am used to using environment variables for this, but my impression is that the Java world prefers XML configuration files.  I'll research further before choosing an approach.)  The application should rely on external libraries as much as possible, to keep its own codebase small.  Specified version numbers of these libraries should be pulled in with a dependency manager rather than bundled with the project.  This prevents the evolution of modified/customized libraries over the years.  Additionally, version numbers can sometimes be hard to figure out from an old JAR or an old script, so the dependency manifest (i.e. `pom.xml`) makes them clear for all time.  As a corollary, we should try to pick libraries that are in broad use and will likely still exist for years to come.

The DDL script(s) that define the database(s) should also be version-controlled, even if the database is not part of the automated build process, both to maintain the history of changes and so they can be used to generate mock databases during automated testing.  I assume that developers will *not* have direct control of their production database, but will be working with a DBA.  They need to be able to design a migration (DDL script to alter or add to the schema), re-build a mock database on their local machine and/or test server, and then hand it to the DBA.  If building a mock database is part of the automated build/test cycle it should quickly alert the the team to database changes that don't get recorded in the code repository, and vice versa.

Since code updates may be "out of sync" with database changes, some effort should be made to ensure backward and forward compatibility (see Ambler & Sadalage's *Refactoring Databases: Evolutionary Database Design* for some ideas). For starters:

- use views or stored procedures whenever possible, so the underlying schema can be changed but the interface stay the same
- if a database change would break older versions of the code, a view or stored procedure or trigger should be used to support the older version of the code
- if new code wouldn't work with an older version of the database, it should fail gracefully with a message like "feature not available" instead of blowing up

## design principles

- Use good, standard Java 8+ coding practices, checked continually with static analysis tools looking for poor style, potential bugs, security vulnerabilities, etc, to keep the code maintainable for many years.
- Document design/architecture decisions from the beginning, comment every class in the code (as well as key methods), and try to keep this documentation up to date (version controlled!).
- Use HTML-based templates for the web front-end rather than a JavaScript-based framework, and keep AJAX to a minimum.  This is to reduce the learning curve for future developers who may not want to have to learn a front-end framework and a back-end framework at the same time.
- Separate the data/query code from direct calls to the front end.  The application should be expose both a website as well as via a JSON REST API without a lot of duplicated code.
- Use external libraries (e.g. Bootstrap, JQuery, Spring Boot) to keep our own code base as small as possible.  The exception to this is we'll write our own SQL rather than using an ORM.
- Pull in those external dependencies using a dependency management system (e.g. Maven) rather than including them in our code repository. Use specific version numbers known to work.
- One codebase and one build for all environments: developer workstation, test server, staging server, production.  It should run on all major operating systems.
- Write automated tests as you go, so a large suite of tests will be built up over time, without requiring time to be budgeted for a separate "testing project".  By re-running these tests after every change, problems can be fixed quickly and QA can approve deployments with confidence.
- Build process, testing process, and database DDL should all be version-controlled and kept up-to-date.
- Since the developers may not have direct control of the production database, build a sample database during automated testing as a way of making sure the application code and DDL are in sync.

## technologies

- Java 8
  - Java is up to version 12 at the time of writing, but Java 8 was a major milestone that every dev should be comfortable with.
  
- Spring Boot
  - This framework does a lot behind the scenes, which lets us keep our own codebase smaller, hence more readable and maintainable by future devs.
  
- HTML5, CSS3, [JQuery](https://jquery.com/), [Bootstrap](https://getbootstrap.com/) 4, and a [Bootswatch](https://bootswatch.com/) theme.
  - JQuery, Bootstrap, and Popper.js (a Bootstrap dependency) are pulled in by Maven at build time from [webjars.org](https://www.webjars.org/), along with a Bootswatch theme (an indulgence, but I like the look of it).  Letting Maven do this ensures that we control exactly which versions we're using, and mitigates the risk of a locally-saved copy which might accumulate undocumented customizations over the years.
  
- [Thymeleaf](https://www.thymeleaf.org/) template engine
  - The problem with JSP is that there's a temptation for lots of business code to creep into the templates; newer template engines such as ThymeLeaf, Velocity, FreeMarker, Groovy, etc, are better at keeping code tidy and responsibilities separate, so someone can understand it in 20 years.  I picked Thymeleaf for this project and will stick with it until I find a reason not to.
  
- *No JavaScript-based front-end framework* 
  - I've used Angular and it's great, but developers on a long-lived project may come in with varying skill levels.  Having to learn Java and Spring at the same time you're learning JavaScript/TypeScript and a JS-based framework like Angular would be too much of a hurdle, and lead to over-complicated code.  We'll use good old-fashioned templates and very limited AJAX to keep it straightforward.
  
- JUnit 5 (aka [JUnit Jupiter](https://junit.org/junit5/)) for automated tests, along with some annotations provided by Spring Boot.
  - More on our testing plan here: [plan/tests.md](plan/tests.md)
  
- [SLF4J](https://www.slf4j.org/) API for logging.
  - Spring Boot provides the logging implementation (I believe the default is *Logback*) but with the SLF4J facade we don't need to know about it.  Simply add a line like this at the top of any class:
  
    ```
    Logger logger = LoggerFactory.getLogger(MyClass.class);
    ```
    
    And then you can log any relevant info throughout the application like so:
    
    ```
    logger.debug("Initializing new instance of MyClass...");
    ```
    
    The available logging levels are `trace`, `debug`, `info`, `warn`, and `severe`.
    
- [UX testing tool]
- [load testing tools]
- [Code quality scanning/linting tools]
- [Security vulnerability scanning tools]
- Maven
  - for build automation and dependency management
- Docker
  - to package the application for easy deployment
- Git/Github
  - `git` is good, but with Github as a front-end it's far more valuable than any other VCS.  Github allows hooking into continuous integration, static analysis, and other 3rd party tools.
- PostgreSQL
  - I'm not a fan of ORM tools that obscure the database from the developer, so I'm going to pick a specific database and write my own SQL.  Postgres is good, free, and popular.
- [backup tools]

## build process

Assuming you have Docker, Maven, and Java 8 installed. (TODO: Make fewer assumptions!)

The `pom.xml` currently uses the Spotify `dockerfile-maven-plugin` to build an image.  Just enter:

    mvn clean install dockerfile:build
    
This creates a docker image called `joeclark77/granite` tagged with the version number, and you can change this all in the POM file.

Currently Maven doesn't yet start the Docker image, run tests, or deploy it to Docker Hub.  I can do that manually with:

    docker push joeclark77/granite

## try it out

You should be able to do this from anywhere (assuming you have Docker):

    docker run -t -p 8080:8080 joeclark77/granite:0.1-SNAPSHOT

And check out the web app running at http://localhost:8080. The test user account is `user` and a password will be printed to the console in the server's startup messages.