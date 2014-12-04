# --- !Ups

create table camp (
  id                        bigint auto_increment NOT NULL,
  title                     varchar(255) NOT NULL,
  create_date               datetime NOT NULL,
  start_date                datetime NOT NULL,
  end_date                  datetime NOT NULL,
  registration_end_date     datetime NOT NULL,
  max_registrations         int(11) NOT NULL,
  description               text NOT NULL,
  cost                      decimal(13,2) NOT NULL,
  schedule_description      varchar(255) NOT NULL,
  constraint pk_camp primary key (id))
;

create table registration (
  id                        bigint auto_increment NOT NULL,
  camp_id                   bigint NOT NULL,
  paid                      tinyint(1) NOT NULL DEFAULT 0,
  payment_type              varchar(16),
  participant_name          varchar(255) NOT NULL,
  notes                     text,
  timestamp                 datetime NOT NULL,
  constraint pk_registration primary key (id))
;

alter table registration add constraint fk_registration foreign key (camp_id) references camp (id) on delete restrict on update restrict;

alter table audit_record add camp_id bigint;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table camp;

drop table registration;

SET FOREIGN_KEY_CHECKS=1;