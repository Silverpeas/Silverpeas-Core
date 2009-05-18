ALTER TABLE SB_Node_Node
ADD lang char(2) NULL
;

ALTER TABLE SB_Node_Node
ADD rightsDependsOn int default(-1) NOT NULL
;

CREATE TABLE SB_Node_NodeI18N
(
	id			int		NOT NULL ,
	nodeId	int	NOT NULL ,
	lang char (2)  NOT NULL ,
	nodeName	varchar (1000)	NOT NULL ,
	nodeDescription		varchar (2000)	NOT NULL
);
