# --- !Ups

create table event (
  id                        varchar(64) NOT NULL,
  name                      varchar(255) NOT NULL,
  public_visibility         tinyint(1) NOT NULL DEFAULT 0,
  reserve_park              tinyint(1) NOT NULL DEFAULT 0,
  notes                     text,
  private_notes             text,
  start_time                datetime NOT NULL,
  end_time                  datetime NOT NULL,
  archived                  tinyint(1) NOT NULL DEFAULT 0,
  constraint pk_event primary key (id))
;

alter table audit_record add event_id varchar(64);

alter table news_item add front_page tinyint(1) NOT NULL DEFAULT 0;

INSERT INTO security_role (id, role_name)
VALUES (7,"EVENTS"),
       (8,"CAMP");

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

alter table registration add constraint fk_registration foreign key (camp_id) references camp (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table event;

alter table audit_record drop column event_id;

alter table news_item drop column front_page;

DELETE FROM security_role WHERE id=7 or id=8;

DELETE FROM users_security_role WHERE security_role_id=7 OR security_role_id=8;

drop table registration;

SET FOREIGN_KEY_CHECKS=1;