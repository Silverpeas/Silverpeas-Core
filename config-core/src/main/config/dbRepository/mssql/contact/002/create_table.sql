CREATE TABLE SB_Contact_Contact 
(
	contactId		int		NOT NULL ,
	contactFirstName	varchar (1000)	NULL ,
	contactLastName		varchar (1000)	NULL ,
	contactEmail		varchar (1000)	NULL ,
	contactPhone		varchar (20)	NULL ,
	contactFax		varchar (20)	NULL ,
	userId			varchar (100)	NULL ,
	contactCreationDate	varchar (10)	NOT NULL ,
	contactCreatorId	varchar (100)	NOT NULL ,
	instanceId		varchar (50)	NOT NULL
);

CREATE TABLE SB_Contact_Contactfather 
(
	contactId	int		NOT NULL ,
	nodeId		int		NOT NULL 
);

CREATE TABLE SB_Contact_Info 
(
	infoId		int		NOT NULL ,
	contactId	int		NOT NULL ,
	modelId		int		NOT NULL ,
	instanceId	varchar (50)	NOT NULL
);

CREATE TABLE SB_Contact_InfoAttachment 
(
	infoAttachmentId		int		NOT NULL ,
	infoId				int		NOT NULL ,
	infoAttachmentPhysicalName	varchar (1000)	NOT NULL ,
	infoAttachmentLogicalName	varchar (1000)	NOT NULL ,
	infoAttachmentDescription	varchar (2000)	NULL ,
	infoAttachmentType		varchar (50)	NOT NULL ,
	infoAttachmentSize		int		NULL ,
	infoAttachmentDisplayOrder	int		NOT NULL 
);

CREATE TABLE SB_Contact_InfoImage 
(
	infoImageId		int		NOT NULL ,
	infoId			int		NOT NULL ,
	infoImagePhysicalName	varchar (1000)	NOT NULL ,
	infoImageLogicalName	varchar (1000)	NOT NULL ,
	infoImageDescription	varchar (2000)	NULL ,
	infoImageType		varchar (50)	NOT NULL ,
	infoImageSize		int		NULL ,
	infoImageDisplayOrder	int		NOT NULL 
);

CREATE TABLE SB_Contact_InfoLink 
(
	infoLinkId		int		NOT NULL ,
	infoId			int		NOT NULL ,
	pubId			int		NOT NULL ,
	infoLinkDisplayOrder	int		NOT NULL 
);

CREATE TABLE SB_Contact_InfoText 
(
	infoTextId		int		NOT NULL ,
	infoId			int		NOT NULL ,
	infoTextContent		varchar (4000)	NULL ,
	infoTextDisplayOrder	int		NOT NULL 
);