# --- !Ups

create table unlimited_pass (
  id                        bigint auto_increment not null,
  membership_id             bigint,
  added_by_id               bigint,
  created                   datetime,
  starts                    datetime,
  expires                   datetime,
  constraint pk_unlimited_pass primary key (id))
;

create table visit (
  id                        bigint auto_increment not null,
  membership_id             bigint,
  verified_by_id            bigint,
  type                      varchar(16),
  time                      datetime,
  expires                   datetime,
  refunded                  tinyint(1) default 0,
  visit_type                varchar(16) DEFAULT NULL,
  previous_visit_id         bigint,
  constraint pk_visit primary key (id))
;

create table membership (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  parent_name               varchar(255),
  address                   varchar(255),
  city                      varchar(255),
  state                     varchar(255),
  zipcode                   varchar(16),
  country                   varchar(255),
  birth_date                datetime,
  telephone                 varchar(255),
  email                     varchar(255),
  emergency_contact_name    varchar(255),
  emergency_contact_number  varchar(255),
  notes                     text,
  session_passes            bigint not null default 0,
  all_day_passes            bigint not null default 0,
  create_date               datetime,
  last_visit_id             bigint,
  credit                    DECIMAL(13, 2),
  constraint pk_membership primary key (id))
;

alter table users add photo_url varchar(255);

create table audit_record (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  membership_id             bigint,
  news_item_id              varchar(64),
  unlimited_pass_id         bigint,
  visit_id                  bigint,
  timestamp                 datetime,
  delta                     varchar(255),
  constraint pk_audit_record primary key (id))
;

INSERT INTO security_role (id, role_name)
VALUES (5,"BLOG"),
       (6, "USER_ADMIN");

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table unlimited_pass;

drop table visit;

drop table membership;

alter table users drop column photo_url;

drop table audit_record;

DELETE FROM security_role WHERE id=5 OR id=6;

DELETE FROM users_security_role WHERE security_role_id=5 or security_role_id=6;

SET FOREIGN_KEY_CHECKS=1;

