# --- !Ups

alter table news_item modify id varchar(32);
alter table audit_record modify news_item_id varchar(32);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table news_item modify id bigint auto_increment not null;
alter table audit_record modify news_item_id bigint;

SET FOREIGN_KEY_CHECKS=1;