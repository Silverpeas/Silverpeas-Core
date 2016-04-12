create table SR_RELLOG2
(
    SR_DATE            DATE                   not null,
    SR_MODULE          VARCHAR2(100)          not null,
    SR_ACTION          VARCHAR2(32)           not null,
    SR_TXT             VARCHAR2(2000)         null
)
;

create table SR_PACKAGES
(
    SR_PACKAGE         VARCHAR2(32)           not null,
    SR_VERSION         CHAR(3)                not null
)
;

create table SR_UNINSTITEMS
(
    SR_ITEM_ID         VARCHAR2(65)           not null,
    SR_PACKAGE         VARCHAR2(32)           not null,
    SR_ACTION_TAG      VARCHAR2(32)           not null,
    SR_ITEM_ORDER      SMALLINT               not null,
    SR_FILE_NAME       VARCHAR2(256)          not null,
    SR_FILE_TYPE       VARCHAR2(32)           not null,
    SR_DELIMITER       VARCHAR2(256)          null    ,
    SR_KEEP_DELIMITER  SMALLINT               null    ,
    SR_DBPROC_NAME     VARCHAR2(256)          null
)
;

create table SR_SCRIPTS
(
    SR_ITEM_ID         VARCHAR2(65)           not null,
    SR_SEQ_NUM         SMALLINT               not null,
    SR_TEXT            VARCHAR2(1100)         not null
)
;

create table SR_DEPENDENCIES
(
    SR_PACKAGE         VARCHAR2(32)           not null,
    SR_PKDEPENDENCY    VARCHAR2(32)           not null
)
;
