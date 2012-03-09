CREATE TABLE uniqueId (
	maxId int NOT NULL ,
	tableName VARCHAR(100) NOT NULL
);

CREATE TABLE sb_filesharing_ticket
(
	shared_object BIGINT NOT NULL,
	componentId VARCHAR(255) NOT NULL,
	creatorId VARCHAR(50) NOT NULL,
	creationDate CHAR(13) NOT NULL,
	updateId VARCHAR(50)	NULL,
	updateDate CHAR(13) NULL,
	endDate CHAR(13) NULL,
	nbAccessMax INTEGER NOT NULL,
	nbAccess INTEGER NULL,
	keyfile	VARCHAR(255) NOT NULL,
    shared_object_type VARCHAR(255) NOT NULL
)
;

CREATE TABLE sb_filesharing_history
(
	id BIGINT NOT NULL,
	keyfile VARCHAR(255) NOT NULL,
	downloadDate CHAR(13)	NOT NULL,
	downloadIp VARCHAR(50)	NOT NULL
)
;