CREATE TABLE SB_ContainerManager_Instance
(
	instanceId		int NOT NULL ,
	componentId		varchar(100) NOT NULL ,
	containerType varchar(100) NOT NULL ,
	contentType		varchar(100) NOT NULL
);

CREATE TABLE SB_ContainerManager_Links
(
	positionId						int NOT NULL ,
	containerInstanceId		int NOT NULL
);