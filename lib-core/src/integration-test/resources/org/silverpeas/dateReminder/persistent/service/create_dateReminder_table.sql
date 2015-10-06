/* Tables */
CREATE TABLE IF NOT EXISTS ST_Domain (
	id			int NOT NULL ,
	name			varchar (100) NOT NULL ,
	description		varchar (400) NULL ,
	propFileName		varchar (100) NOT NULL ,
	className		varchar (100) NOT NULL ,
	authenticationServer	varchar (100) NOT NULL ,
  theTimeStamp            varchar (100) DEFAULT('0') NOT NULL ,
  silverpeasServerURL     varchar (400) NULL
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
  notifManualReceiverLimit      INT
);

CREATE TABLE IF NOT EXISTS st_dateReminder (
  id             VARCHAR(40) NOT NULL,
  resourceType   VARCHAR(50) NOT NULL,
  resourceId     VARCHAR(50) NOT NULL ,
  dateReminder   TIMESTAMP   NOT NULL ,
  message        VARCHAR(2000),
  processStatus  INT DEFAULT (0) NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate TIMESTAMP   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        INT8        NOT NULL
);

/* Constraints */
ALTER TABLE st_dateReminder ADD CONSTRAINT const_st_dateReminder_pk PRIMARY KEY (id);

/* Indexes */
CREATE UNIQUE INDEX idx_uc_st_dateReminder ON st_dateReminder(resourceType, resourceId);
