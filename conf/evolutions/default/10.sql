# --- !Ups

alter table users add membership_id bigint;
alter table users add public_id varchar(16);
alter table users add gift_card_balance decimal(13,2);
alter table users add promotional_credit decimal(13,2);

create table guardian (
  users_id                       bigint not null,
  membership_id                  bigint not null,
  constraint pk_guardian primary key (users_id, membership_id))
;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table users drop membership_id;
alter table users drop public_id;
alter table users drop gift_card_balance;
alter table users drop promotional_credit;

drop table guardian;

SET FOREIGN_KEY_CHECKS=1;