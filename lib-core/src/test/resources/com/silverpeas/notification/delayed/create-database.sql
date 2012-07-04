/* Tables */
CREATE TABLE IF NOT EXISTS uniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
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
   resourceUrl			varchar(1000) NULL
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