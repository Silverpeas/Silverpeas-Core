ALTER TABLE SB_Attachment_Attachment
ADD lang char(2)
;

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
	attachmentInfo		varchar (1000)
);