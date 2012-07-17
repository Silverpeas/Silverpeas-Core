ALTER TABLE sb_sn_externalaccount ADD
	CONSTRAINT pk_externalaccount primary key (
		profileId,
		networkId
	);
