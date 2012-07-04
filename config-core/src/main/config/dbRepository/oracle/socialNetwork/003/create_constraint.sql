alter table  sb_sn_invitation  add
	 constraint pk_sb_sn_invitation primary key
	(
		id
	)
;

alter table  sb_sn_relationship  add
	 constraint pk_sb_sn_relationship primary key
	(
		id
	)
;


alter table  sb_sn_typerelationship  add
	 constraint pk_sb_sn_typerelationship primary key (
            id
         )
;

alter table  sb_sn_status  add
	 constraint pk_sb_sn_status primary key (
            id
         )
;

ALTER TABLE sb_sn_externalaccount ADD
	CONSTRAINT pk_externalaccount primary key (
		profileId,
		networkId
	);
