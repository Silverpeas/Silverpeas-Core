ALTER TABLE sb_sn_externalaccount with nocheck add 
	CONSTRAINT pk_externalaccount PRIMARY KEY clustered
	(
		profileId,
		networkId
	);
