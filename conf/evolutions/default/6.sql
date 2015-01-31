# --- !Ups

create table issue (
  id                        bigint auto_increment NOT NULL,
  title                     varchar(255) NOT NULL,
  description               text,
  created                   datetime NOT NULL,
  resolved                  datetime,
  created_by_id             bigint NOT NULL,
  owner_id                  bigint,
  constraint pk_event primary key (id))
;
# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table issue;

SET FOREIGN_KEY_CHECKS=1;