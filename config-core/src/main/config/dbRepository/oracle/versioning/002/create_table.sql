CREATE TABLE SB_Version_Document 
	(
	documentId int not null, 
	documentName varchar (100) not null,
	documentDescription varchar (255),
	documentStatus int not null,
	documentOwnerId int,
	documentCheckoutDate char (10),
	documentInfo varchar (100),
	foreignId int not null,
	instanceId varchar (50) not null
	);

CREATE TABLE SB_Version_Version 
	(
	versionId int not null,
	documentId int not null,
	versionMajorNumber int not null,
	versionMinorNumber int not null,
	versionAuthorId int not null,
	versionCreationDate char (10) not null,
	versionComments varchar (1000),
	versionType int not null,
	versionStatus int,
	versionPhysicalname varchar (100) not null,
	versionLogicalName varchar (100) not null,
	versionMimeType varchar (64) not null,
	versionSize int not null
	);
