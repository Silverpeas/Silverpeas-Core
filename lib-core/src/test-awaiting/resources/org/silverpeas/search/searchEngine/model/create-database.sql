CREATE TABLE SB_Tree_Tree (
	treeId			int		NOT NULL ,
	id			int		NOT NULL ,
	name			varchar(255)	NOT NULL ,
	description		varchar (1000)	NULL ,
	creationDate		char(10)	NOT NULL ,
	creatorId		varchar(255)	NOT NULL ,
	path			varchar (1000)	NOT NULL ,
	levelNumber		int		NOT NULL ,
	fatherId		int		NOT NULL ,
	orderNumber		int		NULL,
	lang			char(2)		NULL);
ALTER TABLE SB_Tree_Tree ADD  CONSTRAINT PK_Tree_Tree PRIMARY KEY (treeId, id);

CREATE TABLE SB_Thesaurus_Synonym (
	id 			int 			NOT NULL,
	idVoca 		int 			NOT NULL,
	idTree 		int 			NOT NULL,
	idTerm 		int 			NOT NULL,
	name 		varchar(100));
ALTER TABLE SB_Thesaurus_Synonym ADD CONSTRAINT PK_Thesaurus_Synonym PRIMARY KEY (id);


CREATE TABLE SB_TagCloud_TagCloud (
	id           int          not null,
	tag          varchar(100) not null,
	label        varchar(100) not null,
	instanceId   varchar(50)  not null,
	externalId   varchar(50)  not null,
	externalType int          not null);
ALTER TABLE SB_TagCloud_TagCloud ADD CONSTRAINT PK_SB_TagCloud_TagCloud PRIMARY KEY (id);