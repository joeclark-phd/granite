

-- Users+authorities schema based on Spring Boot default schema
---------------------------------------------------------------------------------------------------
drop table if exists authorities;
drop table if exists users;
create table users(
    username varchar(50) not null primary key,
    password text not null,
    enabled boolean not null default false
);
create table authorities(
    username varchar(50) not null references users(username),
    authority text not null,
    primary key (username, authority)
);
---------------------------------------------------------------------------------------------------
-- Default accounts for testing.  Passwords are BCrypt hashed (prefix must be $2a).
-- You can use, for example, https://www.browserling.com/tools/bcrypt to generate hashes.
---------------------------------------------------------------------------------------------------
-- admin : super
insert into users (username, password, enabled) values ('admin','$2a$10$UxM6Ypl9VGVLhAflRO5LX.oxqkTVdG94fLFOqt8UpAGcc76eZA5di',true);
insert into authorities (username, authority) values ('admin','ROLE_SUPER');
-- joe : pass
insert into users (username, password, enabled) values ('joe','$2a$10$YhQsADh6XDhJ8jQmP8xgc.9cjbRiQ3qBpTEAH.yGJX/yyoK..gHcW',true);
insert into authorities (username, authority) values ('joe','ROLE_AGENT');
---------------------------------------------------------------------------------------------------




drop table if exists agencies;
create table agencies (
    id              serial      PRIMARY KEY,
    name            text        NOT NULL,
    city            text,
    state           char(2),
    phone_number    text
);

insert into agencies (name,city,state,phone_number)
values
('Joes Sundries','Bangor','ME','555-207-1234'),
('Clark & Sons','Farmington','ME','555-207-9876');