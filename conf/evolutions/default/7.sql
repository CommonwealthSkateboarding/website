# --- !Ups

create table unheard_sale (
  id                        bigint auto_increment NOT NULL,
  brand                     varchar(64) NOT NULL,
  description               text,
  retail_price              decimal(13,2) NOT NULL,
  team_rider_sale           tinyint(1) NOT NULL,
  created                   datetime NOT NULL,
  sold_by_id                bigint NOT NULL,
  invoiced                  tinyint(1) NOT NULL DEFAULT 0,
  constraint pk_event primary key (id))
;
# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table unheard_sale;

SET FOREIGN_KEY_CHECKS=1;