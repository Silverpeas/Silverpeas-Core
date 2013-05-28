CREATE TABLE UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE sb_attachment_attachment 
(
	attachmentId		int		NOT NULL ,
	attachmentPhysicalName	varchar (500)	NOT NULL ,
	attachmentLogicalName	varchar (100)	NOT NULL ,
	attachmentDescription	varchar (500)	NULL ,
	attachmentType		varchar (100)	NULL ,
	attachmentSize		varchar (100)	NULL ,
	attachmentContext	varchar (500)	NULL ,
	attachmentForeignkey	varchar (100)	NOT NULL,
	instanceId		varchar (50)	NOT NULL,
	attachmentCreationDate	varchar (10)	NULL,
	attachmentAuthor 	varchar	(100) 	NULL,
	attachmentTitle		varchar (100)   NULL,
	attachmentInfo		varchar (1000)  NULL,
	attachmentOrderNum	int		NOT NULL DEFAULT (0),
	workerId		varchar (50)	NULL,
	cloneId 		varchar (50)	NULL,
	lang			char(2),
	reservationDate	varchar (10)    NULL,
	alertDate		varchar (10)	NULL,
	expiryDate		varchar (10)	NULL,
	xmlForm			varchar(50)		NULL
);

CREATE TABLE sb_version_document 
	(
	documentId		INT		NOT NULL, 
	documentName		VARCHAR (255)	NOT NULL,
	documentDescription	VARCHAR (255),
	documentStatus		INT		NOT NULL,
	documentOwnerId		INT,
	documentCheckoutDate	char (10),
	documentInfo		VARCHAR (100),
	foreignId		INT		NOT NULL,
	instanceId		VARCHAR (50)	NOT NULL,
	typeWorkList		INT		NOT NULL,
	currentWorkListOrder	INT,
	alertDate		VARCHAR (10)	NULL,
	expiryDate		VARCHAR (10)	NULL,
	documentOrderNum	INT NOT NULL DEFAULT (0)
	);
