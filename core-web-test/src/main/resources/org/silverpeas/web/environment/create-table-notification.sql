CREATE TABLE ST_LongText
(
    id          INT           NOT NULL,
    orderNum    INT           NOT NULL,
    bodyContent VARCHAR(2000) NOT NULL,
    CONSTRAINT PK_ST_LongText PRIMARY KEY (id, orderNum)
);

CREATE TABLE ST_ServerMessage
(
    ID        int           NOT NULL,
    USERID    int           NOT NULL,
    HEADER    varchar(255)  NULL,
    SUBJECT   varchar(1024) NULL,
    BODY      varchar(4000) NULL,
    SESSIONID varchar(255)  NULL,
    TYPE      char(1)       NULL
);

CREATE TABLE ST_SilverMailMessage
(
    ID         int           NOT NULL,
    USERID     int           NOT NULL,
    FOLDERID   int           NULL,
    HEADER     varchar(255)  NULL,
    SENDERNAME varchar(255)  NULL,
    SUBJECT    varchar(1024) NULL,
    BODY       varchar(4000) NULL,
    SOURCE     varchar(255)  NULL,
    URL        varchar(255)  NULL,
    DATEMSG    varchar(255)  NULL,
    READEN     int           NOT NULL
);

CREATE TABLE ST_PopupMessage
(
    ID            int           NOT NULL,
    USERID        int           NOT NULL,
    BODY          varchar(4000) NULL,
    SENDERID      varchar(10)   NULL,
    SENDERNAME    varchar(200)  NULL,
    ANSWERALLOWED char(1) default '0',
    SOURCE        varchar(255)  NULL,
    URL           varchar(255)  NULL,
    MSGDATE       varchar(10)   NULL,
    MSGTIME       varchar(5)    NULL
);

CREATE TABLE ST_NotifChannel
(
    id               int          NOT NULL,
    name             varchar(20)  NOT NULL,
    description      varchar(200) NULL,
    couldBeAdded     char(1)      NOT NULL DEFAULT ('Y'),
    fromAvailable    char(1)      NOT NULL DEFAULT ('N'),
    subjectAvailable char(1)      NOT NULL DEFAULT ('N'),
    CONSTRAINT PK_NotifChannel PRIMARY KEY (id)
);

CREATE TABLE ST_NotifAddress
(
    id             int          NOT NULL,
    userId         int          NOT NULL,
    notifName      varchar(20)  NOT NULL,
    notifChannelId int          NOT NULL,
    address        varchar(250) NOT NULL,
    usage          varchar(20)  NULL,
    priority       int          NOT NULL,
    CONSTRAINT PK_NotifAddress PRIMARY KEY (id),
    CONSTRAINT FK_NotifAddress_1 FOREIGN KEY (notifChannelId) REFERENCES ST_NotifChannel (id),
    CONSTRAINT FK_NotifAddress_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE ST_NotifDefaultAddress
(
    id             int NOT NULL,
    userId         int NOT NULL,
    notifAddressId int NOT NULL,
    CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY (id),
    CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE ST_NotifPreference
(
    id                  int NOT NULL,
    notifAddressId      int NOT NULL,
    componentInstanceId int NOT NULL,
    userId              int NOT NULL,
    messageType         int NOT NULL,
    CONSTRAINT PK_NotifAddr_Component PRIMARY KEY (id),
    CONSTRAINT FK_NotifPreference_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE ST_NotifSended
(
    notifId     int          NOT NULL,
    userId      int          NOT NULL,
    messageType int          NULL,
    notifDate   char(13)     NOT NULL,
    title       varchar(255) NULL,
    link        varchar(255) NULL,
    sessionId   varchar(255) NULL,
    componentId varchar(255) NULL,
    body        int          NULL,
    CONSTRAINT PK_NotifSended PRIMARY KEY (notifId)
);

CREATE TABLE ST_NotifSendedReceiver
(
    notifId int NOT NULL,
    userId  int NOT NULL,
    CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY (notifId, userId)
);

CREATE TABLE st_delayednotifusersetting
(
    id        int        NOT NULL,
    userId    int        NOT NULL,
    channel   int        NOT NULL,
    frequency varchar(4) NOT NULL,
    CONSTRAINT const_st_dnus_pk PRIMARY KEY (id),
    CONSTRAINT const_st_dnus_fk_userId FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE st_notificationresource
(
    id                  int8          NOT NULL,
    componentInstanceId varchar(50)   NOT NULL,
    resourceId          varchar(50)   NOT NULL,
    resourceType        varchar(50)   NOT NULL,
    resourceName        varchar(500)  NOT NULL,
    resourceDescription varchar(2000) NULL,
    resourceLocation    varchar(500)  NOT NULL,
    resourceUrl         varchar(1000) NULL,
    CONSTRAINT const_st_nr_pk PRIMARY KEY (id)
);

CREATE TABLE st_delayednotification
(
    id                     int8          NOT NULL,
    userId                 int           NOT NULL,
    fromUserId             int           NOT NULL,
    channel                int           NOT NULL,
    action                 int           NOT NULL,
    notificationResourceId int8          NOT NULL,
    language               varchar(2)    NOT NULL,
    creationDate           timestamp     NOT NULL,
    message                varchar(2000) NULL,
    CONSTRAINT const_st_dn_pk PRIMARY KEY (id),
    CONSTRAINT const_st_dn_fk_nrId FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource (id),
    CONSTRAINT const_st_dn_fk_userId FOREIGN KEY (userId) REFERENCES ST_User (id)
);

INSERT INTO ST_NotifChannel
VALUES (1, 'SMTP', '', 'Y', 'E', 'Y');
INSERT INTO ST_NotifChannel
VALUES (2, 'SMS', '', 'Y', ' ', 'N');
INSERT INTO ST_NotifChannel
VALUES (3, 'POPUP', '', 'N', 'I', 'N');
INSERT INTO ST_NotifChannel
VALUES (4, 'SILVERMAIL', '', 'N', 'I', 'Y');
INSERT INTO ST_NotifChannel
VALUES (5, 'REMOVE', '', 'N', ' ', 'N');
INSERT INTO ST_NotifChannel
VALUES (6, 'SERVER', '', 'N', ' ', 'N');