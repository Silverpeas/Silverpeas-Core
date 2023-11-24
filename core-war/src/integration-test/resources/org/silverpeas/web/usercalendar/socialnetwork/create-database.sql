-- System required tables

CREATE TABLE IF NOT EXISTS ST_Space
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

CREATE TABLE IF NOT EXISTS Personalization (
  id                  varchar(100) PRIMARY KEY NOT NULL,
  languages           varchar(100) NULL,
  zoneId              varchar(100) NULL,
  look                varchar(50)  NULL,
  personalWSpace      varchar(50)  NULL,
  thesaurusStatus     int          NOT NULL,
  dragAndDropStatus   int          DEFAULT 1,
  webdavEditingStatus int          DEFAULT 0,
  menuDisplay         varchar(50)  DEFAULT 'DEFAULT'
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
