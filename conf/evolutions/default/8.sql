# --- !Ups

alter table news_item modify id varchar(64);
alter table audit_record modify news_item_id varchar(64);
alter table event modify id varchar(64);
alter table audit_record modify event_id varchar(64);
alter table camp modify id varchar(64);
alter table audit_record modify camp_id varchar(64);

drop table registration;
create table registration (
  id                        bigint auto_increment NOT NULL,
  camp_id                   varchar(64),
  event_id                  varchar(64),
  paid                      tinyint(1) NOT NULL DEFAULT 0,
  payment_type              varchar(16),
  participant_name          varchar(255) NOT NULL,
  notes                     text,
  timestamp                 datetime NOT NULL,
  registration_type         varchar(16) NOT NULL,
  confirmation_id           varchar(16) NOT NULL,
  registrant_email          varchar(255),
  total_paid                decimal(13,2) NOT NULL,
  constraint pk_registration primary key (id))
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

SET FOREIGN_KEY_CHECKS=1;