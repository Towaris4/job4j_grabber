create table post (
id primary key,
name text,
text text,
link text unique,
created Timestamp without time zone
);