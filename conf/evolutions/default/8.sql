# --- !Ups

create table bitcoin_sale (
  id                        bigint auto_increment NOT NULL,
  description               text,
  amount                    decimal(13,2) NOT NULL,
  created                   datetime NOT NULL,
  sold_by_id                bigint NOT NULL,
  constraint pk_bitcoin_sale primary key (id))
;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table bitcoin_sale;

SET FOREIGN_KEY_CHECKS=1;