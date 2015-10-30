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

alter table news_item modify id bigint auto_increment not null;
alter table audit_record modify news_item_id bigint;
alter table event modify id bigint auto_increment not null;
alter table audit_record modify event_id bigint;
alter table registration modify event_id bigint;
alter table camp modify id bigint auto_increment not null;
alter table audit_record modify camp_id bigint;
alter table registration modify camp_id bigint;

drop table bitcoin_sale;

SET FOREIGN_KEY_CHECKS=1;