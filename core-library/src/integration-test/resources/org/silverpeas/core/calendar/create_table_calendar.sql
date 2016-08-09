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
  CONSTRAINT PK_User PRIMARY KEY (id),
  CONSTRAINT UN_User_1 UNIQUE (specificId, domainId),
  CONSTRAINT UN_User_2 UNIQUE (login, domainId),
  CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Calendar (
  id             VARCHAR(40)   NOT NULL,
  instanceId     VARCHAR(30)   NOT NULL,
  title          VARCHAR(2000) NOT NULL,
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

CREATE TABLE IF NOT EXISTS SB_Cal_Event (
  id             VARCHAR(40)   NOT NULL,
  calendarId     VARCHAR(40)   NOT NULL,
  inDays         BOOLEAN       NOT NULL,
  startDate      TIMESTAMP     NOT NULL,
  endDate        TIMESTAMP     NOT NULL,
  title          VARCHAR(2000) NOT NULL,
  description    VARCHAR(6000) NOT NULL,
  attributes     VARCHAR(40)   NULL,
  visibility     VARCHAR(50)   NOT NULL,
  priority       INT           NOT NULL,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  recurrenceId  VARCHAR(40)    NULL,
  CONSTRAINT PK_Event PRIMARY KEY (id),
  CONSTRAINT FK_Recurrence FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Attributes (
  id         VARCHAR(40) NOT NULL,
  name       VARCHAR(255) NOT NULL,
  value      VARCHAR(255) NOT NULL,
  CONSTRAINT PK_Attributes PRIMARY KEY (id, name)
);
