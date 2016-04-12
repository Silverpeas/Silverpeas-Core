CREATE TABLE SB_Attachment_Attachment
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

CREATE TABLE SB_Attachment_AttachmentI18N
(
	id			int		NOT NULL,
	attachmentId		int		NOT NULL,
	lang			char(2)		NOT NULL,
	attachmentPhysicalName	varchar (500)	NOT NULL,
	attachmentLogicalName	varchar (100)	NOT NULL,
	attachmentType		varchar (100),
	attachmentSize		varchar (100),
	instanceId		varchar (50)	NOT NULL,
	attachmentCreationDate	varchar (10),
	attachmentAuthor 	varchar	(100),
	attachmentTitle		varchar (100),
	attachmentInfo		varchar (1000),
	xmlForm				varchar(50)
);

ALTER TABLE SB_Attachment_Attachment ADD
	 CONSTRAINT PK_Attachment_Attachment PRIMARY KEY
	(
		attachmentId
	)
;

ALTER TABLE SB_Attachment_AttachmentI18N ADD
	 CONSTRAINT PK_Attachment_AttachmentI18N PRIMARY KEY
	(
		id
	)
;