CREATE TABLE SB_webConnections_info
(
	connectionId		int				NOT NULL,
	userId				int				NOT NULL,
	componentId			varchar (50)	NOT NULL,
	paramLogin			varchar (100)   NOT NULL,
	paramPassword		varbinary(200) NULL
)
;
