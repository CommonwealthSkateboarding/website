# --- !Ups

create table closure (
  id                        bigint auto_increment NOT NULL,
  message                   text,
  created                   datetime NOT NULL,
  created_by_id             bigint NOT NULL,
  enabled                   tinyint(1) NOT NULL DEFAULT 0,
  archived                  tinyint(1) NOT NULL DEFAULT 0,
  constraint pk_closure primary key (id))
;

alter table audit_record add closure_id bigint;


# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table closure;

alter table audit_record drop column closure_id;

SET FOREIGN_KEY_CHECKS=1;