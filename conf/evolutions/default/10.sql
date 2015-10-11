# --- !Ups

alter table users add membership_id bigint;

create table guardian (
  users_id                       bigint not null,
  membership_id                  bigint not null,
  constraint pk_guardian primary key (users_id, membership_id))
;

create table online_pass_sale (
  id                        bigint auto_increment NOT NULL,
  description               text,
  two_hour_passes           int NOT NULL,
  ten_pack_passes           int NOT NULL,
  unlimited_pass_months     int NOT NULL,
  gift_credit_dollars       int NOT NULL,
  created                   datetime NOT NULL,
  redeemed                  tinyint(1) NOT NULL DEFAULT 0,
  purchased_by_id           bigint NOT NULL,
  alternate_email_address   varchar(255),
  recipient_name            varchar(255),
  applied_to_id             bigint,
  constraint pk_online_sale primary key (id))
;

alter table audit_record add online_pass_sale_id bigint;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table users drop membership_id;

alter table audit_record drop column online_pass_sale_id;

drop table guardian;
drop table online_pass_sale;

SET FOREIGN_KEY_CHECKS=1;