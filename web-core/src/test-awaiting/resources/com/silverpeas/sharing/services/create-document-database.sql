CREATE TABLE IF NOT EXISTS uniqueId (
	maxId int NOT NULL ,
	tableName VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS sb_filesharing_ticket
(
	shared_object BIGINT NOT NULL,
	componentId VARCHAR(255) NOT NULL,
	creatorId VARCHAR(50) NOT NULL,
	creationDate BIGINT NOT NULL,
	updateId VARCHAR(50)	NULL,
	updateDate BIGINT NULL,
	endDate BIGINT NULL,
	nbAccessMax INTEGER NOT NULL,
	nbAccess INTEGER NULL,
	keyfile	VARCHAR(255) NOT NULL,
  shared_object_type VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS sb_filesharing_history
(
	id BIGINT NOT NULL,
	keyfile VARCHAR(255) NOT NULL,
	downloadDate BIGINT	NOT NULL,
	downloadIp VARCHAR(50)	NOT NULL
);

CREATE TABLE IF NOT EXISTS SB_Version_Document
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

CREATE TABLE IF NOT EXISTS SB_Version_Version
	(
	versionId INT NOT NULL,
	documentId INT NOT NULL,
	versionMajorNumber INT NOT NULL,
	versionMinorNumber INT NOT NULL,
	versionAuthorId INT NOT NULL,
	versionCreationDate char (10) NOT NULL,
	versionComments VARCHAR (1000),
	versionType INT NOT NULL,
	versionStatus INT,
	versionPhysicalname VARCHAR (100) NOT NULL,
	versionLogicalName VARCHAR (255) NOT NULL,
	versionMimeType VARCHAR (100) NOT NULL,
	versionSize INT NOT NULL,
	instanceId VARCHAR (50) NOT NULL,
	xmlForm	VARCHAR(50)	NULL
	);