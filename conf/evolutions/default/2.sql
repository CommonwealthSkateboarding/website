# --- !Ups

create table unlimited_pass (
  id                        bigint auto_increment not null,
  membership_id             bigint,
  added_by_id               bigint,
  created                   datetime,
  uses                      bigint,
  last_used                 datetime,
  starts                    datetime,
  expires                   datetime,
  constraint pk_unlimited_pass primary key (id))
;

create table visit (
  id                        bigint auto_increment not null,
  membership_id             bigint,
  verified_by_id            bigint,
  time                      datetime,
  constraint pk_visit primary key (id))
;

create table membership (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  parent_name               varchar(255),
  address                   varchar(255),
  city                      varchar(255),
  state                     varchar(255),
  country                   varchar(255),
  birth_date                datetime,
  telephone                 varchar(255),
  email                     varchar(255),
  emergency_contact_name    varchar(255),
  emergency_contact_number  varchar(255),
  notes                     text,
  session_passes            bigint,
  create_date               datetime,
  last_visited              datetime,
  constraint pk_membership primary key (id))
;

alter table users add photo_url varchar(255);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table unlimited_pass;

drop table visit;

drop table membership;

alter table users drop column photo_url;

SET FOREIGN_KEY_CHECKS=1;

