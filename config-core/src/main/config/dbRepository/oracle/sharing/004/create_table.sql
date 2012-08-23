CREATE TABLE SB_fileSharing_ticket
(
	shared_object	NUMBER(19, 0) NOT NULL,
	componentId		VARCHAR2(255)	NOT NULL,
	creatorId		VARCHAR2(50)	NOT NULL,
	creationDate NUMBER(19, 0) NOT NULL,
	updateId		varchar (50)	NULL,
	updateDate NUMBER(19, 0) NULL,
	endDate NUMBER(19, 0) NULL,
	nbAccessMax INTEGER NOT NULL,
	nbAccess INTEGER NULL,
	keyfile	VARCHAR2(255) NOT NULL,
	shared_object_type VARCHAR2(255) NOT NULL
)
;

CREATE TABLE SB_fileSharing_history
(
	id NUMBER(19, 0) NOT NULL,
	keyFile VARCHAR2(255)	NOT NULL,
	downloadDate NUMBER(19, 0)	NOT NULL,
	downloadIp VARCHAR2(50)	NOT NULL
);
