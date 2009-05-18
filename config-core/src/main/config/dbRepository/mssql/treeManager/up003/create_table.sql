ALTER TABLE SB_Tree_Tree
ADD lang char(2) NULL
;

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

ALTER TABLE SB_Tree_TreeI18N ADD 
	 CONSTRAINT PK_Tree_TreeI18N PRIMARY KEY   
	(
		id
	)
;
