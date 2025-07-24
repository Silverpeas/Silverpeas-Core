-- System required tables

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

CREATE TABLE IF NOT EXISTS SB_ContentManager_Instance
(
  instanceId    int NOT NULL ,
  componentId   varchar(100) NOT NULL ,
  containerType varchar(100) NOT NULL ,
  contentType   varchar(100) NOT NULL
);

CREATE TABLE ST_Token (
  id int8 NOT NULL ,
  tokenType varchar(50) NOT NULL ,
  resourceId varchar(50) NOT NULL ,
  token varchar(50) NOT NULL ,
  saveCount int NOT NULL ,
  saveDate timestamp NOT NULL,
  CONSTRAINT const_st_token_pk PRIMARY KEY (id)
);

-- User

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

-- User Notificication API

CREATE TABLE ST_NotifChannel (
  id int NOT NULL ,
  name varchar (20) NOT NULL ,
  description varchar (200) NULL ,
  couldBeAdded char (1) NOT NULL DEFAULT ('Y') ,
  fromAvailable char (1) NOT NULL DEFAULT ('N') ,
  subjectAvailable char (1) NOT NULL DEFAULT ('N'),
  CONSTRAINT PK_NotifChannel PRIMARY KEY(id)
);

CREATE TABLE ST_NotifAddress (
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

CREATE TABLE ST_NotifDefaultAddress (
  id int NOT NULL ,
  userId int NOT NULL ,
  notifAddressId int NOT NULL,
  CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY(id),
  CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE ST_NotifPreference (
  id int NOT NULL ,
  notifAddressId int NOT NULL ,
  componentInstanceId int NOT NULL ,
  userId int NOT NULL ,
  messageType int NOT NULL,
  CONSTRAINT PK_NotifAddr_Component PRIMARY KEY(id),
  CONSTRAINT FK_NotifPreference_2 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE ST_NotifSended (
  notifId		int		NOT NULL,
  userId		int		NOT NULL,
  messageType	int		NULL,
  notifDate	char (13)	NOT NULL,
  title		varchar (255)	NULL,
  link		varchar (255)	NULL,
  sessionId	varchar (255)	NULL,
  componentId	varchar (255)	NULL,
  body		int		NULL,
  CONSTRAINT PK_NotifSended PRIMARY KEY(notifId)
);

CREATE TABLE ST_NotifSendedReceiver (
  notifId		int		NOT NULL,
  userId		int		NOT NULL,
  CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY(notifId, userId)
);

CREATE TABLE st_delayednotifusersetting (
  id 			int NOT NULL ,
  userId		int NOT NULL ,
  channel		int NOT NULL ,
  frequency	varchar (4) NOT NULL,
  CONSTRAINT const_st_dnus_pk PRIMARY KEY (id),
  CONSTRAINT const_st_dnus_fk_userId FOREIGN KEY (userId) REFERENCES ST_User(id)
);

CREATE TABLE st_notificationresource (
  id 					int8 NOT NULL ,
  componentInstanceId	varchar(50) NOT NULL ,
  resourceId			varchar(50) NOT NULL ,
  resourceType			varchar(50) NOT NULL ,
  resourceName			varchar(500) NOT NULL ,
  resourceDescription	varchar(2000) NULL ,
  resourceLocation		varchar(500) NOT NULL ,
  resourceUrl			varchar(1000) NULL,
  CONSTRAINT const_st_nr_pk PRIMARY KEY (id)
);

CREATE TABLE st_delayednotification (
  id 						int8 NOT NULL ,
  userId					int NOT NULL ,
  fromUserId				int NOT NULL ,
  channel					int NOT NULL ,
  action					int NOT NULL ,
  notificationResourceId	int8 NOT NULL ,
  language					varchar(2) NOT NULL ,
  creationDate				timestamp NOT NULL ,
  message					varchar(2000) NULL,
  CONSTRAINT const_st_dn_pk PRIMARY KEY (id),
  CONSTRAINT const_st_dn_fk_nrId FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource(id),
  CONSTRAINT const_st_dn_fk_userId FOREIGN KEY (userId) REFERENCES ST_User(id)
);

-- Calendar API

CREATE TABLE IF NOT EXISTS SB_Cal_Calendar (
  id             VARCHAR(40)   NOT NULL,
  instanceId     VARCHAR(30)   NOT NULL,
  title          VARCHAR(255)  NOT NULL,
  zoneId         VARCHAR(40)   NOT NULL,
  externalUrl    VARCHAR(250),
  synchroDate    TIMESTAMP,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  CONSTRAINT PK_CALENDAR PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Recurrence (
  id                   VARCHAR(40)  NOT NULL,
  recur_periodInterval INT          NOT NULL,
  recur_periodUnit     VARCHAR(5)   NOT NULL,
  recur_count          INT,
  recur_endDate        TIMESTAMP,
  CONSTRAINT PK_RECURRENCE PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Recurrence_DayOfWeek (
  recurrenceId    VARCHAR(40) NOT NULL,
  recur_nth       INT         NOT NULL,
  recur_dayOfWeek INT         NOT NULL,
  CONSTRAINT FK_Recurrence_DayOfWeek FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Recurrence_Exception (
  recurrenceId        VARCHAR(40) NOT NULL,
  recur_exceptionDate TIMESTAMP   NOT NULL,
  CONSTRAINT FK_Recurrence_Exception FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Components (
  id             VARCHAR(40)   NOT NULL,
  calendarId     VARCHAR(40)   NOT NULL,
  startDate      TIMESTAMP     NOT NULL,
  endDate        TIMESTAMP     NOT NULL,
  inDays         BOOLEAN       NOT NULL,
  title          VARCHAR(255)  NOT NULL,
  description    VARCHAR(2000) NOT NULL,
  location       VARCHAR(255)  NULL,
  attributes     VARCHAR(40)   NULL,
  priority       INT           NOT NULL,
  sequence       INT8          NOT NULL DEFAULT 0,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  CONSTRAINT PK_CalComponent PRIMARY KEY (id),
  CONSTRAINT FK_Calendar     FOREIGN KEY (calendarId) REFERENCES SB_Cal_Calendar(id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Event (
  id             VARCHAR(40)   NOT NULL,
  externalId     VARCHAR(255)  NULL,
  synchroDate    TIMESTAMP,
  componentId    VARCHAR(40)   NOT NULL,
  visibility     VARCHAR(50)   NOT NULL,
  recurrenceId   VARCHAR(40)   NULL,
  CONSTRAINT PK_Event            PRIMARY KEY (id),
  CONSTRAINT FK_Event_Component  FOREIGN KEY (componentId)  REFERENCES SB_Cal_Components(id),
  CONSTRAINT FK_Event_Recurrence FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Occurrences (
  id             VARCHAR(60)   NOT NULL,
  eventId        VARCHAR(40)   NOT NULL,
  componentId    VARCHAR(40)   NOT NULL,
  CONSTRAINT PK_Occurrence           PRIMARY KEY (id),
  CONSTRAINT FK_Occurrence_Event     FOREIGN KEY (eventId)     REFERENCES SB_Cal_Event,
  CONSTRAINT FK_Occurrence_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components(id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Attributes (
  id         VARCHAR(40)  NOT NULL,
  name       VARCHAR(255) NOT NULL,
  value      VARCHAR(255) NOT NULL,
  CONSTRAINT PK_Attributes PRIMARY KEY (id, name)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Categories (
  id       VARCHAR(40)  NOT NULL,
  category VARCHAR(255) NOT NULL,
  CONSTRAINT Pk_Categories PRIMARY KEY (id, category)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Attendees (
  id                VARCHAR(40) NOT NULL,
  attendeeId        VARCHAR(40) NOT NULL,
  componentId       VARCHAR(40) NOT NULL,
  type              INT         NOT NULL,
  participation     VARCHAR(12) NOT NULL DEFAULT 'AWAITING',
  presence          VARCHAR(12) NOT NULL DEFAULT 'REQUIRED',
  delegate          VARCHAR(40) NULL,
  createDate        TIMESTAMP   NOT NULL,
  createdBy         VARCHAR(40) NOT NULL,
  lastUpdateDate    TIMESTAMP   NOT NULL,
  lastUpdatedBy     VARCHAR(40) NOT NULL,
  version           INT8        NOT NULL,
  CONSTRAINT PK_Attendee           PRIMARY KEY (id),
  CONSTRAINT FK_Attendee_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components(id),
  CONSTRAINT FK_Delegate           FOREIGN KEY (delegate) REFERENCES SB_Cal_Attendees(id)
);
