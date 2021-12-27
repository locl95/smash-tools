create table movements(
    character text,
    name text,
    advantage integer,
    "type" text not null,
    first_frame integer
    primary key (character, name),
    foreign key(character) references characters(name)
);
