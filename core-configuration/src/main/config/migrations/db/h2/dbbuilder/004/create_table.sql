create table SR_PACKAGES
(
    SR_PACKAGE         VARCHAR(32)           not null,
    SR_VERSION         CHAR(3)                not null
)
;

alter table SR_PACKAGES add constraint PK_SR_PACKAGES primary key (SR_PACKAGE)
;
