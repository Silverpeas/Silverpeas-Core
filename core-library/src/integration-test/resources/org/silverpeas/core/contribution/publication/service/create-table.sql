CREATE TABLE IF NOT EXISTS UniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
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
    isInheritanceBlocked int default (0) NOT NULL
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
  pubdraftoutdate      VARCHAR(10)   NULL
);

CREATE TABLE SB_Publication_PubliFather
(
  pubId       INT             NOT NULL,
  nodeId      INT             NOT NULL,
  instanceId  VARCHAR(50)     NOT NULL,
  aliasUserId INT,
  aliasDate   VARCHAR(20),
  pubOrder    INT DEFAULT (0) NULL
);
