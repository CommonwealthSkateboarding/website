# --- !Ups

create table closure (
  id                        bigint auto_increment NOT NULL,
  message                   text,
  created                   datetime NOT NULL,
  created_by_id             bigint NOT NULL,
  enabled                   tinyint(1) NOT NULL DEFAULT 0,
  archived                  tinyint(1) NOT NULL DEFAULT 0,
  constraint pk_closure primary key (id))
;

alter table audit_record add closure_id bigint;

alter table registration add emergency_contact_name varchar(255);
alter table registration add emergency_telephone varchar(255);
alter table registration add alternate_emergency_contact_name varchar(255);
alter table registration add alternate_emergency_telephone varchar(255);

alter table membership add duplicate tinyint(1) NOT NULL;

alter table camp add early_registration_end_date datetime;
alter table camp add early_registration_discount decimal(13,2);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table closure;

alter table audit_record drop column closure_id;

alter table registration drop column emergency_contact_name;
alter table registration drop column emergency_telephone;
alter table registration drop column alternate_emergency_contact_name;
alter table registration drop column alternate_emergency_telephone;

alter table membership drop duplicate;

alter table camp drop early_registration_end_date;
alter table camp drop early_registration_discount;

SET FOREIGN_KEY_CHECKS=1;