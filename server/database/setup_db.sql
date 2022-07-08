drop database week2;
create database week2;
use week2;

create table users(
    id int,
    rating int,
    username varchar(20),
    primary key(id)
);
create table levels(
    id int,
    levelname varchar(32),
    boardsize smallint,
    -- authorid int,
    primary key(id)
);
-- Insert examples.
insert into users(id, rating, username) values (0, 100000000, "admin");
insert into levels(id, levelname, boardsize) values 
(1954, "Test Level", 4),
(2000, "Two Thousand", 2),
(9999, "Goo goo goo", 9),
(17039, "ID over", 201);