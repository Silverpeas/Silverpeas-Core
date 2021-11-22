CREATE TABLE SB_Node_Node
(
    nodeId           int              NOT NULL,
    nodeName         varchar(1000)    NOT NULL,
    nodeDescription  varchar(2000)    NULL,
    nodeCreationDate varchar(10)      NOT NULL,
    nodeCreatorId    varchar(100)     NOT NULL,
    nodePath         varchar(1000)    NOT NULL,
    nodeLevelNumber  int              NOT NULL,
    nodeFatherId     int              NOT NULL,
    modelId          varchar(1000)    NULL,
    nodeStatus       varchar(1000)    NULL,
    instanceId       varchar(50)      NOT NULL,
    type             varchar(50)      NULL,
    orderNumber      int DEFAULT (0)  NULL,
    lang             char(2),
    rightsDependsOn  int default (-1) NOT NULL,
    constraint PK_Node_Node primary key (nodeId, instanceId)
);

CREATE TABLE SB_Publication_Publi
(
    pubId                int           NOT NULL,
    infoId               varchar(50)   NULL,
    pubName              varchar(400)  NOT NULL,
    pubDescription       varchar(2000) NULL,
    pubCreationDate      varchar(10)   NOT NULL,
    pubBeginDate         varchar(10)   NOT NULL,
    pubEndDate           varchar(10)   NOT NULL,
    pubCreatorId         varchar(100)  NOT NULL,
    pubImportance        int           NULL,
    pubVersion           varchar(100)  NULL,
    pubKeywords          varchar(1000) NULL,
    pubContent           varchar(2000) NULL,
    pubStatus            varchar(100)  NULL,
    pubUpdateDate        varchar(10)   NULL,
    instanceId           varchar(50)   NOT NULL,
    pubUpdaterId         varchar(100)  NULL,
    pubValidateDate      varchar(10)   NULL,
    pubValidatorId       varchar(50)   NULL,
    pubBeginHour         varchar(5)    NULL,
    pubEndHour           varchar(5)    NULL,
    pubAuthor            varchar(50)   NULL,
    pubTargetValidatorId varchar(50)   NULL,
    pubCloneId           int DEFAULT (-1),
    pubCloneStatus       varchar(50)   NULL,
    lang                 char(2)       NULL,
    pubdraftoutdate      varchar(10)   NULL,
    constraint PK_Publication_Publi primary key (pubId)
);

CREATE TABLE SB_Publication_PubliFather
(
    pubId       int             NOT NULL,
    nodeId      int             NOT NULL,
    instanceId  varchar(50)     NOT NULL,
    aliasUserId int,
    aliasDate   varchar(20),
    pubOrder    int DEFAULT (0) NULL,
    constraint PK_Publication_PubliFather primary key (pubId, nodeId, instanceId)
);

CREATE TABLE SB_Publication_PubliI18N
(
    id          int          NOT NULL,
    pubId       int          NOT NULL,
    lang        char(2)      NOT NULL,
    name        varchar(400) NOT NULL,
    description varchar(2000),
    keywords    varchar(1000),
    CONSTRAINT PK_Publication_PubliI18N PRIMARY KEY (id)
);

CREATE TABLE SB_Thumbnail_Thumbnail
(
    instanceId             varchar(50)  NOT NULL,
    objectId               int          NOT NULL,
    objectType             int          NOT NULL,
    originalAttachmentName varchar(250) NOT NULL,
    modifiedAttachmentName varchar(250) NULL,
    mimeType               varchar(250) NULL,
    xStart                 int          NULL,
    yStart                 int          NULL,
    xLength                int          NULL,
    yLength                int          NULL,
    CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY (objectId, objectType, instanceId)
);