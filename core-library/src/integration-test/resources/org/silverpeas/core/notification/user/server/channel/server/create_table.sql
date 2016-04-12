CREATE TABLE IF NOT EXISTS ST_ServerMessage (
	ID int NOT NULL ,
	USERID int NOT NULL ,
	HEADER varchar (255) NULL ,
	SUBJECT varchar (1024) NULL ,
	BODY varchar (4000) NULL ,
	SESSIONID varchar (255) NULL,
	TYPE char (1) NULL
);