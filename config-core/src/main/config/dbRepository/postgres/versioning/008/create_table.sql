CREATE TABLE SB_Version_Document 
	(
	documentId		int		not null, 
	documentName		varchar (100)	not null,
	documentDescription	varchar (255),
	documentStatus		int		not null,
	documentOwnerId		int,
	documentCheckoutDate	char (10),
	documentInfo		varchar (100),
	foreignId		int		not null,
	instanceId		varchar (50)	not null,
	typeWorkList		int		not null,
	currentWorkListOrder	int,
	alertDate		varchar (10)	NULL,
	expiryDate		varchar (10)	NULL

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
	versionMimeType varchar (100) not null,
	versionSize int not null,
	instanceId varchar (50) not null,
	xmlForm	varchar(50)	NULL
	);

CREATE TABLE SB_Document_WorkList
	(
	documentId	int		not null,
	userid		int		not null,
	orderBy		int		not null,
	writer		varchar(100)	null,
	approval	varchar(100)	null,
	instanceId varchar (50) not null,
	settype		character(1) DEFAULT 'U',
	saved integer NOT NULL DEFAULT 0,
	used integer NOT NULL DEFAULT 1,
	listtype integer NOT NULL DEFAULT 0
	);

CREATE TABLE sb_doc_readers_acl
(
  id integer NOT NULL,
  componentid varchar(50) NOT NULL
)
;
CREATE TABLE sb_doc_readers_acl_list
(
  id integer NOT NULL,
  settype character(1) NOT NULL,
  settypeid integer NOT NULL,
  accessid integer NOT NULL
)
;




