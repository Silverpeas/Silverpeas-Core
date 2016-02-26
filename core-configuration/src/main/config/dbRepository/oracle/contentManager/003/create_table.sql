CREATE TABLE SB_ContentManager_Instance
(
	instanceId	int		NOT NULL ,
	componentId	varchar(100)	NOT NULL ,
	containerType	varchar(100)	NOT NULL ,
	contentType	varchar(100)	NOT NULL
);

CREATE TABLE SB_ContentManager_Content
(
	silverContentId			int		NOT NULL ,
	internalContentId		varchar(100)	NOT NULL ,
	contentInstanceId		int		NOT NULL,
	authorId			int		NOT NULL,
	creationDate			date		NOT NULL,
	beginDate			varchar(10)	NULL,
	endDate				varchar(10)	NULL,
	isVisible			int		NULL
);
