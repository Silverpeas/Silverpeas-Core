CREATE TABLE ST_PopupMessage (
	ID int NOT NULL ,
	USERID int NOT NULL ,
	BODY varchar (4000) NULL ,
	SENDERID varchar (10) NULL ,
	SENDERNAME varchar (200) NULL ,
	ANSWERALLOWED char(1) default '0' not null ,
	SOURCE varchar (255) NULL,
	URL varchar (255) NULL,
	MSGDATE varchar(10),
	MSGTIME varchar(5)
);
