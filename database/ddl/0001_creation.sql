

-- users+authorities schema based on Spring Boot default schema
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