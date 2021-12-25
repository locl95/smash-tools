create table cache(
    "table" text not null primary key,
    last_update timestamp not null default current_timestamp,
    valid boolean not null default false
);