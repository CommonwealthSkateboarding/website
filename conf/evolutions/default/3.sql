# --- !Ups

create table camp (
  id                        varchar(64) NOT NULL,
  title                     varchar(255) NOT NULL,
  create_date               datetime NOT NULL,
  start_date                datetime NOT NULL,
  end_date                  datetime NOT NULL,
  registration_end_date     datetime NOT NULL,
  max_registrations         int(11) NOT NULL,
  description               text NOT NULL,
  cost                      decimal(13,2) NOT NULL,
  schedule_description      varchar(255) NOT NULL,
  instructors               varchar(255) NOT NULL,
  archived                  tinyint(1) NOT NULL DEFAULT 0,
  constraint pk_camp primary key (id))
;

alter table audit_record add camp_id varchar(64);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table camp;

alter table audit_record drop column camp_id;

SET FOREIGN_KEY_CHECKS=1;