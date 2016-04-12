create table SR_RELLOG2
(
    SR_DATE            DATE                   not null,
    SR_MODULE          VARCHAR(100)          not null,
    SR_ACTION          VARCHAR(32)           not null,
    SR_TXT             VARCHAR(2000)         null
)
;

create table SR_PACKAGES
(
    SR_PACKAGE         VARCHAR(32)           not null,
    SR_VERSION         CHAR(3)                not null
)
;

create table SR_UNINSTITEMS
(
    SR_ITEM_ID         VARCHAR(65)           not null,
    SR_PACKAGE         VARCHAR(32)           not null,
    SR_ACTION_TAG      VARCHAR(32)           not null,
    SR_ITEM_ORDER      SMALLINT               not null,
    SR_FILE_NAME       VARCHAR(256)          not null,
    SR_FILE_TYPE       VARCHAR(32)           not null,
    SR_DELIMITER       VARCHAR(256)          null    ,
    SR_KEEP_DELIMITER  SMALLINT               null    ,
    SR_DBPROC_NAME     VARCHAR(256)          null
)
;

create table SR_SCRIPTS
(
    SR_ITEM_ID         VARCHAR(65)           not null,
    SR_SEQ_NUM         SMALLINT               not null,
    SR_TEXT            VARCHAR(1100)         not null
)
;

create table SR_DEPENDENCIES
(
    SR_PACKAGE         VARCHAR(32)           not null,
    SR_PKDEPENDENCY    VARCHAR(32)           not null
)
;
