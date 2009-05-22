CREATE TABLE ST_PortletColumn (
	id		int		NOT NULL,
	spaceId		int		NOT NULL,
	columnWidth	varchar(10) 	NULL,
	nbCol		int		NOT NULL
);

CREATE TABLE ST_PortletRow (
	id		int NOT NULL ,
	InstanceId	int NOT NULL ,
	portletColumnId	int NOT NULL ,
	rowHeight	int NOT NULL ,
	nbRow		int NOT NULL 
);

CREATE TABLE ST_PortletState (
	id		int		NOT NULL,
	state		int		NOT NULL DEFAULT (0),
	userId		int		NOT NULL,
	portletRowId	int		NOT NULL 
);

CREATE TABLE ST_Component (
	id		int 		NOT NULL,
	componentName	varchar(100) 	NOT NULL ,
	description	varchar(400) 	NULL 
);
