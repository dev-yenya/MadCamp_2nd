drop database week2;
create database week2;
use week2;

create table users(
    id varchar(56),
    rating int,
    username varchar(20) character set utf8 collate utf8_general_ci,
    primary key(id)
);

create table levels(
    id int auto_increment,
    levelname varchar(32) character set utf8 collate utf8_general_ci,
    boardsize smallint,
    -- authorid int,
    primary key(id)
);
create table completedlevels(
    userid varchar(40),
    levelid int
);
-- Insert examples.
insert into levels(id, levelname, boardsize) values 
(0, "Test Level", 5);

insert into users(id, rating, username) values ("123515132521341", 15000, "김성혁");