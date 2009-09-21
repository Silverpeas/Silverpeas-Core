CREATE TABLE SB_Version_Document 
	(
	documentId int not null, 
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
	xmlForm varchar (50)
	);

CREATE TABLE SB_Document_WorkList
	(
	documentId	int		not null,
	userId		int		not null,
	orderBy		int		not null,
	writer		varchar(100)	null,
	approval	varchar(100)	null,
	instanceId varchar (50) not null,
	settype		character(1) DEFAULT 'U',
	saved integer DEFAULT 0 NOT NULL,
	used integer DEFAULT 1 NOT NULL,
	listtype integer DEFAULT 0 NOT NULL
	);
	
CREATE TABLE sb_doc_readers_acl
(
  id integer  not null,
  componentid varchar(50)  not null
)
;
CREATE TABLE sb_doc_readers_acl_list
(
  id integer  not null,
  settype character(1)  not null,
  settypeid integer  not null,
  accessid integer not null
)
;

	