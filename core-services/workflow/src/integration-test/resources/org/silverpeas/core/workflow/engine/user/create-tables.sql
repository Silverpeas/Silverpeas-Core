CREATE TABLE ST_Space
(
  id                    int PRIMARY KEY NOT NULL,
  domainFatherId        int,
  name                  varchar(100)  NOT NULL,
  description           varchar(400),
  createdBy             int,
  firstPageType         int NOT NULL,
  firstPageExtraParam   varchar(400),
  orderNum              int DEFAULT (0) NOT NULL,
  createTime            varchar(20),
  updateTime            varchar(20),
  removeTime            varchar(20),
  spaceStatus           char(1),
  updatedBy             int,
  removedBy             int,
  lang                  char(2),
  isInheritanceBlocked  int default(0) NOT NULL,
  look                  varchar(50),
  displaySpaceFirst     smallint,
  isPersonal            smallint
);

CREATE TABLE ST_SpaceI18N (
  id          INT          NOT NULL,
  spaceId     INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE ST_ComponentInstance (
  id                   INT              NOT NULL,
  spaceId              INT              NOT NULL,
  name                 VARCHAR(100)     NOT NULL,
  componentName        VARCHAR(100)     NOT NULL,
  description          VARCHAR(400),
  createdBy            INT,
  orderNum             INT DEFAULT (0)  NOT NULL,
  createTime           VARCHAR(20),
  updateTime           VARCHAR(20),
  removeTime           VARCHAR(20),
  componentStatus      CHAR(1),
  updatedBy            INT,
  removedBy            INT,
  isPublic             INT DEFAULT (0)  NOT NULL,
  isHidden             INT DEFAULT (0)  NOT NULL,
  lang                 CHAR(2),
  isInheritanceBlocked INT DEFAULT (0)  NOT NULL
);

CREATE TABLE ST_ComponentInstanceI18N (
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE ST_Instance_Data (
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  name        VARCHAR(100) NOT NULL,
  label       VARCHAR(100) NOT NULL,
  value       VARCHAR(400)
);

CREATE TABLE SB_ContentManager_Instance
(
  instanceId    int NOT NULL ,
  componentId   varchar(100) NOT NULL ,
  containerType varchar(100) NOT NULL ,
  contentType   varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS ST_AccessLevel
(
  id   CHAR(1)      NOT NULL,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
  CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS ST_User (
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
  CONSTRAINT UN_User_1 UNIQUE (specificId, domainId),
  CONSTRAINT UN_User_2 UNIQUE (login, domainId),
  CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id)
);

CREATE TABLE ST_Group (
  id            INT          NOT NULL,
  domainId      INT          NOT NULL,
  specificId    VARCHAR(500) NOT NULL,
  superGroupId  INT,
  name          VARCHAR(100) NOT NULL,
  description   VARCHAR(400),
  synchroRule   VARCHAR(100),
  creationDate  timestamp,
  saveDate      timestamp,
  state         varchar(30)  NOT NULL,
  stateSaveDate timestamp    NOT NULL,
  CONSTRAINT PK_Group   PRIMARY KEY (id),
  CONSTRAINT UN_Group_1 UNIQUE (specificId, domainId),
  CONSTRAINT UN_Group_2 UNIQUE (superGroupId, name, domainId),
  CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group (id)
);

CREATE TABLE ST_Group_User_Rel (
  groupId INT NOT NULL,
  userId  INT NOT NULL,
  CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId),
  CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group (id),
  CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE ST_UserRole (
  id          INT             NOT NULL,
  instanceId  INT             NOT NULL,
  name        VARCHAR(100)    NULL,
  roleName    VARCHAR(100)    NOT NULL,
  description VARCHAR(400),
  isInherited INT DEFAULT (0) NOT NULL,
  objectId    INT,
  objectType  VARCHAR(50),
  CONSTRAINT PK_UserRole PRIMARY KEY (id),
  CONSTRAINT UN_UserRole_1 UNIQUE (instanceId, roleName, isInherited, objectId, objectType),
  CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance (id)
);

CREATE TABLE ST_UserRole_User_Rel (
  userRoleId INT NOT NULL,
  userId     INT NOT NULL,
  CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId),
  CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole (id),
  CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE IF NOT EXISTS SB_Workflow_Replacements (
  id             VARCHAR(40)   NOT NULL,
  incumbentId    VARCHAR(40)   NOT NULL,
  substituteId   VARCHAR(40)   NOT NULL,
  workflowId     VARCHAR(40)   NOT NULL,
  startDate      DATE          NOT NULL,
  endDate        DATE          NOT NULL,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  CONSTRAINT PK_REPLACEMENT PRIMARY KEY (id)
)