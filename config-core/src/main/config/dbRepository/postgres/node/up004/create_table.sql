ALTER TABLE SB_Node_Node
ADD lang char(2) NULL
;

ALTER TABLE SB_Node_Node
ADD COLUMN rightsDependsOn int
;
ALTER TABLE SB_Node_Node
ALTER COLUMN rightsDependsOn SET DEFAULT -1
;
UPDATE SB_Node_Node
SET rightsDependsOn = -1
;

CREATE TABLE SB_Node_NodeI18N
(
	id			int		NOT NULL ,
	nodeId	int	NOT NULL ,
	lang char (2)  NOT NULL ,
	nodeName	varchar (1000)	NOT NULL ,
	nodeDescription		varchar (2000)	NOT NULL
);
