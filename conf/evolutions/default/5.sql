# --- !Ups

alter table event add registrable tinyint(1) NOT NULL DEFAULT 0;
alter table event add registration_end_date datetime;
alter table event add max_registrations int(11);
alter table event add cost decimal(13,2);

alter table registration add event_id bigint;
alter table registration add registration_type varchar(16) NOT NULL;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table event drop column registrable;
alter table event drop column registration_end_date;
alter table event drop column max_registrations;
alter table event drop column cost;

alter table registration drop column event_id;
alter table registration drop column registration_type;

SET FOREIGN_KEY_CHECKS=1;