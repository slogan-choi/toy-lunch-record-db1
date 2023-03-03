drop table lunch if exists cascade;
create table lunch (
    id integer not null auto_increment,
    restaurant varchar(255) not null,
    menu varchar(255) not null,
    image binary large object not null,
    price numeric not null,
    grade real not null,
    updateAt time not null,
    createAt time not null,
    primary key (id)
)