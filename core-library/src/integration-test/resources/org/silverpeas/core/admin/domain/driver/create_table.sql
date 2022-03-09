CREATE TABLE IF NOT EXISTS SB_ContentManager_Instance
(
	instanceId	int		NOT NULL ,
	componentId	varchar(100)	NOT NULL ,
	containerType	varchar(100)	NOT NULL ,
	contentType	varchar(100)	NOT NULL
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
  CONSTRAINT PK_User PRIMARY KEY (id),
  CONSTRAINT UN_User_1 UNIQUE(specificId, domainId),
  CONSTRAINT UN_User_2 UNIQUE(login, domainId),
  CONSTRAINT FK_User_1 FOREIGN KEY(accessLevel) REFERENCES ST_AccessLevel(id)
);

CREATE TABLE IF NOT EXISTS ST_Group
(
  id            INT          NOT NULL,
  domainId      INT          NOT NULL,
  specificId    VARCHAR(500) NOT NULL,
  superGroupId  INT,
  name          VARCHAR(100) NOT NULL,
  description   VARCHAR(400),
  synchroRule   VARCHAR(100),
  creationDate  TIMESTAMP,
  saveDate      TIMESTAMP,
  state         VARCHAR(30)  NOT NULL,
  stateSaveDate TIMESTAMP    NOT NULL,
  CONSTRAINT PK_Group PRIMARY KEY (id),
  CONSTRAINT UN_Group_1 UNIQUE(specificId, domainId),
  CONSTRAINT UN_Group_2 UNIQUE(superGroupId, name, domainId),
  CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group(id)
);

CREATE TABLE IF NOT EXISTS ST_Group_User_Rel
(
  groupId INT NOT NULL,
  userId  INT NOT NULL,
  CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId),
  CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group(id),
  CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS ST_Space
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
  isPersonal           SMALLINT,
  CONSTRAINT PK_Space PRIMARY KEY (id),
  CONSTRAINT UN_Space_1 UNIQUE(domainFatherId, name),
  CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User(id),
  CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space(id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceI18N
(
  id          INT          NOT NULL,
  spaceId     INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE IF NOT EXISTS ST_ComponentInstance
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
  isInheritanceBlocked INT DEFAULT (0)  NOT NULL,
  CONSTRAINT PK_ComponentInstance PRIMARY KEY (id),
  CONSTRAINT UN_ComponentInstance_1 UNIQUE(spaceId, name),
  CONSTRAINT FK_ComponentInstance_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id),
  CONSTRAINT FK_ComponentInstance_2 FOREIGN KEY (createdBy) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS ST_ComponentInstanceI18N
(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE IF NOT EXISTS ST_Instance_Data
(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  name        VARCHAR(100) NOT NULL,
  label       VARCHAR(100) NOT NULL,
  value       VARCHAR(400),
  CONSTRAINT PK_Instance_Data PRIMARY KEY (id),
  CONSTRAINT UN_Instance_Data_1 UNIQUE(componentId, name),
  CONSTRAINT FK_Instance_Data_1 FOREIGN KEY (componentId) REFERENCES ST_ComponentInstance(id)
);

CREATE TABLE IF NOT EXISTS ST_UserRole
(
  id          INT             NOT NULL,
  instanceId  INT             NOT NULL,
  name        VARCHAR(100)    NULL,
  roleName    VARCHAR(100)    NOT NULL,
  description VARCHAR(400),
  isInherited INT DEFAULT (0) NOT NULL,
  objectId    INT,
  objectType  VARCHAR(50),
  CONSTRAINT PK_UserRole PRIMARY KEY (id),
  CONSTRAINT UN_UserRole_1 UNIQUE(instanceId, roleName, isInherited, objectId),
  CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance(id)
);

CREATE TABLE IF NOT EXISTS ST_UserRole_User_Rel
(
  userRoleId INT NOT NULL,
  userId     INT NOT NULL,
  CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId),
  CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id),
  CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS ST_UserRole_Group_Rel
(
  userRoleId INT NOT NULL,
  groupId    INT NOT NULL,
  CONSTRAINT PK_UserRole_Group_Rel PRIMARY KEY (userRoleId, groupId),
  CONSTRAINT FK_UserRole_Group_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id),
  CONSTRAINT FK_UserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceUserRole
(
  id          INT             NOT NULL,
  spaceId     INT             NOT NULL,
  name        VARCHAR(100)    NULL,
  roleName    VARCHAR(100)    NOT NULL,
  description VARCHAR(400),
  isInherited INT DEFAULT (0) NOT NULL,
  CONSTRAINT PK_SpaceUserRole PRIMARY KEY (id),
  CONSTRAINT UN_SpaceUserRole_1 UNIQUE(spaceId, roleName, isInherited),
  CONSTRAINT FK_SpaceUserRole_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceUserRole_User_Rel
(
  spaceUserRoleId INT NOT NULL,
  userId          INT NOT NULL,
  CONSTRAINT PK_SpaceUserRole_User_Rel PRIMARY KEY (spaceUserRoleId, userId),
  CONSTRAINT FK_SpaceUserRole_User_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id),
  CONSTRAINT FK_SpaceUserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceUserRole_Group_Rel
(
  spaceUserRoleId INT NOT NULL,
  groupId         INT NOT NULL,
  CONSTRAINT PK_SpaceUserRole_Group_Rel PRIMARY KEY (spaceUserRoleId, groupId),
  CONSTRAINT FK_SpaceUserRole_Group_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id),
  CONSTRAINT FK_SpaceUserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id)
);

