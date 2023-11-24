/* Tables */
CREATE TABLE IF NOT EXISTS uniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE ST_Domain
(
    id                   int                        NOT NULL,
    name                 varchar(100)               NOT NULL,
    description          varchar(400)               NULL,
    propFileName         varchar(100)               NOT NULL,
    className            varchar(100)               NOT NULL,
    authenticationServer varchar(100)               NOT NULL,
    theTimeStamp         varchar(100) DEFAULT ('0') NOT NULL,
    silverpeasServerURL  varchar(400)               NULL,
    sensitive            boolean      DEFAULT FALSE NOT NULL
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
    stateSaveDate                 TIMESTAMP            NOT NULL,
    notifManualReceiverLimit      INT,
    sensitiveData                 BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE st_delayednotifusersetting (
   id 			int NOT NULL ,
   userId		int NOT NULL ,
   channel		int NOT NULL ,
   frequency	varchar (4) NOT NULL
);

CREATE TABLE st_notificationresource (
   id 					int8 NOT NULL ,
   componentInstanceId	varchar(50) NOT NULL,
   resourceId			varchar(50) NOT NULL ,
   resourceType			varchar(50) NOT NULL ,
   resourceName			varchar(500) NOT NULL ,
   resourceDescription	varchar(2000) NULL ,
   resourceLocation		varchar(500) NOT NULL ,
   resourceUrl			varchar(1000) NULL,
   attachmentTargetId varchar(500) NULL,
   resourceDetails			varchar(8000) NULL
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
   message					varchar(2000) NULL
);

/* Indexes */
CREATE INDEX idx_st_dnus_userId ON st_delayednotifusersetting(userId);
CREATE INDEX idx_st_dnus_channel ON st_delayednotifusersetting(channel);
CREATE UNIQUE INDEX idx_st_dnus_uc ON st_delayednotifusersetting(userId, channel);

CREATE INDEX idx_st_nr_resourceId ON st_notificationresource(resourceId);

CREATE INDEX idx_st_dn_userId ON st_delayednotification(userId);
CREATE INDEX idx_st_dn_channel ON st_delayednotification(channel);

/* Constraints */
ALTER TABLE st_delayednotifusersetting
        ADD CONSTRAINT const_st_dnus_pk
        PRIMARY KEY (id);
-- ALTER TABLE st_delayednotifusersetting
--		ADD CONSTRAINT const_st_dnus_fk_userId
--		FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE st_notificationresource
        ADD CONSTRAINT const_st_nr_pk
        PRIMARY KEY (id);

ALTER TABLE st_delayednotification
        ADD CONSTRAINT const_st_dn_pk
        PRIMARY KEY (id);
ALTER TABLE st_delayednotification
		ADD CONSTRAINT const_st_dn_fk_nrId
		FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource(id);
--ALTER TABLE st_delayednotification
--		ADD CONSTRAINT const_st_dn_fk_userId
--		FOREIGN KEY (userId) REFERENCES ST_User(id);