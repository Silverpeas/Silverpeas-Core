CREATE TABLE sb_node_node
(
	nodeId			int		NOT NULL ,
	nodeName		varchar (1000)	NOT NULL ,
	nodeDescription		varchar (2000)  NULL,
	nodeCreationDate	varchar (10)	NOT NULL ,
	nodeCreatorId		varchar (100)	NOT NULL ,
	nodePath		varchar (1000)	NOT NULL ,
	nodeLevelNumber		int		NOT NULL ,
	nodeFatherId		int		NOT NULL ,
	modelId			varchar (1000)	NULL ,
	nodeStatus		varchar (1000)	NULL ,
	instanceId		varchar (50)	NOT NULL,
	type			varchar (50)	NULL ,
	orderNumber		int		DEFAULT (0) NULL ,
	lang			char(2),
	rightsDependsOn		int		default(-1) NOT NULL
);

CREATE TABLE sb_node_nodei18N
(
	id			int		NOT NULL ,
	nodeId	int	NOT NULL ,
	lang char (2)  NOT NULL ,
	nodeName	varchar (1000)	NOT NULL ,
	nodeDescription		varchar (2000)
);

CREATE TABLE sb_coordinates_coordinates
(
	coordinatesId			int		NOT NULL ,
	nodeId				int		NOT NULL ,
	coordinatesLeaf			varchar (50)	NOT NULL ,
	coordinatesDisplayOrder		int		NULL	 ,
	instanceId			varchar (50)	NOT NULL
);

ALTER TABLE SB_Node_Node ADD
	 CONSTRAINT PK_Node_Node
	 PRIMARY KEY
	(
		nodeId,
		instanceId
	)
;
ALTER TABLE SB_Node_NodeI18N ADD
	 CONSTRAINT PK_Node_NodeI18N
	 PRIMARY KEY
	(
		id
	)
;

ALTER TABLE sb_coordinates_coordinates ADD
	 CONSTRAINT PK_Coordinates_Coordinates PRIMARY KEY
	(
		coordinatesId,
		nodeId,
		instanceId
	)
;

CREATE TABLE subscribe (
  subscriberId       VARCHAR(100) NOT NULL,
  subscriberType     VARCHAR(50)  NOT NULL,
  subscriptionMethod VARCHAR(50)  NOT NULL,
  resourceId         VARCHAR(100) NOT NULL,
  resourceType       VARCHAR(50)  NOT NULL,
  space              VARCHAR(50)  NOT NULL,
  instanceId         VARCHAR(50)  NOT NULL,
  creatorId          VARCHAR(100) NOT NULL,
  creationDate       TIMESTAMP    NOT NULL
);
