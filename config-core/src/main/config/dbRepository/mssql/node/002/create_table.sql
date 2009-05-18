CREATE TABLE SB_Node_Node 
(
	nodeId			int		NOT NULL ,
	nodeName		varchar (1000)	NOT NULL ,
	nodeDescription		varchar (2000)	NOT NULL ,
	nodeCreationDate	varchar (10)	NOT NULL ,
	nodeCreatorId		varchar (100)	NOT NULL ,
	nodePath		varchar (1000)	NOT NULL ,
	nodeLevelNumber		int		NOT NULL ,
	nodeFatherId		int		NOT NULL ,
	modelId			varchar (1000)	NULL ,
	nodeStatus		varchar (1000)	NULL ,
	instanceId		varchar (50)	NOT NULL
);