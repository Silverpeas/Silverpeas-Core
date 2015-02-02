create table SR_RELLOG2
(
    SR_DATE            datetime              not null,
    SR_MODULE          varchar(100)          not null,
    SR_ACTION          varchar(32)           not null,
    SR_TXT             varchar(2000)         null
)
;

create table SR_PACKAGES
(
    SR_PACKAGE         varchar(32)           not null,
    SR_VERSION         char(3)               not null
)
;

create table SR_UNINSTITEMS
(
    SR_ITEM_ID         varchar(65)           not null,
    SR_PACKAGE         varchar(32)           not null,
    SR_ACTION_TAG      varchar(32)           not null,
    SR_ITEM_ORDER      smallint              not null,
    SR_FILE_NAME       varchar(256)          not null,
    SR_FILE_TYPE       varchar(32)           not null,
    SR_DELIMITER       varchar(256)          null    ,
    SR_KEEP_DELIMITER  smallint              null    ,
    SR_DBPROC_NAME     varchar(256)          null
)
;

create table SR_SCRIPTS
(
    SR_ITEM_ID         varchar(65)           not null,
    SR_SEQ_NUM         smallint              not null,
    SR_TEXT            varchar(1100)         not null
)
;

create table SR_DEPENDENCIES
(
    SR_PACKAGE         varchar(32)           not null,
    SR_PKDEPENDENCY    varchar(32)           not null
)
;