CREATE TABLE IF NOT EXISTS DomainSP_Group (
  id           INT          NOT NULL,
  superGroupId INT          NULL,
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(400) NULL,
  CONSTRAINT PK_DomainSP_Group PRIMARY KEY (id),
  CONSTRAINT UN_DomainSP_Group_1 UNIQUE(superGroupId, name),
  CONSTRAINT FK_DomainSP_Group_1 FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group(id)
);

CREATE TABLE IF NOT EXISTS DomainSP_User (
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
  password      VARCHAR(123)          NULL,
  passwordValid CHAR(1) DEFAULT ('Y') NOT NULL,
  loginMail     VARCHAR(100)          NULL,
  email         VARCHAR(100)          NULL,
  CONSTRAINT PK_DomainSP_User PRIMARY KEY (id),
  CONSTRAINT UN_DomainSP_User_1 UNIQUE(login)
);

CREATE TABLE IF NOT EXISTS DomainSP_Group_User_Rel (
  groupId INT NOT NULL,
  userId  INT NOT NULL,
  CONSTRAINT PK_DomainSP_Group_User_Rel PRIMARY KEY (groupId,userId),
  CONSTRAINT FK_DomainSP_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES DomainSP_Group(id),
  CONSTRAINT FK_DomainSP_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES DomainSP_User(id)
);

