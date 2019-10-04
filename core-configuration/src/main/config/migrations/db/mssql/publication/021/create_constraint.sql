ALTER TABLE SB_Publication_Publi WITH NOCHECK ADD
	 CONSTRAINT PK_Publication_Publi PRIMARY KEY  CLUSTERED
	(
		pubId
	)
;

ALTER TABLE SB_Publication_PubliFather WITH NOCHECK ADD
	 CONSTRAINT PK_Publication_PubliFather PRIMARY KEY  CLUSTERED
	(
		pubId,
		nodeId,
		instanceId
	)
;

ALTER TABLE SB_SeeAlso_Link WITH NOCHECK ADD
	 CONSTRAINT PK_SeeAlso_Link PRIMARY KEY CLUSTERED
	(
		id
	)
;

ALTER TABLE SB_Publication_PubliI18N WITH NOCHECK ADD
	 CONSTRAINT PK_Publication_PubliI18N PRIMARY KEY CLUSTERED
	(
		id
	)
;

ALTER TABLE SB_Thumbnail_Thumbnail WITH NOCHECK ADD
	 CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY CLUSTERED
	(
		objectId,
		objectType,
		instanceId
	)
;