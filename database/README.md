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