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

CREATE INDEX IN_Publi_Father
ON SB_Publication_PubliFather (nodeId);

CREATE TABLE SB_SeeAlso_Link
(
  id               INT         NOT NULL,
  objectId         INT         NOT NULL,
  objectInstanceId VARCHAR(50) NOT NULL,
  targetId         INT         NOT NULL,
  targetInstanceId VARCHAR(50) NOT NULL
);

CREATE TABLE SB_Publication_PubliI18N
(
  id          INT          NOT NULL,
  pubId       INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(400) NOT NULL,
  description VARCHAR(2000),
  keywords    VARCHAR(1000)
);

CREATE TABLE SB_Publication_Validation
(
  id           INT         NOT NULL,
  pubId        INT         NOT NULL,
  instanceId   VARCHAR(50) NOT NULL,
  userId       INT         NOT NULL,
  decisionDate VARCHAR(20) NOT NULL,
  decision     VARCHAR(50) NOT NULL
);

/*
CONSTRAINTS
 */

ALTER TABLE SB_Publication_Publi  ADD
CONSTRAINT PK_Publication_Publi
PRIMARY KEY (pubId);

ALTER TABLE SB_Publication_PubliFather  ADD
CONSTRAINT PK_Publication_PubliFather
PRIMARY KEY (pubId, nodeId, instanceId);

ALTER TABLE SB_SeeAlso_Link  ADD
CONSTRAINT PK_SeeAlso_Link
PRIMARY KEY (id);

ALTER TABLE SB_Publication_PubliI18N  ADD
CONSTRAINT PK_Publication_PubliI18N
PRIMARY KEY (id);