# --- !Ups

create table event (
  id                        bigint auto_increment NOT NULL,
  name                      varchar(255) NOT NULL,
  public_visibility         tinyint(1) NOT NULL DEFAULT 0,
  reserve_park              tinyint(1) NOT NULL DEFAULT 0,
  notes                     text,
  start_time                datetime NOT NULL,
  end_time                  datetime NOT NULL,
  archived                  tinyint(1) NOT NULL DEFAULT 0,
  constraint pk_event primary key (id))
;

alter table audit_record add event_id bigint;

INSERT INTO security_role (id, role_name)
VALUES (7,"EVENTS"),
       (8,"CAMP");

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table event;

alter table audit_record drop column event_id;

DELETE FROM security_role WHERE id=7 or id=8;

DELETE FROM users_security_role WHERE security_role_id=7 OR security_role_id=8;

SET FOREIGN_KEY_CHECKS=1;