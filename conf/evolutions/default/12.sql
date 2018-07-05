# --- !Ups

alter table camp add private_notes text;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table camp drop private_notes;

SET FOREIGN_KEY_CHECKS=1;