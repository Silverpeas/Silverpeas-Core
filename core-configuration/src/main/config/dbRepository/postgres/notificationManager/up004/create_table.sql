CREATE TABLE st_delayednotifusersetting (
   id 			int NOT NULL ,
   userId		int NOT NULL ,
   channel		int NOT NULL ,
   frequency	varchar (4) NOT NULL
);

CREATE TABLE st_notificationresource (
   id 					int8 NOT NULL ,
   componentInstanceId	varchar(50) NOT NULL ,
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