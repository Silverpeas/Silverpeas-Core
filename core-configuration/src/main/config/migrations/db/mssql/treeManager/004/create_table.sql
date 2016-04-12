CREATE TABLE SB_Tree_Tree
(
	treeId			int		NOT NULL ,
	id			int		NOT NULL ,
	name			varchar(255)	NOT NULL ,
	description		varchar (1000)	NULL ,
	creationDate		char(10)	NOT NULL ,
	creatorId		varchar(255)	NOT NULL ,
	path			varchar (900)	NOT NULL ,
	levelNumber		int		NOT NULL ,
	fatherId		int		NOT NULL ,
	orderNumber		varchar (100)	NULL,
	lang			char(2)		NULL
);

CREATE TABLE SB_Tree_TreeI18N
(
	id			int		NOT NULL ,
	treeId			int		NOT NULL ,
	nodeId			int		NOT NULL ,
	lang			char(2)		NOT NULL ,
	name			varchar(255)	NOT NULL ,
	description		varchar(1000)	NULL
)
;