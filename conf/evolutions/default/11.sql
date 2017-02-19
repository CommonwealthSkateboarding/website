# --- !Ups

alter table membership add promo_passes bigint not null default 0;
alter table membership add last_active datetime;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table membership drop promo_passes;
alter table membership drop last_active;

SET FOREIGN_KEY_CHECKS=1;