CREATE TABLE ST_NotifChannel (
	id int NOT NULL ,
	name varchar (20) NOT NULL ,
	description varchar (200) NULL ,
	couldBeAdded char (1) NOT NULL DEFAULT ('Y') ,
	fromAvailable char (1) NOT NULL DEFAULT ('N') ,
	subjectAvailable char (1) NOT NULL DEFAULT ('N')
)
;

CREATE TABLE ST_NotifAddress (
	id int NOT NULL ,
	userId int NOT NULL ,
	notifName varchar (20) NOT NULL ,
	notifChannelId int NOT NULL ,
	address varchar (250) NOT NULL ,
	usage varchar (20) NULL ,
	priority int NOT NULL
)
;

CREATE TABLE ST_NotifDefaultAddress (
	id int NOT NULL ,
	userId int NOT NULL ,
	notifAddressId int NOT NULL
)
;

CREATE TABLE ST_NotifPreference (
	id int NOT NULL ,
	notifAddressId int NOT NULL ,
	componentInstanceId int NOT NULL ,
	userId int NOT NULL ,
	messageType int NOT NULL
)
;

CREATE TABLE ST_NotifSended (
	notifId		int		NOT NULL,
	userId		int		NOT NULL,
	messageType	int		NULL,
	notifDate	char (13)	NOT NULL,
	title		varchar (255)	NULL,
	link		varchar (255)	NULL,
	sessionId	varchar (255)	NULL,
	componentId	varchar (255)	NULL,
	body		int		NULL
);

CREATE TABLE ST_NotifSendedReceiver (
	notifId		int		NOT NULL,
	userId		int		NOT NULL
);

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