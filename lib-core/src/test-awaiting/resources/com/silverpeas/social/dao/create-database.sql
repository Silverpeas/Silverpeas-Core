CREATE TABLE sb_sn_externalaccount (
	profileId varchar(100) NOT NULL ,
	networkId varchar(10) not NULL,
	silverpeasUserId varchar(50) NULL
);

ALTER TABLE sb_sn_externalaccount  ADD
	CONSTRAINT pk_externalaccount PRIMARY KEY
	(
		profileId,
		networkId
	);

CREATE TABLE IF NOT EXISTS UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);