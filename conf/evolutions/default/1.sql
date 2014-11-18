# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table linked_account (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_linked_account primary key (id))
;

create table news_item (
  id                        bigint auto_increment not null,
  expires                   tinyint(1) default 0,
  expire_date               datetime,
  create_date               datetime,
  title                     varchar(255),
  content                   text,
  extended_content          text,
  sticky                    tinyint(1) default 0,
  constraint pk_news_item primary key (id))
;

create table users (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  name                      varchar(255),
  active                    tinyint(1) default 0,
  email_validated           tinyint(1) default 0,
  constraint pk_users primary key (id))
;

alter table linked_account add constraint fk_linked_account_user_1 foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_linked_account_user_1 on linked_account (user_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table linked_account;

drop table news_item;

drop table users;

SET FOREIGN_KEY_CHECKS=1;