CREATE TABLE IF NOT EXISTS ST_Domain (
  id                   INT                        NOT NULL,
  name                 VARCHAR(100)               NOT NULL,
  description          VARCHAR(400)               NULL,
  propFileName         VARCHAR(100)               NOT NULL,
  className            VARCHAR(100)               NOT NULL,
  authenticationServer VARCHAR(100)               NOT NULL,
  theTimeStamp         VARCHAR(100) DEFAULT ('0') NOT NULL,
  silverpeasServerURL  VARCHAR(400)               NULL,
  CONSTRAINT PK_ST_Domain PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ST_KeyStore (
  userKey  DECIMAL(18, 0) NOT NULL,
  login    VARCHAR(50)    NOT NULL,
  domainId INT            NOT NULL
);


CREATE TABLE IF NOT EXISTS ST_LongText (
  id          INT           NOT NULL,
  orderNum    INT           NOT NULL,
  bodyContent VARCHAR(2000) NOT NULL,
  CONSTRAINT PK_ST_LongText PRIMARY KEY (id,orderNum)
);

CREATE TABLE IF NOT EXISTS ST_GroupUserRole
(
  id       INT          NOT NULL,
  groupId  INT          NOT NULL,
  roleName VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS ST_GroupUserRole_User_Rel
(
  groupUserRoleId INT NOT NULL,
  userId          INT NOT NULL
);

CREATE TABLE IF NOT EXISTS ST_GroupUserRole_Group_Rel
(
  groupUserRoleId INT NOT NULL,
  groupId         INT NOT NULL
);

CREATE TABLE IF NOT EXISTS st_instance_modelused
(
  instanceId VARCHAR(50)               NOT NULL,
  modelId    VARCHAR(50)               NOT NULL,
  objectId   VARCHAR(50) DEFAULT ('0') NOT NULL,
  CONSTRAINT PK_st_instance_modelused PRIMARY KEY
    (
      instanceId,
      modelId,
      objectId
    )
);

CREATE TABLE IF NOT EXISTS ST_UserFavoriteSpaces
(
  id      INT NOT NULL,
  userid  INT NOT NULL,
  spaceid INT NOT NULL,
  CONSTRAINT PK_UserFavoriteSpaces PRIMARY KEY (id),
  CONSTRAINT FK_UserFavoriteSpaces_1 FOREIGN KEY (userid) REFERENCES ST_User(id),
  CONSTRAINT FK_UserFavoriteSpaces_2 FOREIGN KEY (spaceid) REFERENCES ST_Space(id)
);

CREATE TABLE IF NOT EXISTS ST_NotifChannel
(
  id int NOT NULL ,
  name varchar (20) NOT NULL ,
  description varchar (200) NULL ,
  couldBeAdded char (1) NOT NULL DEFAULT ('Y') ,
  fromAvailable char (1) NOT NULL DEFAULT ('N') ,
  subjectAvailable char (1) NOT NULL DEFAULT ('N'),
  CONSTRAINT PK_NotifChannel PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS ST_NotifAddress
(
  id int NOT NULL ,
  userId int NOT NULL ,
  notifName varchar (20) NOT NULL ,
  notifChannelId int NOT NULL ,
  address varchar (250) NOT NULL ,
  usage varchar (20) NULL ,
  priority int NOT NULL,
  CONSTRAINT PK_NotifAddress PRIMARY KEY(id),
  CONSTRAINT FK_NotifAddress_1 FOREIGN KEY(notifChannelId) REFERENCES ST_NotifChannel(id),
  CONSTRAINT FK_NotifAddress_2 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS ST_NotifDefaultAddress
(
  id int NOT NULL ,
  userId int NOT NULL ,
  notifAddressId int NOT NULL,
  CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY(id),
  CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS ST_NotifPreference (
  id int NOT NULL ,
  notifAddressId int NOT NULL ,
  componentInstanceId int NOT NULL ,
  userId int NOT NULL ,
  messageType int NOT NULL,
  CONSTRAINT PK_NotifAddr_Component PRIMARY KEY(id),
  CONSTRAINT FK_NotifPreference_1 FOREIGN KEY(componentInstanceId) REFERENCES ST_ComponentInstance (id),
  CONSTRAINT FK_NotifPreference_2 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS ST_NotifSendedReceiver (
  notifId int NOT NULL,
  userId int NOT NULL,
  CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY(notifId, userId)
);

CREATE TABLE IF NOT EXISTS st_delayednotifusersetting (
  id int NOT NULL ,
  userId int NOT NULL ,
  channel int NOT NULL ,
  frequency varchar (4) NOT NULL,
  CONSTRAINT const_st_dnus_pk PRIMARY KEY (id),
  CONSTRAINT const_st_dnus_fk_userId FOREIGN KEY (userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS st_notificationresource (
  id int8 NOT NULL ,
  componentInstanceId varchar(50) NOT NULL ,
  resourceId varchar(50) NOT NULL ,
  resourceType varchar(50) NOT NULL ,
  resourceName varchar(500) NOT NULL ,
  resourceDescription varchar(2000) NULL ,
  resourceLocation varchar(500) NOT NULL ,
  resourceUrl varchar(1000) NULL,
  CONSTRAINT const_st_nr_pk PRIMARY KEY (id)
);

CREATE TABLE st_delayednotification (
  id int8 NOT NULL ,
  userId int NOT NULL ,
  fromUserId int NOT NULL ,
  channel int NOT NULL ,
  action int NOT NULL ,
  notificationResourceId int8 NOT NULL ,
  language varchar(2) NOT NULL ,
  creationDate timestamp NOT NULL ,
  message varchar(2000) NULL,
  CONSTRAINT const_st_dn_pk PRIMARY KEY (id),
  CONSTRAINT const_st_dn_fk_nrId FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource(id),
  CONSTRAINT const_st_dn_fk_userId FOREIGN KEY (userId) REFERENCES ST_User(id)
);