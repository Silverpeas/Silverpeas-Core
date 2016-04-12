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
	modelId		varchar (100)	NOT NULL ,
	instanceId	varchar (50)	NOT NULL
);
