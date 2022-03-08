create table sets(
    "id" integer primary key,
    id_event integer not null,
    id_winner integer not null,
    id_loser integer not null,
    score_winner integer not null,
    score_loser integer not null
);