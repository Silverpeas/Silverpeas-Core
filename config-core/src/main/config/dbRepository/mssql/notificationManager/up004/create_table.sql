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