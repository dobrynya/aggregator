create table INSTRUMENT_PRICE_MODIFIER (
  id bigint primary key auto_increment,
  name varchar2(256) not null unique,
  multiplier number(10,2) not null
);

insert into INSTRUMENT_PRICE_MODIFIER (name, multiplier) values ('INSTRUMENT1', 1.05);
insert into INSTRUMENT_PRICE_MODIFIER (name, multiplier) values ('INSTRUMENT2', 1.10);
insert into INSTRUMENT_PRICE_MODIFIER (name, multiplier) values ('INSTRUMENT3', 1.15);
insert into INSTRUMENT_PRICE_MODIFIER (name, multiplier) values ('INSTRUMENT5', 2);