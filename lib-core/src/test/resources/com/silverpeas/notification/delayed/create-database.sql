/* Tables */
CREATE TABLE uniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE st_delayednotificationusersetting (
   id 			int NOT NULL ,
   userId		int NOT NULL ,
   channel		int NOT NULL ,
   frequency	varchar (4) NOT NULL
);

CREATE TABLE st_notificationresource (
   id 					int NOT NULL ,
   componentInstanceId	int NOT NULL default -1 ,
   resourceId			varchar(50) NOT NULL ,
   resourceType			varchar(50) NOT NULL ,
   resourceName			varchar(500) NOT NULL ,
   resourceDescription	varchar(2000) NULL ,
   resourceLocation		varchar(500) NOT NULL ,
   resourceUrl			varchar(1000) NULL
);

CREATE TABLE st_delayednotification (
   id 						int NOT NULL ,
   userId					int NOT NULL ,
   fromUserId				int NOT NULL ,
   channel					int NOT NULL ,
   action					int NOT NULL ,
   notificationResourceId	int NOT NULL ,
   language					varchar(2) NOT NULL ,
   creationDate				timestamp NOT NULL ,
   message					varchar(2000) NULL
);

/* Indexes */
CREATE INDEX idx_st_delayednotificationusersetting_id ON st_delayednotificationusersetting(id);
CREATE INDEX idx_st_delayednotificationusersetting_userId ON st_delayednotificationusersetting(userId);
CREATE INDEX idx_st_delayednotificationusersetting_channel ON st_delayednotificationusersetting(channel);
CREATE UNIQUE INDEX idx_st_delayednotificationusersetting_uc ON st_delayednotificationusersetting(userId, channel);

CREATE INDEX idx_st_notificationresource_id ON st_notificationresource(id);
CREATE INDEX idx_st_notificationresource_resourceId ON st_notificationresource(resourceId);

CREATE INDEX idx_st_delayednotification_id ON st_delayednotification(id);
CREATE INDEX idx_st_delayednotification_userId ON st_delayednotification(userId);
CREATE INDEX idx_st_delayednotification_channel ON st_delayednotification(channel);

/* Constraints */
ALTER TABLE st_delayednotificationusersetting
        ADD CONSTRAINT const_st_delayednotificationusersetting_pk
        PRIMARY KEY (id);
-- ALTER TABLE st_delayednotificationusersetting
--		ADD CONSTRAINT const_st_delayednotificationusersetting_fk_userId
--		FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE st_notificationresource
        ADD CONSTRAINT const_st_notificationresource_pk
        PRIMARY KEY (id);

ALTER TABLE st_delayednotification
        ADD CONSTRAINT const_st_delayednotification_pk
        PRIMARY KEY (id);
ALTER TABLE st_delayednotification
		ADD CONSTRAINT const_st_delayednotification_fk_notificationResourceId
		FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource(id);
--ALTER TABLE st_delayednotification
--		ADD CONSTRAINT const_st_delayednotification_fk_userId
--		FOREIGN KEY (userId) REFERENCES ST_User(id);