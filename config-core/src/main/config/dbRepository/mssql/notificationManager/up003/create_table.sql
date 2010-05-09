CREATE TABLE ST_NotifSended (
	notifId		int		NOT NULL,
	userId		int		NOT NULL,
	messageType	int		NULL,
	notifDate	char (13)	NOT NULL,	
	title		varchar (255)	NULL,
	link		varchar (255)	NULL,
	sessionId	varchar (255)	NULL,
	componentId	varchar (255)	NULL,
	body		int		NULL
);

CREATE TABLE ST_NotifSendedReceiver (
	notifId		int		NOT NULL,
	userId		int		NOT NULL
);