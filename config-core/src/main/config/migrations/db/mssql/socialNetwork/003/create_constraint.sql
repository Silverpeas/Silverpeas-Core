alter table  sb_sn_invitation  with nocheck add
	 constraint pk_sb_sn_invitation primary key clustered
	(
		id
	)
;

alter table  sb_sn_relationship  with nocheck add
	 constraint pk_sb_sn_relationship primary key clustered
	(
		id
	)
;


alter table  sb_sn_typerelationship  with nocheck add
	 constraint pk_sb_sn_typerelationship primary key clustered
         (
            id
         )
;

alter table  sb_sn_status  with nocheck add
	 constraint pk_sb_sn_status primary key clustered
         (
            id
         )
;

ALTER TABLE sb_sn_externalaccount with nocheck add
	CONSTRAINT pk_externalaccount PRIMARY KEY clustered
	(
		profileId,
		networkId
	);
