CREATE TABLE SB_fileSharing_ticket
(
	fileId			int		NOT NULL,
	componentId		varchar (255)	NOT NULL,
	versioning		varchar (1)	NOT NULL,
	creatorId		varchar (50)	NOT NULL,
	creationDate		char(13)	NOT NULL,
	updateId		varchar (50)	NULL,
	updateDate		char(13)	NULL,
	endDate			char(13)	NOT NULL,
	nbAccessMax		int		NOT NULL,
	nbAccess		int		NULL,
	keyFile			char(32)	NOT NULL
) 
;

CREATE TABLE SB_fileSharing_history
(
	id			int		NOT NULL,
	keyFile			char(32)	NOT NULL,
	downloadDate		char(13)	NOT NULL,
	downloadIp		varchar (50)	NOT NULL
) 
;
