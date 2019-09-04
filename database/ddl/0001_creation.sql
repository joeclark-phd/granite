create table if not exists agencies (
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