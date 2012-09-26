CREATE TABLE SB_Version_Document 
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

CREATE TABLE SB_Version_Version 
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

CREATE TABLE SB_Document_WorkList
	(
	documentId	INT	NOT NULL,
	userid		INT	NOT NULL,
	orderBy		INT	NOT NULL,
	writer		VARCHAR(100)	NULL,
	approval	VARCHAR(100)	NULL,
	instanceId VARCHAR (50) NOT NULL,
	settype		character(1) DEFAULT 'U',
	saved INT NOT NULL DEFAULT 0,
	used INT NOT NULL DEFAULT 1,
	listtype INT NOT NULL DEFAULT 0
	);

CREATE TABLE sb_doc_readers_acl
(
  id INT NOT NULL,
  componentid VARCHAR(50) NOT NULL
)
;
CREATE TABLE sb_doc_readers_acl_list
(
  id INT NOT NULL,
  settype character(1) NOT NULL,
  settypeid INT NOT NULL,
  accessid INT NOT NULL
)
;




