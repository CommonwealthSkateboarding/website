# --- !Ups

create table unheard_sale (
  id                        bigint auto_increment NOT NULL,
  brand                     varchar(64) NOT NULL,
  description               text,
  retail_price              decimal(13,2) NOT NULL,
  team_rider_sale           tinyint(1) NOT NULL,
  created                   datetime NOT NULL,
  sold_by_id                bigint NOT NULL,
  invoiced                  tinyint(1) NOT NULL DEFAULT 0,
  constraint pk_event primary key (id))
;

alter table membership add emergency_contact_name_b varchar(255);
alter table membership add emergency_contact_number_b varchar(255);
alter table membership add emergency_contact_name_c varchar(255);
alter table membership add emergency_contact_number_c varchar(255);

alter table registration add registrant_email varchar(255);
alter table registration add total_paid decimal(13,2) NOT NULL;

alter table camp add instructors varchar(255);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table unheard_sale;

alter table membership drop column emergency_contact_name_b;
alter table membership drop column emergency_contact_number_b;
alter table membership drop column emergency_contact_name_c;
alter table membership drop column emergency_contact_number_c;

alter table registration drop column registrant_email;
alter table registration drop column total_paid;

alter table camp drop column instructors;

SET FOREIGN_KEY_CHECKS=1;