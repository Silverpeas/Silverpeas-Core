CREATE TABLE uniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE sp_delayednotificationusersetting (
   id 			int NOT NULL ,
   userId		int NOT NULL ,
   channel		int NOT NULL ,
   frequency	varchar (4) NOT NULL
);

CREATE INDEX idx_sp_delayednotificationusersetting_1 ON sp_delayednotificationusersetting(id);
CREATE INDEX idx_sp_delayednotificationusersetting_2 ON sp_delayednotificationusersetting(userId);
CREATE INDEX idx_sp_delayednotificationusersetting_3 ON sp_delayednotificationusersetting(channel);
CREATE UNIQUE INDEX idx_sp_delayednotificationusersetting_u1 ON sp_delayednotificationusersetting(userId, channel);

ALTER TABLE sp_delayednotificationusersetting
        add constraint const_st_delayednotificationusersetting
        primary key (id);

CREATE TABLE sp_delayednotification (
   id 						int NOT NULL ,
   userId					int NOT NULL ,
   fromUserId				int NOT NULL ,
   channel					int NOT NULL ,
   action					int NOT NULL ,
   componentInstanceId		int NOT NULL default -1 ,
   notificationResourceId	int8 NOT NULL ,
   language					varchar(2) NOT NULL ,
   creationDate				timestamp NOT NULL ,
   message					varchar(2000) NULL
);

ALTER TABLE sp_delayednotification
        add constraint const_sp_delayednotification
        primary key (id);

CREATE INDEX idx_sp_delayednotification_1 ON sp_delayednotification(id);
CREATE INDEX idx_sp_delayednotification_2 ON sp_delayednotification(userId);
CREATE INDEX idx_sp_delayednotification_3 ON sp_delayednotification(channel);

CREATE TABLE sp_notificationresource (
   id 					int8 NOT NULL ,
   resourceId			varchar(50) NOT NULL ,
   resourceType			varchar(50) NOT NULL ,
   resourceName			varchar(500) NOT NULL ,
   resourceDescription	varchar(2000) NULL ,
   resourceLocation		varchar(500) NOT NULL ,
   resourceUrl			varchar(1000) NULL
);

ALTER TABLE sp_notificationresource
        add constraint const_sp_notificationresource
        primary key (id);

CREATE INDEX idx_sp_notificationresource_1 ON sp_notificationresource(id);
CREATE INDEX idx_sp_notificationresource_2 ON sp_notificationresource(resourceId);