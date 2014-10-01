CREATE TABLE IF NOT EXISTS UniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);

CREATE TABLE DomainSP_Group (
  id           INT          NOT NULL,
  superGroupId INT          NULL,
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(400) NULL
);

CREATE TABLE DomainSP_User (
  id            INT                   NOT NULL,
  firstName     VARCHAR(100)          NULL,
  lastName      VARCHAR(100)          NOT NULL,
  phone         VARCHAR(20)           NULL,
  homePhone     VARCHAR(20)           NULL,
  cellPhone     VARCHAR(20)           NULL,
  fax           VARCHAR(20)           NULL,
  address       VARCHAR(500)          NULL,
  title         VARCHAR(100)          NULL,
  company       VARCHAR(100)          NULL,
  position      VARCHAR(100)          NULL,
  boss          VARCHAR(100)          NULL,
  login         VARCHAR(50)           NOT NULL,
  password      VARCHAR(32)           NULL,
  passwordValid CHAR(1) DEFAULT ('Y') NOT NULL,
  loginMail     VARCHAR(100)          NULL,
  email         VARCHAR(100)          NULL
);


CREATE TABLE ST_User
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
  stateSaveDate                 TIMESTAMP            NOT NULL
);

CREATE TABLE ST_Group
(
  id           INT          NOT NULL,
  domainId     INT          NOT NULL,
  specificId   VARCHAR(500) NOT NULL,
  superGroupId INT,
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(400),
  synchroRule  VARCHAR(100)
);

CREATE TABLE ST_Group_User_Rel
(
  groupId INT NOT NULL,
  userId  INT NOT NULL
);

CREATE TABLE ST_Space
(
  id                   INT             NOT NULL,
  domainFatherId       INT,
  name                 VARCHAR(100)    NOT NULL,
  description          VARCHAR(400),
  createdBy            INT,
  firstPageType        INT             NOT NULL,
  firstPageExtraParam  VARCHAR(400),
  orderNum             INT DEFAULT (0) NOT NULL,
  createTime           VARCHAR(20),
  updateTime           VARCHAR(20),
  removeTime           VARCHAR(20),
  spaceStatus          CHAR(1),
  updatedBy            INT,
  removedBy            INT,
  lang                 CHAR(2),
  isInheritanceBlocked INT DEFAULT (0) NOT NULL,
  look                 VARCHAR(50),
  displaySpaceFirst    SMALLINT,
  isPersonal           SMALLINT
);

CREATE TABLE ST_SpaceI18N
(
  id          INT          NOT NULL,
  spaceId     INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE ST_Component (
  id            INT          NOT NULL,
  componentName VARCHAR(100) NOT NULL,
  description   VARCHAR(400) NULL
);

CREATE TABLE ST_ComponentInstance
(
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

CREATE TABLE ST_ComponentInstanceI18N
(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE ST_Domain (
  id                   INT                        NOT NULL,
  name                 VARCHAR(100)               NOT NULL,
  description          VARCHAR(400)               NULL,
  propFileName         VARCHAR(100)               NOT NULL,
  className            VARCHAR(100)               NOT NULL,
  authenticationServer VARCHAR(100)               NOT NULL,
  theTimeStamp         VARCHAR(100) DEFAULT ('0') NOT NULL,
  silverpeasServerURL  VARCHAR(400)               NULL
);

CREATE TABLE DomainSP_Group_User_Rel (
  groupId INT NOT NULL,
  userId  INT NOT NULL
);

CREATE TABLE ST_Instance_Data
(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  name        VARCHAR(100) NOT NULL,
  label       VARCHAR(100) NOT NULL,
  value       VARCHAR(400)
);

CREATE TABLE ST_UserRole
(
  id          INT             NOT NULL,
  instanceId  INT             NOT NULL,
  name        VARCHAR(100)    NULL,
  roleName    VARCHAR(100)    NOT NULL,
  description VARCHAR(400),
  isInherited INT DEFAULT (0) NOT NULL,
  objectId    INT,
  objectType  VARCHAR(50)
);

CREATE TABLE ST_UserRole_User_Rel
(
  userRoleId INT NOT NULL,
  userId     INT NOT NULL
);

CREATE TABLE ST_UserRole_Group_Rel
(
  userRoleId INT NOT NULL,
  groupId    INT NOT NULL
);

CREATE TABLE ST_SpaceUserRole
(
  id          INT             NOT NULL,
  spaceId     INT             NOT NULL,
  name        VARCHAR(100)    NULL,
  roleName    VARCHAR(100)    NOT NULL,
  description VARCHAR(400),
  isInherited INT DEFAULT (0) NOT NULL
);

CREATE TABLE ST_SpaceUserRole_User_Rel
(
  spaceUserRoleId INT NOT NULL,
  userId          INT NOT NULL
);

CREATE TABLE ST_SpaceUserRole_Group_Rel
(
  spaceUserRoleId INT NOT NULL,
  groupId         INT NOT NULL
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
  rightsDependsOn  INT DEFAULT (-1) NOT NULL
);