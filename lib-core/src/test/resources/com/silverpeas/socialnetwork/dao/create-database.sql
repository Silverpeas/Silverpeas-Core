CREATE TABLE sb_socialnetwork_externalaccount (
	profileId varchar(100) NOT NULL ,
	networkId varchar(10) not NULL,
	silverpeasUserId varchar(50) NULL
);

ALTER TABLE sb_socialnetwork_externalaccount  ADD
	CONSTRAINT PK_ExternalAccount PRIMARY KEY
	(
		profileId,
		networkId
	);