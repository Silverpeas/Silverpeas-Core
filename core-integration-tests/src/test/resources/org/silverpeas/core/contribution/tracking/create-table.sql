CREATE TABLE IF NOT EXISTS UniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS ST_AccessLevel
(
    id   CHAR(1)      NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
    CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS ST_User
(
    id                            INT                  NOT NULL,
    domainId                      INT                  NOT NULL,
    specificId                    VARCHAR(500)         NOT NULL,
    firstName                     VARCHAR(100),
    lastName                      VARCHAR(100)         NOT NULL,
    email                         VARCHAR(100),
    login                         VARCHAR(50)          NOT NULL,
    loginMail                     VARCHAR(100),
    accessLevel                   CHAR(1) DEFAULT 'U'  NOT NULL,
    loginquestion                 VARCHAR(200),
    loginanswer                   VARCHAR(200),
    creationDate                  TIMESTAMP,
    saveDate                      TIMESTAMP,
    version                       INT DEFAULT 0        NOT NULL,
    tosAcceptanceDate             TIMESTAMP,
    lastLoginDate                 TIMESTAMP,
    nbSuccessfulLoginAttempts     INT DEFAULT 0        NOT NULL,
    lastLoginCredentialUpdateDate TIMESTAMP,
    expirationDate                TIMESTAMP,
    state                         VARCHAR(30)          NOT NULL,
    stateSaveDate                 TIMESTAMP            NOT NULL,
    notifManualReceiverLimit      INT,
    sensitiveData                 BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT PK_User PRIMARY KEY (id),
    CONSTRAINT UN_User_1 UNIQUE(specificId, domainId),
    CONSTRAINT UN_User_2 UNIQUE(login, domainId),
    CONSTRAINT FK_User_1 FOREIGN KEY(accessLevel) REFERENCES ST_AccessLevel(id)
);

CREATE TABLE ST_ComponentInstance
(
    id                   int             NOT NULL,
    spaceId              int             NOT NULL,
    name                 varchar(100)    NOT NULL,
    componentName        varchar(100)    NOT NULL,
    description          varchar(400),
    createdBy            int,
    orderNum             int DEFAULT (0) NOT NULL,
    createTime           varchar(20),
    updateTime           varchar(20),
    removeTime           varchar(20),
    componentStatus      char(1),
    updatedBy            int,
    removedBy            int,
    isPublic             int DEFAULT (0) NOT NULL,
    isHidden             int DEFAULT (0) NOT NULL,
    lang                 char(2),
    isInheritanceBlocked int default (0) NOT NULL,
    CONSTRAINT PK_ComponentInstance PRIMARY KEY (id),
    CONSTRAINT UN_ComponentInstance_1 UNIQUE(spaceId, name)
);

CREATE TABLE SB_Node_Node
(
    nodeId           INT              NOT NULL,
    nodeName         VARCHAR(1000)    NOT NULL,
    nodeDescription  VARCHAR(2000)    NULL,
    nodeCreationDate VARCHAR(10)      NOT NULL,
    nodeCreatorId    VARCHAR(100)     NOT NULL,
    nodePath         VARCHAR(1000)    NOT NULL,
    nodeLevelNumber  INT              NOT NULL,
    nodeFatherId     INT              NOT NULL,
    modelId          VARCHAR(1000)    NULL,
    nodeStatus       VARCHAR(1000)    NULL,
    instanceId       VARCHAR(50)      NOT NULL,
    type             VARCHAR(50)      NULL,
    orderNumber      INT DEFAULT (0)  NULL,
    lang             CHAR(2),
    rightsDependsOn  INT DEFAULT (-1) NOT NULL,
    nodeRemovalDate  VARCHAR (10)     NULL,
    nodeRemoverId    VARCHAR (100)    NULL,
    CONSTRAINT PK_Node_Node PRIMARY KEY(nodeId, instanceId)
);

CREATE TABLE SB_Node_NodeI18N
(
    id              INT           NOT NULL,
    nodeId          INT           NOT NULL,
    lang            CHAR(2)       NOT NULL,
    nodeName        VARCHAR(1000) NOT NULL,
    nodeDescription VARCHAR(2000),
    CONSTRAINT PK_Node_NodeI18N PRIMARY KEY(id)
);

CREATE TABLE SB_SeeAlso_Link
(
    id			        INT		        NOT NULL,
    objectId		    INT		        NOT NULL,
    objectInstanceId	VARCHAR (50)	NOT NULL,
    targetId		    INT		        NOT NULL,
    targetInstanceId	VARCHAR (50)	NOT NULL
);

CREATE TABLE SB_Thumbnail_Thumbnail
(
    instanceId		        VARCHAR (50) NOT NULL,
    objectId              	INT	NOT NULL,
    objectType              INT	NOT NULL,
    originalAttachmentName  VARCHAR(250) NOT NULL,
    modifiedAttachmentName	VARCHAR(250) NULL,
    mimeType		        VARCHAR(250) NULL,
    xStart	                INT          NULL,
    yStart	                INT          NULL,
    xLength	                INT          NULL,
    yLength	                INT          NULL,
    CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY (objectId, objectType, instanceId)
);

CREATE TABLE SB_Notation_Notation
(
    id           INT         NOT NULL,
    instanceId   VARCHAR(50) NOT NULL,
    externalId   VARCHAR(50) NOT NULL,
    externalType VARCHAR(50) NOT NULL,
    author       VARCHAR(50) NOT NULL,
    note         INT         NOT NULL,
    CONSTRAINT PK_SB_Notation_Notation PRIMARY KEY (id),
    CONSTRAINT UN_SB_Notation_Notation UNIQUE (instanceId, externalId, externalType, author)
);

CREATE TABLE SB_Publication_Publi
(
  pubId                INT           NOT NULL,
  infoId               VARCHAR(50)   NULL,
  pubName              VARCHAR(400)  NOT NULL,
  pubDescription       VARCHAR(2000) NULL,
  pubCreationDate      VARCHAR(10)   NOT NULL,
  pubBeginDate         VARCHAR(10)   NOT NULL,
  pubEndDate           VARCHAR(10)   NOT NULL,
  pubCreatorId         VARCHAR(100)  NOT NULL,
  pubImportance        INT           NULL,
  pubVersion           VARCHAR(100)  NULL,
  pubKeywords          VARCHAR(1000) NULL,
  pubContent           VARCHAR(2000) NULL,
  pubStatus            VARCHAR(100)  NULL,
  pubUpdateDate        VARCHAR(10)   NULL,
  instanceId           VARCHAR(50)   NOT NULL,
  pubUpdaterId         VARCHAR(100)  NULL,
  pubValidateDate      VARCHAR(10)   NULL,
  pubValidatorId       VARCHAR(50)   NULL,
  pubBeginHour         VARCHAR(5)    NULL,
  pubEndHour           VARCHAR(5)    NULL,
  pubAuthor            VARCHAR(50)   NULL,
  pubTargetValidatorId VARCHAR(50)   NULL,
  pubCloneId           INT DEFAULT (-1),
  pubCloneStatus       VARCHAR(50)   NULL,
  lang                 CHAR(2)       NULL,
  pubdraftoutdate      VARCHAR(10)   NULL,
  pubRemovalDate       VARCHAR(10)	NULL,
  pubRemoverId         VARCHAR(100)	NULL,
  CONSTRAINT PK_Publication_Publi PRIMARY KEY(pubId)
);

CREATE TABLE SB_Publication_PubliFather
(
  pubId       INT             NOT NULL,
  nodeId      INT             NOT NULL,
  instanceId  VARCHAR(50)     NOT NULL,
  aliasUserId INT,
  aliasDate   VARCHAR(20),
  pubOrder    INT DEFAULT (0) NULL,
  CONSTRAINT PK_Publication_PubliFather PRIMARY KEY(pubId, nodeId, instanceId)
);

CREATE TABLE SB_Publication_PubliI18N
(
    id		int		NOT NULL,
    pubId		int		NOT NULL,
    lang		char (2)	NOT NULL,
    name		varchar (400)	NOT NULL,
    description	varchar (2000),
    keywords	varchar (1000),
    CONSTRAINT PK_Publication_PubliI18N PRIMARY KEY(id)
);

CREATE TABLE SB_Publication_Validation
(
    id		int		NOT NULL,
    pubId		int		NOT NULL,
    instanceId	varchar(50)	NOT NULL,
    userId		int		NOT NULL,
    decisionDate	varchar(20)	NOT NULL,
    decision	varchar(50)	NOT NULL
);

CREATE TABLE SB_Contribution_Tracking
(
    id                  VARCHAR(40)  NOT NULL,
    context             VARCHAR(255) NULL DEFAULT '',
    contrib_id          VARCHAR(40)  NOT NULL,
    contrib_type        VARCHAR(40)  NOT NULL,
    contrib_instanceId  VARCHAR(50)  NOT NULL,
    action_type         VARCHAR(20)  NOT NULL,
    action_date         TIMESTAMP    NOT NULL,
    action_by           VARCHAR(50)  NOT NULL,
    CONSTRAINT PK_CONTRIBUTION_TRACKING PRIMARY KEY (id)
);

CREATE INDEX IN_Publi_Father
    ON SB_Publication_PubliFather (nodeId);

CREATE INDEX IDX_SB_CONTRIBUTION_TRACKING_CONTRIBUTION
    ON SB_Contribution_Tracking (contrib_id, contrib_type, contrib_instanceId);