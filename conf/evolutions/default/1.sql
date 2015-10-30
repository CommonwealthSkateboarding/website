# --- !Ups

create table linked_account (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_linked_account primary key (id))
;

create table news_item (
  id                        varchar(64) not null,
  expires                   tinyint(1) default 0,
  expire_date               datetime,
  create_date               datetime,
  title                     varchar(255),
  content                   text,
  extended_content          text,
  sticky                    tinyint(1) default 0,
  constraint pk_news_item primary key (id))
;

INSERT INTO `news_item` (`id`, `expires`, `expire_date`, `create_date`, `title`, `content`, `extended_content`, `sticky`)
VALUES
	('test1', 0, NULL, '2014-11-18 05:00:25', 'Sticky News', 'Sticky<br><br><img src="http://i.imgur.com/iMCMwXH.gif">', '', 1),
    ('test2', 0, NULL, '2014-11-18 05:00:40', 'Not sticky 2nd post', '2nd', 'Extended 2nd', 0),
	('test3', 1, '2014-11-17 00:00:00', '2014-11-18 05:00:55', 'Expires in the past', 'expired', '', 0);

create table security_role (
  id                        bigint auto_increment not null,
  role_name                 varchar(255),
  constraint pk_security_role primary key (id))
;

create table users (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  name                      varchar(255),
  last_login                datetime,
  active                    tinyint(1) default 0,
  email_validated           tinyint(1) default 0,
  constraint pk_users primary key (id))
;

create table users_security_role (
  users_id                       bigint not null,
  security_role_id               bigint not null,
  constraint pk_users_security_role primary key (users_id, security_role_id))
;

INSERT INTO security_role (id, role_name)
VALUES (1,"USER"),
    (2,"ADMIN"),
    (3,"SKATER"),
    (4,"PARENT");

alter table linked_account add constraint fk_linked_account_user_1 foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_linked_account_user_1 on linked_account (user_id);

alter table users_security_role add constraint fk_users_security_role_users_01 foreign key (users_id) references users (id) on delete restrict on update restrict;

alter table users_security_role add constraint fk_users_security_role_security_role_02 foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table linked_account;

drop table news_item;

drop table security_role;

drop table users;

drop table users_security_role;

SET FOREIGN_KEY_CHECKS=1;

