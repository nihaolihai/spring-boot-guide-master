drop table if exists customer;

create table customer
(
    id            bigint generated by default as identity,
    customer_name varchar(32),
    age           int,
    create_time   timestamp default current_timestamp,
    edit_time     timestamp default current_timestamp,
    primary key (id)
);