CREATE TABLE SB_Attachment_Attachment 
(
	attachmentId			int				NOT NULL ,
	attachmentPhysicalName	varchar (500)	NOT NULL ,
	attachmentLogicalName	varchar (100)	NOT NULL ,
	attachmentDescription	varchar (500)	NULL ,
	attachmentType			varchar (100)	NULL ,
	attachmentSize			varchar (100)	NULL ,
	attachmentContext		varchar (500)	NULL ,
	attachmentForeignkey	varchar (100)	NOT NULL,
	instanceId				varchar (50)	NOT NULL,
	attachmentCreationDate	varchar	(10)	NULL,
	attachmentAuthor		varchar (100)   NULL,
	attachmentTitle			varchar (100)   NULL,
	attachmentInfo			varchar (1000)  NULL,
	attachmentOrderNum		int				DEFAULT (0) NOT NULL ,
	workerId				varchar (50)  	NULL,
	cloneId 				varchar (50) 	NULL
);