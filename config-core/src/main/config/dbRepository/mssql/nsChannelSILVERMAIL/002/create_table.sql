CREATE TABLE ST_SilverMailMessage (
	ID int NOT NULL ,
	USERID int NOT NULL ,
	FOLDERID int NULL ,
	HEADER varchar (255) NULL ,
	SENDERNAME varchar (255) NULL ,
	SUBJECT varchar (1024) NULL ,
	BODY varchar (4000) NULL
);
