CREATE TABLE sb_filesharing_ticket (
	shared_object BIGINT NOT NULL,
	componentId VARCHAR(255)	NOT NULL,
	creatorId	 VARCHAR(50)	NOT NULL,
	creationDate BIGINT NOT NULL,
	updateId VARCHAR(50)	NULL,
	updateDate BIGINT NULL,
	endDate BIGINT NULL,
	nbAccessMax		int		NOT NULL,
	nbAccess		int		NULL,
	keyfile	VARCHAR(255) NOT NULL,
	shared_object_type VARCHAR(255) NOT NULL
)
;

CREATE TABLE sb_filesharing_history (
	id BIGINT NOT NULL,
	keyFile	VARCHAR(255) NOT NULL,
	downloadDate BIGINT NOT NULL,
	downloadIp VARCHAR(50)	NOT NULL
)
;
