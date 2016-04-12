CREATE TABLE SB_MyLinks_Link
(
	linkId			int		NOT NULL,
	name			varchar (255)	NOT NULL,
	description		varchar (255)	NULL,
	url			varchar (255)	NOT NULL,
	visible			int		NOT NULL,
	popup			int		NOT NULL,
	userId			varchar (50)    NOT NULL,
	instanceId		varchar (50)	NULL,
	objectId		varchar	(50)	NULL
)
;