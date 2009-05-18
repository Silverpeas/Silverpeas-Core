CREATE TABLE SB_Attachment_Attachment 
(
	attachmentId		int		NOT NULL ,
	attachmentPhysicalName	varchar (1000)	NOT NULL ,
	attachmentLogicalName	varchar (1000)	NOT NULL ,
	attachmentDescription	varchar (2000)	NULL ,
	attachmentType		varchar (1000)	NULL ,
	attachmentSize		varchar (1000)	NULL ,
	attachmentContext	varchar (1000)	NULL ,
	attachmentForeignkey	varchar (1000)	NOT NULL,
	instanceId		varchar (50)	NOT NULL,
	attachmentCreationDate	varchar (10)	NULL
);