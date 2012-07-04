ALTER TABLE sb_sn_externalaccount ADD
	CONSTRAINT pk_externalaccount PRIMARY KEY
	(
		profileId,
		networkId
	);

